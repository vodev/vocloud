package cz.mrq.vocloud.ejb;

import cz.mrq.vocloud.entity.UserAccount;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.NoResultException;

/**
 *
 * @author voadmin
 */
@Stateless
@LocalBean
public class UserAccountFacade extends AbstractFacade<UserAccount> {

    private static final Logger logger = Logger.getLogger(UserAccountFacade.class.getName());

    @PersistenceContext(unitName = "vokorelPU")
    private EntityManager em;

    @Resource(name = "vokorel-mail")
    private Session mailSession;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UserAccountFacade() {
        super(UserAccount.class);
    }

    @Override
    public void create(UserAccount entity) {
        entity.setSince(new Date());
        super.create(entity);
    }

    public void resetPassword(UserAccount entity) {

        String newPass = generatePassword();

        // sent email with pass
        Message message = new MimeMessage(mailSession);
        try {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(entity.getEmail()));
            message.setFrom();
            message.setSubject("vo-korel: Password reset");
            message.setText("Your password for account " + entity.getUsername() + " was changed to: " + newPass);
            message.setHeader("X-Mailer", "My Mailer");
            Transport.send(message);
            logger.log(Level.INFO, "Email with password has been sent to the user.");
            changePassword(entity, newPass);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "failed to reset pass", ex);
        }
    }

    public void changePassword(UserAccount user, String password) {
        user.setPass(password);
        edit(user);
    }

    public Boolean resetPassword(String email) {
        UserAccount user = findByEmail(email);
        if (user == null) {
            return false;
        }
        resetPassword(user);
        return true;
    }

    public UserAccount findByEmail(String email) {
        if (email == null) {
            return null;
        }
        UserAccount user = null;
        Query q = getEntityManager().createNamedQuery("UserAccount.findByEmail");
        q.setParameter("email", email);
        try {
            user = (UserAccount) q.getSingleResult();
        } catch (NoResultException ex) {
            //nothing to do here
        } catch (PersistenceException pe) {
            logger.log(Level.WARNING, "query failed: {0} for email {1}", new Object[]{pe.toString(), email});
        }
        return user;
    }

    public UserAccount findByUsername(String username) {
        if (username == null) {
            return null;
        }
        UserAccount user = null;
        Query q = getEntityManager().createNamedQuery("UserAccount.findByUsername");
        q.setParameter("username", username);
        try {
            user = (UserAccount) q.getSingleResult();
        } catch (NoResultException ex) {
            //nothing to do here
        } catch (PersistenceException pe) {
            logger.log(Level.WARNING, "query failed: {0} for username {1}", new Object[]{pe.toString(), username});
        }
        return user;
    }

    private String generatePassword() {
        Random rng = new Random();
        int length = 6;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }

}
