package cz.ivoa.vocloud.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.enterprise.inject.Vetoed;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author radio.koza
 */
@Vetoed
@Entity
@Table(name = "ssap_download_item")
@NamedQueries({
    @NamedQuery(name = "SSAPDownloadJobItem.countParentItems", query = "SELECT COUNT(i) FROM SSAPDownloadJobItem i WHERE i.parent.id = :parentJobId"),
    @NamedQuery(name = "SSAPDownloadJobItem.findAllByIdOrdered", query = "SELECT i FROM SSAPDownloadJobItem i WHERE i.parent.id = :parentJobId ORDER BY i.id")
})
public class SSAPDownloadJobItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Column(name = "finish_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date finishTime;

    @Basic(optional = false)
    @Column(name = "download_url", length = 2048)
    private String downloadUrl;

    @Enumerated(EnumType.STRING)
    @Basic(optional = false)
    @Column(name = "\"state\"")
    private DownloadState downloadState;

    @Column(name = "filename")
    private String fileName;//this filename should be used if the name of file is not specified in http request

    @ManyToOne(optional = false)
    private SSAPDownloadJob parent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public DownloadState getDownloadState() {
        return downloadState;
    }

    public void setDownloadState(DownloadState downloadState) {
        this.downloadState = downloadState;
    }

    public SSAPDownloadJob getParent() {
        return parent;
    }

    public void setParent(SSAPDownloadJob parent) {
        this.parent = parent;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + Objects.hashCode(this.id);
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
        final SSAPDownloadJobItem other = (SSAPDownloadJobItem) obj;
        return Objects.equals(this.id, other.id);
    }

}
