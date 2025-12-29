package com.rental.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients; // activer feign
import org.springframework.retry.annotation.EnableRetry; // ajouter

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.rental.payment.client")
@EnableRetry // ajouter
public class PaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentServiceApplication.class, args);
		System.out.println("""
							
		╔══════════════════════════════════════════╗
		║ 💳 Payment Service Started! 🚀           ║
		║ Port: 8084                               ║
		║ Feign Clients: Enabled ✅                ║
		║ Blockchain: Ethereum Sepolia Testnet     ║
		╚══════════════════════════════════════════╝
					""");
			}

}