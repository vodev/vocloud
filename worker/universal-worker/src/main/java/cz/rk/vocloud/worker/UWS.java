package cz.rk.vocloud.worker;

import cz.rk.vocloud.schema.UwsSettings;
import cz.rk.vocloud.schema.Worker;
import uws.UWSException;
import uws.UWSToolBox;
import uws.job.JobList;
import uws.service.QueuedBasicUWS;
import uws.service.UWSUrl;
import uws.service.UserIdentifier;
import uws.service.controller.DestructionTimeController.DateField;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

@WebServlet(name = "uws-preprocess", urlPatterns = {"/uws/*"})
public class UWS extends HttpServlet {

    private static final long serialVersionUID = 2L;
    private static final Logger LOG = Logger.getLogger(UWS.class.getName());

    protected QueuedBasicUWS<Job> uws;
    protected File restoreFile;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = config.getServletContext();

        UwsSettings settings = Config.settings;
        if (settings == null) {
            //load default
            try {
                JAXBContext jxb = JAXBContext.newInstance("cz.rk.vocloud.schema");
                Unmarshaller un = jxb.createUnmarshaller();
                settings = (UwsSettings) un.unmarshal(UWS.class.getResourceAsStream("/uws-config.xml"));
            } catch (JAXBException ex) {
                LOG.log(Level.SEVERE, null, ex);
                throw new ServletException(ex);
            }
        }
        if (settings == null){
            throw new ServletException("Unable to load xml config file");
        } else {
            Config.settings = settings;
        }
        //set result directory
        Config.resultsDir = context.getRealPath("/") + "/results";
        Config.resultsLink = settings.getLocalAddress() + context.getContextPath() + "/results";
        // Restore the last saved UWS, if any:
        restoreFile = new File(context.getRealPath("/WEB-INF/"), "uwsRestore");
        try {
            uws = (QueuedBasicUWS<Job>) UWSToolBox.restoreUWS(restoreFile, true);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "loading saved uws failed, creating new");
            uws = null;
        }

        // If no saved UWS has been found, initialize the UWS:
        if (uws == null) {
            // Ensure the results directory is empty (this wont delete directories)
            UWSToolBox.clearDirectory(Config.resultsDir);

            try {
                // Create the UWS:
                uws = new QueuedBasicUWS<>(Job.class, settings.getMaxJobs().intValue());
                // setup destruction time controller
                if (settings.getDefaultDestructionInterval() == null) {
                    //use default value 1 month
                    uws.getDestructionTimeController().setDefaultDestructionInterval(1, DateField.MONTH);
                } else {
                    uws.getDestructionTimeController().setDefaultDestructionInterval(settings.getDefaultDestructionInterval().intValue(), DateField.SECOND);
                }
                if (settings.getMaxDestructionInterval() == null) {
                    uws.getDestructionTimeController().setMaxDestructionInterval(1, DateField.MONTH);
                } else {
                    uws.getDestructionTimeController().setMaxDestructionInterval(settings.getMaxDestructionInterval().intValue(), DateField.SECOND);
                }

                // setup execution duration controller
                if (settings.getDefaultExecutionDuration() == null) {
                    uws.getExecutionDurationController().setDefaultExecutionDuration(3600);
                } else {
                    uws.getExecutionDurationController().setDefaultExecutionDuration(settings.getDefaultExecutionDuration().intValue());
                }
                if (settings.getMaxExecutionDuration() == null) {
                    uws.getExecutionDurationController().setMaxExecutionDuration(3600);
                } else {
                    uws.getExecutionDurationController().setMaxExecutionDuration(settings.getMaxExecutionDuration().intValue());
                }

                // Set the way the UWS must identify a user:
                uws.setUserIdentifier(new UserIdentifier() {
                    private static final long serialVersionUID = 2L;

                    //identification by remote ip - jobs of vocloud will be visible only for vocloud...

                    @Override
                    public String extractUserId(UWSUrl urlInterpreter, HttpServletRequest request) throws UWSException {
                        return request.getRemoteAddr();
                    }
                });

                // Set a description:
                uws.setDescription(settings.getDescription());

                // for every specified worker create joblist
                for (Worker worker : settings.getWorkers().getWorker()) {
                    if (worker.getIdentifier().contains(" ")) {
                        LOG.log(Level.WARNING, "Worker identifier: {0} has not valid name", worker.getIdentifier());
                        continue;
                    }
                    uws.addJobList(new JobList<Job>(worker.getIdentifier()));
                }
            } catch (UWSException ex) {
                throw new ServletException(ex);
            }
        }
    }

//    private void loadProperties(ServletContext context) {
//        Properties configFile = new Properties();
//        try {
//            configFile.load(UWS.class.getResourceAsStream("/uws.conf"));
//            LOG.log(Level.INFO, "successfully loaded config from uws.conf");
//        } catch (IOException ex) {
//            LOG.log(Level.WARNING, "failed to load config file uws.conf", ex);
//        }
//
//        Config.serverAddress = configFile.getProperty("server_address", "http://localhost:8080");
//        Config.maxJobs = Integer.parseInt(configFile.getProperty("jobs_max", "4"));
//
//        // Fetch the results directory path:
//        String resultsDirectoryPath = context.getRealPath("/") + "/results";
//        try {
//            new File(resultsDirectoryPath).mkdirs();
//        } catch (SecurityException e) {
//            LOG.log(Level.SEVERE, "failed to create result directory " + resultsDirectoryPath, e);
//        }
//
//        Config.resultsDir = resultsDirectoryPath;
//        Config.resultsLink = Config.serverAddress + context.getContextPath() + "/results";
//
//        Config.binariesLocation = configFile.getProperty("binaries_location", Config.binariesLocation);
//    }

    @Override
    public void destroy() {
        // Save the current state of this UWS:
        UWSToolBox.saveUWS(uws, restoreFile, true);
        super.destroy();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //logger.log(Level.INFO, "Executing service");
        try {
            uws.executeRequest(req, resp);
        } catch (UWSException uwsEx) {
            resp.sendError(uwsEx.getHttpErrorCode(), uwsEx.getMessage());
        }
    }
}
