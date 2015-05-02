package cz.mrq.vocloud.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.enterprise.inject.Vetoed;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 *
 * @author radio.koza
 */
@Vetoed
@Entity
@NamedQueries({
    @NamedQuery(name = "Worker.findAllByIdOrdered", query = "SELECT w FROM Worker w ORDER BY w.id"),
    @NamedQuery(name = "Worker.findWorkersWithUwsType", query = "SELECT DISTINCT u.worker FROM UWS u WHERE :uwsType = u.uwsType"),
    @NamedQuery(name = "Worker.countWorkerJobsInPhase", query = "SELECT COUNT(j) FROM Job j WHERE j.uws.worker = :worker AND j.phase = :phase")
})
public class Worker implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(nullable = false, length = 255)
    private String resourceUrl;
    @Column(nullable = false, length = 100)
    private String shortDescription;
    @Column(nullable = true, length = 1000)
    private String description;
    @Column(nullable = false)
    private Integer maxJobs;

    //relation definition
    @OneToMany(mappedBy = "worker")
    private List<UWS> uwsList = new ArrayList<>();

    public Worker() {
        //nothing to do here
    }

    public Worker(String resourceUrl, String shortDescription, String description, Integer maxJobs) {
        this.resourceUrl = resourceUrl;
        this.shortDescription = shortDescription;
        this.description = description;
        this.maxJobs = maxJobs;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = cleanUrl(resourceUrl);
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMaxJobs() {
        return maxJobs;
    }

    public void setMaxJobs(Integer maxJobs) {
        this.maxJobs = maxJobs;
    }

    public List<UWS> getUwsList() {
        return uwsList;
    }

    public void setUwsList(List<UWS> uwsList) {
        this.uwsList = uwsList;
    }

    /**
     * Get out trailing whitespaces and slashes at the end of url
     *
     * @param urlAddress Url address to be cleaned
     * @return Cleaned url address
     */
    private static String cleanUrl(String urlAddress) {
        urlAddress = urlAddress.trim();
        while (urlAddress.charAt(urlAddress.length() - 1) == '/') {
            //there can be possibly more
            urlAddress = urlAddress.substring(0, urlAddress.length() - 1);
        }
        return urlAddress;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Worker other = (Worker) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "Worker{" + "id=" + id + ", resourceUrl=" + resourceUrl + ", shortDescription=" + shortDescription + ", description=" + description + ", maxJobs=" + maxJobs + ", uwsList=" + uwsList + '}';
    }

}
