package cz.ivoa.vocloud.view;

import cz.ivoa.vocloud.ejb.JobFacade;
import cz.ivoa.vocloud.ejb.UserSessionBean;
import cz.ivoa.vocloud.entity.UserAccount;
import cz.ivoa.vocloud.tools.Config;
import cz.ivoa.vocloud.tools.Toolbox;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.inject.Inject;

/**
 * @author voadmin
 */
@ManagedBean(name = "sessionBean")
@SessionScoped
public class SessionBean implements Serializable {

    @EJB
    private UserSessionBean usb;
    @EJB
    private JobFacade jf;

    private UserAccount user;
    private boolean loggedIn = false;
    private String last = "";

    public SessionBean() {
    }

    /**
     * This method will update user entity after login (lastip, last login)
     *
     * @return user's identity
     */
    public String login() {
        if (!loggedIn) {
            UserAccount u = usb.getUser();
            if (u == null) {
                return "anonymous";
            }
            user = u;
            DateFormat df = new SimpleDateFormat();
            Date lastLogin = u.getLastLogin();
            if (lastLogin == null) {
                last = "this is your first login";
                // if first and no jobs, copy examples
//                if (jf.findByOwnerId(user).isEmpty()) {
//                    jf.prepareKorelJobExamples(user);
//                }
            } else {
                last = u.getLastIp() + " (" + df.format(lastLogin) + ")";
            }
            loggedIn = true;
            FacesContext currentInstance = FacesContext.getCurrentInstance();
            HttpServletRequest request = (HttpServletRequest) currentInstance.getExternalContext().getRequest();
            user.setLastIp(Toolbox.realIp(request));
            user.setLastLogin(new Date());
            usb.login();
        }
        return user.getFirstName();
    }

    public boolean getLoggedIn() {
        return loggedIn;
    }

    public UserAccount getUser() {
        if (user == null) {
            user = usb.getUser();
            if (user == null) {
                user = new UserAccount();
            }
        }
        return user;
    }

    public String getLast() {
        return last;
    }

}
