package cz.mrq.vocloud.managedbeans;

import cz.mrq.vocloud.ejb.UserAccountFacade;
import cz.mrq.vocloud.entity.UserAccount;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.Serializable;

/**
 *
 * @author voadmin
 */
@ManagedBean(name = "resetPass")
@RequestScoped
public class ResetPass implements Serializable {

    @EJB
    private UserAccountFacade ejbFacade;

    private UserAccount user = new UserAccount();
    private Boolean disabled = false;

    /**
     * Creates a new instance of ResetPass
     */
    public ResetPass() {
    }

    public UserAccount getUser() {
        return user;
    }

    public void reset() {
        Boolean result = ejbFacade.resetPassword(user.getEmail());
        if (!result) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No user with email " + user.getEmail() + " exists.", " "));
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "New password has been sent to " + user.getEmail(), "Email should arrive in next few minutes."));
            this.disabled = true;
        }
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

}
