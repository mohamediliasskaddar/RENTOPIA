package com.rental.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentServiceApplication.class, args);
		System.out.println("""
            
            ╔══════════════════════════════════════════╗
            ║   💳 Payment Service Started! 🚀         ║
            ║   Port: 8084                             ║
            ║   Blockchain: Ethereum Sepolia Testnet   ║
            ╚══════════════════════════════════════════╝
            """);
	}
}