package cz.ivoa.vocloud.entity;

import cz.ivoa.vocloud.uwsparser.model.UWSJob;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Logger;
import javax.enterprise.inject.Vetoed;

/**
 *
 * @author voadmin
 */
@Vetoed
@Entity
@NamedQueries({
    @NamedQuery(name = "Job.findAll", query = "SELECT j FROM Job j"),
    @NamedQuery(name = "Job.findById", query = "SELECT j FROM Job j WHERE j.id = :id"),
    @NamedQuery(name = "Job.findByCreatedDate", query = "SELECT j FROM Job j WHERE j.createdDate = :createdDate"),
    @NamedQuery(name = "Job.findByFinishedDate", query = "SELECT j FROM Job j WHERE j.finishedDate = :finishedDate"),
    @NamedQuery(name = "Job.findByLabel", query = "SELECT j FROM Job j WHERE j.label = :label"),
    @NamedQuery(name = "Job.findByNotes", query = "SELECT j FROM Job j WHERE j.notes = :notes"),
    @NamedQuery(name = "Job.findByPhase", query = "SELECT j FROM Job j WHERE j.phase = :phase"),
    @NamedQuery(name = "Job.findByOwnerId", query = "SELECT j FROM Job j WHERE j.owner = :owner"),
    @NamedQuery(name = "Job.userJobList", query = "SELECT j FROM Job j WHERE j.owner = :owner ORDER BY j.createdDate DESC"),
    @NamedQuery(name = "Job.findAllCreateDateOrdered", query = "SELECT j FROM Job j ORDER BY j.createdDate DESC"),
    @NamedQuery(name = "Job.findByRemoteId", query = "SELECT j FROM Job j WHERE j.remoteId = :remoteId"),
    @NamedQuery(name = "Job.countUserJobs", query = "SELECT COUNT(j) FROM Job j WHERE j.owner = :owner")
})
public class Job implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NotNull
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String label;

    @Size(max = 2048)
    @Column(length = 2048)
    private String notes;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdDate;//assigned by vocloud
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date startedDate;//value from worker
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date finishedDate;//value from worker

    private String remoteId;//id of the job on the worker
    @Enumerated(EnumType.STRING)
    private Phase phase;
    @Column(nullable = false)
    private Boolean resultsEmail;//flag if user should be informed in email about end of job
    @ManyToOne(optional = false)
    private UserAccount owner;
    @ManyToOne
    private UWS uws;//TODO uws is assigned in pending phase despite option to run immediately
    @ManyToOne(optional = false)
    private UWSType uwsType;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private String configurationJson;
    //atributes for copy after job completion
    @Column(nullable = true, length = 1000)
    private String targetDir;
    @Column(nullable = true, length = 1000)
    private String copyMessage;

//    @Transient
//    private UWSJob uwsJob;
//    @Transient
//    private String uwsJobXml;
//    @Transient
//    private UWSParserManager parser;
//
    /**
     * updates attributes from local UWS job object (only if uwsjob isn't null
     * obviously)
     *
     * @param uwsJob
     */
    public void updateFromUWSJob(UWSJob uwsJob) {
        if (uwsJob != null) {
            this.startedDate = uwsJob.getStartTime();
            this.finishedDate = uwsJob.getEndTime();
            this.remoteId = uwsJob.getJobId();
            this.phase = uwsJob.getPhase();
        } else {
            Logger.getLogger(Job.class.getName()).warning("trying to update from non-existent uwsJob");
        }
    }

    public boolean isCompleted() {
        return (this.phase == Phase.COMPLETED);
    }

    public boolean hasEndState() {
        return this.phase == Phase.ABORTED || this.phase == Phase.COMPLETED || this.phase == Phase.ERROR;
    }

    /**
     * Returns execution time in seconds.
     *
     * @return Execution time in seconds.
     */
    @Transient
    public long getExecutingTime() {
        Date sd = startedDate;
        Date fd = finishedDate;
        if (sd == null) {
            return 0;
        }
        if (fd == null) {
//            sd = createdDate;
            fd = new Date();
        }
        return (fd.getTime() - sd.getTime()) / 1000;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(Date startedDate) {
        this.startedDate = startedDate;
    }

    public Date getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(Date finishedDate) {
        this.finishedDate = finishedDate;
    }

    public String getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public Boolean getResultsEmail() {
        return resultsEmail;
    }

    public void setResultsEmail(Boolean resultsEmail) {
        this.resultsEmail = resultsEmail;
    }

    public UserAccount getOwner() {
        return owner;
    }

    public void setOwner(UserAccount owner) {
        this.owner = owner;
    }

    public UWS getUws() {
        return uws;
    }

    public void setUws(UWS uws) {
        this.uws = uws;
    }

    public UWSType getUwsType() {
        return uwsType;
    }

    public void setUwsType(UWSType uwsType) {
        this.uwsType = uwsType;
    }

    public String getConfigurationJson() {
        return configurationJson;
    }

    public void setConfigurationJson(String configurationJson) {
        this.configurationJson = configurationJson;
    }

    public String getTargetDir() {
        return targetDir;
    }

    public void setTargetDir(String targetDir) {
        this.targetDir = targetDir;
    }

    public String getCopyMessage() {
        return copyMessage;
    }

    public void setCopyMessage(String copyMessage) {
        this.copyMessage = copyMessage;
    }

    @Transient
    public String getStringId() {
        return owner.getId().toString() + "-" + id.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.id);
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
        final Job other = (Job) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "Job{" + "id=" + id + ", label=" + label + ", notes=" + notes + ", createdDate=" + createdDate + ", startedDate=" + startedDate + ", finishedDate=" + finishedDate + ", remoteId=" + remoteId + ", phase=" + phase + ", resultsEmail=" + resultsEmail + ", owner=" + owner + ", uws=" + uws + ", uwsType=" + uwsType + '}';
    }

}
