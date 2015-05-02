package cz.mrq.vocloud.managedbeans;

import cz.mrq.vocloud.tools.Toolbox;
import org.apache.commons.io.FileUtils;
import org.primefaces.component.panel.Panel;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * CreateKorelJob
 *
 * @author Lumir Mrkva (lumir.mrkva@topmonks.com)
 */
@ManagedBean(name = "createKorelJob")
@ViewScoped
public class CreateKorelJob extends CreateJob {

    private static final Logger logger = Logger.getLogger(CreateKorelJob.class.getName());

    private String parFileContents;
    private Boolean par = false;
    private Boolean dat = false;
    private Boolean tmp = false;
    private Panel editParPanel = new Panel();

    @Override
    public String getJobType() {
        return "Korel";
    }

    @Override
    public void postInit() {
//        editParPanel.setCollapsed(true);
//        // read par file contents
//        for (File f : uploadedFiles) {
//            checkFileName(f.getName());
//        }
//        if (parent != null && par) {
//            setParFileContents(new File(jf.getFileDir(parent), "korel.par"));
//        }
    }

    @Override
    protected File prepareParameters() {
        File parameters = new File(getJobFolder(), "parameters.zip");
        Toolbox.compressFiles(getJobFolder(), parameters);
        return parameters;
    }

    @Override
    public void handleFileUpload(FileUploadEvent event) {
        UploadedFile uploaded = event.getFile();
        String fileName = uploaded.getFileName();
        checkFileName(fileName);

        uploadDir.mkdirs();
        File outFile = new File(uploadDir, uploaded.getFileName());

        copyUploadedFile(outFile, uploaded);

        //extract uploaded zip file
        if (fileName.endsWith(".zip")) {
            try {
                ZipEntry entry;
                ZipFile zf = new ZipFile(outFile);
                Enumeration e = zf.entries();
                while (e.hasMoreElements()) {
                    entry = (ZipEntry) e.nextElement();

                    // flatten directories
                    String filename = entry.getName().substring(entry.getName().lastIndexOf('/') + 1);

                    if (checkFileName(filename)) {

                        File exFile = new File(uploadDir, filename);
                        copyFile(exFile, zf.getInputStream(entry));
                        uploadedFiles.add(exFile);
                    }
                }
            } catch (ZipException ex) {
                logger.log(Level.SEVERE, "zip exception");
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Not a valid zip file.", null));
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        } else {
            uploadedFiles.add(outFile);
        }

        // read par file contents
        if (par) {
            setParFileContents(new File(uploadDir, "korel.par"));
        }

        logger.info("File uploaded: " + event.getFile().getFileName());
    }

    @Override
    public void handleSave() {
//        //replace korel.par by data from textfield
//        try {
//            FileUtils.writeStringToFile(new File(getJobFolder(), "korel.par"), getParFileContents());
//        } catch (IOException ex) {
//            logger.log(Level.SEVERE, null, ex);
//        }
    }

    Boolean checkFileName(String fileName) {
        if ("korel.par".equals(fileName)) {
            par = true;
            return true;
        }
        if ("korel.dat".equals(fileName)) {
            dat = true;
            return true;
        }
        if ("korel.tmp".equals(fileName)) {
            tmp = true;
            return true;
        }
        return false;
    }

//    public String getParFileContents() {
//        if (parFileContents == null) {
//            if (parent != null) {
//                setParFileContents(new File(jf.getFileDir(parent), "korel.par"));
//            }
//        }
//        return parFileContents;
//    }

    public Boolean getDat() {
        return dat;
    }

    public void setDat(Boolean dat) {
        this.dat = dat;
    }

    public Boolean getPar() {
        return par;
    }

    public void setPar(Boolean par) {
        this.par = par;
    }

    public Boolean getTmp() {
        return tmp;
    }

    public void setTmp(Boolean tmp) {
        this.tmp = tmp;
    }

    public void setParFileContents(String contents) {
        editParPanel.setCollapsed(false);
        parFileContents = contents;
    }

    private void setParFileContents(File file) {
        try {
            parFileContents = FileUtils.readFileToString(file);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            return;
        }
        editParPanel.setCollapsed(false);
    }

    public Panel getEditParPanel() {
        return editParPanel;
    }

    public void setEditParPanel(Panel editParPanel) {
        this.editParPanel = editParPanel;
    }
}
