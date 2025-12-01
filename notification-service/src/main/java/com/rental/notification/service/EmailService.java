package com.rental.notification.service;

import com.rental.notification.dto.EmailRequest;
import com.rental.notification.exception.NotificationException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    /**
     * Envoyer un email simple (texte brut)
     */
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@rentalhub.com");

            mailSender.send(message);
            log.info("Email simple envoyé à: {}", to);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", to, e.getMessage());
            throw new NotificationException("Échec d'envoi de l'email: " + e.getMessage());
        }
    }

    /**
     * Envoyer un email HTML avec template
     */
    public void sendHtmlEmail(EmailRequest emailRequest) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(emailRequest.getTo());
            helper.setSubject(emailRequest.getSubject());
            helper.setFrom("noreply@rentalhub.com");

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

            mailSender.send(mimeMessage);
            log.info("Email HTML envoyé à: {} avec template: {}",
                    emailRequest.getTo(), emailRequest.getTemplateName());

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email HTML à {}: {}",
                    emailRequest.getTo(), e.getMessage());
            throw new NotificationException("Échec d'envoi de l'email HTML: " + e.getMessage());
        }
    }

    /**
     * Envoyer email de confirmation de réservation
     */
    public void sendBookingConfirmation(String to, String guestName,
                                        String propertyTitle, String checkIn,
                                        String checkOut, Double totalAmount) {
        EmailRequest emailRequest = EmailRequest.builder()
                .to(to)
                .subject("Confirmation de réservation - " + propertyTitle)
                .templateName("booking-confirmation")
                .templateVariables(java.util.Map.of(
                        "guestName", guestName,
                        "propertyTitle", propertyTitle,
                        "checkInDate", checkIn,
                        "checkOutDate", checkOut,
                        "totalAmount", totalAmount
                ))
                .build();

        sendHtmlEmail(emailRequest);
    }
}