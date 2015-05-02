package cz.mrq.vocloud.ejb;

import cz.mrq.vocloud.entity.UserAccount;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import java.security.Principal;

/**
 *
 * @author voadmin
 */
@Stateful
@LocalBean
public class UserSessionBean {

    @EJB
    private UserAccountFacade uaf;
    private UserAccount user;
    @Resource
    private SessionContext sessionContext;

    public UserAccount getUser() {
        if (user == null) {
            Principal principal = sessionContext.getCallerPrincipal();
            if (principal != null) {
                user = getUaf().findByUsername(principal.getName());
            }
        }
        return user;
    }

    private UserAccountFacade getUaf() {
        return uaf;
    }

    public void login() {
        uaf.edit(user);
    }

}
