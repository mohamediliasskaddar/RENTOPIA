package com.rental.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class NotificationServiceApplication {

	public static void main(String[] args) {
		// Désactiver Config Server
		System.setProperty("spring.cloud.config.enabled", "false");
		System.setProperty("spring.cloud.config.import-check.enabled", "false");

		SpringApplication.run(NotificationServiceApplication.class, args);
	}
}