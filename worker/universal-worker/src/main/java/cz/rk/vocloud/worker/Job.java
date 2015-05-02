package cz.rk.vocloud.worker;

import cz.rk.vocloud.schema.Worker;
import uws.UWSException;
import uws.job.AbstractJob;
import uws.job.Result;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zeroturnaround.zip.ZipUtil;

/**
 *
 * @author mrq
 */
public class Job extends AbstractJob {

    private static final Logger LOG = Logger.getLogger(Job.class.getName());

    private static final String OUTPUT_FILE_NAME = "run.out";
    private static final String ERROR_FILE_NAME = "run.err";
    private static final String RETURN_FILE_NAME = "run.ret";
    private static final String DOWNLOAD_LOG_FILE_NAME = "download.log";
    private static final String PROCESS_DATA_FOLDER_NAME = ".processData";
    private static final String WORKING_DIR_NAME = "workingDir";

    private String configFile;
    private transient JSONObject parsedJsonConfig;
    private File workingDir;
    private File jobDir;
    private Worker workerSettings;

    private File outputFile;
    private File errorFile;
    private File returnFile;
    private File downloadLogFile;
    private File processDataDir;

    private boolean resultsPrepared = false;

    private final List<File> externalFiles = new ArrayList<>();

    public Job(Map<String, String> lstParam) throws UWSException {
        super(lstParam);
    }

    @Override
    protected boolean loadAdditionalParams() throws UWSException {
        // check for parameter file
        LOG.log(Level.FINE, "Loading job parameters");
        if (additionalParameters.containsKey("config")) {//especially in post application/x-www-url-encoded request
            configFile = additionalParameters.get("config");//could be big file - remove from map
            additionalParameters.remove("config");
        } else {
//            for (Map.Entry<String, String> i: additionalParameters.entrySet()){
//                System.out.println("key: |" + i.getKey() + "|");
//            }
            if (configFile == null) {
                throw new UWSException(UWSException.BAD_REQUEST, "Config file has to be specified.");
            }
        }
        return true;
    }

    @Override
    @SuppressWarnings({"UseSpecificCatch", "SleepWhileInLoop"})
    protected void jobWork() throws UWSException, InterruptedException {
        try {
            LOG.log(Level.INFO, "Starting job {0}", getJobId());
            //save worker settings
            for (Worker w : Config.settings.getWorkers().getWorker()) {
                if (w.getIdentifier().equals(getJobList().getName())) {
                    workerSettings = w;
                    break;//break from the cycle
                }
            }
            //check that proper worker was found in settings
            if (workerSettings == null) {
                //note: this should not happen
                LOG.log(Level.SEVERE, "Worker with identifier {0} is not specified in settings", getJobList().getName());
                throw new UWSException("Worker with identifier " + getJobList().getName() + " was not found");
            }
            //try to parse json config
            try {
                parsedJsonConfig = new JSONObject(configFile);
            } catch (JSONException ex) {
                LOG.log(Level.WARNING, "JSON parsing failed");
            }
            //create job directory
            jobDir = new File(Config.resultsDir + "/" + getJobId());
            jobDir.mkdirs();
            //create working directory
            workingDir = new File(jobDir, WORKING_DIR_NAME);
            workingDir.mkdir();
            processDataDir = new File(workingDir, PROCESS_DATA_FOLDER_NAME);
            processDataDir.mkdir();
            Process process = null;
            try {
                //print config file
                File config = new File(processDataDir, "config.json");
                try (FileOutputStream fos = new FileOutputStream(config)) {
                    fos.write(configFile.getBytes(Charset.forName("UTF-8")));
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "Unable to create config.json file in working directory", ex);
                    throw new UWSException("Unable to create config.json file in working directory");
                }
                //for backward compatibility copy config.json to working dir too 
                //todo remove after python fix
                try {
                    FileUtils.copyFile(config, new File(workingDir, "config.json"));
                    externalFiles.add(new File(workingDir, "config.json"));
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "Unable to create config.json file in working directory", ex);
                    throw new UWSException("Unable to create config.json file in working directory");
                }
                configFile = null;
                //download necessary files into working directory
                downloadFiles();//extract them from config file
                // prepare output files
                try {
                    outputFile = new File(processDataDir, OUTPUT_FILE_NAME);
                    errorFile = new File(processDataDir, ERROR_FILE_NAME);
                    returnFile = new File(processDataDir, RETURN_FILE_NAME);
                    outputFile.createNewFile();
                    errorFile.createNewFile();
                    returnFile.createNewFile();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                    throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "IO Exception when creating output files.");
                }
                //create process itself
                List<String> commands = workerSettings.getExecCommand().getCommand();
                //use substitution for commands
                commands = substituteCommands(commands);
                ProcessBuilder pb = new ProcessBuilder(commands);
                pb.directory(workingDir);

