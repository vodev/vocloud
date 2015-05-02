package cz.rk.vocloud.filesystem.model;

import java.io.File;
import java.io.Serializable;
import java.nio.file.InvalidPathException;
import java.util.Date;
/**
 *
 * @author radio.koza
 */
public abstract class FilesystemItem implements Serializable{
    
    private final String name;
    private final String prefix;
    
    protected FilesystemItem(String name, String prefix){
        this.name = name.trim();
        this.prefix = prefix.trim();
        //check prefix and name correctness
        if (!isValidName(name)){
            throw new IllegalArgumentException("Name of filesystem item is invalid: " + name);
        }
        if (prefix.contains(".")){
            throw new IllegalArgumentException("Invalid use of . or .. in filesystem item prefix: " + prefix);
        }
    }
    
    /**
     * Can return null in case the item is folder.
     * @return 
     */
    public abstract Long getSizeInBytes();
    
    public abstract Date getLastModified();
    
    public abstract boolean isFolder();

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }
    
    private final static String forbiddenChars = "/\\?%*:|\"<>";
    
    public static boolean isValidName(String name){
        if (name.contains("/") || name.contains("\\")){
            return false;
        }
        try {
            new File(name).toPath();
        } catch (InvalidPathException ex){
            return false;
        }
        return true;
    }
    
    public String getCompletePath(){
        return prefix + "/" + name;
    }
    
}
