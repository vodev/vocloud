package cz.ivoa.vocloud.downloader;

/**
 *
 * @author radio.koza
 */
public class UrlWithName {
    private final String url;
    private final String name;

    public UrlWithName(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
    
    
}
