package cz.mrq.vocloud.managedbeans;

import cz.mrq.vocloud.ejb.JobFacade;
import cz.mrq.vocloud.ejb.UWSFacade;
import cz.mrq.vocloud.ejb.UserSessionBean;
import cz.mrq.vocloud.entity.Job;
import cz.mrq.vocloud.entity.Phase;
import cz.mrq.vocloud.tools.Config;
import cz.mrq.vocloud.tools.Toolbox;
import cz.mrq.vocloud.uwsparser.UWSParserManager;
import cz.mrq.vocloud.uwsparser.model.UWSJob;
import org.apache.commons.io.FileUtils;
import org.primefaces.component.panel.Panel;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 *
 * @author voadmin
 */
public abstract class CreateJob implements Serializable {

    private static final Logger logger = Logger.getLogger(CreateJob.class.getName());

    @EJB
    protected JobFacade jf;
    @EJB
    protected UWSFacade uws;
    @EJB
    protected UserSessionBean usb;

    @Inject
    @Config
    protected String applicationAddress;
    @Inject
    @Config
    protected String tempDir;
    @Inject
    @Config
    protected String jobsDir;

    protected Job parent;
    protected Job job;
    protected UUID tid;
    protected Boolean run = false;
    protected Boolean email = false;
    protected File uploadDir;
    protected List<File> uploadedFiles;

    protected abstract String getJobType();

    public CreateJob() {
        job = new Job();
        tid = UUID.randomUUID();
//        job.setJobType(getJobType());
        uploadedFiles = new ArrayList<>();
        uploadDir = new File(tempDir, tid.toString());
    }

    @PostConstruct
    public void init() {
//
//        // if we are creating from other job, get parent
//        parent = (Job) FacesContext.getCurrentInstance().getAttributes().get("parent");
//
//        // copy values from parent
//        if (parent != null) {
//            job.setLabel(parent.getLabel() + " (copy)");
//            job.setNotes(parent.getNotes());
//
//            // add parameters
//            File dir = jf.getFileDir(parent);
//
//            File[] files = dir.listFiles();
//            if (files != null) {
//                for (File f : files) {
//                    uploadedFiles.add(f);
//                }
//            }
//            postInit();
//        }
    }

    protected abstract void postInit();

    @PreDestroy
    public void deleteTempFiles() {
        if (uploadDir.exists()) {
            Toolbox.delete(uploadDir);
        }
    }

    void copyUploadedFile(File out, UploadedFile uf) {
        try {
            copyFile(out, uf.getInputstream());
        } catch (IOException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "File copy failed.", ex.toString()));
            logger.log(Level.SEVERE, null, ex);
        }
    }

    void copyFile(File out, InputStream is) {
        try {
            BufferedInputStream bis = new BufferedInputStream(is);
            FileUtils.copyInputStreamToFile(bis, out);
        } catch (IOException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "File copy failed.", ex.toString()));
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * creates job
     */
    public void save() {
//        FacesContext currentInstance = FacesContext.getCurrentInstance();
//
//        // check for free space
//        if (usb.getUser().getQuota() <= jf.getSize(usb.getUser())) {
//            logger.log(Level.SEVERE, "Job cant be created not enough user space");
//            currentInstance.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                    "Job could been created, you exceeded your disk quota.", "Please free some space first."));
//        }
//
//        // set owner and save to the database to get id
//        job.setOwner(usb.getUser());
//        job.setId(0L);
//        job.setCreatedDate(new Date());
//        jf.create(job);
//
//        // results email
//        if (email) {
//            job.setResultsEmail(usb.getUser().getEmail());
//        }
//
//        // create job folder and copy uploaded files there
//        getJobFolder().mkdirs();
//        for (File f : uploadedFiles) {
//            try {
//                if (f.exists()) {
//                    FileUtils.copyFileToDirectory(f, getJobFolder());
//                }
//            } catch (IOException ex) {
//                logger.log(Level.SEVERE, "cannot copy file", ex);
//            }
//        }
//
//        handleSave();
//
//        // prepare params
//        File parameters = prepareParameters();
//
//        // expose for download
//        String exposeLocal = currentInstance.getExternalContext().getRealPath("/") + "/download/" + tid;
//        File expose = new File(exposeLocal);
//        expose.mkdirs();
//        try {
//            FileUtils.copyFileToDirectory(parameters, expose);
//        } catch (IOException ex) {
//            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                    "Can't create job!", "Parameters file can't be exposed."));
//            logger.log(Level.SEVERE, null, ex);
//        }
//
//        String link = applicationAddress + "/download/" + tid + "/" + parameters.getName();
//
//        // find uws and create job there
//        job.setUws(uws.assign(job.getJobType()));
//
//        if (job.getUws() == null) {
//            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                    "Can't create job!", "No worker is available."));
//            jf.delete(job);
//            return;
//        }
//
//        String param = "zip=" + link;
//        String reply;
//        try {
//            reply = job.getUws().createJob(param);
//        } catch (IOException ex) {
//            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                    "Can't create job!", "Job worker is unavailable."));
//            logger.log(Level.SEVERE, "cant create a job", ex);
//            jf.delete(job);
//            return;
//        }
//
//        // link up Job with UWSjob
//        UWSJob uwsJob = UWSParserManager.getInstance().parseJob(reply);
//        if (uwsJob == null || uwsJob.getJobId() == null) {
//            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                    "Failed to create a job on worker '" + job.getUws().getLabel() + "' !",
//                    "Worker is probably not configured properly."));
//            logger.log(Level.SEVERE, "can't create job");
//            job.setPhase(Phase.ERROR);
//        }
//        job.setUwsJob(uwsJob);
//        job.updateFromUWSJob();
//
//        jf.edit(job);
//
//        // execute if wanted
//        if (run) {
//            try {
//                jf.start(job);
//            } catch (IOException ex) {
//                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                        "Failed to start a job on worker '" + job.getUws().getLabel() + "' !", ex.getMessage()));
//                logger.log(Level.SEVERE, ex.getMessage(), ex);
//            }
//        }
//
//        // refresh jobs bean (if exist)
//        JobsBean jb = (JobsBean) currentInstance.getExternalContext().getSessionMap().get("jobs");
//        if (jb != null) {
//            jb.refresh();
//        }
//
//        //navigate away
//        //currentInstance.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Job created.", "New job '" + job.getLabel() + "' created."));
//        NavigationHandler myNav = currentInstance.getApplication().getNavigationHandler();
//        myNav.handleNavigation(currentInstance, "create", "index");
//
//        deleteTempFiles();
    }

    protected abstract File prepareParameters();

    protected abstract void handleFileUpload(FileUploadEvent event);

    protected abstract void handleSave();

    public void saveRun() {
        run = true;
        save();
    }

    public void cancel() {
        FacesContext currentInstance = FacesContext.getCurrentInstance();
        NavigationHandler myNav = currentInstance.getApplication().getNavigationHandler();
        myNav.handleNavigation(currentInstance, "create", "index");
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Boolean getEmail() {
        return email;
    }

    public void setEmail(Boolean email) {
        this.email = email;
    }

    public Boolean getRun() {
        return run;
    }

    public void setRun(Boolean run) {
        this.run = run;
    }

    public List<File> getUploadedFiles() {
        return uploadedFiles;
    }

    @Override
    public String toString() {
        return "CreateJob{" + "tid=" + tid.toString() + "job=" + job + ", run=" + run + ", email=" + email + ", uploadedFiles(" + uploadedFiles.size() + ")=" + uploadedFiles + '}';
    }

    public File getJobFolder() {
        return new File(jobsDir, job.getId().toString());
    }
}
