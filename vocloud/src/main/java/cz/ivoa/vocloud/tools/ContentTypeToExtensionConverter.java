package cz.ivoa.vocloud.tools;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author radio.koza
 */
public class ContentTypeToExtensionConverter {

    private static final Logger LOG = Logger.getLogger(ContentTypeToExtensionConverter.class.getName());

    protected static ContentTypeToExtensionConverter instance;

    public static synchronized ContentTypeToExtensionConverter getInstance() {
        if (instance == null) {
            instance = new ContentTypeToExtensionConverter();
        }
        return instance;
    }

    private ResourceBundle extensionProperties;

    private ContentTypeToExtensionConverter() {
        try {
            this.extensionProperties = ResourceBundle.getBundle("cz.ivoa.vocloud.extensions");
        } catch (MissingResourceException ex) {
            LOG.severe("Extensions resource file is missing");
        }
    }

    public String convertToExtension(String contentType) {
        if (extensionProperties == null){
            //do nothing - missing file was already logged
            return null;
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Content-Type argument must not be null");
        }
        try {
            return extensionProperties.getString(contentType);
        } catch (MissingResourceException ex) {
            LOG.log(Level.WARNING, "Definition of extension for content-type: {0} is missing", contentType);
            return null;//not found
        }
    }

}
