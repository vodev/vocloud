package cz.ivoa.vocloud.view;

import cz.ivoa.vocloud.ejb.JobFacade;
import cz.ivoa.vocloud.ejb.UWSTypeFacade;
import cz.ivoa.vocloud.ejb.UserSessionBean;
import cz.ivoa.vocloud.entity.Job;
import cz.ivoa.vocloud.entity.UWSType;
import cz.ivoa.vocloud.entity.UserAccount;
import cz.ivoa.vocloud.entity.UserGroupName;
import cz.ivoa.vocloud.filesystem.FilesystemManipulator;
import cz.ivoa.vocloud.filesystem.model.FilesystemFile;
import cz.ivoa.vocloud.filesystem.model.FilesystemItem;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.commons.io.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.util.TreeUtils;

/**
 *
 * @author radio.koza
 */
@Named
@ViewScoped
public class CreateJobBean implements Serializable {

    private static final Logger LOG = Logger.getLogger(CreateJobBean.class.getName());

    @EJB
    private UWSTypeFacade uwsTypeFacade;
    @EJB
    private JobFacade jobFacade;
    @EJB
    private UserSessionBean usb;
    @EJB
    private FilesystemManipulator fsm;

    private UserAccount userAcc;

    private UWSType chosenUwsType;

    private String configurationJson;

    private String jobLabel;
    private String jobNotes;
    private boolean jobEmail;
    private boolean copyAfter = false;
    private String targetFolder = "/";

    private TreeNode folderTreeRootNode;
    private List<FilesystemFile> precreatedConfigs;
    private FilesystemFile selectedPrecreatedConfig;

    @PostConstruct
    private void init() {
        userAcc = usb.getUser();
        Job rerun = (Job) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("rerunJob");
        if (rerun != null) {
            //fetch with lazy config file
            rerun = jobFacade.findWithConfig(rerun.getId());
        }//next null check is necessary too
        if (rerun != null) {
            chosenUwsType = rerun.getUwsType();
            initializePrecreatedConfigs();
            configurationJson = rerun.getConfigurationJson();
            jobLabel = rerun.getLabel() + "(copy)";
            jobNotes = rerun.getNotes();
            jobEmail = rerun.getResultsEmail();
            return;
        }
        String uwsTypeStrId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("uwsType");
        if (uwsTypeStrId == null) {
            //undefined param
            return;//page will be rendered with error message
        }
        chosenUwsType = uwsTypeFacade.findByStringIdentifier(uwsTypeStrId);
        initializePrecreatedConfigs();
    }

    private void initializePrecreatedConfigs() {
        //assumming the chosenUwsType instance field is set
        if (chosenUwsType == null) {
            throw new IllegalStateException("Chosen UWS type field is null");
        }
        precreatedConfigs = fsm.listPrecreatedConfigFiles(chosenUwsType);
    }

    private void generateFolderTreeStructure() {
        folderTreeRootNode = new DefaultTreeNode(null, null);
        TreeNode rootFolderNode = new DefaultTreeNode(new FolderElement("/", "/", fsm.getRootFolderDescriptor()), folderTreeRootNode);
        TreeNode dummy = new DefaultTreeNode("DUMMY", rootFolderNode);
    }

    public void onFilesystemFolderSelect(NodeSelectEvent event) {
        DefaultTreeNode selected = (DefaultTreeNode) event.getTreeNode();
        targetFolder = ((FolderElement) selected.getData()).getFullPath();
        selected.setSelected(false);
    }

    public void onFilesystemNodeExpand(NodeExpandEvent event) {
        DefaultTreeNode parent = (DefaultTreeNode) event.getTreeNode();
        if (parent.getChildCount() == 1 && parent.getChildren().get(0).getData().toString().equals("DUMMY")) {
            parent.getChildren().remove(0);
            FolderElement ele = (FolderElement) parent.getData();
            TreeNode tmpNode;
            for (File i : ele.getTargetFile().listFiles()) {
                if (!i.isDirectory()) {
                    continue;
                }
                tmpNode = new DefaultTreeNode(new FolderElement(i.getName(), ele.getFullPath() + i.getName() + "/", i), parent);
                //add dummy to tmpNode
                TreeNode dummy = new DefaultTreeNode("DUMMY", tmpNode);
            }
            //sort folders by name
            TreeUtils.sortNode(parent, new Comparator<TreeNode>() {

                @Override
                public int compare(TreeNode o1, TreeNode o2) {
                    return ((FolderElement) o1.getData()).compareTo((FolderElement) o2.getData());
                }
            });
        }
    }

    public boolean isNonRestrictedUwsTypeFound() {
        return chosenUwsType != null && !chosenUwsType.getRestricted();
    }

    public boolean isRestrictedUwsTypeFound() {
        return chosenUwsType != null && chosenUwsType.getRestricted();
    }

    public UWSType getChosenUwsType() {
        return chosenUwsType;
    }

