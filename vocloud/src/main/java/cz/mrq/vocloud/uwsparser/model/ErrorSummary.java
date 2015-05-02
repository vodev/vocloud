package cz.mrq.vocloud.uwsparser.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Lumir Mrkva (lumir.mrkva@topmonks.com)
 */
@XmlRootElement(name = "errorSummary")
public class ErrorSummary {

    @XmlAttribute
    String type;
    String message;
    String detail;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "ErrorSummary{"
                + "type='" + type + '\''
                + ", message='" + message + '\''
                + ", detail='" + detail + '\''
                + '}';
    }
}
