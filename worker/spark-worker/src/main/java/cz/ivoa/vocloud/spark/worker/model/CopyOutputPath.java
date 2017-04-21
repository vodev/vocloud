package cz.ivoa.vocloud.spark.worker.model;

/**
 * Created by radiokoza on 21.4.17.
 */
public class CopyOutputPath {

    private final String path;
    private final boolean mergeParts;

    public CopyOutputPath(String path, boolean mergeParts) {
        this.path = path;
        this.mergeParts = mergeParts;
    }

    public String getPath() {
        return path;
    }

    public boolean isMergeParts() {
        return mergeParts;
    }
}
