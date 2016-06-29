package cz.ivoa.vocloud.view;

import cz.ivoa.vocloud.filesystem.FilesystemManipulator;
import cz.ivoa.vocloud.filesystem.exception.IllegalPathException;
import cz.ivoa.vocloud.filesystem.model.FilesystemItem;
import cz.ivoa.vocloud.tools.Toolbox;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class CloudDownloadBean implements Serializable {

    private static final Logger LOG = Logger.getLogger(CloudDownloadBean.class.getName());
    private static final int ARCHIVE_COUNT_LIMIT = 100;

    private String targetFolder;
    @EJB
    private FilesystemManipulator fsm;
    private long filesSize;
    private List<FilesystemItem> files;
    private List<Archive> archiveLinks;

    private boolean linksGenerated = false;
    private int archiveCount = 1;

    @PostConstruct
    protected void init() {
        targetFolder = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("targetFolder");
        if (targetFolder == null) {
            return;
        }
        try {
            List<FilesystemItem> items = fsm.listFilesystemItems(targetFolder);
            //check validity of directory
            if (items == null) {
                return;//fail
            }
            //count items
            files = new ArrayList<>();

            for (FilesystemItem i : items) {
                if (!i.isFolder()) {
                    files.add(i);
                    filesSize += i.getSizeInBytes();
                }
            }
        } catch (IllegalPathException ex) {
            LOG.severe("Illegal path for initialization of mass download");
        }
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    public String getFilesSize() {
        return Toolbox.humanReadableByteCount(filesSize, true);
    }

    public int getFilesCount() {
        if (files == null) {
            return 0;
        }
        return files.size();
    }

    public int getArchiveCount() {
        return archiveCount;
    }

    public void setArchiveCount(int archiveCount) {
        this.archiveCount = archiveCount;
    }

    public boolean isLinksGenerated() {
        return linksGenerated;
    }

    public void chooseDifferentCount() {
        archiveLinks = null;
        linksGenerated = false;
    }

    private int[] archiveFileCounts() {
        int[] counts = new int[archiveCount];
        int factor = files.size() / archiveCount;
        for (int i = 0; i < counts.length; i++) {
            counts[i] = factor;
        }
        for (int i = 0; i < files.size() % archiveCount; i++) {
            counts[i] += 1;
        }
        return counts;
    }

    public List<Archive> getArchiveLinks() {
        return archiveLinks;
    }

    public void generateLinks() {
        //do validation
        if (archiveCount < 1) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Archive count must be at least 1"));
            return;
        }
        if (archiveCount > ARCHIVE_COUNT_LIMIT) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Archive count must not be greater than " + ARCHIVE_COUNT_LIMIT));
            return;
        }
        if (archiveCount > files.size()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed", "Archive count must not be greater than count of files"));
            return;
        }
        archiveLinks = new ArrayList<>(archiveCount);
        Archive arch;
        int[] counts = archiveFileCounts();
        int pointer = 0;
        for (int i = 0; i < archiveCount; i++) {
            arch = new Archive(i, pointer, pointer + counts[i]);
            pointer += counts[i];
            archiveLinks.add(arch);
        }
        linksGenerated = true;
    }

    public void downloadArchive(int archiveIndex) {
        Archive archive = archiveLinks.get(archiveIndex);
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        ctx.responseReset();
        ctx.setResponseContentType("application/zip");
        String attachmentName = "attachment; filename=\"" + archive.getArchiveName() + "\"";
        ctx.setResponseHeader("Content-Disposition", attachmentName);
        List<FilesystemItem> selected = new ArrayList<>(archive.getLastIndexExcl() - archive.getFirstIndexIncl());
        for (int i = archive.getFirstIndexIncl(); i < archive.getLastIndexExcl(); i++) {
            selected.add(files.get(i));
        }
        try {
            OutputStream output = ctx.getResponseOutputStream();
            fsm.setupZippedDownloadStream(selected, output);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (IOException ex) {
            Logger.getLogger(FilesystemViewBean.class.getName()).log(Level.SEVERE, "Fatal exception during opening output zip stream", ex);
        }
    }

    public static class Archive {
        private static final String ARCHIVE_NAME_PREFIX = "archive";
        private static final String ARCHIVE_NAME_POSTFIX = ".zip";

        private final int archiveIndex;
        private final int firstIndexIncl;
        private final int lastIndexExcl;

        public Archive(int archiveIndex, int firstIndexIncl, int lastIndexExcl) {
            this.archiveIndex = archiveIndex;
            this.firstIndexIncl = firstIndexIncl;
            this.lastIndexExcl = lastIndexExcl;
        }

        public int getArchiveIndex() {
            return archiveIndex;
        }

        public int getFirstIndexIncl() {
            return firstIndexIncl;
        }

        public int getLastIndexExcl() {
            return lastIndexExcl;
        }

        public String getArchiveName() {
            return ARCHIVE_NAME_PREFIX + (archiveIndex + 1) + ARCHIVE_NAME_POSTFIX;
        }
    }
}
