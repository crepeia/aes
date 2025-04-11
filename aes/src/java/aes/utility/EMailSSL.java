package aes.utility;

import com.sun.mail.util.MailSSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class EMailSSL {

    private Properties props;
    private Session session;
    private Authenticator authenticator;
    private Object senderOrRecipient;

    public EMailSSL() {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        props = new Properties();
        try {
            props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("aes/utility/mail.properties"));
            //MailSSLSocketFactory sf = new MailSSLSocketFactory();
            
            //sf.setTrustAllHosts(true);
            //props.put("mail.smtp.ssl.enable", "true");
            //props.put("mail.smtp.ssl.socketFactory", sf);
            this.authenticator = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                        (String)props.get("mail.auth.username"), 
                        (String)props.get("mail.auth.password"));
                }
            };
            this.senderOrRecipient = props.getProperty("mail.smtp.mailer");
        } catch (IOException ex) {
            Logger.getLogger(EMailSSL.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        session = Session.getInstance(props, this.authenticator);
        //session.setDebug(true);
    }

    public void send(String from, String to, String subject, String content, ByteArrayOutputStream pdf, String pdfName) throws MessagingException {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());            
           //Message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject(subject,"utf-8");
            message.setSentDate(new Date());

            MimeMultipart mainMultipart = new MimeMultipart("related");
            MimeMultipart htmlAndTextMultipart = new MimeMultipart("alternative");

            //CONTENT
            if (content != null) {
                MimeBodyPart htmlBodyPart = new MimeBodyPart();
                htmlBodyPart.setContent(content, "text/html;charset=UTF-8");
                htmlAndTextMultipart.addBodyPart(htmlBodyPart);
            }

            MimeBodyPart htmlAndTextBodyPart = new MimeBodyPart();
            htmlAndTextBodyPart.setContent(htmlAndTextMultipart,"charset=UTF-8");
            mainMultipart.addBodyPart(htmlAndTextBodyPart);

            //PDF
                if (pdf != null) {
                    MimeBodyPart pdfBodyPart = new MimeBodyPart();
                    byte[] bytes = pdf.toByteArray();
                    DataSource dataSource = new ByteArrayDataSource(bytes, "application/pdf");
                    pdfBodyPart.setDataHandler(new DataHandler(dataSource));
                    pdfBodyPart.setFileName(pdfName);
                    mainMultipart.addBodyPart(pdfBodyPart);
                }

            message.setContent(mainMultipart);

            Transport.send(message);

    }

    public String replaceEmail(String senderOrRecipient) {
        if(Objects.equals(this.senderOrRecipient, "")) {
            return senderOrRecipient;
        }
        return (String)this.senderOrRecipient;
    }  
}
