package cz.ivoa.vocloud.view;

import cz.ivoa.vocloud.tools.Toolbox;
import cz.ivoa.vocloud.downloader.DownloadManager;
import cz.ivoa.vocloud.ssap.UnparseableVotableException;
import cz.ivoa.vocloud.ssap.VotableParser;
import cz.ivoa.vocloud.ssap.model.IndexedSSAPVotable;
import cz.ivoa.vocloud.ssap.model.Option;
import cz.ivoa.vocloud.ssap.model.Param;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.input.CountingInputStream;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.event.FileUploadEvent;

/**
 *
 * @author radio.koza
 */
@Named
@ViewScoped
public class SsapBean implements Serializable {

    private String targetFolder;
    private String votableURL;
    private String downloadedVotable;

    private String inputMethod;
    private boolean votableParsed = false;
    private boolean datalinkAvailable = false;

    private boolean fileUploaded = false;//upload method
    private boolean fileDownloadSet = false;//remote download method

    private boolean allowDatalink = true;

    private String downloadUrl;
    private List<ResourceInfo> processedInfo;
    private IndexedSSAPVotable parsedVotable;
    private HtmlPanelGrid datalinkPanelGrid = new HtmlPanelGrid();
    
    //download manager
    @EJB
    private DownloadManager downloadManager;

    @PostConstruct
    protected void init() {
        targetFolder = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("targetFolder");
    }

    private void resetVariables() {
        this.fileUploaded = false;
        this.fileDownloadSet = false;
        this.votableParsed = false;
        this.datalinkAvailable = false;
        this.allowDatalink = true;
        this.processedInfo = null;
        this.parsedVotable = null;
        this.downloadUrl = null;
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    public String getVotableURL() {
        return votableURL;
    }

    public String getDownloadedVotable() {
        return downloadedVotable;
    }

    public void setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
    }

    public void setVotableURL(String votableURL) {
        this.votableURL = votableURL;
    }

    public void setInputMethod(String method) {
        if (method.equals(this.inputMethod)) {
            return;//nothing to change
        }
        this.inputMethod = method;
        //properly reset variables
        resetVariables();
    }

    public String getInputMethod() {
        return inputMethod;
    }

