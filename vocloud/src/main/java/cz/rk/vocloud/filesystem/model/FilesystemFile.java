package cz.rk.vocloud.filesystem.model;

import java.util.Date;

/**
 *
 * @author radio.koza
 */
public class FilesystemFile extends FilesystemItem{
    
    protected final long sizeInBytes;
    protected final Date lastModified;
    
    public FilesystemFile(String fileName, String relativeDir, long byteSize, Date lastMod){
        super(fileName, relativeDir);
        this.sizeInBytes = byteSize;
        this.lastModified = lastMod;
    }

    @Override
    public Long getSizeInBytes() {
        return sizeInBytes;
    }

    @Override
    public Date getLastModified() {
        return lastModified;
    }

    @Override
    public boolean isFolder() {
        return false;
    }
    
    
    
}
