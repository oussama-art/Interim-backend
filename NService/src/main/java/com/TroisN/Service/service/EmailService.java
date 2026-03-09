package com.TroisN.Service.service;

import com.TroisN.Service.dto.CreatedAccountInfo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(List<String> recipients, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recipients.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("your_email@gmail.com");

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }


    public void sendAccountsCreatedEmail(
            String companyEmail,
            List<CreatedAccountInfo> accounts
    ) {

        StringBuilder content = new StringBuilder();
        content.append("Vos comptes ont été créés avec succès.\n\n");

        accounts.forEach(acc -> {
            content.append("Email: ")
                    .append(acc.getEmail())
                    .append("\nMot de passe: ")
                    .append(acc.getPassword())
                    .append("\n\n");
        });

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(companyEmail);
        message.setSubject("Création de vos comptes – 3N Service");
        message.setText(content.toString());

        mailSender.send(message);
    }

    public void sendNewAccountRequestNotification(
            List<String> adminEmails,
            String requesterName,
            String company,
            int requestedAccounts
    ) {

        String subject = "Nouvelle demande de création de compte – 3N Service";

        String html = """
        <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Nouvelle demande de création de compte</h2>
                <p><strong>Demandeur :</strong> %s</p>
                <p><strong>Société :</strong> %s</p>
                <p><strong>Nombre de comptes demandés :</strong> %d</p>
                <br/>
                <p>
                    Connectez-vous au panneau d’administration pour
                    examiner et traiter cette demande.
                </p>
            </body>
        </html>
        """.formatted(
                requesterName,
                company,
                requestedAccounts
        );

        sendEmail(adminEmails, subject, html);
    }

}