    public void handleFileUpload(FileUploadEvent event) {
        fileUploaded = true;
        processedInfo = new ArrayList<>();
        String fileName = new String(event.getFile().getFileName().getBytes(Charset.forName("ISO-8859-1")), Charset.forName("UTF-8"));//this encoding bug could be system specific! --need retest on other sys
        processedInfo.add(new ResourceInfo("File name: ", fileName));
        processedInfo.add(new ResourceInfo("File size: ", Toolbox.humanReadableByteCount(event.getFile().getSize(), false)));
        try {
            parsedVotable = VotableParser.parseVotable(event.getFile().getInputstream());
        } catch (IOException ex) {
            Logger.getLogger(SsapBean.class.getName()).log(Level.SEVERE, null, ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error uploading file", "File was not uploaded successfully"));
            fileUploaded = false;
            return;
        } catch (UnparseableVotableException ex) {
            Logger.getLogger(SsapBean.class.getName()).log(Level.INFO, "Unparseable votable uploaded", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Error parsing VOTABLE", "Unable to parse VOTABLE properly"));
            fileUploaded = false;
            return;
        }
        String queryStatus = parsedVotable.getQueryStatus();//can be null if votable has not the format defined by specification
        processedInfo.add(new ResourceInfo("Votable query status: ", queryStatus == null ? "undefined" : queryStatus));
        //throw hint if query status is error
        if ("ERROR".equals(queryStatus)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Error query status", "Votable has ERROR status set"));
        }
        processedInfo.add(new ResourceInfo("Record count: ", Integer.toString(parsedVotable.getRows().size())));
        processedInfo.add(new ResourceInfo("Datalink: ", parsedVotable.isDatalinkAvailable() ? "available" : "not available"));
        if (parsedVotable.isDatalinkAvailable()) {
            processedInfo.add(new ResourceInfo("Datalink access url: ", parsedVotable.getDatalinkResourceUrl()));
            constructDatalinkPanelGrid();
            datalinkAvailable = true;
        } else {
            datalinkAvailable = false;
            allowDatalink = false;
        }
        votableParsed = true;
    }
    
    public void downloadVotable(){
        try {
            fileDownloadSet = true;
            HttpURLConnection conn = (HttpURLConnection) (new URL(downloadUrl).openConnection());
            //setup processedInfo
            processedInfo = new ArrayList<>();
            processedInfo.add(new ResourceInfo("Resource url: ", "<a href=\"" + downloadUrl + "\" target=\"_blank\">" + downloadUrl + "</a>"));
            CountingInputStream is = new CountingInputStream(conn.getInputStream());
            //try to parse votable
            parsedVotable = VotableParser.parseVotable(is);
            String queryStatus = parsedVotable.getQueryStatus();
            //throw hint if query status is error
            processedInfo.add(new ResourceInfo("Votable query status: ", queryStatus == null ? "undefined" : queryStatus));
            if ("ERROR".equals(queryStatus)){
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Error query status", "Votable has ERROR status set"));
            }
            processedInfo.add(new ResourceInfo("Downloaded size: ", Toolbox.humanReadableByteCount(is.getByteCount(), false)));
            processedInfo.add(new ResourceInfo("Record count: ", Integer.toString(parsedVotable.getRows().size())));
            processedInfo.add(new ResourceInfo("Datalink: ", parsedVotable.isDatalinkAvailable() ? "available" : "not available"));
            if (parsedVotable.isDatalinkAvailable()) {
                processedInfo.add(new ResourceInfo("Datalink access url: ", parsedVotable.getDatalinkResourceUrl()));
                constructDatalinkPanelGrid();
                datalinkAvailable = true;
            } else {
                datalinkAvailable = false;
                allowDatalink = false;
            }
            votableParsed = true;
        } catch (ClassCastException | MalformedURLException ex) {
            Logger.getLogger(SsapBean.class.getName()).log(Level.SEVERE, null, ex);
            //this should not happen thanks to validation
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid format of URL address", "URL address is malformed"));
            fileDownloadSet = false;
        } catch (IOException ex){
            Logger.getLogger(SsapBean.class.getName()).log(Level.WARNING, null, ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error in connection", "Connection to the resource failed"));
            fileDownloadSet = false;
        } catch (UnparseableVotableException ex) {
            Logger.getLogger(SsapBean.class.getName()).log(Level.INFO, "Unparseable resource", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Error parsing VOTABLE", "Unable to parse VOTABLE properly"));
            fileDownloadSet = false;
        }
    }

    public boolean hasSomeData() {
        if (parsedVotable == null) {
            return false;
        }
        return !parsedVotable.getRows().isEmpty();
    }

    public boolean isVotableParsed() {
        return votableParsed;
    }

    public void setVotableParsed(boolean votableParsed) {
        this.votableParsed = votableParsed;
    }

    public boolean isDatalinkAvailable() {
        return datalinkAvailable;
    }

    public boolean isFileUploaded() {
        return fileUploaded;
    }

    public void setFileUploaded(boolean fileUploaded) {
        this.fileUploaded = fileUploaded;
    }

    public List<ResourceInfo> getProcessedFileInfo() {
        return processedInfo;
    }

    public void replaceUploadedFile() {
        resetVariables();
    }
    
    public void replaceDownloadedFile() {
        resetVariables();
    }

    public boolean isAllowDatalink() {
        return allowDatalink;
    }

    public void setAllowDatalink(boolean allowDatalink) {
        this.allowDatalink = allowDatalink;
    }

    private void constructDatalinkPanelGrid() {
        List<UIComponent> children = this.datalinkPanelGrid.getChildren();
        children.clear();
        for (Param p : parsedVotable.getDatalinkInputParams()) {
            if (p.isIdParam()) {
                continue;
            }
            HtmlOutputText label = new HtmlOutputText();
            label.setValue(p.getName() + ": ");
            children.add(label);
            //check if there are some options available
            if (p.getOptions().isEmpty()) {
                //no options - just inputText
                InputText text = new InputText();
                text.setId("datalinkProperty" + p.getName());
                children.add(text);
            } else {
                SelectOneMenu menu = new SelectOneMenu();
                menu.setId("datalinkProperty" + p.getName());
                List<SelectItem> items = new ArrayList<>();
                SelectItem item = new SelectItem();//null value
                item.setValue("");
                item.setLabel("Nothing selected");
                items.add(item);
                for (Option o : p.getOptions()) {
                    item = new SelectItem();
                    item.setValue(o.getValue());
                    if (o.getValue().equals(o.getName())){
                        item.setLabel(o.getName());
                    } else if (o.getName().trim().isEmpty()){
                        item.setLabel(o.getValue());
                    } else {
                        item.setLabel(o.getName() + ": " + o.getValue());
                    }
                    items.add(item);
                }
                UISelectItems uiContainer = new UISelectItems();
                uiContainer.setValue(items);
                menu.getChildren().add(uiContainer);
                children.add(menu);
            }
        }
    }

    public void createDownloadTask() {
        boolean success = false;
        if (!allowDatalink){
            //download jobs through accref value
            success = downloadManager.enqueueVotableAccrefDownload(parsedVotable, targetFolder);
        } else {
            //datalink is allowed
            //first catch datalink parameters from the request
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            Map<String, String> paramMap = new HashMap<>();
            for (Param p : parsedVotable.getDatalinkInputParams()) {
                if (p.isIdParam()) {
                    continue;
                }
                String value = request.getParameter("mainForm:datalinkProperty" + p.getName() + (p.getOptions().isEmpty() ? "" : "_input"));
                //moreover select one menu has postfix _input at the end
                if (value != null && !value.trim().equals("")){
                    paramMap.put(p.getName(), value);
                }
            }
            success = downloadManager.enqueueVotableDatalinkDownload(parsedVotable, paramMap, targetFolder);
        }
        if (success){
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully enqueued", "Download task was successfully enqueued"));
            FacesContext.getCurrentInstance().getApplication().getNavigationHandler()
                    .handleNavigation(FacesContext.getCurrentInstance(), null, "download-queue?faces-redirect=true");
            resetVariables();
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error enqueuing new download task", "New download task was NOT successfully enqueued"));
        }
    }

    public HtmlPanelGrid getDatalinkPanelGrid() {
        return datalinkPanelGrid;
    }

    public void setDatalinkPanelGrid(HtmlPanelGrid datalinkPanelGrid) {
        this.datalinkPanelGrid = datalinkPanelGrid;
    }

    public boolean isFileDownloadSet() {
        return fileDownloadSet;
    }

    public void setFileDownloadSet(boolean fileDownloaded) {
        this.fileDownloadSet = fileDownloaded;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadURL) {
        this.downloadUrl = downloadURL;
    }

    public static class ResourceInfo implements Serializable{

        private final String label;
        private final String value;

        public ResourceInfo(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }

    }
    
}
