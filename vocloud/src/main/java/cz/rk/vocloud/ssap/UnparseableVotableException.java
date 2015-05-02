package cz.rk.vocloud.ssap;

/**
 *
 * @author radio.koza
 */
public class UnparseableVotableException extends Exception {

    public UnparseableVotableException() {
        super("Unable to parse VOTABLE");
    }

    public UnparseableVotableException(String message) {
        super(message);
    }

    public UnparseableVotableException(Throwable exception) {
        super(exception);
    }

}
