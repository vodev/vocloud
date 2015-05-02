package cz.mrq.vocloud.managedbeans;

import cz.mrq.vocloud.ejb.UserAccountFacade;
import cz.mrq.vocloud.entity.UserAccount;
import cz.mrq.vocloud.entity.UserGroupName;
import cz.mrq.vocloud.tools.Config;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 *
 * @author voadmin
 */
@ManagedBean(name = "register")
@RequestScoped
public class RegisterBean implements Serializable {

    @EJB
    private UserAccountFacade uaf;

    @Inject
    @Config
    private String defaultQuota;
    private final UserAccount user;

    public RegisterBean() {
        user = new UserAccount();
    }

    public void register() {
        FacesContext currentInstance = FacesContext.getCurrentInstance();

        user.setEnabled(Boolean.TRUE);

        if (user.getUsername().equals("admin")) {
            user.setGroupName(UserGroupName.ADMIN);
        } else {
            user.setGroupName(UserGroupName.USER);
        }
        if (defaultQuota == null) {
            user.setQuota(100000000L);
        } else {
            user.setQuota(Long.decode(defaultQuota));

        }

        try {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            user.setRegisteredIp(request.getRemoteAddr());

            uaf.create(user);
            currentInstance.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Welcome " + user.getFirstName() + "!", null));
            currentInstance.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Your account has been successfully registered, you can login right away.", null));
        } catch (Exception e) {
            currentInstance.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Account registration failed: " + e.toString(), null));
        }

        String redirect = "login";
        NavigationHandler myNav = currentInstance.getApplication().getNavigationHandler();

        myNav.handleNavigation(currentInstance, null, redirect);
    }

    public void validateUniqUsername(FacesContext context,
            UIComponent toValidate,
            Object value) {
        String username = (String) value;
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Username is already registered.", null);

        if (uaf.findByUsername(username) != null) {
            context.addMessage(toValidate.getClientId(context), message);
            ((UIInput) toValidate).setValid(false);
        }
    }

    public void validateUniqEmail(FacesContext context,
            UIComponent toValidate,
            Object value) {
        String email = (String) value;
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "E-mail is already registered.", null);

        if (uaf.findByEmail(email) != null) {
            context.addMessage(toValidate.getClientId(context), message);
            ((UIInput) toValidate).setValid(false);
        }
    }

    public UserAccount getUser() {
        return user;
    }

    private UserAccountFacade getUaf() {
        return uaf;
    }
}
