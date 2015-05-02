package cz.rk.vocloud.view;

import java.security.Principal;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author radio.koza
 */
@Named
@RequestScoped
public class LoginBean {

    private String username;
    private String password;

    public LoginBean() {
        //nothing to do here
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private static HttpServletRequest getRequest() {
        return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    }

    private static Principal principal() {
        return getRequest().getUserPrincipal();
    }

    public String getPrincipalName() {
        Principal p = principal();
        if (p == null) {
            return "not logged";
        }
        return p.getName();
    }

    public boolean isUserLogged() {
        return principal() != null;
    }

    public String login() {
        try {
            //logout if already logged
            if (isUserLogged()) {
                getRequest().logout();
            }
            //otherwise try to login
            getRequest().login(username, password);
        } catch (ServletException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Invalid credentials", "Username or password is incorrect"));
            return "loginerror?faces-redirect=true";
        }
        return "index?faces-redirect=true";
    }

    public String logout() {
        if (!isUserLogged()) {
            return null;
        }
        clearCache(getPrincipalName());//will not be used later //TODO
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        try {
            getRequest().logout();
            return "/login?faces-redirect=true";
        } catch (ServletException ex) {
            Logger.getLogger(getClass().toString()).warning("Logout unsuccessful!");
        }
        return null;
    }

    //temp method for fixing bug in wildfly
    private void clearCache(String username) {
        try {
            ObjectName jaasMgr = new ObjectName("jboss.as:subsystem=security,security-domain=VocloudSecurityDomain");
            Object[] params = {username};
            String[] signature = {"java.lang.String"};
            MBeanServer server = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);
            server.invoke(jaasMgr, "flushCache", params, signature);
        } catch (MalformedObjectNameException | InstanceNotFoundException | MBeanException | ReflectionException ex) {
        }
    }

}
