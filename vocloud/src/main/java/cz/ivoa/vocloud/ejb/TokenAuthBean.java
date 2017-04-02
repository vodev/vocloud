package cz.ivoa.vocloud.ejb;

import cz.ivoa.vocloud.entity.AuthToken;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.ejb.Timer;
import java.util.*;

/**
 * Created by radiokoza on 1.4.17.
 */
@Singleton
@LocalBean
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@TransactionManagement(TransactionManagementType.BEAN)
public class TokenAuthBean {
    private static final int CLEANSE_TIMEOUT = 5000; //timeout in milliseconds
    public static final int DEFAULT_DURATION = 300; //token implicit duration in seconds
    // actual token storage
    private List<AuthToken> tokenList = new ArrayList<>();
    // index to token storage where key is index value and value is a current index inside token storage list
    private Map<String, Integer> indexToken = new HashMap<>();

    @Resource
    private TimerService timerService;

    @SuppressWarnings("unused")
    @Timeout
    @Lock(LockType.WRITE)
    private void cleanseExpiredTokens(Timer timer) {
        // cancel all other queued timers
        for (Timer t : timerService.getTimers()) {
            try {
                t.cancel();
            } catch (NoSuchObjectLocalException ex) {
                //do nothing
            }
        }
        Iterator<AuthToken> it = tokenList.iterator();
        while (it.hasNext()) {
            AuthToken token = it.next();
            if (token.isExpired()) {
                it.remove();
                indexToken.remove(token.getToken());
            }
        }
    }


    /**
     * Removes the specified token object from the storage.
     *
     * @param tokenObj <code>{@link AuthToken}</code> object instance to be removed.
     */
    @Lock(LockType.WRITE)
    public void removeToken(AuthToken tokenObj) {
        System.out.println("token removal body");
        if (tokenObj == null) {
            throw new IllegalArgumentException("tokenObj must not be null");
        }
        Integer index = indexToken.get(tokenObj.getToken());
        if (index == null) {
            return; //token is not in storage
        }
        //removal
        indexToken.remove(tokenObj.getToken());
        tokenList.remove(index);
    }

    /**
     * Create programmatic timer that after the specified interval clears all
     * expired tokens from the storage. This method should be used exclusively
     * to remove expired tokens.
     */
    @Lock(LockType.READ)
    private void scheduleCleanse() {
        timerService.createSingleActionTimer(CLEANSE_TIMEOUT, new TimerConfig(null, false));
    }

    /**
     * Private method. This method is capable of searching token by the passed identifier.
     * It is expected that this identifier is valid, because the index comes from the index
     * maps that should be consistent with the token list storage at any time.
     *
     * @param index Index to the token inside the storage token list. Can be <code>null</code>.
     * @return <code>{@link AuthToken}</code> object instance of the found token or <code>null</code>
     * if no such token was found.
     */
    @Lock(LockType.READ)
    private AuthToken findByIndexValue(Integer index) {
        if (index == null) {
            return null;
        }
        AuthToken token = tokenList.get(index);
        if (token.isExpired()) {
            scheduleCleanse();
            return null;//token is expired and therefore is marked as not found
        }
        return token;
    }

    /**
     * Tries to find the token in the storage by token value.
     *
     * @param token Value of the token (long string token representation).
     * @return <code>{@link AuthToken}</code> instance of the found token or <code>null</code>
     * if the token could not be found.
     */
    @Lock(LockType.READ)
    public AuthToken findByToken(String token) {
        return findByIndexValue(indexToken.get(token));
    }

    /**
     * Finds the token by the token value and consumes it - removes it from the storage.
     *
     * @param token Value of the token.
     * @return <code>{@link AuthToken}</code> instance of the found and consumed token or <code>null</code>
     * if no such token was found.
     */
    @Lock(LockType.WRITE)
    public AuthToken consumeToken(String token) {
        AuthToken tokenObj = findByToken(token);
        if (tokenObj == null) {
            return null;
        }
        //remove the token
        System.out.println("calling remove token");
        removeToken(tokenObj);
        System.out.println("end calling");
        return tokenObj;
    }


    /**
     * Same functionality as {@link #generateToken(String, String, int)} but uses implicit duration
     * {@link #DEFAULT_DURATION}.
     *
     * @param username Username assigned to the token.
     * @param service  Service name assigned to the token.
     * @return String representation of the token value.
     */
    @Lock(LockType.WRITE)
    public String generateToken(String username, String service) {
        return generateToken(username, service, DEFAULT_DURATION);
    }

    /**
     * Creates new token with the specified username and service name. This token expires in <code>duration</code>
     * seconds. Note that token values are unique whereas username and services may have more tokens.
     *
     * @param username Username assigned to the token.
     * @param service  Service name assigned to the token.
     * @param duration Expiration time of token in seconds.
     * @return String representation of the newly created token.
     */
    @Lock(LockType.WRITE)
    public String generateToken(String username, String service, int duration) {
        //check parameters
        if (username == null || service == null) {
            throw new IllegalArgumentException("username and service parameters must not be null");
        }
        String value;
        do {
            value = AuthToken.randomToken();
        } while (indexToken.containsKey(value)); // just to be sure that token is unique
        AuthToken token = new AuthToken(username, value, service, duration);
        int index = tokenList.size();
        tokenList.add(token);
        indexToken.put(value, index);
        //schedule expiration cleanse
        scheduleCleanse();
        //return token value
        return value;
    }

}
