package cz.ivoa.vocloud.view;

import cz.ivoa.vocloud.ejb.UWSFacade;
import cz.ivoa.vocloud.ejb.UWSTypeFacade;
import cz.ivoa.vocloud.ejb.WorkerFacade;
import cz.ivoa.vocloud.entity.UWS;
import cz.ivoa.vocloud.entity.UWSType;
import cz.ivoa.vocloud.entity.Worker;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.SessionScoped;

@ManagedBean(name = "uWSController")
@SessionScoped
public class UWSController implements Serializable {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("/cz/ivoa/vocloud/Bundle");
    private static final Logger LOG = Logger.getLogger(UWSController.class.getName());

    @EJB
    private WorkerFacade workerFacade;
    @EJB
    private UWSTypeFacade uwsTypeFacade;
    @EJB
    private UWSFacade uwsFacade;

    //lists
    private List<Worker> workers;
    private List<UWSType> uwsTypes;
    private List<UWS> uwss;

    //helping variables for creation of UWS
    private int selectedUwsType;
    private int selectedWorker;

    //currently editing entities
    private Worker editingWorker;
    private UWSType editingUwsType;
    private UWS editingUws;

    @PostConstruct
    private void init() {
        refresh();//fetch lists from facades
    }

    private void refresh() {
        workers = workerFacade.findAllOrderedById();
        uwsTypes = uwsTypeFacade.findAllOrderedByIdentifier();
        uwss = uwsFacade.findAll();
    }

    //=============================getters for lists============================
    public List<Worker> getWorkers() {
        return workers;
    }

    public List<UWS> getUwss() {
        return uwss;
    }

    public List<UWSType> getUwsTypes() {
        return uwsTypes;
    }

    //=====================getters and setters for helping entities=============
    public int getSelectedUwsType() {
        return selectedUwsType;
    }

    public void setSelectedUwsType(int selectedUwsType) {
        this.selectedUwsType = selectedUwsType;
    }

    public int getSelectedWorker() {
        return selectedWorker;
    }

    public void setSelectedWorker(int selectedWorker) {
        this.selectedWorker = selectedWorker;
    }

    //==========================getters for current editing entities============
    public Worker getEditingWorker() {
        if (editingWorker == null) {
            editingWorker = new Worker();
        }
        return editingWorker;
    }

    public UWSType getEditingUwsType() {
        if (editingUwsType == null) {
            editingUwsType = new UWSType();
        }
        return editingUwsType;
    }

    public UWS getEditingUws() {
        if (editingUws == null) {
            editingUws = new UWS();
        }
        return editingUws;
    }

    //==============================create new methods==========================
    public String uwsTypeCreateNew() {
        editingUwsType = null;
        return "create-uws-type?faces-redirect=true";
    }

    public String workerCreateNew() {
        editingWorker = null;
        return "create-worker?faces-redirect=true";
    }

