package com.king.pos.ImplementServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;



import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailServiceImpl {

    @Autowired private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("kingproduct45@gmail.com"); // expéditeur
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            System.out.println("Email envoyé à : " + to);
        } catch (Exception e) {
            System.err.println("Erreur d'envoi d'email : " + e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email. Cause : " + e.getMessage(), e);
        }
    }



public void sendHtmlEmail(String to, String subject, String htmlContent) {
    try {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        helper.setFrom("kingproduct45@gmail.com");

        mailSender.send(mimeMessage);
        System.out.println("Email HTML envoyé à : " + to);
    } catch (MessagingException e) {
        throw new RuntimeException("Erreur d'envoi de mail HTML : " + e.getMessage(), e);
    }
}

   
}






