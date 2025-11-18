package com.rentaldapp.userservice.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class NotificationServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private DiscoveryClient discoveryClient;

    public void sendVerificationEmail(String email, String token) {
        try {
            // Trouver l'URL du service notification via Eureka
            String notificationServiceUrl = getNotificationServiceUrl();

            if (notificationServiceUrl == null) {
                System.err.println("Notification Service non trouvé dans Eureka");
                return;
            }

            EmailVerificationRequest request = new EmailVerificationRequest(email, token);

            restTemplate.postForObject(
                    notificationServiceUrl + "/notifications/send-verification-email",
                    request,
                    Void.class
            );

            System.out.println("Email de vérification envoyé via: " + notificationServiceUrl);
        } catch (Exception e) {
            System.err.println("Erreur envoi email: " + e.getMessage());
        }
    }

    private String getNotificationServiceUrl() {
        List<ServiceInstance> instances = discoveryClient.getInstances("NOTIFICATION-SERVICE");
        if (instances != null && !instances.isEmpty()) {
            ServiceInstance instance = instances.get(0);
            return instance.getUri().toString();
        }
        return null;
    }

    public static class EmailVerificationRequest {
        private String email;
        private String token;
        private String templateType = "EMAIL_VERIFICATION";

        public EmailVerificationRequest(String email, String token) {
            this.email = email;
            this.token = token;
        }

        // Getters et setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getTemplateType() { return templateType; }
        public void setTemplateType(String templateType) { this.templateType = templateType; }
    }
}