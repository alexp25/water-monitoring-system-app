package com.example.watermonitoringsystem.utils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import android.util.Log;

import com.example.watermonitoringsystem.authentication.ConfigMail;

/**
 * @author Ioan-Alexandru Chirita
 */
public class GMailSender {

    final String emailPort = "587";// gmail's smtp port
    final String smtpAuth = "true";
    final String starttls = "true";
    final String emailHost = "smtp.gmail.com";

    String toEmail;
    String emailSubject;
    String emailBody;

    Properties emailProperties;
    Session mailSession;
    MimeMessage emailMessage;


    public GMailSender(String toEmail, String emailSubject, String emailBody) {
        this.toEmail = toEmail;
        this.emailSubject = emailSubject;
        this.emailBody = emailBody;

        emailProperties = System.getProperties();
        emailProperties.put("mail.smtp.port", emailPort);
        emailProperties.put("mail.smtp.auth", smtpAuth);
        emailProperties.put("mail.smtp.starttls.enable", starttls);
        Log.i("GMail", "Mail server properties set.");
    }

    public void createEmailMessage() {
        try {
            mailSession = Session.getDefaultInstance(emailProperties, null);
            emailMessage = new MimeMessage(mailSession);

            emailMessage.setFrom(new InternetAddress(ConfigMail.CONFIG_APP_EMAIL, ConfigMail.CONFIG_APP_EMAIL));
            emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            emailMessage.setSubject(emailSubject);
            emailMessage.setText(emailBody);
            Log.i("GMail", "Email Message created.");
        } catch (UnsupportedEncodingException | MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendEmail() {

        Transport transport;
        try {
            transport = mailSession.getTransport("smtp");

            transport.connect(emailHost, ConfigMail.CONFIG_APP_EMAIL, ConfigMail.CONFIG_APP_PASSWORD);
            Log.i("GMail", "allrecipients: " + Arrays.toString(emailMessage.getAllRecipients()));
            transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
            transport.close();
            Log.i("GMail", "Email sent successfully.");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}