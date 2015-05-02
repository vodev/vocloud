package cz.rk.vocloud.entity;

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
import javax.persistence.Lob;
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
@Table(name = "download_job")
@NamedQueries({
    @NamedQuery(name = "DownloadJob.findJobsInStates", query = "SELECT j FROM DownloadJob j WHERE j.downloadState IN (:states)"),
    @NamedQuery(name = "DownloadJob.findCreateDescOrdered", query = "SELECT j FROM DownloadJob j ORDER BY j.createTime DESC")
})
public class DownloadJob implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "finish_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date finishTime;

    @Basic(optional = false)
    @Column(name = "download_url", length = 2048)
    private String downloadUrl;

    @Basic(optional = false)
    @Column(name = "directory")
    private String saveDir;

    @Enumerated(EnumType.STRING)
    @Basic(optional = false)
    @Column(name = "\"state\"")
    private DownloadState downloadState;

    @Lob
    @Column(name = "log")
    private String messageLog;

    @Column(name = "username")
    private String username;
    
    @Column(name = "pass")
    private String pass;
    
    @Column(name = "filename")
    private String fileName;
    
    public DownloadJob() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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

    public DownloadState getState() {
        return downloadState;
    }

    public void setState(DownloadState state) {
        this.downloadState = state;
    }

    public String getMessageLog() {
        return messageLog;
    }

    public void setMessageLog(String messageLog) {
        this.messageLog = messageLog;
    }

    public String getSaveDir() {
        return saveDir;
    }

    public void setSaveDir(String saveDir) {
        this.saveDir = saveDir;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPass() {
        return pass;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.id);
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
        final DownloadJob other = (DownloadJob) obj;
        return Objects.equals(this.id, other.id);
    }
    
    
    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    

}
