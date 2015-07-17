package cz.ivoa.vocloud.entity;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.inject.Vetoed;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 *
 * @author radio.koza
 */
@Vetoed
@Entity
@DiscriminatorValue("SSAP")
public class SSAPDownloadJob extends DownloadJob {

    @Column(name = "ssap_url", length = 2048)
    private String ssapUrl;//optional

    public SSAPDownloadJob() {
        //nothing to do here
    }

    @OneToMany(mappedBy = "parent")
    private List<SSAPDownloadJobItem> items = new ArrayList<>();

    public List<SSAPDownloadJobItem> getItems() {
        return items;
    }

    public void setItems(List<SSAPDownloadJobItem> items) {
        this.items = items;
    }

    @Override
    public boolean isSSAP() {
        return true;
    }

    public String getSsapUrl() {
        return ssapUrl;
    }

    public void setSsapUrl(String ssapUrl) {
        this.ssapUrl = ssapUrl;
    }
    
    //for back compatibility
    public String getDownloadUrl(){
        return getSsapUrl();
    }

}
