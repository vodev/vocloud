package cz.rk.vocloud.view;

import cz.rk.vocloud.downloader.DownloadManager;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 *
 * @author radio.koza
 */
@Named
@ViewScoped
public class RemoteDownloadBean implements Serializable {

    private String targetFolder;
    private String resourceUrl;

    @EJB
    private DownloadManager manager;
    
    @PostConstruct
    protected void init() {
        targetFolder = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("targetFolder");
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    public void setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public void download() {
        boolean result = manager.enqueueNewURLDownload(resourceUrl, targetFolder);
        if (result){
            resourceUrl = "";
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("New download was successfully enqueued"));
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  "Download enqueue was unsuccessful", "URL address or used protocol is invalid"));
        }
    }

}
