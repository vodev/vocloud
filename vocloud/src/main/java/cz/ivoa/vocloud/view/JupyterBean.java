package cz.ivoa.vocloud.view;

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

    @Inject
    @Config
    private String jupyterToken;

    @Inject
    @Config
    private String jupyterUrl;

    public void redirect() {
        String user = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
        UserAccount userAcc = uaf.findByUsername(user);
        if (userAcc == null || userAcc.getGroupName().equals(UserGroupName.USER)) {
            try {
                FacesContext.getCurrentInstance().getExternalContext()
                        .responseSendError(403, "Unauthorized");
            } catch (IOException ex) {
                Logger.getLogger(this.getClass()).error("IOException during 403 call", ex);
            }
            return;
        }
        // user is authenticated
        String url = jupyterUrl;
        if (!url.endsWith("/")) {
            url += "/";
        }
        url += "login?token=" + jupyterToken;
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(url);
        } catch (IOException ex) {
            Logger.getLogger(this.getClass()).error("IOException during redirect to jupyter", ex);
        }
    }
}
