package cz.mrq.vocloud.worker.preprocessing;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.ZipUtil;
import uws.UWSException;
import uws.job.AbstractJob;
import uws.job.Result;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mrq
 */
public class Job extends AbstractJob {

    private static final Logger logger = Logger.getLogger(Job.class.getName());

    URL zip;
    File workingDir;

    File outputFile = new File(workingDir, "run.out");
    File errorFile = new File(workingDir, "run.err");
    File returnFile = new File(workingDir, "run.ret");

    public Job(Map<String, String> lstParam) throws UWSException {
        super(lstParam);
    }

    @Override
    protected boolean loadAdditionalParams() throws UWSException {
        // check for parameter file
        logger.log(Level.INFO, "loading params");
        if (additionalParameters.containsKey("zip")) {
            try {
                this.zip = new URL(additionalParameters.get("zip"));
            } catch (MalformedURLException ex) {
                throw new UWSException(UWSException.BAD_REQUEST, "ZIP input file address malformed.");
            }
        } else {
            throw new UWSException(UWSException.BAD_REQUEST, "ZIP input file has to be specified.");
        }

        return true;
    }

    @Override
    protected void jobWork() throws UWSException, InterruptedException {

        Boolean aborted = false;
        logger.info("creating result");
        workingDir = new File(Config.resultsDir + "/" + getJobId());
        workingDir.mkdir();

        File zipFile = new File(Config.resultsDir + "/" + getJobId() + "/parameters.zip");

        try {
            logger.info("downloading");

            if (!downloadFile(zip, zipFile)) {
                if (!downloadFile(new URL(zip.getProtocol(), owner, zip.getPort(), zip.getPath()), zipFile)) {
                    throw new Exception();
                }
            }
        } catch (Exception e) {
            throw new UWSException(UWSException.BAD_REQUEST, "Cannot download ZIP file.");
        }

        ZipUtil.unpack(zipFile, workingDir);

        // prepare output files
        try {
            outputFile.createNewFile();
            errorFile.createNewFile();
            returnFile.createNewFile();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "IO Exception when creating output files.");
        }

        Process process = null;
        ProcessBuilder pb = new ProcessBuilder("python3", Config.binariesLocation + "/run_preprocessing.py", "config.json");
        pb.directory(workingDir);

        try {
            pb.redirectOutput(outputFile);
            pb.redirectError(errorFile);

            process = pb.start();
            process.waitFor();

        } catch (InterruptedException ie) {
            //kill process if job is aborted
            process.destroy();
            aborted = true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error when executing job", e);
            throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, e, "error when executing job");
        }

        // write return code to the file
        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(returnFile));
            logger.info("process exit value: " + process.exitValue());
            out.write("" + process.exitValue());
            out.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
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

    private void copyBinaries() throws UWSException {
        try {
            FileUtils.copyDirectory(new File(Config.binariesLocation), workingDir);
        } catch (IOException e) {
            throw new UWSException("binaries copy failed");
        }
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
            logger.log(Level.WARNING, "failed to download file from url " + url, e);
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
        // delete working dir
        if (workingDir != null) {
            for (File f : workingDir.listFiles()) {
                f.delete();
            }
            boolean res = workingDir.delete();
            if (!res) {
                logger.log(Level.WARNING, "Cannot delete working dir {0}", workingDir.toString());
            }
        }
        super.clearResources();
    }

    private void prepareResults() throws UWSException {
        File zip = new File(workingDir, "results.zip");
        File results = new File(workingDir, "result");

        try {
            FileUtils.copyFileToDirectory(outputFile, results);
            FileUtils.copyFileToDirectory(errorFile, results);
            FileUtils.copyFileToDirectory(returnFile, results);
        } catch (IOException e) {
            throw new UWSException("failed to copy log files to the results");
        }

        ZipUtil.pack(results, zip);

        addResult(new Result("Results", "zip", Config.resultsLink + "/" + this.getJobId() + "/results.zip"));
    }
}
