package cz.mrq.vocloud.managedbeans;

import cz.mrq.vocloud.ejb.UserAccountFacade;
import cz.mrq.vocloud.ejb.UserSessionBean;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import java.io.Serializable;

/**
 *
 * @author voadmin
 */
@ManagedBean
@RequestScoped
public class SettingsBean implements Serializable {

    @EJB
    private UserSessionBean usb;
    @EJB
    private UserAccountFacade uaf;

    private String oldpass;
    private String newpass;
    private String newpassAgain;

    public SettingsBean() {
    }

    public void changePassword() {
        FacesContext currentInstance = FacesContext.getCurrentInstance();
        //check that newpass and newpassAgain are equals
        if (!newpass.equals(newpassAgain)){
            currentInstance.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Password does not match", ""));
            return;
        }
        if (usb.getUser().isPassword(oldpass)) {
            //change password
            uaf.changePassword(usb.getUser(), newpass);
            currentInstance.addMessage(null, new FacesMessage("Your password has been successfully changed."));
        } else {
            currentInstance.addMessage("#oldpass", new FacesMessage("Wrong password!"));
        }
    }

    public void validatePassword(FacesContext context,
            UIComponent toValidate,
            Object value) {

        String password = (String) value;
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Wrong password.", null);

        if (!usb.getUser().isPassword(password)) {
            context.addMessage(toValidate.getClientId(context), message);
            ((UIInput) toValidate).setValid(false);
        }
    }

    public String getNewpass() {
        return newpass;
    }

    public void setNewpass(String newpass) {
        this.newpass = newpass;
    }

    public String getOldpass() {
        return oldpass;
    }

    public void setOldpass(String oldpass) {
        this.oldpass = oldpass;
    }

    public String getNewpassAgain() {
        return newpassAgain;
    }

    public void setNewpassAgain(String newpassAgain) {
        this.newpassAgain = newpassAgain;
    }
    
    

}
