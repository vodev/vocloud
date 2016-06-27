package cz.ivoa.vocloud.view;

import cz.ivoa.vocloud.filesystem.FilesystemManipulator;
import cz.ivoa.vocloud.filesystem.model.FilesystemFile;
import cz.ivoa.vocloud.filesystem.model.FilesystemItem;
import cz.ivoa.vocloud.tools.Toolbox;
import org.apache.commons.io.IOUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author radio.koza
 */
@Named
@ViewScoped
public class FilesystemViewBean implements Serializable {

    private static final Logger LOG = Logger.getLogger(FilesystemViewBean.class.getName());

    @EJB
    protected FilesystemManipulator fsm;

    private MenuModel breadcrumb;

    protected String prefix;
    protected List<FilesystemItem> items;
    protected List<FilesystemItem> selected;
    protected int filesCount, foldersCount;
    private boolean sampBtnEnabled = false;

    protected FilesystemFile selectedViewedFile;

    public String onLoad() {
        if (prefix == null) {
            prefix = "";
        }
        //do prefix validation
        if (!init()) {
            return getLocalRedirect();
        }
        return null;//no redirect
    }

    protected boolean init() {
        try {
            items = fsm.listFilesystemItems(prefix);
        } catch (IllegalArgumentException ex) {
            return false;
        }
        //check validity of directory
        if (items == null) {
            //reset to nominal state
            prefix = "";
            return false;
        }
        //count items
        filesCount = foldersCount = 0;
        for (FilesystemItem i : items) {
            if (i.isFolder()) {
                foldersCount++;
            } else {
                filesCount++;
            }
        }
        //menu initialization
        breadcrumb = new DefaultMenuModel();
        DefaultMenuItem menuItem = new DefaultMenuItem("Root", "ui-icon-home");
        menuItem.setCommand("#{" + getThisNamedBeanName() + ".goToFolderIndex(0)}");
        menuItem.setAjax(false);
        breadcrumb.addElement(menuItem);
        String[] folders = prefix.split("/");
        int counter = 0;
        for (String i : folders) {
            menuItem = new DefaultMenuItem(i);
            menuItem.setAjax(false);
            menuItem.setCommand("#{" + getThisNamedBeanName() + ".goToFolderIndex(" + (++counter) + ")}");
            breadcrumb.addElement(menuItem);
        }
        return true;
    }

    protected String getThisNamedBeanName() {
        return "filesystemViewBean";
    }

