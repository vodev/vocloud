package cz.mrq.vocloud.worker.korel;

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

@WebServlet(name = "uws-korel",urlPatterns = {"/uws/*"})
public class UWSKorel extends HttpServlet {

    private static final long serialVersionUID = 2L;
    private static final Logger logger = Logger.getLogger(UWSKorel.class.getName());

    protected QueuedBasicUWS<JobKorel> uws = null;
	protected File restoreFile = null;
    protected String serverAddress = "http://localhost:8080";
    protected int jobsMax = 4;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		ServletContext context = config.getServletContext();
		
        loadProperties(context);
                
		// Restore the last saved UWS, if any:
		restoreFile = new File(context.getRealPath("/WEB-INF/"), "uwsRestoreKorel");
        try {
		    uws = (QueuedBasicUWS<JobKorel>) UWSToolBox.restoreUWS(restoreFile, true);
        } catch (Exception e) {
            logger.log(Level.WARNING, "loading saved uws failed, creating new");
            uws = null;
        }
		
		// If no saved UWS has been found, initialize the UWS:
		if (uws == null){
			// Ensure the results directory is empty (this wont delete directories)
			UWSToolBox.clearDirectory(JobKorel.getResultDirectory());
			
			try{
				// Create the UWS:
				uws = new QueuedBasicUWS<>(JobKorel.class,jobsMax);
				
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
				uws.setDescription("UWS Korel");
				
				// Create the job list "korel":
				uws.addJobList(new JobList<JobKorel>("korel"));
				
				// Add an action to get the last executed action:
				uws.addUWSAction(0, new GetLastAction(uws));
			}catch(UWSException ex){
				throw new ServletException(ex);
			}
		}
	}

    private void loadProperties(ServletContext context) {
        Properties configFile = new Properties();
        try {
            configFile.load(UWSKorel.class.getResourceAsStream("/uws-korel.conf"));
            logger.log(Level.INFO, "successfully loaded config from uws-korel.conf");
        } catch (IOException ex) {
            logger.log(Level.WARNING, "failed to load config file uws-korel.conf", ex);
        }

        // set configured values
        // server address
        serverAddress = configFile.getProperty("server_address", "http://localhost:8080");
        // jobs max, default: 4
        jobsMax = Integer.parseInt(configFile.getProperty("jobs_max", "4"));

        // korel bin, default: /bin/korel
        JobKorel.setKorelExecutable(configFile.getProperty("korel_bin", "/bin/korel"));


        //set scripts dir
        JobKorel.setScriptsDir(context.getRealPath("/WEB-INF/scripts/"));


        // Fetch the results directory path:
        String resultsDirectoryPath = context.getRealPath("/results/korel");
        try {
            new File(resultsDirectoryPath).mkdirs();
        } catch (SecurityException e) {
            logger.log(Level.SEVERE, "failed to create result directory " + resultsDirectoryPath, e);
        }


        // set result directory for korel job
        JobKorel.setResultDirectory(resultsDirectoryPath);
        JobKorel.setResultLink(serverAddress + context.getContextPath() + "/results/korel/");
    }

	@Override
	public void destroy() {
		// Save the current state of this UWS:
        UWSToolBox.saveUWS(uws, restoreFile, true);
		super.destroy();
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			uws.executeRequest(req, resp);
		} catch(UWSException uwsEx) {
			resp.sendError(uwsEx.getHttpErrorCode(), uwsEx.getMessage());
		}
	}
}
