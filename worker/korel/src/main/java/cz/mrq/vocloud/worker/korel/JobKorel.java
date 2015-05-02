package cz.mrq.vocloud.worker.korel;

import uws.UWSException;
import uws.job.AbstractJob;
import uws.job.Result;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author mrq
 */
public class JobKorel extends AbstractJob {

    private static final Logger logger = Logger.getLogger(JobKorel.class.getName());

    private static String resultsDir;
    private static String resultsLink;
    private static String korelExecutable;
    private static String scriptsDir;
    private URL zip;
    private File workingDir;

    public JobKorel(Map<String, String> lstParam) throws UWSException {
        super(lstParam);
    }

    @Override
    protected boolean loadAdditionalParams() throws UWSException {
        // check for parameter file
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

        // prepare env
        workingDir = new File(resultsDir + "/" + getJobId());
        workingDir.mkdir();

        // download args (dat and par file)
        File zipFile = new File(resultsDir + "/" + getJobId() + "/parameters.zip");

        try {
            if (!downloadFile(zip, zipFile))
            if (!downloadFile(new URL(zip.getProtocol(), owner, zip.getPort(), zip.getPath()), zipFile))
                throw new Exception();
        } catch (Exception e) {
            throw new UWSException(UWSException.BAD_REQUEST, "Cannot download ZIP file.");
        }

        decompress(zipFile, workingDir);

        // prepare output files
        File outputFile = new File(workingDir, "korel.out");
        File errorFile = new File(workingDir, "korel.err");
        File returnFile = new File(workingDir, "korel.ret");
        try {
            outputFile.createNewFile();
            errorFile.createNewFile();
            returnFile.createNewFile();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "IO Exception when creating output files.");
        }

        // prepare korel binary to execute
        Process korelProcess = null;
        ProcessBuilder korelPB = new ProcessBuilder(korelExecutable);
        korelPB.directory(workingDir);

        try {
            // redirect output 
            //JAVA7 only
            korelPB.redirectOutput(outputFile);

            korelPB.redirectError(errorFile);

            korelProcess = korelPB.start();

            // wait for process to finish
            korelProcess.waitFor();

        } catch (InterruptedException ie) {
            //kill process if job is aborted
            korelProcess.destroy();
            aborted = true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error when executing binary", e);
            throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, e, "error when executing binary");
        }

        // write return code to the file
        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(returnFile));
            logger.info("korel exit value: " + korelProcess.exitValue());
            out.write("" + korelProcess.exitValue());
            out.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new UWSException(ex);
        }

        // postprocessing scripts only if job is not aborted
        if (!aborted) {

            // prepare post-process
            File postOutput = new File(workingDir + "/post-scripts-worker.out");
            try {
                postOutput.createNewFile();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
                throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "IO Exception when creating post process output file.");
            }
            Process postProcess = null;

            
            
            // run parts run any executable (must have permission) scripts *.sh in scripts directory
            ProcessBuilder postPB = new ProcessBuilder("run-parts","-v", "--regex='^.*.sh$'", scriptsDir);
            //ProcessBuilder plotsPB = new ProcessBuilder("bash",scriptsDir + "/run-all.sh");

            postPB.directory(workingDir);

            try {
                postPB.redirectErrorStream(true);
                postPB.redirectOutput(postOutput);
                postProcess = postPB.start();
                postProcess.waitFor();
            } catch (InterruptedException ie) {
                //kill process if job is aborted
                aborted = true;
                postProcess.destroy();
            } catch (Exception e) {
                logger.log(Level.SEVERE, null, e);
                throw new UWSException(e);
            }
        }

        // submit results, send error if korel failed
        prepareResults();
        if (korelProcess.exitValue() != 0) {
            throw new UWSException("Korel exit value:" + korelProcess.exitValue());
        }

        if (thread.isInterrupted() || aborted) {
            throw new InterruptedException();
        }
    }

    private boolean downloadFile(URL url, File file) {
        try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
             FileOutputStream fos = new FileOutputStream(file)) {
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            return true;
        } catch (IOException e) {
            logger.log(Level.WARNING, "failed to download file", e);
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

    private void prepareResults() {
        try {
            // check if there are some result files
            String[] list = workingDir.list();
            if (list != null && list.length != 0) {

                // compress files in the working dir
                compress();

                // add result.zip to the job results
                addResult(new Result("Results", "zip", getResultLink() + this.getJobId() + "/results.zip"));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    public static String getResultDirectory() {

        return resultsDir;
    }

    public static void setResultDirectory(String directoryPath) {
        if (resultsDir == null) {
            resultsDir = directoryPath;
        }
    }

    public static String getResultLink() {
        return resultsLink;
    }

    public static void setResultLink(String directoryPath) {
        if (resultsLink == null) {
            resultsLink = directoryPath;
        }
    }

    public static void setKorelExecutable(String directoryPath) {
        if (korelExecutable == null) {
            korelExecutable = directoryPath;
        }
    }

    public static void setScriptsDir(String directoryPath) {
        if (scriptsDir == null) {
            scriptsDir = directoryPath;
        }
    }

    //FROM http://java.sun.com/developer/technicalArticles/Programming/compression/
    private void decompress(File file, File outputDirectory) {
        try {
            int buffer = 2048;
            BufferedOutputStream dest;
            BufferedInputStream is;
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(file);
            Enumeration e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                //logger.log(Level.INFO, "Extracting: {0}", entry);
                is = new BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                byte data[] = new byte[buffer];
                File out = new File(outputDirectory, entry.getName());
                FileOutputStream fos = new FileOutputStream(out);
                dest = new BufferedOutputStream(fos, buffer);
                while ((count = is.read(data, 0, buffer))
                        != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
                is.close();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Cannot compress files {0}", e.toString());
        }

    }

    private void compress() {
        int buffer = 2048;
        try {
            String output = workingDir.getAbsolutePath() + "/results.zip";
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(output);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[2048];
            // get a list of files from current directory
            String files[] = workingDir.list();

            for (String file : files) {
                if ("results.zip".equals(file)
                        || "korel.par".equals(file)
                        || "korel.dat".equals(file)
                        || "korel.tmp".equals(file)
                        || "parameters.zip".equals(file)) {
                    continue;
                }
                //logger.log(Level.INFO, "Adding: {0}", files[i]);
                File f = new File(workingDir, file);
                FileInputStream fi = new FileInputStream(f);
                origin = new BufferedInputStream(fi, buffer);
                ZipEntry entry = new ZipEntry(file);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0,
                        buffer)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Cannot compress files {0}", e.toString());
        }

    }

    private static String readFileAsString(String filePath) throws java.io.IOException {
        byte[] buffer = new byte[(int) new File(filePath).length()];
        BufferedInputStream f = null;
        try {
            f = new BufferedInputStream(new FileInputStream(filePath));
            f.read(buffer);
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException ignored) {
                }
            }
        }
        return new String(buffer);
    }
}
