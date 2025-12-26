package com.rental.notification.dto;

import com.rental.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {
    private NotificationType type;
    private String title;
    private String message;
    private String email;
    private String phone;
    private Integer userId;
    private Integer reservationId;
    private Integer bookingRequestId;
    private Map<String, Object> additionalData;
}