package cz.ivoa.vocloud.view;

import cz.ivoa.vocloud.tools.Toolbox;
import cz.ivoa.vocloud.filesystem.FilesystemManipulator;
import cz.ivoa.vocloud.filesystem.model.FilesystemItem;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;

/**
 *
 * @author radio.koza
 */
@Named
@ViewScoped
public class FilesystemViewBean implements Serializable {

    @EJB
    protected FilesystemManipulator fsm;

    private MenuModel breadcrumb;

    protected String prefix;
    protected List<FilesystemItem> items;
    protected List<FilesystemItem> selected;
    private boolean sampBtnEnabled = false;
    
    @PostConstruct
    protected void viewBeanInitialization() {
        //look for folderPrefix set from other windows
        prefix = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("folderPrefix");
        if (prefix == null) {
            prefix = "";
        }
        init();
    }

    protected void init() {
        items = fsm.listFilesystemItems(prefix);
        //check validity of directory
        if (items == null) {
            //reset to nominal state
            prefix = "";
            init();
            return;
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

    public void goToFolder(FilesystemItem item) {
        //check that item is folder
        if (!item.isFolder()) {
            throw new IllegalArgumentException("Presentation tier is in inconsistent state - passed item is not folder");
        }
        prefix += item.getName() + "/";
        //call initialization of view bean
        init();
    }

    public boolean isInRoot() {
        return prefix.equals("");
    }

    public void goBack() {
        if (isInRoot()) {
            //do nothing
            return;
        }
        prefix = prefix.replaceAll("[^/]+/\\z", "");
        //call initialization of view bean
        init();
    }

    public void goToFolderIndex(int folderIndex) {
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
        //call initialization of view bean
        init();
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
    
    public void enableSamp(boolean enable){
        this.sampBtnEnabled = enable;
    }

    public boolean isSampBtnEnabled() {
        return sampBtnEnabled;
    }
    
    
}