package cz.ivoa.vocloud.utils;

import cz.ivoa.vocloud.entity.UserAccount;
import cz.ivoa.vocloud.entity.UserGroupName;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

/**
 *
 * @author radio.koza
 */
public class DatabaseUtils {

    private final static EntityManagerFactory emf;

    static {
        emf = Persistence.createEntityManagerFactory("VocloudTestPU");
    }

    public static void createUser(String username, String password, UserGroupName group) {
        final EntityManager em = emf.createEntityManager();
        try {
            UserAccount acc = new UserAccount();
            acc.setSince(new Date());
            acc.setEmail(username.toLowerCase() + "@test.cz");
            acc.setEnabled(true);
            acc.setUsername(username);
            acc.setFirstName("Tester");
            acc.setLastName(username);
            acc.setRegisteredIp("127.0.0.1");
            acc.setGroupName(group);
            acc.setPass(password);
            em.getTransaction().begin();
            em.persist(acc);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public static void deleteUser(String username) {
        final EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<UserAccount> q = em.createNamedQuery("UserAccount.findByUsername", UserAccount.class);
            q.setParameter("username", username);
            UserAccount acc;
            try {
                acc = q.getSingleResult();
            } catch (NoResultException ex) {
                Logger.getLogger(DatabaseUtils.class.getName()).log(Level.WARNING, "Unable to find user to delete: {0}", username);
                return;
            }
            em.remove(acc);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

}