    public String uwsCreateNew() {
        editingUws = null;
        if (workers.isEmpty() && uwsTypes.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "You must first create UWS Type and Worker"));
        } else if (workers.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "You must first create Worker"));
        } else if (uwsTypes.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "You must first create UWS Type"));
        } else {
            return "create-uws?faces-redirect=true";
        }
        return null;
    }

    //===============================editing methods=====================
    public String createEditingWorker() {
        try {
            workerFacade.create(editingWorker);
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Worker creation failed"));
            LOG.log(Level.SEVERE, null, ex);
            return null;
        }
        editingWorker = null;
        refresh();
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "New Worker was successfully created"));
        return "list?faces-redirect=true";
    }

    public String updateEditingWorker() {
        try {
            workerFacade.edit(editingWorker);
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Worker update failed"));
            LOG.log(Level.SEVERE, null, ex);
            return null;
        }
        editingWorker = null;
        refresh();
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Worker was successfully updated"));
        return "list?faces-redirect=true";
    }

    public String createEditingUwsType() {
        try {
            uwsTypeFacade.create(editingUwsType);
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "UWS Type creation failed"));
            LOG.log(Level.SEVERE, null, ex);
            return null;
        }
        editingUwsType = null;
        refresh();
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "New UWS Type was successfully created"));
        return "list?faces-redirect=true";
    }

    public String updateEditingUwsType() {
        try {
            uwsTypeFacade.edit(editingUwsType);
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "UWS Type update failed"));
            LOG.log(Level.SEVERE, null, ex);
            return null;
        }
        editingUwsType = null;
        refresh();
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "UWS Type was successfully updated"));
        return "list?faces-redirect=true";
    }

    public String createEditingUws() {
        try {
            uwsFacade.createWithAssignedRelations(editingUws, selectedUwsType, selectedWorker);
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "UWS creation failed"));
            LOG.log(Level.SEVERE, null, ex);
            return null;
        }
        editingUws = null;
        refresh();
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "New UWS was successfully created"));
        return "list?faces-redirect=true";
    }

    public String updateEditingUws() {
        try {
            uwsFacade.editWithAssignedRelations(editingUws, selectedUwsType, selectedWorker);
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "UWS update failed"));
            LOG.log(Level.SEVERE, null, ex);
            return null;
        }
        editingUws = null;
        refresh();
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "UWS was successfully updated"));
        return "list?faces-redirect=true";
    }

    //================================Worker manipulation methods===============
    public String workerView(Worker item) {
        editingWorker = item;
        return "view-worker?faces-redirect=true";
    }

    public String workerEdit(Worker item) {
        editingWorker = item;
        return "edit-worker?faces-redirect=true";
    }

    public String workerDestroy(Worker item) {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        try {
            workerFacade.remove(item);
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Worker deletion failed - delete its UWSs first"));
            editingWorker = null;
            return "list?faces-redirect=true";
        }
        refresh();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Worker " + item.getShortDescription() + " was deleted"));
        editingWorker = null;
        return "list?faces-redirect=true";
    }

    //================================UWS Type manipulation methods=============
    public String uwsTypeView(UWSType item) {
        editingUwsType = item;
        return "view-uws-type?faces-redirect=true";
    }

    public String uwsTypeEdit(UWSType item) {
        editingUwsType = item;
        return "edit-uws-type?faces-redirect=true";
    }

    public String uwsTypeDestroy(UWSType item) {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        try {
            uwsTypeFacade.remove(item);
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "UWS Type deletion failed - delete its UWSs first"));
            editingUwsType = null;
            return "list?faces-redirect=true";
        }
        refresh();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "UWS Type " + item.getShortDescription() + " was deleted"));
        editingUwsType = null;
        return "list?faces-redirect=true";
    }

    //================================UWS manipulation methods=============
    public String uwsView(UWS item) {
        editingUws = item;
        return "view-uws?faces-redirect=true";
    }

    public String uwsEdit(UWS item) {
        editingUws = item;
        selectedUwsType = item.getUwsType().getId();
        selectedWorker = item.getWorker().getId();
        return "edit-uws?faces-redirect=true";
    }

    public String uwsDestroy(UWS item) {
        uwsFacade.remove(item);
        refresh();
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "UWS with ID " + item.getId() + " was deleted"));
        editingUws = null;
        return "list?faces-redirect=true";
    }