    public String checkParamValidity() {
        if (isRestrictedUwsTypeFound() && (userAcc.getGroupName().equals(UserGroupName.ADMIN) || userAcc.getGroupName().equals(UserGroupName.MANAGER))) {
            return null;//no navigation
        } //else
        if (isNonRestrictedUwsTypeFound()) {
            return null;//no navigation
        }
        //otherwise redirect to home page
        return "/index?faces-redirect=true";
    }

    public String getConfigurationJson() {
        return configurationJson;
    }

    public void setConfigurationJson(String configurationJson) {
        this.configurationJson = configurationJson;
    }

    public void handleConfigUpload(FileUploadEvent event) {
        StringWriter writer = new StringWriter();
        FacesMessage message = null;
        try {
            IOUtils.copy(event.getFile().getInputstream(), writer, "UTF-8");
            IOUtils.closeQuietly(event.getFile().getInputstream());
            configurationJson = writer.toString();
            message = new FacesMessage("Success", event.getFile().getFileName() + " was uploaded.");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Exception during loading uploaded file to memory", ex);
            message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Upload was NOT successful");
        }
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public String getJobLabel() {
        return jobLabel;
    }

    public void setJobLabel(String jobLabel) {
        this.jobLabel = jobLabel;
    }

    public String getJobNotes() {
        return jobNotes;
    }

    public void setJobNotes(String jobNotes) {
        this.jobNotes = jobNotes;
    }

    public boolean isJobEmail() {
        return jobEmail;
    }

    public void setJobEmail(boolean jobEmail) {
        this.jobEmail = jobEmail;
    }

    public boolean isCopyAfter() {
        return copyAfter;
    }

    public void setCopyAfter(boolean copyAfter) {
        //generate folder structure if already isnt
        if (folderTreeRootNode == null) {
            generateFolderTreeStructure();
        }
        this.copyAfter = copyAfter;
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    public void setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
    }

    public String saveNewJob(boolean runImmediately) {
        Job job = new Job();
        job.setLabel(jobLabel);
        if (jobNotes.length() > 0) {
            job.setNotes(jobNotes);
        }
        job.setResultsEmail(jobEmail);
        job.setUwsType(chosenUwsType);
        job.setConfigurationJson(configurationJson);
        if (copyAfter) {
            job.setTargetDir(targetFolder);
            //else targetDir remains null in job
        }
        try {
            jobFacade.enqueueNewJob(job, runImmediately);
        } catch (EJBException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Exception thrown during job enqueuing"));
            return null;//no navigation
        }
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);//to survive redirect
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "New " + chosenUwsType.getShortDescription() + " job was successfully enqueued"));
        return "index?faces-redirect=true";
    }

    public TreeNode getFolderTreeRootNode() {
        return folderTreeRootNode;
    }

    public void selectFolder(FolderElement element) {
        targetFolder = element.getFullPath();
    }

    public boolean isFilesystemManageAccess() {
        if (userAcc == null) {
            return false;
        }
        return userAcc.getGroupName().equals(UserGroupName.MANAGER) || userAcc.getGroupName().equals(UserGroupName.ADMIN);
    }

    public List<FilesystemFile> getPrecreatedConfigs() {
        return precreatedConfigs;
    }

    public FilesystemFile getSelectedPrecreatedConfig() {
        return selectedPrecreatedConfig;
    }

    public void setSelectedPrecreatedConfig(FilesystemFile selectedPrecreatedConfig) {
        this.selectedPrecreatedConfig = selectedPrecreatedConfig;
    }

    private String readPrecreatedConfigContents(FilesystemItem config){
        if (config.getSizeInBytes() > 500000) {//0.5 MB max
            return "Error: Contents too long to be viewed";
        }
        InputStream stream = null;
        try {
            stream = fsm.getDownloadStream(config);
            String content = IOUtils.toString(stream, "UTF-8");
            return content;
        } catch (Throwable ex) {
            LOG.log(Level.SEVERE, null, ex);
            return "Error: Exception during reading file " + ex.getMessage();
        } finally {
            if (stream != null){
                IOUtils.closeQuietly(stream);
            }
        }
    }
    
    public String getSelectedPrecreatedConfigContents() {
        if (selectedPrecreatedConfig == null) {
            return "No content";
        }
        return readPrecreatedConfigContents(selectedPrecreatedConfig);
    }
    
    public void loadPrecreatedConfig(FilesystemItem config){
        configurationJson = readPrecreatedConfigContents(config);
    }

    public static class FolderElement implements Serializable, Comparable<FolderElement> {

        private final String folderName;
        private final String fullPath;
        private final File targetFile;

        public FolderElement(String folderName, String fullPath, File targetFile) {
            this.folderName = folderName;
            this.fullPath = fullPath;
            this.targetFile = targetFile;
        }

        public String getFolderName() {
            return folderName;
        }

        public String getFullPath() {
            return fullPath;
        }

        public File getTargetFile() {
            return targetFile;
        }

        @Override
        public int compareTo(FolderElement o) {
            return this.folderName.compareTo(o.folderName);
        }

    }
}
