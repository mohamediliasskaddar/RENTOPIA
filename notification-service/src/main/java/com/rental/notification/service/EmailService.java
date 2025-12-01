package com.rental.notification.service;

import com.rental.notification.dto.EmailRequest;
import com.rental.notification.exception.NotificationException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    // ⭐ NOUVEAU : Récupère le nom d'expéditeur depuis application.yml
    @Value("${spring.mail.sender-name:Rentopia}")
    private String senderName;

    @Value("${spring.mail.username}")
    private String senderEmail;

    /**
     * Envoyer un email simple (texte brut) avec nom personnalisé
     */
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            // ⭐ CORRECTION : Format correct pour le nom d'expéditeur
            message.setFrom(String.format("%s <%s>", senderName, senderEmail));

            mailSender.send(message);
            log.info("Email simple envoyé à: {} de la part de: {}", to, senderName);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", to, e.getMessage());
            throw new NotificationException("Échec d'envoi de l'email: " + e.getMessage());
        }
    }

    /**
     * Envoyer un email HTML avec template et nom personnalisé
     */
    public void sendHtmlEmail(EmailRequest emailRequest) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(emailRequest.getTo());
            helper.setSubject(emailRequest.getSubject());

            // ⭐ CORRECTION : Utilise InternetAddress pour le nom personnalisé
            InternetAddress fromAddress = new InternetAddress(senderEmail, senderName);
            helper.setFrom(fromAddress);

            // Générer le contenu HTML depuis le template
            Context context = new Context();
            if (emailRequest.getTemplateVariables() != null) {
                context.setVariables(emailRequest.getTemplateVariables());
            }

            String htmlContent = templateEngine.process(
                    emailRequest.getTemplateName(),
                    context
            );

            helper.setText(htmlContent, true);

            // Optionnel : Ajouter un header personnalisé
            mimeMessage.setHeader("X-Sender-Name", senderName);
            mimeMessage.setHeader("X-Service", "Rentopia Notification Service");

            mailSender.send(mimeMessage);
            log.info("Email HTML envoyé à: {} de la part de: {} avec template: {}",
                    emailRequest.getTo(), senderName, emailRequest.getTemplateName());

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Erreur lors de l'envoi de l'email HTML à {}: {}",
                    emailRequest.getTo(), e.getMessage());
            throw new NotificationException("Échec d'envoi de l'email HTML: " + e.getMessage());
        }
    }

    /**
     * Envoyer email de confirmation de réservation avec design amélioré
     */
    public void sendBookingConfirmation(String to, String guestName,
                                        String propertyTitle, String checkIn,
                                        String checkOut, Double totalAmount,
                                        String bookingReference) {

        EmailRequest emailRequest = EmailRequest.builder()
                .to(to)
                .subject("🎉 Confirmation de réservation - " + propertyTitle)
                .templateName("booking-confirmation")
                .templateVariables(java.util.Map.of(
                        "guestName", guestName,
                        "propertyTitle", propertyTitle,
                        "checkInDate", checkIn,
                        "checkOutDate", checkOut,
                        "totalAmount", String.format("%.2f €", totalAmount),
                        "bookingReference", bookingReference,
                        "senderName", senderName, // ⭐ Inclut le nom dans le template
                        "currentYear", java.time.Year.now().getValue()
                ))
                .build();

        sendHtmlEmail(emailRequest);
        log.info("Email de confirmation envoyé à {} pour la réservation {}",
                guestName, bookingReference);
    }

    /**
     * Méthode utilitaire pour formater l'adresse d'expéditeur
     */
    private String formatSenderAddress() {
        return String.format("\"%s\" <%s>", senderName, senderEmail);
    }

    /**
     * Vérifier la configuration email
     */
    public void testEmailConfiguration() {
        try {
            SimpleMailMessage testMessage = new SimpleMailMessage();
            testMessage.setFrom(formatSenderAddress());
            testMessage.setTo(senderEmail); // S'envoyer un test
            testMessage.setSubject("✅ Test configuration Rentopia Email");
            testMessage.setText(
                    "Ceci est un test de configuration.\n\n" +
                            "Nom d'expéditeur: " + senderName + "\n" +
                            "Email: " + senderEmail + "\n" +
                            "Date: " + java.time.LocalDateTime.now()
            );

            mailSender.send(testMessage);
            log.info("✅ Test email envoyé avec succès. Nom d'expéditeur: {}", senderName);

        } catch (Exception e) {
            log.error("❌ Échec du test email: {}", e.getMessage());
            throw new NotificationException("Test de configuration email échoué: " + e.getMessage());
        }
    }
}