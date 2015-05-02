package cz.rk.vocloud.view.exceptionhandler;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

/**
 *
 * @author radio.koza
 */
public class ViewExpiredExceptionHandler extends ExceptionHandlerWrapper {

    private static final Logger LOG = Logger.getLogger(ViewExpiredExceptionHandler.class.getName());

    private final ExceptionHandler wrapped;

    public ViewExpiredExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return this.wrapped;
    }

    @Override
    public void handle() throws FacesException {
        for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i.hasNext();) {
            ExceptionQueuedEvent event = i.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
            Throwable t = context.getException();
            if (t instanceof ViewExpiredException) {
                ViewExpiredException vee = (ViewExpiredException) t;
                LOG.log(Level.WARNING, "View Expired {0}", vee.getViewId());
                FacesContext fc = FacesContext.getCurrentInstance();
                NavigationHandler nav = fc.getApplication().getNavigationHandler();
                try {
                    String navigation = vee.getViewId();
                    if ("jobs/index.xhtml".equals(vee.getViewId())){
                        navigation += "?faces-redirect=true";
                    }
                    nav.handleNavigation(fc, null, navigation);
                    fc.renderResponse();

                } finally {
                    i.remove();
                }
            }
        }
        // At this point, the queue will not contain any ViewExpiredEvents.
        // Therefore, let the parent handle them.
        getWrapped().handle();

    }

}
