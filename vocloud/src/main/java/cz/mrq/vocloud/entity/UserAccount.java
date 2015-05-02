package cz.mrq.vocloud.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.Vetoed;

/**
 *
 * @author voadmin
 */
@Vetoed
@Entity
@NamedQueries({
    @NamedQuery(name = "UserAccount.findAll", query = "SELECT u FROM UserAccount u"),
    @NamedQuery(name = "UserAccount.findById", query = "SELECT u FROM UserAccount u WHERE u.id = :id"),
    @NamedQuery(name = "UserAccount.findByEmail", query = "SELECT u FROM UserAccount u WHERE u.email = :email"),
    @NamedQuery(name = "UserAccount.findByEnabled", query = "SELECT u FROM UserAccount u WHERE u.enabled = :enabled"),
    @NamedQuery(name = "UserAccount.findByFirstName", query = "SELECT u FROM UserAccount u WHERE u.firstName = :firstName"),
    @NamedQuery(name = "UserAccount.findByLastIp", query = "SELECT u FROM UserAccount u WHERE u.lastIp = :lastIp"),
    @NamedQuery(name = "UserAccount.findByLastLogin", query = "SELECT u FROM UserAccount u WHERE u.lastLogin = :lastLogin"),
    @NamedQuery(name = "UserAccount.findByLastName", query = "SELECT u FROM UserAccount u WHERE u.lastName = :lastName"),
    @NamedQuery(name = "UserAccount.findByOrganization", query = "SELECT u FROM UserAccount u WHERE u.organization = :organization"),
    @NamedQuery(name = "UserAccount.findByPass", query = "SELECT u FROM UserAccount u WHERE u.pass = :pass"),
    @NamedQuery(name = "UserAccount.findByRegisteredIp", query = "SELECT u FROM UserAccount u WHERE u.registeredIp = :registeredIp"),
    @NamedQuery(name = "UserAccount.findBySince", query = "SELECT u FROM UserAccount u WHERE u.since = :since"),
    @NamedQuery(name = "UserAccount.findByUsername", query = "SELECT u FROM UserAccount u WHERE u.username = :username")})
public class UserAccount implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(UserAccount.class.getName());

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    private String organization;
    @Column(unique = true, nullable = false)
    @Pattern(regexp = "[a-zA-Z]+([a-zA-Z0-9][._-]?)+[a-zA-Z0-9]")
    @Size(min = 3)
    private String username;
    @Size(min = 6)
    private String pass;
    @Column(unique = true, nullable = false)
    @Pattern(regexp = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\."
            + "[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
            + "(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message = "invalid email")
    private String email;
    @NotNull
    private Boolean enabled;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date since;
    private String registeredIp;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date lastLogin;
    private String lastIp;
    @Column(nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private UserGroupName groupName;
    private Long quota = 100000000L;
    @OneToMany(mappedBy = "owner")
    private List<Job> jobs;

    public UserAccount() {

    }

    public UserGroupName getGroupName() {
        return groupName;
    }

    public void setGroupName(UserGroupName groupName) {
        this.groupName = groupName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getPass() {
        return pass;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public Long getQuota() {
        //default 1000000
        if (quota == null) {
            return 100000000L;
        }
        return quota;
    }

    public void setQuota(Long quota) {
        this.quota = quota;
    }

    /**
     * Password is hashed using sha256
     *
     * http://www.mkyong.com/java/java-sha-hashing-example/
     *
     * @param password
     * @return hashed password
     */
    private String hashPassword(String password) {
        String encPass = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes("UTF-8"));

            byte[] digest = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte aDigest : digest) {
                hexString.append(Integer.toString((aDigest & 0xff) + 0x100, 16).substring(1));
            }
            encPass = hexString.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return encPass;
    }

    public void setPass(String pass) {
        this.pass = hashPassword(pass);
    }

    public boolean isPassword(String pass) {
        return (this.pass == null ? hashPassword(pass) == null : this.pass.equals(hashPassword(pass)));
    }

    public String getRegisteredIp() {
        return registeredIp;
    }

    public void setRegisteredIp(String registeredIp) {
        this.registeredIp = registeredIp;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
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
        final UserAccount other = (UserAccount) obj;
        return !((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)));
    }
    
    

    @Override
    public String toString() {
        return "entity.user[ id=" + id + " ]";
    }
}
