package cz.rk.vocloud.view.validator;

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
            return;
        }
        if (!urlValue.startsWith("http://", 0) || !urlValue.startsWith("https://", 0)) {
            url.append("http://");
        }
        url.append(urlValue);

        try {
            URI uri = new URI(url.toString());
        } catch (URISyntaxException e) {
            FacesMessage msg
                    = new FacesMessage("Invalid format of URL address", "Invalid URL format");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }

}
