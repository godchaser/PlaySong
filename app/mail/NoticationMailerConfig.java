package mail;

public class NoticationMailerConfig {
    
	private static boolean notification_mailer_enabled = false;
	private static String notification_mailer_username =  null;
    private static String notification_mailer_password = null;
    private static String notification_mailer_smtp = null;
    private static String notification_mailer_port = null;
    private static String notification_mailer_recipient = null;
    
    public static boolean isNotification_mailer_enabled() {
		return notification_mailer_enabled;
	}
	public static void setNotification_mailer_enabled(boolean notification_mailer_enabled) {
		NoticationMailerConfig.notification_mailer_enabled = notification_mailer_enabled;
	}
   
    public static String getNotification_mailer_username() {
        return notification_mailer_username;
    }
    public static void setNotification_mailer_username(String notification_mailer_username) {
        NoticationMailerConfig.notification_mailer_username = notification_mailer_username;
    }
    public static String getNotification_mailer_password() {
        return notification_mailer_password;
    }
    public static void setNotification_mailer_password(String notification_mailer_password) {
        NoticationMailerConfig.notification_mailer_password = notification_mailer_password;
    }
    public static String getNotification_mailer_smtp() {
        return notification_mailer_smtp;
    }
    public static void setNotification_mailer_smtp(String notification_mailer_smtp) {
        NoticationMailerConfig.notification_mailer_smtp = notification_mailer_smtp;
    }
    public static String getNotification_mailer_port() {
        return notification_mailer_port;
    }
    public static void setNotification_mailer_port(String notification_mailer_port) {
        NoticationMailerConfig.notification_mailer_port = notification_mailer_port;
    }
    public static String getNotification_mailer_recipient() {
        return notification_mailer_recipient;
    }
    public static void setNotification_mailer_recipient(String notification_mailer_recipient) {
        NoticationMailerConfig.notification_mailer_recipient = notification_mailer_recipient;
    }
    
}
