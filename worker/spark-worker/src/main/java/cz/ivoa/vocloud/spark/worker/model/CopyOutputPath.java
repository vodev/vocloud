package cz.ivoa.vocloud.spark.worker.model;

import java.io.Serializable;

/**
 * Created by radiokoza on 21.4.17.
 */
public class CopyOutputPath implements Serializable {

    private final String path;
    private final String outputName;
    private final boolean mergeParts;


    public CopyOutputPath(String path, String outputName, boolean mergeParts) {
        this.path = path;
        this.outputName = outputName;
        this.mergeParts = mergeParts;
    }

    public String getPath() {
        return path;
    }

    public String getOutputName() {
        return this.outputName;
    }

    public boolean isMergeParts() {
        return mergeParts;
    }
}
