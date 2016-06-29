package cz.ivoa.vocloud.filesystem.exception;

/**
 * Created by radiokoza on 29.6.16.
 */
public class IllegalPathException extends Exception {
    public IllegalPathException(Throwable ex){
        super(ex);
    }

    public IllegalPathException(String message){
        super(message);
    }

    public IllegalPathException(){
        super("Passed path is invalid");
    }
}
