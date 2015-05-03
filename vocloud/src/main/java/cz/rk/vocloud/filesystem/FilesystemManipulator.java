package cz.rk.vocloud.filesystem;

import cz.mrq.vocloud.tools.Config;
import cz.rk.vocloud.filesystem.model.FilesystemFile;
import cz.rk.vocloud.filesystem.model.FilesystemItem;
import cz.rk.vocloud.filesystem.model.Folder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author radio.koza
 */
@LocalBean
@Stateless
public class FilesystemManipulator {

    @Inject
    @Config
    private String filesystemDir;
    private static final Logger LOG = Logger.getLogger(FilesystemManipulator.class.getName());

    private File filesystemDirectory;

    @PostConstruct
    public void init() {
        //initialize filesystem directory from String and check right access permissions
        filesystemDirectory = new File(filesystemDir).getAbsoluteFile();
        //check that directory exists
        if (!filesystemDirectory.exists()) {
            LOG.info("Filesystem directory does not exist. Creating...");
            if (!filesystemDirectory.mkdirs()) {
                LOG.severe("Unable to create filesystem directory path. Do you have permissions?");
                filesystemDirectory = null;
                return;
            }
        }
        //check that it is really directory
        if (!filesystemDirectory.isDirectory()) {
            //if not, use parent directory of this file
            filesystemDirectory = filesystemDirectory.getParentFile().getAbsoluteFile();
        }
        if (!filesystemDirectory.canRead()) {
            LOG.severe("Unable to read filesystem directory");
        }
        if (!filesystemDirectory.canWrite()) {
            LOG.severe("Missing permissions to write into filesystem directory");
        }
    }

    public List<FilesystemItem> listFilesystemItems(String prefix) {
        if (filesystemDirectory == null) {
            throw new IllegalStateException("Filesystem directory is uninitialized");
        }
        File[] files = filesystemDirectory.toPath().resolve(prefix).toFile().listFiles();
        if (files == null) {
            return null;
        }
        List<Folder> folders = new ArrayList<>();
        List<FilesystemFile> fsFiles = new ArrayList<>();
        for (File i : files) {
            if (i.isDirectory()) {
                folders.add(new Folder(i.getName(), prefix));
            } else if (i.isFile()) {
                fsFiles.add(new FilesystemFile(i.getName(), prefix, i.length(), new Date(i.lastModified())));
            } else {
                //partially deleted file - cached but not present
                LOG.log(Level.WARNING, "Nor directory nor file! {0}", i.getPath());
            }
        }
        //merge collections
        List<FilesystemItem> result = new ArrayList<>(files.length);
        result.addAll(folders);
        result.addAll(fsFiles);
        return result;
    }

    public List<FilesystemItem> listFilesystemItems() {
        //consider prefix as ""
        return listFilesystemItems("");
    }

    public InputStream getDownloadStream(FilesystemItem item) throws FileNotFoundException {
        if (item.isFolder()) {
            throw new IllegalArgumentException("Unable to download folder " + item.getName());
        }
        File file = filesystemDirectory.toPath().resolve(item.getPrefix()).resolve(item.getName()).toFile();
        return new FileInputStream(file);
    }

    public InputStream getDownloadStream(String filePath) throws FileNotFoundException {
        filePath = clearPath(filePath);
        File file = filesystemDirectory.toPath().resolve(filePath).toFile();
        if (file.isFile()) {
            return new FileInputStream(file);
        } else {
            throw new FileNotFoundException("File denoted by " + filePath + " does not exist");
        }
    }

    public long getFileSize(FilesystemItem item) throws FileNotFoundException {
        File file = filesystemDirectory.toPath().resolve(item.getPrefix()).resolve(item.getName()).toFile();
        if (file.isFile()) {
            return file.length();
        }//else
        throw new FileNotFoundException("File denoted by " + item.toString() + " does not exist");
    }

    public long getFileSize(String filePath) throws FileNotFoundException {
        filePath = clearPath(filePath);
        File file = filesystemDirectory.toPath().resolve(filePath).toFile();
        if (file.isFile()) {
            return file.length();
        }
        throw new FileNotFoundException("File denoted by " + filePath + " does not exist");
    }

    public boolean tryToCreateFolder(Folder folder) throws InvalidPathException {
        File folderDescriptor = filesystemDirectory.toPath().resolve(folder.getPrefix()).resolve(folder.getName()).toFile();
        return folderDescriptor.mkdir();
    }

