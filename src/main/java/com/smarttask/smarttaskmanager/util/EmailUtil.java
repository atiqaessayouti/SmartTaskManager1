package com.smarttask.smarttaskmanager.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailUtil {


    private static final String MY_EMAIL = "atiqaessayouti@gmail.com";


    private static final String MY_PASSWORD = "vebp eywz lxqe xjez";

    public static void sendEmail(String recipient, String subject, String content) throws Exception {
        System.out.println("🚀 Préparation de l'envoi de l'email à " + recipient + "...");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MY_EMAIL, MY_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(MY_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);
        message.setText(content);

        Transport.send(message);
        System.out.println("✅ Email envoyé avec succès !");
    }
}