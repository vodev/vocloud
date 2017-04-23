package cz.ivoa.vocloud.spark.worker;

import cz.ivoa.vocloud.spark.schema.Worker;
import cz.ivoa.vocloud.spark.worker.model.CopyOutputPath;
import cz.ivoa.vocloud.spark.worker.model.DownloadFileRule;
import cz.ivoa.vocloud.spark.worker.model.ParsedJsonConfig;
import cz.ivoa.vocloud.spark.worker.model.SparkConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.*;
import uws.UWSException;
import uws.job.AbstractJob;
import uws.job.Result;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.ZipUtil;
import org.apache.hadoop.conf.Configuration;

import javax.json.*;

/**
 * @author kozajaku
 */
public class Job extends AbstractJob {

    private static final Logger LOG = Logger.getLogger(Job.class.getName());

    private static final String OUTPUT_FILE_NAME = "run.out";
    private static final String ERROR_FILE_NAME = "run.err";
    private static final String RETURN_FILE_NAME = "run.ret";
    private static final String SPARK_JOB_CONFIG_FILE_NAME = "sparkJobConfig.json";
    private static final String WHOLE_CONFIG_FILE_NAME = "config.json";
    private static final String DOWNLOAD_LOG_FILE_NAME = "download.log";
    private static final String SUBMIT_COMMAND_FILE_NAME = "submitCommand";
    private static final String PROCESS_DATA_FOLDER_NAME = ".processData";
    private static final String WORKING_DIR_NAME = "workingDir";

    private String configFile;
    private ParsedJsonConfig parsedJsonConfig;
    private SparkConfiguration sparkConfiguration;
    private File workingDir;
    private File jobDir;
    private Worker workerSettings;
    private Configuration hadoopConfiguration;

    private File outputFile;
    private File errorFile;
    private File returnFile;
    private File downloadLogFile;
    private File processDataDir;

    private boolean resultsPrepared = false;

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

    /**
     * Finds the Worker specification for the current Job. If the Worker settings could not
     * be found, throws <code>{@link UWSException}</code>.
     *
     * @return Instance of the <code>{@link Worker}</code> of the current Job.
     * @throws UWSException in case that this Job does not have
     *                      settings inside xml configuration file.
     */
    private Worker getThisWorker() throws UWSException {
        for (Worker w : Config.settings.getWorkers().getWorker()) {
            if (w.getIdentifier().equals(getJobList().getName())) {
                return w;
            }
        }
        //note: this should not happen
        LOG.log(Level.SEVERE, "Worker with identifier {0} is not specified in settings", getJobList().getName());
        throw new UWSException("Worker with identifier " + getJobList().getName() + " was not found");
    }

    private List<String> constructSubmitCommands() {
        List<String> commands = new ArrayList<>();
        //add spark-submit script
        commands.add(Config.settings.getSparkExecutable());
        //append spark submit parameters
        commands.addAll(sparkConfiguration.commandsList());
        //append spark program
        commands.add(workerSettings.getSubmitTarget().trim());
        //append configuration file
        commands.add(new File(processDataDir, SPARK_JOB_CONFIG_FILE_NAME).getAbsolutePath().trim());
        return commands;
    }

    private void printSubmitCommand(List<String> commands) {
        if (commands == null || commands.isEmpty()) {
            return;
        }
        Iterator<String> iter = commands.iterator();
        StringBuilder builder = new StringBuilder(iter.next());
        while (iter.hasNext()) {
            builder.append(" ").append(iter.next());
        }
        File commandFile = new File(processDataDir, SUBMIT_COMMAND_FILE_NAME);
        String commString = builder.toString();
        try (FileOutputStream fos = new FileOutputStream(commandFile)) {
            fos.write(commString.getBytes(Charset.forName("UTF-8")));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Unable to create submitCommand file in working directory", ex);
        }
    }

