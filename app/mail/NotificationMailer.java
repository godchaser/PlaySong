package mail;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import play.Logger;
 
 
public class NotificationMailer {
 
    static Properties mailServerProperties;
    static Session getMailSession;
    static MimeMessage generateMailMessage;
 
    public static void generateAndSendEmail(String notification_mailer_username, String notification_mailer_password, String notification_mailer_smtp, String notification_mailer_port, String notification_mailer_recipient, String messageSubject, String messageBody) {
        if (notification_mailer_username!=null){
        // Step1
        Logger.debug("Creating notification mailer session");
        mailServerProperties = System.getProperties();
        mailServerProperties.put("mail.smtp.port", notification_mailer_port);
        mailServerProperties.put("mail.smtp.auth", "true");
        mailServerProperties.put("mail.smtp.starttls.enable", "true");
 
        // Step2
        getMailSession = Session.getDefaultInstance(mailServerProperties, null);
        generateMailMessage = new MimeMessage(getMailSession);
        try {
            generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(notification_mailer_recipient));
            generateMailMessage.setSubject("PlaySong song submitted by: " + messageSubject,  "utf-8" );
            String emailBody = "New song submitted in PlaySong <br><pre>" + messageBody + "</pre><br><br> Regards, <br>PlaySong server";
            generateMailMessage.setContent(emailBody, "text/html; charset=utf-8");
     
            // Step3
            Logger.debug("Notification mailer sending mail...");
            Transport transport = getMailSession.getTransport("smtp");
     
            // Enter your correct gmail UserID and Password
            transport.connect(notification_mailer_smtp, notification_mailer_username, notification_mailer_password);
            transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
            transport.close();
            Logger.info("Notification mailer has sent the email succesfully!");
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Logger.error("Notification mailer failed to send email: " + e.getLocalizedMessage());
        }
        }
        else {
            Logger.info("Notification mailer not correctly set, check env variables, or feature switch");
        }
    }

    public static void generateAndSendEmail(String messageSubject, String messageBody) {
        generateAndSendEmail(NoticationMailerConfig.getNotification_mailer_username(), NoticationMailerConfig.getNotification_mailer_password(), NoticationMailerConfig.getNotification_mailer_smtp(), NoticationMailerConfig.getNotification_mailer_smtp(), NoticationMailerConfig.getNotification_mailer_recipient(), messageSubject, messageBody);
    }
}