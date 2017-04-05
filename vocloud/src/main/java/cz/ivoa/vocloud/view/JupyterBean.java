package cz.ivoa.vocloud.view;

import cz.ivoa.vocloud.ejb.TokenAuthBean;
import cz.ivoa.vocloud.ejb.UserAccountFacade;
import cz.ivoa.vocloud.entity.UserAccount;
import cz.ivoa.vocloud.entity.UserGroupName;
import cz.ivoa.vocloud.tools.Config;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiokoza on 29.3.17.
 */
@Named
@RequestScoped
public class JupyterBean {
    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(FilesystemViewBean.class.getName());

    @EJB
    private UserAccountFacade uaf;

    @EJB
    private TokenAuthBean tokenAuthBean;

    @Inject
    @Config
    private String jupyterhubBaseUrl;

    @Inject
    @Config
    private String jupyterhubServiceName;

    private String url = null;
    private String username = null;
    private String password = null;

    @PostConstruct
    public void init() {
        if (!jupyterhubBaseUrl.endsWith("/")) {
            jupyterhubBaseUrl += "/";
        }
    }

    public String logIn() {
        String user = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
        UserAccount userAcc = uaf.findByUsername(user);
        if (userAcc == null ||
                (!userAcc.getGroupName().equals(UserGroupName.MANAGER) &&
                        !userAcc.getGroupName().equals(UserGroupName.ADMIN))) {
            try {
                FacesContext.getCurrentInstance().getExternalContext()
                        .responseSendError(403, "Unauthorized");
            } catch (IOException ex) {
                Logger.getLogger(this.getClass()).error("IOException during 403 call", ex);
            }
            return "/index?faces-redirect=true";
        }
        // user is authenticated
        url = jupyterhubBaseUrl + "hub/login";
        username = user;
        password = tokenAuthBean.generateToken(username, jupyterhubServiceName);
        //logout from jupyterhub
        jupyterhubLogout();
        return null;
    }

    /**
     * Invalidates jupyterhub cookies that user possibly have from the last login.
     */
    private void jupyterhubLogout() {
        Pattern pattern = Pattern.compile("https?://[^/]*(/.*)");
        Matcher matcher = pattern.matcher(jupyterhubBaseUrl);
        if (!matcher.matches()) {
            LOG.severe("Unable to invalidate jupyterhub cookies due to matcher error. Unable to " +
                    "cookie path prefix from " + jupyterhubBaseUrl);
            return;
        }
        String prefix = matcher.group(1);
        while (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        String[][] cookies = {//name -> path
                {"jupyter-hub-token", prefix + "/hub/"},
                {"jupyterhub-services", prefix + "/services"},
                {"jupyter-hub-token-" + username, prefix + "/user/" + username}
        };
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> prop = new HashMap<>();
        prop.put("maxAge", 0);
        for (String[] cookie : cookies) {
            prop.put("path", cookie[1]);
            ctx.addResponseCookie(cookie[0], "", prop);
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
