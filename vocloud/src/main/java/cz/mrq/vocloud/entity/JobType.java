package cz.mrq.vocloud.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.io.Serializable;
import javax.enterprise.inject.Vetoed;

/**
 * JobType - still not used entity
 *
 * @author Lumir Mrkva (lumir@mrq.cz)
 */
@Vetoed
@Entity
@NamedQueries({
    @NamedQuery(name = "JobType.findAll", query = "SELECT jt FROM JobType jt"),
    @NamedQuery(name = "JobType.findByName", query = "SELECT jt FROM JobType jt WHERE jt.name = :name")
})
public class JobType implements Serializable {

    String name;
    String params;
    String docsUrl;
    String runScript;
    String postProcessDir;

    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getDocsUrl() {
        return docsUrl;
    }

    public void setDocsUrl(String docsUrl) {
        this.docsUrl = docsUrl;
    }

    public String getRunScript() {
        return runScript;
    }

    public void setRunScript(String runScript) {
        this.runScript = runScript;
    }

    public String getPostProcessDir() {
        return postProcessDir;
    }

    public void setPostProcessDir(String postProcessDir) {
        this.postProcessDir = postProcessDir;
    }
}