    @Override
    @SuppressWarnings({"UseSpecificCatch", "SleepWhileInLoop"})
    protected void jobWork() throws UWSException, InterruptedException {
        try {
            LOG.log(Level.INFO, "Starting job {0}", getJobId());
            //save worker settings
            workerSettings = getThisWorker();
            //try to parse json config
            parsedJsonConfig = new ParsedJsonConfig(configFile);
            sparkConfiguration = new SparkConfiguration(workerSettings, configFile);
            //create hadoop configuration
            hadoopConfiguration = new Configuration();
            hadoopConfiguration.set("fs.defaultFS", Config.settings.getHadoopDefaultFs());
            //free config file
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
                File config = new File(processDataDir, WHOLE_CONFIG_FILE_NAME);
                File sparkJobConfig = new File(processDataDir, SPARK_JOB_CONFIG_FILE_NAME);
                try (FileOutputStream fos = new FileOutputStream(config);
                     FileOutputStream fosSpark = new FileOutputStream(sparkJobConfig)) {
                    fos.write(configFile.getBytes(Charset.forName("UTF-8")));
                    fosSpark.write(parsedJsonConfig.getJobConfig().getBytes(Charset.forName("UTF-8")));
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "Unable to create config files in working directory", ex);
                    throw new UWSException("Unable to create config files in working directory");
                }
                configFile = null;
                //download necessary files into HDFS
                downloadFiles();
                // prepare output files
                try {
                    outputFile = new File(processDataDir, OUTPUT_FILE_NAME);
                    errorFile = new File(processDataDir, ERROR_FILE_NAME);
                    returnFile = new File(processDataDir, RETURN_FILE_NAME);
                    assert outputFile.createNewFile();
                    assert errorFile.createNewFile();
                    assert returnFile.createNewFile();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                    throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "IO Exception when creating output files.");
                }
                //create process itself
                List<String> commands = constructSubmitCommands();
                //print call commands
                printSubmitCommand(commands);
                //use substitution for commands
                ProcessBuilder pb = new ProcessBuilder(commands);
                pb.environment().putAll(sparkConfiguration.getEnvironment());
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
                            //it was completed
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
                    LOG.log(Level.INFO, "Calling kill signal to process");
                    process.destroy();
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "error when executing job", e);
                    throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, e, "error when executing job");
                }
                // write return code to the file
                try (PrintStream ps = new PrintStream(returnFile)) {
                    LOG.log(Level.INFO, "Process exit value: {0}", process.exitValue());
                    ps.println(process.exitValue());
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                    throw new UWSException(ex);
                }
                copyOutput();
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
                throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, ex, "unexpected throwable exception/error caught during job execution");
            }
        }
    }

    private void copyOutput() throws InterruptedException, UWSException {
        try {
            for (CopyOutputPath path : parsedJsonConfig.getCopyOutputPaths()) {
                try (FileSystem fs = FileSystem.get(hadoopConfiguration)) {
                    Path hdfsPath = new Path(path.getPath());
                    if (fs.exists(hdfsPath)) {
                        if (fs.isDirectory(hdfsPath) && path.isMergeParts()) {
                            try (OutputStream out = new FileOutputStream(new File(workingDir, path.getOutputName()))) {
                                FileStatus contents[] = fs.listStatus(hdfsPath);
                                for (FileStatus fileStatus : contents) {
                                    //ignore directories
                                    if (fileStatus.isDirectory()) {
                                        continue;
                                    }
                                    try (InputStream in = fs.open(fileStatus.getPath())) {
                                        IOUtils.copy(in, out);
                                    }
                                }
                            }
                        } else {
                            FileUtil.copy(fs, new Path(path.getPath()),
                                    new File(workingDir, path.getOutputName()),
                                    false, hadoopConfiguration);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "IOException in copyOutput function", ex);
            throw new UWSException(UWSException.BAD_REQUEST, ex, "exception during copying files from HDFS");
        }
    }

    private void downloadFiles() throws InterruptedException {
        downloadLogFile = new File(processDataDir, DOWNLOAD_LOG_FILE_NAME);
        try (PrintStream log = new PrintStream(downloadLogFile, "UTF-8") {

            private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            @Override
            public void println(String x) {
                String timestamp = format.format(new Date());
                super.println(timestamp + " " + x);
            }

        }) {
            for (DownloadFileRule rule : parsedJsonConfig.getDownloadRules()) {
                for (String url : rule.getUrls()) {
                    if (thread.isInterrupted()) {
                        throw new InterruptedException();//aborted
                    }
                    downloadRemoteResource(url, rule.getFolder(), log);
                }
            }
        } catch (FileNotFoundException ex) {
            LOG.log(Level.SEVERE, "Unable to create download log file");
        } catch (UnsupportedEncodingException ex) {
            LOG.log(Level.SEVERE, "Unsupported encoding", ex);
        }
    }

    private void downloadRemoteResource(String urlStr, String folderPath, PrintStream log) {
        folderPath = folderPath.trim();
        urlStr = urlStr.trim();
        if (urlStr.isEmpty()) {
            log.println("Found empty url");
            return;
        }
        //expand resource url if it starts with vocloud://
        urlStr = urlStr.replaceFirst("^vocloud://", Config.settings.getVocloudServerAddress() + "/files/");
        //create URL
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException ex) {
            log.println("Malformed url: " + urlStr);
            return;
        }
        //try to create HttpUrlConnection
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException ex) {
            log.println("Unable to connect to: " + urlStr);
            return;
        } catch (ClassCastException ex) {
            log.println("Resource is not http/https connection: " + urlStr);
            return;
        }
        //check if it is json - possible folder
        if ("application/json".equals(conn.getContentType())) {
            //possible folder
            StringBuilder folderJson = new StringBuilder();
            //read into folderJson
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    folderJson.append(line);
                }
            } catch (IOException ex) {
                log.println("Exception during reading folder json from: " + urlStr);
                conn.disconnect();
                return;
            }
            conn.disconnect();
            JsonObject folderDesc;
            try (JsonReader jsonReader = Json.createReader(new StringReader(folderJson.toString()));) {
                folderDesc = jsonReader.readObject();
            } catch (JsonException ex) {
                log.println("Unable to parse folder describing json from: " + urlStr);
                return;
            }
            JsonArray folders;
            JsonArray files;
            try {
                folders = folderDesc.getJsonArray("folders");
                files = folderDesc.getJsonArray("files");
            } catch (JsonException ex) {
                log.println("Invalid format of folder json from: " + urlStr);
                return;
            }
            try {
                //call function recursively on folders and files
                for (JsonValue i : folders) {
                    String folderName = ((JsonString) i).getString();
                    downloadRemoteResource(conc(urlStr, URLEncoder.encode(folderName, "UTF-8")),
                            conc(folderPath, folderName), log);
                }
                for (JsonValue i : files) {
                    String fileName = ((JsonString) i).getString();
                    downloadRemoteResource(conc(urlStr, URLEncoder.encode(fileName, "UTF-8")), folderPath, log);
                }
            } catch (UnsupportedEncodingException ex) {
                LOG.log(Level.SEVERE, "Unsupported encoding", ex);
            }
        } else {
            //create folder descriptor
            String[] split = urlStr.split("/");
            String fileName = split[split.length - 1];
            try {
                fileName = URLDecoder.decode(fileName, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                LOG.log(Level.SEVERE, "Unsupported encoding", ex);
                return;
            }
            String hdfsPath = conc(folderPath, fileName);
            String error = downloadFile(conn, hdfsPath);
            if (error == null) {
                log.println("File " + fileName + " was successfully downloaded to " + folderPath);
            } else {
                log.println("Failed to download file " + fileName + " to folder " + folderPath + ". Reason: " + error);
            }
            conn.disconnect();
        }
    }

    private String conc(String prefix, String value) {
        //concatenates two strings and puts slash between them if it is not already
        if (prefix.endsWith("/")) {
            return prefix + value;
        }
        //else
        return prefix + '/' + value;
    }

    private String downloadFile(HttpURLConnection conn, String hdfsPath) {
        int bufferSize = 1 << 16;
        try (FileSystem file = FileSystem.get(hadoopConfiguration);
             FSDataOutputStream fos = file.create(new Path(hdfsPath), false, bufferSize);
             ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream());
             WritableByteChannel wbc = Channels.newChannel(fos)) {
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            int bytesRead = rbc.read(buffer);
            while (bytesRead != -1) {
                buffer.flip();
                wbc.write(buffer);
                buffer.clear();
                bytesRead = rbc.read(buffer);
            }
            return null;
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "File download failed", ex);
            return ex.getMessage();
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
            LOG.log(Level.WARNING, "Cannot delete job directory {0}", jobDir.toString());
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
        //delete empty folders
        ZipUtil.pack(workingDir, zip);
        addResult(new Result("Results", "zip", Config.resultsLink + "/" + this.getJobId() + "/results.zip"));
        resultsPrepared = true;
    }
}
