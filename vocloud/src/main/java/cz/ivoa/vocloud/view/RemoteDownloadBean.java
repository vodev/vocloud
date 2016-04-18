package cz.ivoa.vocloud.view;

import cz.ivoa.vocloud.downloader.DownloadManager;
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

    private boolean showAuth = false;

    //authorization fields
    private String username;
    private String password;

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

    public boolean isShowAuth() {
        return showAuth;
    }

    public void setShowAuth(boolean showAuth) {
        this.showAuth = showAuth;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void download() {
        //validate username and pass if authorization mode is chosen
        if (showAuth) {
            if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()){
                //show error message
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  "Authorization error", "You must enter non empty username and password"));
                return;
            }
        }
        boolean result;
        if (showAuth){
            result = manager.enqueueNewURLDownload(resourceUrl, targetFolder, username.trim(), password);
        } else {
            result = manager.enqueueNewURLDownload(resourceUrl, targetFolder);
        }
        if (result){
            resourceUrl = "";
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("New download was successfully enqueued"));
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  "Download enqueue was unsuccessful", "URL address or used protocol is invalid"));
        }
    }

}
