package cz.ivoa.vocloud.view;

import cz.ivoa.vocloud.filesystem.model.FilesystemItem;
import cz.ivoa.vocloud.filesystem.model.Folder;
import cz.ivoa.vocloud.tools.Config;
import org.primefaces.context.RequestContext;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.InvalidPathException;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author radio.koza
 */
@Named
@ViewScoped
public class FilesystemManageBean extends FilesystemViewBean {

    private static final Logger LOG = Logger.getLogger(FilesystemManageBean.class.getName());

    private String folderName;
    private String itemToRename;
    private FilesystemItem filesystemItemToRename;
    private String plotViewSrc;

    @Inject
    @Config
    private String spectraPlotterUrl;

    @Override
    protected String getThisNamedBeanName() {
        return "filesystemManageBean";
    }

    public boolean createNewFolder(String folderName) {
        //test validity of the name
        if (folderName.trim().isEmpty() || folderName.contains(".") || folderName.contains("/") || folderName.contains("\\")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed", "Name of the new folder is invalid"));
            return false;
        }
        Folder folder = new Folder(folderName, prefix);
        try {
            boolean success = fsm.tryToCreateFolder(folder);
            if (success) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Folder " + folderName + " was successfully created"));
                init();
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Folder with name " + folderName + " already exists"));
            }
            return success;
        } catch (InvalidPathException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Name of the new folder is invalid"));
            return false;
        }
    }

    public String getPlotViewSrc() {
        return plotViewSrc;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public void createFolder() {
        boolean success = createNewFolder(folderName);
        if (success) {
            folderName = null;
        }
    }

    public void delete(FilesystemItem item) {
        boolean success = fsm.tryToDeleteFilesystemItem(item);
        if (success) {
            if (item.isFolder()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Folder " + item.getName() + " was successfully deleted"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "File " + item.getName() + " was successfully deleted"));
            }
            selected = null;
            init();
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Deletion failed"));
        }
    }

    public String getItemToRename() {
        return itemToRename;
    }

    public void setItemToRename(String itemToRename) {
        this.itemToRename = itemToRename;
    }

    public FilesystemItem getFilesystemItemToRename() {
        return filesystemItemToRename;
    }

    public void setFilesystemItemToRename(FilesystemItem filesystemItemToRename) {
        this.filesystemItemToRename = filesystemItemToRename;
    }

    public void renameFilesystemItem() {
        //check validity of the name
        if (itemToRename == null || !FilesystemItem.isValidName(itemToRename)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "New name is invalid"));
            return;
        }
        //check property load
        if (filesystemItemToRename == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Unknown error"));
            Logger.getLogger(this.getClass().getName()).severe("filesystemItemToRename is null");
            return;
        }
        //if names are same - do nothing
        if (filesystemItemToRename.getName().equals(itemToRename)) {
            return;
        }
        //invoke rename
        boolean success = fsm.renameFilesystemItem(filesystemItemToRename, itemToRename);
        if (success) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Rename was successful"));
            selected = null;
            init();
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Failed", "File or folder with this name already exists"));
        }
    }

    public void deleteSelected() {
        if (selected == null || selected.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "You must select files first"));
        } else {
            boolean successFlag = true;
            for (FilesystemItem item : selected) {
                if (!fsm.tryToDeleteFilesystemItem(item)) {
                    successFlag = false;
                }
            }
            if (successFlag) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Deletion was successful"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Some files or folders were not deleted successfully"));
            }
        }
        init();
    }

    public void deleteAll() {
        boolean successFlag = true;
        for (FilesystemItem item : items) {
            if (!fsm.tryToDeleteFilesystemItem(item)) {
                successFlag = false;
            }
        }
        if (successFlag) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Deletion was successful"));
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Some files or folders were not deleted successfully"));
        }
        init();
    }

    public void plotSelectedSpectra() {
        if (selected == null || selected.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "You must select files first"));
        } else {
            for (FilesystemItem item : selected) {
                if (item.isFolder()) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN,
                                    "Failed",
                                    "You can plot only files, not folders"));
                }
            }
            boolean first = true;
            StringBuilder builder = new StringBuilder();

            for (FilesystemItem item : selected) {
                if (!first) {
                    builder.append(',');
                }
                first = false;
                builder.append(item.getName());
            }
            try {
                this.plotViewSrc = spectraPlotterUrl + "view?prefix="
                        + URLEncoder.encode(prefix, "UTF-8") + "&spectra="
                        + URLEncoder.encode(builder.toString(), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                LOG.severe("Unsupported encoding: " + ex.toString());
            }
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('spectraPlotDialog').show();");
        }
    }
}
