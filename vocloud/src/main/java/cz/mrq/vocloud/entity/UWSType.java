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
import javax.validation.constraints.Size;

/**
 *
 * @author radio.koza
 */
@Vetoed
@Entity
@NamedQueries({
    @NamedQuery(name = "UWSType.findAllByIdentifierOrdered", query = "SELECT t FROM UWSType t ORDER BY t.stringIdentifier"),
    @NamedQuery(name = "UWSType.findAllowedNonRestricted", query = "SELECT DISTINCT u.uwsType FROM UWS u WHERE u.enabled = TRUE AND u.uwsType.restricted = FALSE ORDER BY u.uwsType.shortDescription"),
    @NamedQuery(name = "UWSType.findAllowedRestricted", query = "SELECT DISTINCT u.uwsType FROM UWS u WHERE u.enabled = TRUE AND u.uwsType.restricted = TRUE ORDER BY u.uwsType.shortDescription"),
    @NamedQuery(name = "UWSType.findByStringIdentifier", query = "SELECT t FROM UWSType t WHERE t.stringIdentifier = :strId")
})
public class UWSType implements Serializable {

    private static final long serialVersionUID = 1L;//define own serialversionuid constant for serialization

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Size(min = 1, max = 100)
    @Column(nullable = false, unique = true)
    private String stringIdentifier;
    @Column(nullable = false, length = 100)
    private String shortDescription;//short description to be used instead of stringIdentifier
    @Column(nullable = true, length = 1000) //max 1000 characters
    private String description;//description of this uws type
    @Column(nullable = true, length = 255)
    private String documentationUrl;//url link to the documentation - nullable
    @Column(nullable = false)//must be defined
    private Boolean restricted;//restricted to managers only?

    //define relations
    @OneToMany(mappedBy = "uwsType")
    private List<UWS> uwsList = new ArrayList<>();//initialized to empty collection
    @OneToMany(mappedBy = "uwsType")
    private List<Job> jobs = new ArrayList<>();

    public UWSType() {
        //nothing to do here
    }

    /**
     * Initialization constructor with all columns specified
     *
     * @param stringIdentifier Unique identifier of this job. (like som, korel,
     * ...)
     * @param shortDescription Short description of uws type to be used instead
     * of stringIdentifier column
     * @param description Description of this type of job. (maximum 1000
     * characters)
     * @param documentationUrl Url referencing the documentation of this type of
     * job
     * @param restricted If it is for managers and admins only then true. False
     * otherwise.
     */
    public UWSType(String stringIdentifier, String shortDescription, String description, String documentationUrl, Boolean restricted) {
        this.stringIdentifier = stringIdentifier;
        this.restricted = restricted;
        this.description = description;
        this.documentationUrl = documentationUrl;
        this.shortDescription = shortDescription;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStringIdentifier() {
        return stringIdentifier;
    }

    public void setStringIdentifier(String stringIdentifier) {
        //remove all spaces
        this.stringIdentifier = stringIdentifier.trim().replace(" ", "");
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription.trim();
        if (this.shortDescription.length() == 0) {
            this.shortDescription = null;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description.trim();
        if (this.description.length() == 0) {
            this.description = null;
        }
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl.trim();
        if (this.documentationUrl.length() == 0) {
            this.documentationUrl = null;
        }
    }

    public Boolean getRestricted() {
        return restricted;
    }

    public void setRestricted(Boolean restricted) {
        this.restricted = restricted;
    }

    public List<UWS> getUwsList() {
        return uwsList;
    }

    public void setUwsList(List<UWS> uwsList) {
        this.uwsList = uwsList;
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
        hash = 29 * hash + Objects.hashCode(this.id);
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
        final UWSType other = (UWSType) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "UWSType{" + "id=" + id + ", stringIdentifier=" + stringIdentifier + ", shortDescription=" + shortDescription + ", description=" + description + ", documentationUrl=" + documentationUrl + ", restricted=" + restricted + ", uwsList=" + uwsList + '}';
    }

}
