package cz.rk.vocloud.filesystem.model;

import java.util.Date;

/**
 *
 * @author radio.koza
 */
public class Folder extends FilesystemItem{
    
    public static Folder createByFullPath(String folderPath){
        if (folderPath == null){
            throw new IllegalArgumentException("Folder path argument is null");
        }
        if (folderPath.trim().equals("")){
            throw new IllegalArgumentException("Folder path argument is empty");
        }
        folderPath = folderPath.replaceAll("\\\\", "/").replaceAll("//", "/");
        if (folderPath.charAt(0) == '/'){
            folderPath = folderPath.substring(1);
        }
        String[] splitArray = folderPath.split("/");
        StringBuilder prefixBuilder = new StringBuilder();
        if (splitArray.length > 1){
            prefixBuilder.append(splitArray[0]);
        }
        for (int i = 1; i < splitArray.length - 1; i++){
            prefixBuilder.append('/');
            prefixBuilder.append(splitArray[i]);
        }
        return new Folder(splitArray[splitArray.length - 1], prefixBuilder.toString());
    }
    
    public Folder(String folderName, String relativeDir){
        super(folderName, relativeDir);
    }

    @Override
    public Long getSizeInBytes() {
        return null;
    }

    @Override
    public Date getLastModified() {
        return null;
    }

    @Override
    public boolean isFolder() {
        return true;
    }
    
}