    public List<FilesystemItem> getFilesystemItemList() {
        return items;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getFilesCount() {
        return filesCount;
    }

    public int getFoldersCount() {
        return foldersCount;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public MenuModel getBreadcrumbModel() {
        return breadcrumb;
    }

    public String humanReadableSize(FilesystemItem item) {
        if (item.getSizeInBytes() == null) {
            //is folder - do nothing
            return null;
        }
        return Toolbox.humanReadableByteCount(item.getSizeInBytes(), true);
    }

    public int sortByName(Object first, Object second) {
        FilesystemItem a = (FilesystemItem) first;
        FilesystemItem b = (FilesystemItem) second;
        //folders first
        if (a.isFolder() && !b.isFolder()) {
            return -1;
        }
        if (!a.isFolder() && b.isFolder()) {
            return 1;
        }
        return a.getName().compareTo(b.getName());
    }

    public String goToFolder(FilesystemItem item) {
        //check that item is folder
        if (!item.isFolder()) {
            throw new IllegalArgumentException("Presentation tier is in inconsistent state - passed item is not folder");
        }
        prefix += item.getName() + "/";
        return redirectToPath(prefix);
    }

    public boolean isInRoot() {
        return prefix.equals("");
    }

    public String goBack() {
        if (isInRoot()) {
            //do nothing
            return getLocalRedirect();
        }
        prefix = prefix.replaceAll("[^/]+/\\z", "");
        return redirectToPath(prefix);
    }

    public String goToFolderIndex(int folderIndex) {
        if (folderIndex < 0) {
            throw new IllegalArgumentException("Folder index must not be negative value");
        }
        //0 is considered as root
        if (folderIndex == 0) {
            prefix = "";
        } else {
            String[] folders = prefix.split("/");
            //test size
            if (folderIndex > folders.length) {
                throw new IllegalArgumentException("Folder index has greater value than folder depth");
            }
            prefix = "";
            for (int i = 0; i < folderIndex; i++) {
                prefix += folders[i] + "/";
            }
        }
        return redirectToPath(prefix);
    }

    public StreamedContent downloadFile(FilesystemItem item) {
        InputStream stream;
        try {
            stream = fsm.getDownloadStream(item);
            return new DefaultStreamedContent(stream, "application/octet-stream", item.getName());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FilesystemViewBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public List<FilesystemItem> getSelectedItems() {
        return selected;
    }

    public void setSelectedItems(List<FilesystemItem> selected) {
        this.selected = selected;
    }

    private static String escapePathToUrl(String path) {
        String[] array = path.split("/");
        StringBuilder escaped = new StringBuilder();
        boolean isFirst = true;
        try {
            for (String s : array) {
                if (!isFirst) {
                    escaped.append('/');
                }
                isFirst = false;
                escaped.append(URLEncoder.encode(s, "UTF-8"));
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(FilesystemViewBean.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return escaped.toString();
    }

    public void sendSelectedThroughSAMP() {
        if (selected == null || selected.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "You must select files first"));
            return;
        }
        StringBuilder arrayText = new StringBuilder();
        boolean isFirst = true;
        for (FilesystemItem item : selected) {
            if (item.isFolder()) {
                continue;
            }
            if (!isFirst) {
                arrayText.append(',');
            }
            isFirst = false;
            try {
                arrayText.append('"').append(URLEncoder.encode(item.getName(), "UTF-8")).append('"');
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(FilesystemViewBean.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        }
        String arrayTextString = arrayText.toString();
        if (arrayTextString.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "You must select files first"));
            return;
        }
        RequestContext context = RequestContext.getCurrentInstance();
        if (prefix.isEmpty()) {
            context.execute("baseUrl = serviceUrl;");
        } else {
            context.execute("baseUrl = serviceUrl + '" + escapePathToUrl(prefix) + "/';");//TODO escape prefix
        }
        context.execute("fits = new Array(" + arrayText.toString() + ");");
        context.execute("connector.runWithConnection(send);");
    }

    public void enableSamp(boolean enable) {
        this.sampBtnEnabled = enable;
    }

    public boolean isSampBtnEnabled() {
        return sampBtnEnabled;
    }

    public void setSelectedViewedFile(FilesystemItem item) {
        if (item == null || item.isFolder()) {
            return;
        }
        this.selectedViewedFile = (FilesystemFile) item;
    }

    public FilesystemItem getSelectedViewedFile() {
        return selectedViewedFile;
    }

    public String getSelectedViewedFileContents() {
        if (selectedViewedFile == null) {
            return "Error: File does not exist";
        }
        if (selectedViewedFile.getSizeInBytes() > 500000) {//0.5 MB max
            return "Error: Contents too long to be viewed";
        }
        InputStream stream = null;
        try {
            stream = fsm.getDownloadStream(selectedViewedFile);
            String content = IOUtils.toString(stream, "UTF-8");
            content = content.replaceAll("[\\p{Cntrl}&&[^\\r]&&[^\\n]&&[^\\t]]", "?");
            return content;
        } catch (Throwable ex) {
            LOG.log(Level.SEVERE, null, ex);
            return "Error: Exception during reading file " + ex.getMessage();
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    public void downloadSelectedFiles() {
        if (selected == null || selected.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "You must select files first - Note that downloading of folders is forbidden"));
            return;//nothing selected
        }
        List<FilesystemItem> filteredItems = new ArrayList<>();
        for (FilesystemItem item : selected) {
            if (!item.isFolder()) {
                //filter folders out
                filteredItems.add(item);
            }
        }
        //check again for empty list
        if (filteredItems.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "You must select files first - Note that downloading of folders is forbidden"));
            return;//nothing selected
        }
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        ctx.responseReset();
        ctx.setResponseContentType("application/zip");
        String attachmentName = "attachment; filename=\"archive.zip\"";
        ctx.setResponseHeader("Content-Disposition", attachmentName);
        try {
            OutputStream output = ctx.getResponseOutputStream();
            fsm.setupZippedDownloadStream(selected, output);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (IOException ex) {
            Logger.getLogger(FilesystemViewBean.class.getName()).log(Level.SEVERE, "Fatal exception during opening output zip stream", ex);
        }
    }

    protected String getViewId() {
        return FacesContext.getCurrentInstance().getViewRoot().getViewId();
    }

    protected String getLocalRedirect() {
        return getViewId() + "?faces-redirect=true";
    }

    protected String redirectToPath(String path) {
        return getLocalRedirect() + "&path=" + path;
    }

    public String getPath() {
        return prefix;
    }

    public void setPath(String path) {
        prefix = path;
    }


}
