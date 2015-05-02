package cz.mrq.vocloud.tools;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author voadmin
 */
public class Configuration {

    private static final Logger logger = Logger.getLogger(Configuration.class.getName());

    private volatile static Properties properties;
    private static final String propertiesFile = "/cz/mrq/vocloud/vokorel.properties";

    public synchronized static Properties getProperties() {
        if (properties == null) {
            properties = new Properties();

            try {
                properties.load(Configuration.class.getResourceAsStream(propertiesFile));
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "config file load failed", ex);
                properties = null;
                throw new RuntimeException(ex);
            }
        }
        return properties;
    }

    public @Produces
    @Config
    String getConfiguration(InjectionPoint p) {
        Properties config = getProperties();

        // class specific key
        String key = p.getMember().getDeclaringClass().getSimpleName() + "." + p.getMember().getName();

        if (config.getProperty(key) == null) {
            // global key
            key = p.getMember().getName();
            if (config.getProperty(key) == null) {
                logger.log(Level.WARNING, "property not set {0}", key);
            }
        }

        return config.getProperty(key);
    }
}
