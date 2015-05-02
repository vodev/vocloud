package cz.mrq.vocloud.entity;

/**
 *
 * @author voadmin
 */
public enum Phase {

    CREATED,
    QUEUED,
    PENDING,
    EXECUTING,
    COMPLETED,
    ERROR,
    ABORTED,
    UNKNOWN,
    HELD,
    SUSPENDED,
    PROCESSING; // not standard phase, used for manager needs

    public static String getStr(Phase ph) {
        return (ph == null) ? Phase.UNKNOWN.name() : ph.name();
    }

    public static Phase getPhase(String phStr) {
        try {
            return valueOf(phStr);
        } catch (Exception ex) {
            return Phase.UNKNOWN;
        }

    }
}
