/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.utility;

import aes.model.Contact;
import java.io.ByteArrayOutputStream;
import java.util.Date;
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

/**
 *
 * @author hedersb
 */
public class EMailSSL {

    private Properties props;
    private Session session;
    private Authenticator authenticator;
    private String from;

    public EMailSSL() {
        props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        
        this.authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("acoolesaude", "forininhosenha");
            }
        };

        session = Session.getInstance(props, this.authenticator);

    }

    public void send(String to, String subject, String text, String html, ByteArrayOutputStream pdfAttachment, String attachmentName) {
        try {
            //Message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject(subject);
            message.setSentDate(new Date());

            MimeMultipart mainMultipart = new MimeMultipart("related");
            MimeMultipart htmlAndTextMultipart = new MimeMultipart("alternative");

            //Text
            if (text != null) {
                MimeBodyPart textBodyPart = new MimeBodyPart();
                textBodyPart.setText(text);
                htmlAndTextMultipart.addBodyPart(textBodyPart);
            }

            //HTML
            if (html != null) {
                MimeBodyPart htmlBodyPart = new MimeBodyPart();
                htmlBodyPart.setContent(html, "text/html");
                htmlAndTextMultipart.addBodyPart(htmlBodyPart);
            }

            MimeBodyPart htmlAndTextBodyPart = new MimeBodyPart();
            htmlAndTextBodyPart.setContent(htmlAndTextMultipart);
            mainMultipart.addBodyPart(htmlAndTextBodyPart);

            //PDF Attachment
            if (pdfAttachment != null) {
                MimeBodyPart pdfBodyPart = new MimeBodyPart();
                byte[] bytes = pdfAttachment.toByteArray();
                DataSource dataSource = new ByteArrayDataSource(bytes, "application/pdf");
                pdfBodyPart.setDataHandler(new DataHandler(dataSource));
                pdfBodyPart.setFileName(attachmentName);
                mainMultipart.addBodyPart(pdfBodyPart);
            }

            message.setContent(mainMultipart);

            Transport.send(message);

        } catch (MessagingException ex) {
            Logger.getLogger(EMailSSL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void send (Contact contact){
        send(contact.getFrom(), contact.getTo(), contact.getSubject(), 
            contact.getHtmlTemplate(), contact.getAttachment(), contact.getAttachmentName());
    }
    

    

    
}
