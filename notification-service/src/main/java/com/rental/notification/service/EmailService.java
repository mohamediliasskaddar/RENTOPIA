package com.rental.notification.service;

import com.rental.notification.dto.EmailRequest;
import com.rental.notification.exception.NotificationException;
import com.rental.notification.enums.NotificationType; // AJOUTER CET IMPORT
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
import java.util.HashMap; // AJOUTER
import java.util.Map; // AJOUTER

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

        Map<String, Object> variables = new HashMap<>();
        variables.put("guestName", guestName);
        variables.put("propertyTitle", propertyTitle);
        variables.put("checkInDate", checkIn);
        variables.put("checkOutDate", checkOut);
        variables.put("totalAmount", String.format("%.2f €", totalAmount));
        variables.put("bookingReference", bookingReference);
        variables.put("senderName", senderName);
        variables.put("currentYear", java.time.Year.now().getValue());

        EmailRequest emailRequest = EmailRequest.builder()
                .to(to)
                .subject("🎉 Confirmation de réservation - " + propertyTitle)
                .templateName("booking-confirmation")
                .templateVariables(variables)
                .build();

        sendHtmlEmail(emailRequest);
        log.info("Email de confirmation envoyé à {} pour la réservation {}",
                guestName, bookingReference);
    }

    // AJOUTER LES NOUVELLES MÉTHODES ICI

    /**
     * Envoyer email de vérification (pour User Service)
     */
    public void sendVerificationEmail(String email, String verificationToken) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("verificationToken", verificationToken);
        variables.put("email", email);

        EmailRequest emailRequest = EmailRequest.builder()
                .to(email)
                .subject("Vérifiez votre email - Rentopia")
                .templateName("email-verification")
                .templateVariables(variables)
                .build();

        sendHtmlEmail(emailRequest);
        log.info("✅ Verification email sent to: {}", email);
    }

    /**
     * Envoyer notification générique basée sur le type
     */
    public void sendNotificationEmail(String email, String title, String message,
                                      NotificationType type, Map<String, Object> variables) {

        String templateName = determineTemplateName(type);

        if (templateName != null) {
            // Utiliser template HTML
            Map<String, Object> allVariables = new HashMap<>(variables);
            allVariables.put("title", title);
            allVariables.put("message", message);

            EmailRequest emailRequest = EmailRequest.builder()
                    .to(email)
                    .subject(title)
                    .templateName(templateName)
                    .templateVariables(allVariables)
                    .build();

            sendHtmlEmail(emailRequest);
        } else {
            // Fallback: email simple
            sendSimpleEmail(email, title, message);
        }

        log.info("✅ {} email sent to: {}", type, email);
    }

    private String determineTemplateName(NotificationType type) {
        // Même logique que dans EmailVerificationConsumer
        switch (type) {
            case BOOKING_CONFIRMATION: return "booking-confirmation";
            case BOOKING_CANCELLED: return "booking-cancelled";
            case PAYMENT_RECEIVED: return "payment-received";
            case PAYMENT_FAILED: return "payment-failed";
            case REVIEW_REQUEST: return "review-request";
            case BOOKING_REQUEST_RECEIVED: return "booking-request-received";
            case BOOKING_REQUEST_ACCEPTED: return "booking-request-accepted";
            case BOOKING_REQUEST_DECLINED: return "booking-request-declined";
            case CHECK_IN_REMINDER: return "checkin-reminder";
            case CHECK_OUT_REMINDER: return "checkout-reminder";
            default: return null;
        }
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