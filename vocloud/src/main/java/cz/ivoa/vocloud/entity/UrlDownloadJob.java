package cz.ivoa.vocloud.entity;

import javax.enterprise.inject.Vetoed;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author radio.koza
 */
@Vetoed
@Entity
@DiscriminatorValue("URL")
public class UrlDownloadJob extends DownloadJob {

    @Basic(optional = false)
    @Column(name = "download_url", length = 2048)
    private String downloadUrl;
    
    public UrlDownloadJob() {
        //nothing to do here
    }

    @Override
    public boolean isSSAP() {
        return false;
    }
    
    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