    public boolean tryToDeleteFilesystemItem(FilesystemItem item) {
        File fileDescriptor = filesystemDirectory.toPath().resolve(item.getPrefix()).resolve(item.getName()).toFile();
        if (!fileDescriptor.exists()) {
            return false;
        }
        deleteFileRecursively(fileDescriptor);
        return true;
    }

    private void deleteFileRecursively(File descriptor) {
        if (descriptor.isFile()) {
            descriptor.delete();
        } else {
            for (File i : descriptor.listFiles()) {
                deleteFileRecursively(i);
            }
            descriptor.delete();
        }
    }

    public boolean renameFilesystemItem(FilesystemItem item, String newName) {
        File source = filesystemDirectory.toPath().resolve(item.getPrefix()).resolve(item.getName()).toFile();
        File target = filesystemDirectory.toPath().resolve(item.getPrefix()).resolve(newName).toFile();
        if (target.exists()) {
            return false;
        }
        return source.renameTo(target);
    }

    public String saveUploadedFile(String folder, String fileName, InputStream fileStream) throws IOException {
//        System.out.println("folder: " + folder + "; fileName: " + fileName);
        File targetFile;
        int counter = 0;
        do {
            if (counter == 0) {
                targetFile = filesystemDirectory.toPath().resolve(folder).resolve(fileName).toFile();
            } else {
                targetFile = filesystemDirectory.toPath().resolve(folder).resolve(fileName + " (" + counter + ")").toFile();
            }
            counter++;
        } while (targetFile.exists());
        BufferedInputStream bis = new BufferedInputStream(fileStream);
        FileUtils.copyInputStreamToFile(bis, targetFile);
        counter--;
        return counter == 0 ? fileName : fileName + " (" + counter + ")";
    }

    /**
     *
     * @param pathName
     * @param fileStream
     * @param close
     * @return If saved returns true, otherwise if already present false
     * @throws IOException
     */
    public boolean saveDownloadedFileIfNotExists(String pathName, InputStream fileStream, boolean close) throws IOException {
        if (pathName.contains("..")) {
            throw new IllegalArgumentException("Character sequence .. is not supported");
        }
        //just to be sure
        pathName = pathName.replaceAll("//", "/");
        pathName = pathName.trim();
        if (pathName.charAt(0) == '/') {
            pathName = pathName.substring(1);
        }
        File targetFile = filesystemDirectory.toPath().resolve(pathName).toFile();
        if (targetFile.exists()) {
            return false;
        }
        //mkdirs
        if (targetFile.getParentFile() != null && !targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        BufferedInputStream bis = new BufferedInputStream(fileStream);
        byte[] buffer = new byte[1048576];
        int size;
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            while ((size = bis.read(buffer)) > 0) {
                fos.write(buffer, 0, size);
            }
        }
        if (close) {
            bis.close();
        }
        return true;
    }

    public boolean fileExists(String pathName) {
        pathName = clearPath(pathName);
        File targetFile = filesystemDirectory.toPath().resolve(pathName).toFile();
        return targetFile.isFile();
    }

    public List<String> getFolderNamesInFolder(String pathName) {
        pathName = clearPath(pathName);
        File targetDir = filesystemDirectory.toPath().resolve(pathName).toFile();
        if (!targetDir.isDirectory()) {
            throw new IllegalArgumentException("Not a folder: " + pathName);
        }
        List<String> names = new ArrayList<>();
        for (File i : targetDir.listFiles()) {
            if (i.isDirectory()) {
                names.add(i.getName());
            }
        }
        return names;
    }

    public List<String> getFileNamesInFolder(String pathName) {
        pathName = clearPath(pathName);
        File targetDir = filesystemDirectory.toPath().resolve(pathName).toFile();
        if (!targetDir.isDirectory()) {
            throw new IllegalArgumentException("Not a folder: " + pathName);
        }
        List<String> names = new ArrayList<>();
        for (File i : targetDir.listFiles()) {
            if (i.isFile()) {
                names.add(i.getName());
            }
        }
        return names;
    }

    public boolean directoryExists(String pathName) {
        pathName = clearPath(pathName);
        File targetDir = filesystemDirectory.toPath().resolve(pathName).toFile();
        return targetDir.isDirectory();
    }

    private static String clearPath(String pathName) {
        if (pathName.contains("..")) {
            throw new IllegalArgumentException("Character sequence .. is not supported");
        }
        //just to be sure
        pathName = pathName.replaceAll("\\\\", "/").replaceAll("//", "/");
        pathName = pathName.trim();
        if (pathName.charAt(0) == '/') {
            pathName = pathName.substring(1);
        }
        return pathName;
    }

    public File getRootFolderDescriptor() {
        return filesystemDirectory;
    }
}
