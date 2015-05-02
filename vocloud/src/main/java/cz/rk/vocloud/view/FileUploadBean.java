package cz.rk.vocloud.view;

import cz.rk.vocloud.filesystem.FilesystemManipulator;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.event.FileUploadEvent;

/**
 *
 * @author radio.koza
 */
@Named
@ViewScoped
public class FileUploadBean implements Serializable {

    @EJB
    private FilesystemManipulator fsm;
    private String targetFolder;

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

    public void handleFileUpload(FileUploadEvent event) {
        String finalName;
        String fileName = new String(event.getFile().getFileName().getBytes(Charset.forName("ISO-8859-1")), Charset.forName("UTF-8"));//this encoding bug could be system specific! --need retest on other sys
        try {
            finalName = fsm.saveUploadedFile(targetFolder, fileName, event.getFile().getInputstream());
            if (finalName.equals(fileName)) {
                FacesMessage message = new FacesMessage("Succesful", fileName + " was uploaded.");
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else {
                FacesMessage message = new FacesMessage("Succesful", fileName + " was saved successfully as " + finalName);
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        } catch (IOException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Failed", fileName + " was not successfully saved."));
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
}
