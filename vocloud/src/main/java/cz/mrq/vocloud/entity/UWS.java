package cz.mrq.vocloud.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.enterprise.inject.Vetoed;

/**
 *
 * @author radio.koza
 */
@Vetoed
@Entity
@NamedQueries({
    @NamedQuery(name = "UWS.findAll", query = "SELECT u FROM UWS u"),
    @NamedQuery(name = "UWS.findById", query = "SELECT u FROM UWS u WHERE u.id = :id"),
    @NamedQuery(name = "UWS.findByEnabled", query = "SELECT u FROM UWS u WHERE u.enabled = :enabled"),
})
public class UWS implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(nullable = false)
    private Boolean enabled;

    //definition of relations
    @ManyToOne(optional = false)
    private UWSType uwsType;

    @ManyToOne(optional = false)
    private Worker worker;

    @OneToMany(mappedBy = "uws")
    private List<Job> jobs = new ArrayList<>();


    //<editor-fold defaultstate="collapsed" desc="getters setters...">
    //define non-parametric constructor to fullfil specification of JPA entities

    public UWS() {
        //define enabled default true
        this.enabled = true;
    }

    public UWS(UWSType uwsType, Worker worker) {
        this.enabled = true; //enabled by default
        this.uwsType = uwsType;
        this.worker = worker;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public UWSType getUwsType() {
        return uwsType;
    }

    public void setUwsType(UWSType uwsType) {
        this.uwsType = uwsType;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public String getUwsUrl() {
        if (uwsType == null || worker == null) {
            return null;
        }
        return worker.getResourceUrl() + '/' + uwsType.getStringIdentifier();
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.id);
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
        final UWS other = (UWS) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "UWS{" + "id=" + id + ", enabled=" + enabled + ", uwsType=" + uwsType + ", worker=" + worker + '}';
    }

}
