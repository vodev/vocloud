package cz.ivoa.vocloud.view;

import cz.ivoa.vocloud.ejb.TokenAuthBean;
import cz.ivoa.vocloud.ejb.UserAccountFacade;
import cz.ivoa.vocloud.entity.UserAccount;
import cz.ivoa.vocloud.entity.UserGroupName;
import cz.ivoa.vocloud.tools.Config;
import org.jboss.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

/**
 * Created by radiokoza on 29.3.17.
 */
@Named
@RequestScoped
public class JupyterBean {
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
        url = jupyterhubBaseUrl;
        if (!url.endsWith("/")) {
            url += "/";
        }
        url += "hub/login";
        username = user;
        password = tokenAuthBean.generateToken(username, jupyterhubServiceName);
        return null;
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
