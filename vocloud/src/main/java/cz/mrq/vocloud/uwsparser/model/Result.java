package cz.mrq.vocloud.uwsparser.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Lumir Mrkva (lumir.mrkva@topmonks.com)
 */
@XmlRootElement(name = "result")
public class Result {

    @XmlAttribute
    String id;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    String type;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    String href;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHref() {
        return href;
    }

    public String getHrefTrimmed() {
        return href != null ? href.trim() : null;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @Override
    public String toString() {
        return "Result{"
                + "id='" + id + '\''
                + ", type='" + type + '\''
                + ", href='" + href + '\''
                + '}';
    }
}
