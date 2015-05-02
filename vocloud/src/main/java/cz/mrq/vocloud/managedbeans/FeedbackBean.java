package cz.mrq.vocloud.managedbeans;

import cz.mrq.vocloud.ejb.UserAccountFacade;
import cz.mrq.vocloud.entity.UserAccount;
import cz.mrq.vocloud.tools.Config;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.Serializable;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 *
 * @author voadmin
 */
@Named
@RequestScoped
public class FeedbackBean implements Serializable {

    private static final Logger logger = Logger.getLogger(UserAccountFacade.class.getName());

    @EJB
    private UserAccountFacade uaf;

    @Resource(lookup = "java:jboss/mail/vocloud-mail")
    private Session mailSession;
    @Inject
    @Config
    private String feedbackEmail;

    private String name;
    private String email;
    private String topic;
    private String message;
    private UserAccount user;

    public FeedbackBean() {
    }

    @PostConstruct
    private void init() {
        String username = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
        if (username != null) {
            this.user = uaf.findByUsername(username);
            this.name = user.getUsername();
            this.email = user.getEmail();
        }
    }

    public String send() {
        Message emailMessage = new MimeMessage(mailSession);
        try {
            emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(feedbackEmail));
            emailMessage.setFrom();
            emailMessage.setSubject("vo-korel: Feedback: " + topic);
            StringBuilder sb = new StringBuilder();
            sb.append("MESSAGE\n");
            sb.append(message);
            sb.append("\n");
            sb.append("\nINFO\n");
            sb.append("Time: ").append(new Date()).append('\n');
            sb.append("Name: ").append(name).append('\n');
            sb.append("Email: ").append(email).append('\n');
            sb.append("Topic: ").append(topic).append('\n');

            if (user != null) {
                sb.append("\nUSER INFO\n");
                sb.append("Id: ").append(user.getId()).append('\n');
                sb.append("Username: ").append(user.getUsername()).append('\n');
                sb.append("Email: ").append(user.getEmail()).append('\n');
            }

            emailMessage.setText(sb.toString());
            emailMessage.setHeader("X-Mailer", "My Mailer");
            Transport.send(emailMessage);
            logger.log(Level.INFO, "Feedback email has been sent.");

            
            //set flash to keep messages during redirect
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Thank you for your feedback!", "Your message has been sent to the administrator."));
            return "index/faces-redirect=true";

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "error when sending feedback email", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Feedback sending failed"));
            return null;
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

}
