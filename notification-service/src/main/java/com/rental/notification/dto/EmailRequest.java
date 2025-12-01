package com.rental.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequest {
    private String to;
    private String subject;
    private String templateName;  // Pour HTML templates
    private Map<String, Object> templateVariables;
    private String plainTextBody; // Pour texte simple
}