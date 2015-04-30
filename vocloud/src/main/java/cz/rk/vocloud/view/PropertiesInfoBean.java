package cz.rk.vocloud.view;

import cz.mrq.vocloud.tools.Config;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author radio.koza
 */
@Named
@RequestScoped
public class PropertiesInfoBean {
    
    @Inject
    @Config 
    private String buildNumber;
    
    @Inject
    @Config
    private String buildTimestamp;
    
    @Inject
    @Config
    private String buildTimestampFormat;

    public String getBuildNumber() {
        return buildNumber;
    }

    public String getBuildTimestamp() {
        Date time = new Date(Long.parseLong(buildTimestamp));
        SimpleDateFormat sdf = new SimpleDateFormat(buildTimestampFormat);
        return sdf.format(time);
    }
    
    
    
}
