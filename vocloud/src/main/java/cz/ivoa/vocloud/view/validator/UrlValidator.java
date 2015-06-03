package cz.ivoa.vocloud.view.validator;

import java.net.URI;
import java.net.URISyntaxException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author radio.koza
 */
@FacesValidator(value = "urlValidator")
public class UrlValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        StringBuilder url = new StringBuilder();
        String urlValue = value.toString();
        if (urlValue.length() == 0){
            throw new ValidatorException(new FacesMessage("Invalid format of URL address", "URL string is empty"));
        }
        if (!urlValue.matches("^[a-z]+://.*")) {
            url.append("http://");
        } else {
            //supported protocols http, https, ftp only
            String protocol = urlValue.replaceFirst("^([a-z]+)://.*", "$1");
            switch (protocol){
                case "http":
                case "https":
                case "ftp": break;//ok
                default: //unsupported protocol
                    throw new ValidatorException(new FacesMessage("Invalid format of URL address", "Unsupported protocol: " + protocol));
            }
        }
        url.append(urlValue);

        try {
            URI uri = new URI(url.toString());
            int port = uri.getPort();
            if (port == -1){
                return;
            }
            if (port <= 0 || port >= 65536){
                throw new URISyntaxException(url.toString(), "Invalid port: " + port);
            } 
        } catch (URISyntaxException e) {
            FacesMessage msg
                    = new FacesMessage("Invalid format of URL address", "Invalid URL format");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }

}
