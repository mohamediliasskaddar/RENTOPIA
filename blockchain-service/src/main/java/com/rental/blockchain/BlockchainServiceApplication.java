package com.rental.blockchain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Blockchain Service - Microservice pour interactions Ethereum
 *
 * Responsabilités:
 * - Appeler le smart contract RentalPlatform sur Sepolia
 * - Gérer les wallets utilisateurs
 * - Écouter les events blockchain
 * - Synchroniser avec RabbitMQ
 *
 * @author Votre Nom
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BlockchainServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlockchainServiceApplication.class, args);

        System.out.println("\n" +
                "╔═══════════════════════════════════════════╗\n" +
                "║   🔗 BLOCKCHAIN SERVICE STARTED 🔗       ║\n" +
                "║                                           ║\n" +
                "║   Port: 8089                              ║\n" +
                "║   Network: Sepolia Testnet                ║\n" +
                "║   Contract: 0x4c5556c7bb47c8ca...        ║\n" +
                "╚═══════════════════════════════════════════╝\n"
        );
    }
}
