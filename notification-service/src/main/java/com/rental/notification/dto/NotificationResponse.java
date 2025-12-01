package com.rental.notification.dto;

import com.rental.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private Integer userId;
    private Integer reservationId;
    private NotificationType notificationType;
    private String title;
    private String message;
    private Boolean isRead;
    private Boolean sentViaEmail;
    private Boolean sentViaSms;
    private LocalDateTime createdAt;
}