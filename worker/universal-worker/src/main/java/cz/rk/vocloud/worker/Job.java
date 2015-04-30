package cz.rk.vocloud.worker;

import cz.rk.vocloud.schema.Worker;
import uws.UWSException;
import uws.job.AbstractJob;
import uws.job.Result;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
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
    private static final String PROCESS_DATA_FOLDER_NAME = ".processData";
    private static final String WORKING_DIR_NAME = "workingDir";
    
    private String configFile;
    private File workingDir;
    private File jobDir;
    private Worker workerSettings;

    private File outputFile;
    private File errorFile;
    private File returnFile;

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
            if (configFile == null){
                throw new UWSException(UWSException.BAD_REQUEST, "Config file has to be specified.");
            }
        }
        return true;
    }

    @Override
    protected void jobWork() throws UWSException, InterruptedException {
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
        //define aborted variable
        Boolean aborted = false;
        //create job directory
        jobDir = new File(Config.resultsDir + "/" + getJobId());
        jobDir.mkdirs();
        //create working directory
        workingDir = new File(jobDir, WORKING_DIR_NAME);
        workingDir.mkdir();
        File processDataDir = new File(workingDir, PROCESS_DATA_FOLDER_NAME);
        processDataDir.mkdir();
        //print config file
        File config = new File(processDataDir, "config.json");
        try (FileOutputStream fos = new FileOutputStream(config)) {
            fos.write(configFile.getBytes(Charset.forName("UTF-8")));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Unable to create config.json file in working directory", ex);
            throw new UWSException("Unable to create config.json file in working directory");
        }
        configFile = null;
        //download necessary files into working directory
        downloadFiles(aborted);//extract them from config file
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
        Process process = null;
        List<String> commands = workerSettings.getExecCommand().getCommand();
        //use substitution for commands
        commands = substituteCommands(commands);
        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.directory(workingDir);

        try {
            pb.redirectOutput(outputFile);
            pb.redirectError(errorFile);

            process = pb.start();
            boolean processDone = false;
            do {
                processDone = process.waitFor(5, TimeUnit.SECONDS);
            } while (!aborted && !processDone && !thread.isInterrupted());
            if (aborted || thread.isInterrupted()) {
                throw new InterruptedException();
            }
        } catch (InterruptedException ie) {
            //kill process if job is aborted
            if (process != null) {
                LOG.log(Level.INFO, "Calling kill signal to process");
                process.destroyForcibly();
            }
            aborted = true;
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
        // submit results, send error if process failed
        prepareResults();
        if (process.exitValue() != 0) {
            throw new UWSException("process exit value:" + process.exitValue());
        }

        if (thread.isInterrupted() || aborted) {
            throw new InterruptedException();
        }
    }

    private List<String> substituteCommands(List<String> commands) {
        //supported tokens are ${binaries-location}, ${config-file}
        List<String> resolved = new ArrayList<>(commands.size());
        String tmp;
        for (String command : commands) {
            tmp = command
                    .replace("${binaries-location}", workerSettings.getBinariesLocation())
                    .replace("${config-file}", PROCESS_DATA_FOLDER_NAME + "/config.json");
            resolved.add(tmp);
        }
        return resolved;
    }

    private void downloadFiles(Boolean aborted) throws InterruptedException, UWSException {
        //todo not implemented yet
        System.out.println("Called download files");
    }

    private boolean downloadFile(URL url, File file) {

        try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                FileOutputStream fos = new FileOutputStream(file)) {
            long transferred = fos.getChannel().transferFrom(rbc, 0, 1 << 8);
            long pos = transferred;
            while (transferred > 0) {
                transferred = fos.getChannel().transferFrom(rbc, pos, 1 << 8);
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

    private void prepareResults() throws UWSException {
        File zip = new File(jobDir, "results.zip");
        //remove downloaded files
        for (File f : externalFiles) {
            f.delete();
        }
        ZipUtil.pack(workingDir, zip);
        addResult(new Result("Results", "zip", Config.resultsLink + "/" + this.getJobId() + "/results.zip"));
    }
}
