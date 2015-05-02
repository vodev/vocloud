package cz.rk.vocloud.view.exceptionhandler;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

/**
 *
 * @author radio.koza
 */
public class ViewExpiredExceptionExceptionHandlerFactory extends ExceptionHandlerFactory{

    
    private final ExceptionHandlerFactory parent;
 
    public ViewExpiredExceptionExceptionHandlerFactory(ExceptionHandlerFactory parent) {
        this.parent = parent;
    }

    
    @Override
    public ExceptionHandler getExceptionHandler() {
        //wrap the exception handler in new one
        ExceptionHandler result = parent.getExceptionHandler();
        result = new ViewExpiredExceptionHandler(result);
 
        return result;

    }
    
}
