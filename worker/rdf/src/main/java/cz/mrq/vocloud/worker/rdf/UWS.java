package cz.mrq.vocloud.worker.rdf;

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
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "uws-rdf",urlPatterns = {"/uws/*"})
public class UWS extends HttpServlet {

    private static final long serialVersionUID = 2L;
    private static final Logger logger = Logger.getLogger(UWS.class.getName());

    protected QueuedBasicUWS<Job> uws;
	protected File restoreFile;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		ServletContext context = config.getServletContext();
		
        loadProperties(context);
                
		// Restore the last saved UWS, if any:
		restoreFile = new File(context.getRealPath("/WEB-INF/"), "uwsRestore");
        try {
		    uws = (QueuedBasicUWS<Job>) UWSToolBox.restoreUWS(restoreFile, true);
        } catch (Exception e) {
            logger.log(Level.WARNING, "loading saved uws failed, creating new");
            uws = null;
        }
		
		// If no saved UWS has been found, initialize the UWS:
		if (uws == null){
			// Ensure the results directory is empty (this wont delete directories)
			UWSToolBox.clearDirectory(Config.resultsDir);
			
			try{
				// Create the UWS:
				uws = new QueuedBasicUWS<>(Job.class, Config.maxJobs);
				
				// Set the destruction time for all jobs:
				uws.getDestructionTimeController().setDefaultDestructionInterval(1, DateField.MONTH);
				uws.getDestructionTimeController().setMaxDestructionInterval(1, DateField.MONTH);
				
				// Set the execution time for all jobs:
				uws.getExecutionDurationController().setDefaultExecutionDuration(3600);
				uws.getExecutionDurationController().setMaxExecutionDuration(3600);
				
				// Set the way the UWS must identify a user:
				uws.setUserIdentifier(new UserIdentifier() {
					private static final long serialVersionUID = 2L;

					@Override
					public String extractUserId(UWSUrl urlInterpreter, HttpServletRequest request) throws UWSException {
						return request.getRemoteAddr();
					}
				});
				
				// Set a description:
				uws.setDescription("UWS RDF");
				
				// Create the job list "som":
				uws.addJobList(new JobList<Job>("rdf"));
			}catch(UWSException ex){
				throw new ServletException(ex);
			}
		}
	}

    private void loadProperties(ServletContext context) {
        Properties configFile = new Properties();
        try {
            configFile.load(UWS.class.getResourceAsStream("/uws.conf"));
            logger.log(Level.INFO, "successfully loaded config from uws.conf");
        } catch (IOException ex) {
            logger.log(Level.WARNING, "failed to load config file uws.conf", ex);
        }

        Config.serverAddress = configFile.getProperty("server_address", "http://localhost:8080");
        Config.maxJobs = Integer.parseInt(configFile.getProperty("jobs_max", "4"));

        // Fetch the results directory path:
        String resultsDirectoryPath = context.getRealPath("/") + "/results";
        try {
            new File(resultsDirectoryPath).mkdirs();
        } catch (SecurityException e) {
            logger.log(Level.SEVERE, "failed to create result directory " + resultsDirectoryPath, e);
        }

        Config.resultsDir = resultsDirectoryPath;
        Config.resultsLink = Config.serverAddress + context.getContextPath() + "/results";

        Config.binariesLocation = configFile.getProperty("binaries_location", Config.binariesLocation);
    }

	@Override
	public void destroy() {
		// Save the current state of this UWS:
        UWSToolBox.saveUWS(uws, restoreFile, true);
		super.destroy();
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.log(Level.INFO, "Executing service");
		try {
			uws.executeRequest(req, resp);
		} catch(UWSException uwsEx) {
			resp.sendError(uwsEx.getHttpErrorCode(), uwsEx.getMessage());
		}
	}
}
