package com.rental.notification.dto;

import com.rental.notification.enums.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    @NotNull(message = "User ID est obligatoire")
    private Integer userId;

    private Integer reservationId;

    private Integer bookingRequestId;

    @NotNull(message = "Type de notification obligatoire")
    private NotificationType notificationType;

    @NotBlank(message = "Titre obligatoire")
    private String title;

    @NotBlank(message = "Message obligatoire")
    private String message;

    @Email(message = "Email invalide")
    private String recipientEmail;

    private String recipientPhone;

    private Boolean sendEmail = false;

    private Boolean sendSms = false;
}