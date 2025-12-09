package com.rental.blockchain.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;

/**
 * Configuration Web3j pour Ethereum Sepolia
 */
@Slf4j
@Configuration
public class Web3Config {

    @Value("${ethereum.rpc-url}")
    private String rpcUrl;

    @Value("${ethereum.wallet.admin-private-key}")
    private String adminPrivateKey;

    @Value("${ethereum.gas.price}")
    private Long gasPrice;

    @Value("${ethereum.gas.limit}")
    private Long gasLimit;

    /**
     * Bean Web3j - Connexion à Sepolia
     */
    @Bean
    public Web3j web3j() {
        log.info("🔗 Connexion à Ethereum Sepolia via: {}", rpcUrl);

        Web3j web3j = Web3j.build(new HttpService(rpcUrl));

        try {
            String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
            log.info("✅ Connecté à Ethereum! Client: {}", clientVersion);

            BigInteger blockNumber = web3j.ethBlockNumber().send().getBlockNumber();
            log.info("📊 Bloc actuel: {}", blockNumber);

        } catch (Exception e) {
            log.error("❌ Erreur connexion Ethereum: {}", e.getMessage());
        }

        return web3j;
    }

    /**
     * Bean Credentials - Wallet admin
     */
    @Bean
    public Credentials adminCredentials() {
        log.info("🔑 Chargement du wallet admin...");

        try {
            // Enlever le 0x si présent
            String key = adminPrivateKey.startsWith("0x") ?
                    adminPrivateKey.substring(2) : adminPrivateKey;

            Credentials credentials = Credentials.create(key);
            log.info("✅ Wallet admin chargé: {}", credentials.getAddress());

            return credentials;

        } catch (Exception e) {
            log.error("❌ Erreur chargement wallet admin: {}", e.getMessage());
            throw new RuntimeException("Impossible de charger le wallet admin", e);
        }
    }

    /**
     * Bean GasProvider - Configuration du gas
     */
    @Bean
    public StaticGasProvider gasProvider() {
        BigInteger gasPriceBigInt = BigInteger.valueOf(gasPrice);
        BigInteger gasLimitBigInt = BigInteger.valueOf(gasLimit);

        log.info("⛽ Gas configuré - Price: {} wei, Limit: {}",
                gasPriceBigInt, gasLimitBigInt);

        return new StaticGasProvider(gasPriceBigInt, gasLimitBigInt);
    }
}