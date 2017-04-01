package cz.ivoa.vocloud.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Calendar;

/**
 * Entity class that represents an authorization token saved in server memory. This
 * memory should be implemented as an EJB Singleton bean. It is not expected
 * to store these objects inside a persistent storage. The class should be
 * immutable. Every necessary parameter must be passed as constructor arguments.
 * Creation time argument is accessible but cannot be set.
 * <p>
 * Created by radiokoza on 1.4.17.
 */
public class AuthToken implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final SecureRandom RANDOM = new SecureRandom();

    private String username;
    private String token;
    private String serviceName;
    private Calendar created; //set automatically
    private int duration; //token duration in seconds - negative token means indefinitely

    public AuthToken(String username, String token, String serviceName, int duration) {
        if (username == null || token == null || serviceName == null) {
            throw new IllegalArgumentException("username, token and serviceName parameters must be specified");
        }
        this.username = username;
        this.token = token;
        this.serviceName = serviceName;
        this.created = Calendar.getInstance();
        this.duration = duration;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Calendar getCreated() {
        return created;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isExpired() {
        if (duration < 0) {
            return false;
        }
        Calendar curr = Calendar.getInstance();
        return curr.compareTo(created) / 1000 > duration;
    }

    public static String randomToken() {
        return new BigInteger(260, RANDOM).toString(32);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthToken)) return false;

        AuthToken authToken = (AuthToken) o;

        if (duration != authToken.duration) return false;
        if (!username.equals(authToken.username)) return false;
        if (!token.equals(authToken.token)) return false;
        if (!serviceName.equals(authToken.serviceName)) return false;
        return created.equals(authToken.created);
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + token.hashCode();
        result = 31 * result + serviceName.hashCode();
        result = 31 * result + created.hashCode();
        result = 31 * result + duration;
        return result;
    }
}
