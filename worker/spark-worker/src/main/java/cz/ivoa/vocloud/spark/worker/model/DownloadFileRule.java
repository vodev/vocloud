package cz.ivoa.vocloud.spark.worker.model;

import java.util.List;

/**
 * Created by radiokoza on 21.4.17.
 */
public class DownloadFileRule {
    private final List<String> urls;
    private final String folder;

    public DownloadFileRule(List<String> urls, String folder) {
        this.urls = urls;
        this.folder = folder;
    }

    public List<String> getUrls() {
        return urls;
    }

    public String getFolder() {
        return folder;
    }
}
