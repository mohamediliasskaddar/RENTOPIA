package com.rental.notification.service;

import com.rental.notification.dto.SmsRequest;
import com.rental.notification.exception.NotificationException;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;

    private boolean initialized = false;

    private void initializeTwilio() {
        if (!initialized) {
            Twilio.init(accountSid, authToken);
            initialized = true;
            log.info("Twilio initialisé avec succès");
        }
    }

    /**
     * Envoyer un SMS
     */
    public void sendSms(SmsRequest smsRequest) {
        try {
            initializeTwilio();

            Message message = Message.creator(
                    new PhoneNumber(smsRequest.getTo()),
                    new PhoneNumber(fromPhoneNumber),
                    smsRequest.getMessage()
            ).create();

            log.info("SMS envoyé à {} avec SID: {}", smsRequest.getTo(), message.getSid());

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du SMS à {}: {}",
                    smsRequest.getTo(), e.getMessage());
            throw new NotificationException("Échec d'envoi du SMS: " + e.getMessage());
        }
    }

    /**
     * Envoyer SMS de confirmation de réservation
     */
    public void sendBookingConfirmationSms(String to, String guestName,
                                           String propertyTitle, String checkIn) {
        String message = String.format(
                "Bonjour %s, votre réservation pour '%s' est confirmée! Check-in: %s. RentalHub",
                guestName, propertyTitle, checkIn
        );

        SmsRequest smsRequest = SmsRequest.builder()
                .to(to)
                .message(message)
                .build();

        sendSms(smsRequest);
    }
}