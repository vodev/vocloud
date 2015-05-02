package cz.mrq.vocloud.uwsparser.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author Lumir Mrkva (lumir.mrkva@topmonks.com)
 */
@XmlRootElement(name = "parameter")
public class Parameter {

    @XmlAttribute
    String id;
    @XmlValue
    String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value.trim();
    }

    public String getValueTrimmed() {
        return value != null ? value.trim() : null;
    }

    @Override
    public String toString() {
        return "Parameter{"
                + "id='" + id + '\''
                + ", value='" + value + '\''
                + '}';
    }
}
