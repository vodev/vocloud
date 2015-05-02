package cz.mrq.vocloud.helper;

import org.apache.commons.io.FileUtils;
import org.primefaces.component.panel.Panel;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EditPanel
 *
 * @author Lumir Mrkva (lumir.mrkva@topmonks.com)
 */
public class EditPanel {

    private Panel panel = new Panel();
    private String fileContents;

    public EditPanel() {
        panel.setCollapsed(true);
    }

    public void setFileContents(String contents) {
        panel.setCollapsed(false);
        fileContents = contents;
    }

    public void setFileContents(File file) {
        try {
            fileContents = FileUtils.readFileToString(file);
        } catch (IOException ex) {
            Logger.getLogger(EditPanel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        panel.setCollapsed(false);
    }

    public String getFileContents() {
        return fileContents;
    }

    public Panel getPanel() {
        return panel;
    }

    public void setPanel(Panel panel) {
        this.panel = panel;
    }
}
