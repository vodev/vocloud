package cz.mrq.vocloud.managedbeans;

import cz.mrq.vocloud.helper.EditPanel;
import org.apache.commons.io.FileUtils;
import org.primefaces.component.panel.Panel;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.zeroturnaround.zip.ZipUtil;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CreateSomJob
 *
 * @author Lumir Mrkva (lumir.mrkva@topmonks.com)
 */
@ManagedBean(name = "createSomJob")
@ViewScoped
public class CreateSomJob extends CreateJob {

    File parameters;

    Boolean zip = false;
    Boolean config = false;
    EditPanel editPanel = new EditPanel();

    static String CONFIG_FILE = "config.json";

    @Override
    public String getJobType() {
        return "SOM";
    }

    @Override
    public void postInit() {
        List<File> files = new ArrayList<>();
        editPanel.getPanel().setCollapsed(true);
        for (File file : uploadedFiles) {
            if (file.getName().endsWith(".zip") && !file.getName().equals("results.zip")) {
                if (zip = ZipUtil.containsEntry(file, CONFIG_FILE)) {
                    parameters = file;
                    editPanel.setFileContents(new String(ZipUtil.unpackEntry(file, CONFIG_FILE)));
                    files.add(file);
                }
            }
        }
        uploadedFiles = files;
    }

    @Override
    protected File prepareParameters() {
        File params = new File(getJobFolder(), parameters.getName());
        String config = editPanel.getFileContents();
        try {
            FileUtils.writeStringToFile(new File(getJobFolder(), CONFIG_FILE), config);
        } catch (IOException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "failed to save config.json to job directory", null));
        }
        ZipUtil.replaceEntry(params, CONFIG_FILE, config.getBytes());
        return params;
    }

    @Override
    public void handleFileUpload(FileUploadEvent event) {
        UploadedFile uploaded = event.getFile();
        if (uploaded.getFileName().endsWith(".zip")) {
            File file = new File(uploadDir, uploaded.getFileName());
            copyUploadedFile(file, uploaded);
            if (ZipUtil.containsEntry(file, CONFIG_FILE)) {
                zip = true;
                parameters = file;
                uploadedFiles.add(parameters);
                editPanel.setFileContents(new String(ZipUtil.unpackEntry(file, CONFIG_FILE)));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "config.json not found in uploaded zip file", null));
            }
        }
    }

    @Override
    public void handleSave() {

    }

    public Boolean getZip() {
        return zip;
    }

    public EditPanel getEditPanel() {
        return editPanel;
    }
}
