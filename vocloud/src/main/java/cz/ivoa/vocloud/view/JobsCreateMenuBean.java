package cz.ivoa.vocloud.view;

import cz.ivoa.vocloud.ejb.UWSTypeFacade;
import cz.ivoa.vocloud.ejb.UserSessionBean;
import cz.ivoa.vocloud.entity.UWSType;
import cz.ivoa.vocloud.entity.UserAccount;
import cz.ivoa.vocloud.entity.UserGroupName;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.primefaces.component.menuitem.UIMenuItem;
import org.primefaces.component.submenu.UISubmenu;

/**
 * @author radio.koza
 */
@Named
@RequestScoped
public class JobsCreateMenuBean {

    private UISubmenu jobsCreateSubmenu;
    @EJB
    private UWSTypeFacade facade;
    @EJB
    private UserSessionBean usb;

    private Set<String> possibleTypeIds;

    @PostConstruct
    private void init() {
        UserAccount userAcc = usb.getUser();
        if (userAcc == null) {
            //user is not logged in - unnecessary to setup this bean
            return;
        }
        boolean restrictedAccess = userAcc.getGroupName().equals(UserGroupName.ADMIN) || userAcc.getGroupName().equals(UserGroupName.MANAGER);
        //populate menu
        List<UWSType> possibleUnrestrictedTypes = facade.findAllowedNonRestrictedTypes();
        List<UWSType> possibleRestrictedTypes = facade.findAllowedRestrictedTypes();
        if (possibleRestrictedTypes.isEmpty()) {
            restrictedAccess = false;
        }
        possibleTypeIds = new HashSet<>();
        jobsCreateSubmenu = new UISubmenu();
        jobsCreateSubmenu.setLabel("Create job");
        if (!restrictedAccess) {
            for (UWSType type : possibleUnrestrictedTypes) {
                possibleTypeIds.add(type.getStringIdentifier());
                jobsCreateSubmenu.getChildren().add(constructMenuItem(type));
            }
        } else {
            UISubmenu basic = new UISubmenu();
            basic.setLabel("Standard jobs");
            for (UWSType type : possibleUnrestrictedTypes) {
                possibleTypeIds.add(type.getStringIdentifier());
                basic.getChildren().add(constructMenuItem(type));
            }
            jobsCreateSubmenu.getChildren().add(basic);
            UISubmenu restricted = new UISubmenu();
            restricted.setLabel("Restricted jobs");
            for (UWSType type : possibleRestrictedTypes) {
                possibleTypeIds.add(type.getStringIdentifier());
                restricted.getChildren().add(constructMenuItem(type));
            }
            jobsCreateSubmenu.getChildren().add(restricted);
        }
    }

    private UIMenuItem constructMenuItem(UWSType type) {
        UIMenuItem item = new UIMenuItem();
        item.setAjax(false);
        item.setId(type.getStringIdentifier());
        item.setValue(type.getShortDescription());
        item.setActionExpression(FacesContext.getCurrentInstance().getApplication().getExpressionFactory().
                createMethodExpression(FacesContext.getCurrentInstance().getELContext(), "#{jobsCreateMenuBean.navigateToCreateJob('" + type.getStringIdentifier() + "')}", String.class, new Class[]{String.class}));
        return item;
    }

    public UISubmenu getSubmenuBinding() {
        return this.jobsCreateSubmenu;
    }

    public void setSubmenuBinding(UISubmenu submenu) {
        this.jobsCreateSubmenu = submenu;
    }

    public String navigateToCreateJob(String uwsType) {
        //just to be sure check that it is one of possible types
        if (!possibleTypeIds.contains(uwsType)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "This type is no longer possible to invoke", ""));
            return null;
        }
        return "/jobs/create?faces-redirect=true&uwsType=" + uwsType;
    }
}