                try {
                    pb.redirectOutput(outputFile);
                    pb.redirectError(errorFile);

                    process = pb.start();
                    //java 1.8 better solution
//                    boolean processDone = false;
//                    do {
//                        processDone = process.waitFor(5, TimeUnit.SECONDS);//working since java 1.8 (better solution)
//                    } while (!processDone && !thread.isInterrupted());
                    //java 1.7 solution
                    while (true) {
                        Thread.sleep(1000);
                        try {
                            process.exitValue();
                            //it completed
                            break;
                        } catch (IllegalThreadStateException ex) {
                            //not completed yet
                            //will do the next loop
                        }
                    }
                    if (thread.isInterrupted()) {
                        throw new InterruptedException();
                    }
                } catch (InterruptedException ie) {
                    //kill process if job is aborted
                    if (process != null) {
                        LOG.log(Level.INFO, "Calling kill signal to process");
                        process.destroyForcibly();
                    }
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "error when executing job", e);
                    throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, e, "error when executing job");
                }
                if (process == null) {
                    LOG.severe("Process was not started");
                    throw new UWSException("Process was not started");
                }
                // write return code to the file
                try (PrintStream ps = new PrintStream(returnFile)) {
                    LOG.log(Level.INFO, "Process exit value: {0}", process.exitValue());
                    ps.println(process.exitValue());
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                    throw new UWSException(ex);
                }
            } catch (UWSException ex) {
                prepareResults();
                if (thread.isInterrupted()) {
                    throw new InterruptedException();
                }
                throw ex;
            }
            // submit results, send error if process failed
            if (!thread.isInterrupted()) {
                prepareResults();
            } else {
                throw new InterruptedException();
            }
            if (process.exitValue() != 0) {
                throw new UWSException("process exit value:" + process.exitValue());
            }
        } catch (Throwable ex) {
            //ignore interrupted and uws exception
            if (ex instanceof InterruptedException || ex instanceof UWSException) {
                //rethrow exception
                throw ex;
            } else {
                //catch every other exception and log it
                LOG.log(Level.SEVERE, "Unexpected exception", ex);
                throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, ex, "unexpected throwable exception/error catched during job execution");
            }
        }
    }

    private List<String> substituteCommands(List<String> commands) {
        //supported tokens are ${binaries-location}, ${config-file}
        List<String> resolved = new ArrayList<>(commands.size());
        String tmp;
        for (String command : commands) {
            tmp = command
                    .replace("${binaries-location}", workerSettings.getBinariesLocation())
//                    .replace("${config-file}", PROCESS_DATA_FOLDER_NAME + "/config.json");
                    .replace("${config-file}", "config.json");//todo remove after python fix
            resolved.add(tmp);
        }
        return resolved;
    }

    private void downloadFiles() throws InterruptedException, UWSException {
        //check json parsing
        if (parsedJsonConfig == null) {
            return;//nothing to do
        }
        JSONArray downloadArray;
        try {
            downloadArray = parsedJsonConfig.getJSONArray("download_files");
        } catch (JSONException ex) {
            //no download_files key or bad syntax
            return;
        }
        downloadLogFile = new File(processDataDir, DOWNLOAD_LOG_FILE_NAME);
        try (PrintStream log = new PrintStream(downloadLogFile, "UTF-8")) {
            for (int i = 0; i < downloadArray.length(); i++) {
                JSONObject downloadItem;
                try {
                    downloadItem = downloadArray.getJSONObject(i);
                } catch (JSONException ex) {
                    log.println("Error: Expected JSON map");
                    continue;
                }
                String folderName = "/";//default
                try {
                    folderName = downloadItem.getString("folder");
                } catch (JSONException ex) {
                }
                //check characters
                if (folderName.contains("..")) {
                    log.println("Unexpected character sequence .. in folder path");
                    continue;
                }
                if (folderName.contains("//")) {
                    log.println("Unexpected character sequence // in folder path");
                    continue;
                }
                if (folderName.contains("\\")) {
                    log.println("Unexpected character \\ in folder path");
                    continue;
                }
                //remove slash in the beginning
                if (folderName.length() > 0 && folderName.charAt(0) == '/') {
                    folderName = folderName.substring(1);
                }
                JSONArray urlArray;
                try {
                    urlArray = downloadItem.getJSONArray("urls");
                } catch (JSONException ex) {
                    log.println("Warning: Array with key \"urls\" was not found");
                    continue;
                }
                for (int j = 0; j < urlArray.length(); j++) {
                    if (thread.isInterrupted()) {
                        throw new InterruptedException();//aborted
                    }
                    try {
                        String url = urlArray.getString(j);
                        downloadRemoteResource(url, folderName, log);
                    } catch (JSONException ex) {
                        log.println("Warning: Expected string type as url");
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            LOG.log(Level.SEVERE, "Unable to create download log file");
        } catch (UnsupportedEncodingException ex) {
            LOG.log(Level.SEVERE, "Unsupported encoding", ex);
        }
    }

    private void downloadRemoteResource(String urlStr, String folderPath, PrintStream log) {
        urlStr = urlStr.trim();
        if (urlStr.isEmpty()) {
            log.println("Found empty url");
            return;
        }
        //expand resource url if it starts with vocloud://
        urlStr = urlStr.replaceFirst("^vocloud://", Config.settings.getVocloudServerAddress() + "/files/");
        //create folder descriptor
        File folder = new File(workingDir, folderPath);
        folder.mkdirs();
        String[] split = urlStr.split("/");
        String fileName = split[split.length - 1];
        File file = new File(folder, fileName);
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException ex) {
            log.println("Malformed url: " + urlStr);
            return;
        }
        externalFiles.add(file);
        if (downloadFile(url, file)) {
            log.println("File " + fileName + " was successfully downloaded to /" + folderPath);
        } else {
            log.println("Failed to download file " + fileName + " to folder /" + folderPath);
        }
    }

    private boolean downloadFile(URL url, File file) {

        try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                FileOutputStream fos = new FileOutputStream(file)) {
            long transferred = fos.getChannel().transferFrom(rbc, 0, 1 << 16);
            long pos = transferred;
            while (transferred > 0) {
                transferred = fos.getChannel().transferFrom(rbc, pos, 1 << 16);
                pos += transferred;
            }
            return true;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "failed to download file from url " + url, e);
            return false;
        }
    }

    @Override
    public synchronized void abort() throws UWSException {
        // we still want results even when job is aborted
        prepareResults();
        super.abort();
        configFile = null;
    }

    @Override
    public void clearResources() {
        try {
            // delete working dir recursively
            FileUtils.deleteDirectory(jobDir);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Cannot delete job directory {0}", workingDir.toString());
        }
        super.clearResources();
    }

    private synchronized void prepareResults() throws UWSException {
        if (jobDir == null || workingDir == null) {
            return;
        }
        if (resultsPrepared) {
            return;
        }
        File zip = new File(jobDir, "results.zip");
        //remove downloaded files
        for (File f : externalFiles) {
            f.delete();
        }
        //delete empty folders
        for (File i : workingDir.listFiles()) {
            if (!i.isDirectory()) {
                continue;
            }
            if (FileUtils.listFiles(i, null, true).isEmpty()) {
                try {
                    FileUtils.deleteDirectory(i);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        }
        ZipUtil.pack(workingDir, zip);
        addResult(new Result("Results", "zip", Config.resultsLink + "/" + this.getJobId() + "/results.zip"));
        resultsPrepared = true;
    }
}
