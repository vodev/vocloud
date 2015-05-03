package cz.rk.vocloud.view;

import cz.mrq.vocloud.ejb.JobFacade;
import cz.mrq.vocloud.ejb.UserSessionBean;
import cz.mrq.vocloud.entity.Job;
import cz.mrq.vocloud.entity.UserAccount;
import cz.mrq.vocloud.entity.UserGroupName;
import cz.mrq.vocloud.tools.Toolbox;
import cz.rk.vocloud.filesystem.FilesystemManipulator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.commons.io.FileUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.primefaces.util.TreeUtils;

/**
 *
 * @author radio.koza
 */
@Named
@ViewScoped
public class JobDetailsBean implements Serializable {

    private static final Logger LOG = Logger.getLogger(JobDetailsBean.class.getName());

    @EJB
    private UserSessionBean usb;
    @EJB
    private JobFacade jobFacade;
    @EJB
    private FilesystemManipulator fsm;
    private Job selectedJob;

    private List<PathExtendedFile> pages;//html and htm in / and /result and /results
    private List<PathExtendedFile> images;//png, jpg, jpeg, gif

    private TreeNode filesRootElement;
    private File selectedFile;
    private String copyFolder = "/";
    private TreeNode filesystemTreeRootNode;

    @PostConstruct
    private void init() {
        //find out user account
        UserAccount userAcc = usb.getUser();
        String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("jobId");
        long jobId;
        try {
            if (idParam == null) {
                throw new NumberFormatException("Null as jobId");
            }
            jobId = Long.parseLong(idParam);//can throw number format exception
            //try to find job
            selectedJob = jobFacade.findWithConfig(jobId);
            if (selectedJob == null) {
                //not found job with specified id
                throw new NumberFormatException("Job not found");
            }
            //check that it belongs to right user or at least admin
            if (!userAcc.getGroupName().equals(UserGroupName.ADMIN) && !selectedJob.getOwner().equals(userAcc)) {
                throw new NumberFormatException("Job is not owned by this user");
            }
        } catch (NumberFormatException ex) {
            LOG.log(Level.INFO, "Navigation back. Reason: {0}", ex.getMessage());
            //invoke navigation
            FacesContext.getCurrentInstance().getApplication()
                    .getNavigationHandler().
                    handleNavigation(FacesContext.getCurrentInstance(), null, "index?faces-redirect=true");
        }
        //futher initialization
        initializePagesAndImages();
        initializeFileTree();
    }

    private void initializePagesAndImages() {
        List<PathExtendedFile> listFiles = new ArrayList<>();
        File[] fileArray = jobFacade.getFileDir(selectedJob).listFiles();
        for (File i: fileArray){
            listFiles.add(new PathExtendedFile(i.getName(), i));
        }
        //result dir
        File resultDir = new File(jobFacade.getFileDir(selectedJob), "result");
        if (resultDir.isDirectory()) {
            fileArray = resultDir.listFiles();
            for (File i: fileArray){
                listFiles.add(new PathExtendedFile("result/" + i.getName(), i));
            }
        }
        //results dir
        File resultsDir = new File(jobFacade.getFileDir(selectedJob), "results");
        if (resultsDir.isDirectory()) {
            fileArray = resultsDir.listFiles();
            for (File i: fileArray){
                listFiles.add(new PathExtendedFile("results/" + i.getName(), i));
            }
        }
        pages = new ArrayList<>();
        images = new ArrayList<>();
        for (PathExtendedFile file : listFiles) {
            if ((file.getFile().getName().endsWith("html") || file.getFile().getName().endsWith("htm")) & file.getFile().length() != 0) {
                pages.add(file);
            } else if ((file.getFile().getName().endsWith("png")
                    || file.getFile().getName().endsWith("jpg")
                    || file.getFile().getName().endsWith("jpeg")
                    || file.getFile().getName().endsWith("gif")) & file.getFile().length() != 0){
                images.add(file);
            }
        }
        Collections.sort(pages);
        Collections.sort(images);
    }

    private void initializeFileTree() {
        File rootFolder = jobFacade.getFileDir(selectedJob);
        filesRootElement = new DefaultTreeNode(new FileNode(true, "Files", "", rootFolder), null);
        recursiveTreeInitialization(rootFolder, filesRootElement);
    }

    private void recursiveTreeInitialization(File parentFolder, TreeNode parentNode) {
        for (File i : parentFolder.listFiles()) {
            TreeNode node = new DefaultTreeNode(new FileNode(i.isDirectory(), i.getName(), getFileSize(i), i), parentNode);
            if (i.isDirectory()) {
                //recursive call
                recursiveTreeInitialization(i, node);
            }
        }
        TreeUtils.sortNode(parentNode, new Comparator<TreeNode>() {

            @Override
            public int compare(TreeNode o1, TreeNode o2) {
                return ((FileNode) o1.getData()).compareTo((FileNode) o2.getData());
            }
        });
    }

    private String getFileSize(File file) {
        if (!file.isFile()) {
            return "";
        }
        return Toolbox.humanReadableByteCount(file.length(), true);
    }

    public void refresh() {
        selectedJob = jobFacade.find(selectedJob.getId());
        if (selectedJob == null) {
            FacesContext.getCurrentInstance().getApplication()
                    .getNavigationHandler().
                    handleNavigation(FacesContext.getCurrentInstance(), null, "index?faces-redirect=true");
        }
    }

    public boolean stopPolling() {
        return selectedJob.hasEndState();
    }

    public Job getSelectedJob() {
        return selectedJob;
    }

    public String runAgain() {
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("rerunJob", selectedJob);
        return "create";
    }

