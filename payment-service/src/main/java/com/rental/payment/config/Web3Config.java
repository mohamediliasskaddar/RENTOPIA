package com.rental.payment.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import jakarta.annotation.PostConstruct;

@Configuration
@Slf4j
public class Web3Config {

    @Value("${blockchain.infura.endpoint}")
    private String infuraEndpoint;

    @Value("${blockchain.wallet.private-key}")
    private String privateKey;

    @Value("${blockchain.contract.address}")
    private String contractAddress;

    @Value("${blockchain.gas.price:20000000000}") // 20 Gwei par défaut
    private Long gasPrice;

    @Value("${blockchain.gas.limit:500000}")
    private Long gasLimit;

    /**
     * Bean Web3j pour la connexion Ethereum
     */
    @Bean
    public Web3j web3j() {
        log.info("🔗 Initialisation de la connexion Web3j...");
        log.info("📍 Endpoint Infura: {}", infuraEndpoint);

        Web3j web3j = Web3j.build(new HttpService(infuraEndpoint));

        // Test de connexion
        try {
            String version = web3j.web3ClientVersion().send().getWeb3ClientVersion();
            log.info("✅ Connecté au réseau Ethereum: {}", version);
        } catch (Exception e) {
            log.error("❌ Échec de connexion à Ethereum: {}", e.getMessage());
        }

        return web3j;
    }

    /**
     * Bean Credentials pour signer les transactions
     */
    @Bean
    public Credentials credentials() {
        log.info("🔑 Chargement des credentials du wallet...");

        Credentials credentials = Credentials.create(privateKey);

        log.info("📍 Adresse du wallet: {}", credentials.getAddress());

        return credentials;
    }

    /**
     * Configuration du Gas Provider
     */
    @Bean
    public ContractGasProvider contractGasProvider() {
        log.info("⛽ Configuration Gas Provider - Price: {} Gwei, Limit: {}",
                gasPrice / 1_000_000_000, gasLimit);

        return new DefaultGasProvider();
    }

    @PostConstruct
    public void init() {
        log.info("✅ Web3 Configuration initialisée");
        log.info("📍 Contract Address: {}", contractAddress);
    }
}