//    public PaginationHelper getPagination() {
//        if (pagination == null) {
//            pagination = new PaginationHelper(10) {
//
//                @Override
//                public int getItemsCount() {
//                    return getFacade().count();
//                }
//
//                @Override
//                public DataModel createPageDataModel() {
//                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
//                }
//            };
//        }
//        return pagination;
//    }
//
//    public String prepareList() {
//        recreateModel();
//        return "List";
//    }
//
//    public String prepareView() {
//        current = (UWS) getItems().getRowData();
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
//        return "View";
//    }
//
//    public String prepareCreate() {
//        current = new UWS();
//        selectedItemIndex = -1;
//        return "Create";
//    }
//
//    public String create() {
//        try {
//            getFacade().create(current);
//            JsfUtil.addSuccessMessage(bundle.getString("UWSCreated"));
//            return prepareCreate();
//        } catch (Exception e) {
//            JsfUtil.addErrorMessage(e, bundle.getString("PersistenceErrorOccured"));
//            return null;
//        }
//    }
//
//    public String prepareEdit() {
//        current = (UWS) getItems().getRowData();
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
//        return "Edit";
//    }
//
//    public String update() {
//        try {
//            getFacade().edit(current);
//            JsfUtil.addSuccessMessage(bundle.getString("UWSUpdated"));
//            return "View";
//        } catch (Exception e) {
//            JsfUtil.addErrorMessage(e, bundle.getString("PersistenceErrorOccured"));
//            return null;
//        }
//    }
//
//    public String destroy() {
//        current = (UWS) getItems().getRowData();
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
//        performDestroy();
//        recreatePagination();
//        recreateModel();
//        return "List";
//    }
//
//    public String destroyAndView() {
//        performDestroy();
//        recreateModel();
//        updateCurrentItem();
//        if (selectedItemIndex >= 0) {
//            return "View";
//        } else {
//            // all items were removed - go back to list
//            recreateModel();
//            return "List";
//        }
//    }
//
//    private void performDestroy() {
//        try {
//            getFacade().remove(current);
//            JsfUtil.addSuccessMessage(bundle.getString("UWSDeleted"));
//        } catch (Exception e) {
//            JsfUtil.addErrorMessage(e, bundle.getString("PersistenceErrorOccured"));
//        }
//    }
//
//    private void updateCurrentItem() {
//        int count = getFacade().count();
//        if (selectedItemIndex >= count) {
//            // selected index cannot be bigger than number of items:
//            selectedItemIndex = count - 1;
//            // go to previous page if last page disappeared:
//            if (pagination.getPageFirstItem() >= count) {
//                pagination.previousPage();
//            }
//        }
//        if (selectedItemIndex >= 0) {
//            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
//        }
//    }
//
//    public DataModel getItems() {
//        if (items == null) {
//            items = getPagination().createPageDataModel();
//        }
//        return items;
//    }
//
//    private void recreateModel() {
//        items = null;
//    }
//
//    private void recreatePagination() {
//        pagination = null;
//    }
//
//    public String next() {
//        getPagination().nextPage();
//        recreateModel();
//        return "List";
//    }
//
//    public String previous() {
//        getPagination().previousPage();
//        recreateModel();
//        return "List";
//    }
//
//    public SelectItem[] getItemsAvailableSelectMany() {
//        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
//    }
//
//    public SelectItem[] getItemsAvailableSelectOne() {
//        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
//    }
//
//    @FacesConverter(forClass = UWS.class)
//    public static class UWSControllerConverter implements Converter {
//
//        @Override
//        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
//            if (value == null || value.length() == 0) {
//                return null;
//            }
//            UWSController controller = (UWSController) facesContext.getApplication().getELResolver().
//                    getValue(facesContext.getELContext(), null, "uWSController");
//            return controller.ejbFacade.find(getKey(value));
//        }
//
//        java.lang.Integer getKey(String value) {
//            java.lang.Integer key;
//            key = Integer.valueOf(value);
//            return key;
//        }
//
//        String getStringKey(java.lang.Integer value) {
//            StringBuilder sb = new StringBuilder();
//            sb.append(value);
//            return sb.toString();
//        }
//
//        @Override
//        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
//            if (object == null) {
//                return null;
//            }
//            if (object instanceof UWS) {
//                UWS o = (UWS) object;
//                return getStringKey(o.getId());
//            } else {
//                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + UWSController.class.getName());
//            }
//        }
//    }
}