    public String delete() {
        //destroy remote job - idc about results - and delete job dir
        jobFacade.deleteJob(selectedJob);
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);//to survive redirect
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Job with id " + selectedJob.getId() + " was deleted"));
        return "index?faces-redirect=true";
    }

    public List<PathExtendedFile> getPages() {
        return pages;
    }

    public List<PathExtendedFile> getImages() {
        return images;
    }

    public String getCopyFolder() {
        return copyFolder;
    }

    public void setCopyFolder(String copyFolder) {
        this.copyFolder = copyFolder;
    }

    private void generateFolderTreeStructure() {
        filesystemTreeRootNode = new DefaultTreeNode(null, null);
        TreeNode rootFolderNode = new DefaultTreeNode(new FolderElement("/", "/"), filesystemTreeRootNode);
        File rootFolder = fsm.getRootFolderDescriptor();
        recursivelyGenerateTree(rootFolder, "/", rootFolderNode);
    }

    private void recursivelyGenerateTree(File folder, String path, TreeNode parent) {
        TreeNode tmpNode;
        for (File i : folder.listFiles()) {
            if (!i.isDirectory()) {
                continue;//ignoring files
            }
            tmpNode = new DefaultTreeNode(new FolderElement(i.getName(), path + i.getName() + "/"), parent);
            recursivelyGenerateTree(i, path + i.getName() + "/", tmpNode);
        }
        //sort folders by name
        TreeUtils.sortNode(parent, new Comparator<TreeNode>() {

            @Override
            public int compare(TreeNode o1, TreeNode o2) {
                return ((FolderElement) o1.getData()).compareTo((FolderElement) o2.getData());
            }
        });
    }

    public StreamedContent downloadFile(File file) {
        //define content
        String mimeType;
        if (file.getName().endsWith("png")) {
            mimeType = "image/png";
        } else if (file.getName().endsWith("jpg") || file.getName().endsWith("jpeg")) {
            mimeType = "image/jpeg";
        } else if (file.getName().endsWith("gif")) {
            mimeType = "image/gif";
        } else {
            mimeType = "application/octet-stream";
        }
        try {
            return new DefaultStreamedContent(new FileInputStream(file), mimeType, file.getName());
        } catch (FileNotFoundException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public TreeNode getTreeRoot() {
        return filesRootElement;
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setSelectedFile(File selectedFile) {
        this.selectedFile = selectedFile;
    }

    public String getSelectedFileContents() {
        if (selectedFile == null) {
            return "(no file selected)";
        }
        if (selectedFile.getName().endsWith("png")
                || selectedFile.getName().endsWith("jpg")
                || selectedFile.getName().endsWith("jpeg")
                || selectedFile.getName().endsWith("gif")) {
            return "(image)";
        }
        if (selectedFile.getName().endsWith("zip")) {
            return Toolbox.ziplist(selectedFile);
        }
        //check size
        if (selectedFile.length() > 500000) {//0.5 MB max
            return "(file is too big to show in the window)";
        }
        String content = "";
        try {
            content = FileUtils.readFileToString(selectedFile, "UTF-8");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return content;
    }

    public TreeNode getFilesystemTreeRootNode() {
        return filesystemTreeRootNode;
    }

    public boolean isFilesystemManageAccess() {
        UserAccount acc = usb.getUser();
        if (acc == null) {
            return false;
        }
        if (acc.getGroupName().equals(UserGroupName.MANAGER) || acc.getGroupName().equals(UserGroupName.ADMIN)) {
            //generate folder structure if it is not yet
            if (filesystemTreeRootNode == null) {
                generateFolderTreeStructure();
            }
            return true;
        }
        return false;
    }

    public void selectFolder(FolderElement element) {
        this.copyFolder = element.getFullPath();
    }

    public void enqueueFilesystemCopy() {
        selectedJob.setTargetDir(copyFolder);
        jobFacade.copyResultsToFilesystem(selectedJob);
        FacesContext.getCurrentInstance().addMessage("fsCopyMessages", new FacesMessage("Success", "Copy task was successfully enqueued"));
    }

    public static class PathExtendedFile implements Serializable, Comparable<PathExtendedFile> {

        private final String path;
        private final File file;

        public PathExtendedFile(String path, File file) {
            this.path = path;
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        public String getPath() {
            return path;
        }

        @Override
        public int compareTo(PathExtendedFile o) {
            return file.compareTo(o.file);
        }

    }

    public static class FileNode implements Serializable, Comparable<FileNode> {

        private final boolean isFolder;
        private final String name;
        private final String size;
        private final File targetFile;

        public FileNode(boolean isFolder, String name, String size, File target) {
            this.isFolder = isFolder;
            this.name = name;
            this.size = size;
            this.targetFile = target;
        }

        public boolean isFolder() {
            return isFolder;
        }

        public String getName() {
            return name;
        }

        public String getSize() {
            return size;
        }

        public File getTargetFile() {
            return targetFile;
        }

        @Override
        public int compareTo(FileNode o) {
            if (this.isFolder && !o.isFolder) {
                return -1;
            }
            if (!this.isFolder && o.isFolder) {
                return 1;
            }
            return this.getName().compareTo(o.getName());
        }

    }

    public static class FolderElement implements Serializable, Comparable<FolderElement> {

        private final String folderName;
        private final String fullPath;

        public FolderElement(String folderName, String fullPath) {
            this.folderName = folderName;
            this.fullPath = fullPath;
        }

        public String getFolderName() {
            return folderName;
        }

        public String getFullPath() {
            return fullPath;
        }

        @Override
        public int compareTo(FolderElement o) {
            return this.folderName.compareTo(o.folderName);
        }

    }
}
