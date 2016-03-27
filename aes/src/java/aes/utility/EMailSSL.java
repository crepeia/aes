package aes.utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

public class EMailSSL {

    private Properties props;
    private Session session;
    private Authenticator authenticator;

    public EMailSSL() {
        props = new Properties();      
        try {
            props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("aes/utility/mail.properties"));
            this.authenticator = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                        (String)props.get("mail.auth.username"), 
                        (String)props.get("mail.auth.password"));
                }
            };
        } catch (IOException ex) {
            Logger.getLogger(EMailSSL.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        session = Session.getInstance(props, this.authenticator);

    }

    public void send(String from, String to, String subject, String content, ByteArrayOutputStream pdf, String pdfName) throws MessagingException {
            //Message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject(subject);
            message.setSentDate(new Date());

            MimeMultipart mainMultipart = new MimeMultipart("related");
            MimeMultipart htmlAndTextMultipart = new MimeMultipart("alternative");

            //CONTENT
            if (content != null) {
                MimeBodyPart htmlBodyPart = new MimeBodyPart();
                htmlBodyPart.setContent(content, "text/html");
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
       
}
