package cz.mrq.vocloud.uwsparser.model;

import cz.mrq.vocloud.entity.Phase;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author voadmin
 */
@XmlRootElement(name = "job")
public class UWSJob {

    @XmlTransient
    public static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private String jobId;
    @XmlElement(nillable = true)
    private String runId;
    @XmlElement(name = "ownerId")
    private String owner;
    private Phase phase;
    private Date startTime;
    private Date endTime;
    private long executionDuration;
    private Date destruction;
    @XmlElementWrapper(name = "results")
    @XmlElement(name = "result")
    private List<Result> results;
    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    private List<Parameter> parameters;
    @XmlElement(nillable = true)
    private ErrorSummary errorSummary;

    //<editor-fold defaultstate="collapsed" desc="getters and setters">
    public Date getDestruction() {
        return destruction;
    }

    public void setDestruction(Date destruction) {
        this.destruction = destruction;
    }

    public void setDestruction(String destruction) {
        try {
            this.destruction = new SimpleDateFormat(DATE_FORMAT).parse(destruction);
        } catch (ParseException ex) {
            Logger.getLogger(UWSJob.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public void setEndTime(String endTime) {
        try {
            this.endTime = new SimpleDateFormat(DATE_FORMAT).parse(endTime);
        } catch (ParseException ex) {
            Logger.getLogger(UWSJob.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public long getExecutionDuration() {
        return executionDuration;
    }

    public void setExecutionDuration(long executionDuration) {
        this.executionDuration = executionDuration;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<Parameter> getParameters() {
        if (results == null) {
            return new ArrayList<>();
        }
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public List<Result> getResults() {
        if (results == null) {
            return new ArrayList<>();
        }
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public ErrorSummary getErrorSummary() {
        return errorSummary;
    }

    public void setErrorSummary(ErrorSummary errorSummary) {
        this.errorSummary = errorSummary;
    }

    public void setStartTime(String startTime) {
        try {
            this.startTime = new SimpleDateFormat(DATE_FORMAT).parse(startTime);
        } catch (ParseException ex) {
            Logger.getLogger(UWSJob.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Result getResult() {
        return results != null && !results.isEmpty() ? results.get(0) : null;
    }

    public String getResultUrl() {
        return results != null && !results.isEmpty() ? results.get(0).getHref() : null;
    }
    //</editor-fold>

    @Override
    public String toString() {
        return "UWSJob{"
                + "\n\t jobId='" + jobId + '\''
                + "\n\t runId='" + runId + '\''
                + "\n\t owner='" + owner + '\''
                + "\n\t phase=" + phase
                + "\n\t startTime=" + startTime
                + "\n\t endTime=" + endTime
                + "\n\t executionDuration=" + executionDuration
                + "\n\t destruction=" + destruction
                + "\n\t results=" + results
                + "\n\t parameters=" + parameters
                + "\n\t errorSummary='" + errorSummary + '\''
                + "\n}";
    }
}
