package com.rental.blockchain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * Service de gestion des wallets Ethereum
 */
@Slf4j
@Service
public class WalletService {

    private static final String ALGORITHM = "AES";

    // ✅ CORRECTION: Utiliser @Value pour lire depuis application.yml
    @Value("${wallet.encryption-key}")
    private String secretKey;

    /**
     * Créer un nouveau wallet Ethereum
     * @return Credentials (address + private key)
     */
    public Credentials createWallet() throws Exception {
        log.info("🔑 Création d'un nouveau wallet...");

        // Générer une paire de clés aléatoire
        ECKeyPair keyPair = Keys.createEcKeyPair();

        // Créer les credentials
        Credentials credentials = Credentials.create(keyPair);

        log.info("✅ Wallet créé: {}", credentials.getAddress());

        return credentials;
    }

    /**
     * Créer un wallet à partir d'une clé privée
     * @param privateKey Clé privée (avec ou sans 0x)
     * @return Credentials
     */
    public Credentials loadWallet(String privateKey) {
        log.debug("🔑 Chargement wallet depuis clé privée...");

        // Enlever le 0x si présent
        if (privateKey.startsWith("0x")) {
            privateKey = privateKey.substring(2);
        }

        Credentials credentials = Credentials.create(privateKey);

        log.debug("✅ Wallet chargé: {}", credentials.getAddress());

        return credentials;
    }

    /**
     * Chiffrer une clé privée (pour stockage en BDD)
     * @param privateKey Clé privée à chiffrer
     * @return Clé privée chiffrée (Base64)
     */
    public String encryptPrivateKey(String privateKey) throws Exception {
        log.debug("🔒 Chiffrement clé privée...");

        // Vérifier que la clé de chiffrement est configurée
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalStateException("❌ Encryption key n'est pas configurée dans application.yml!");
        }

        SecretKeySpec key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encrypted = cipher.doFinal(privateKey.getBytes("UTF-8"));
        String encoded = Base64.getEncoder().encodeToString(encrypted);

        log.debug("✅ Clé privée chiffrée: {} bytes", encrypted.length);

        return encoded;
    }

    /**
     * Déchiffrer une clé privée
     * @param encryptedKey Clé privée chiffrée (Base64)
     * @return Clé privée déchiffrée
     */
    public String decryptPrivateKey(String encryptedKey) throws Exception {
        log.debug("🔓 Déchiffrement clé privée...");

        // Vérifier que la clé de chiffrement est configurée
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalStateException("❌ Encryption key n'est pas configurée dans application.yml!");
        }

        SecretKeySpec key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decoded = Base64.getDecoder().decode(encryptedKey);
        byte[] decrypted = cipher.doFinal(decoded);

        log.debug("✅ Clé privée déchiffrée");

        return new String(decrypted, "UTF-8");
    }

    /**
     * Générer une clé AES depuis la secret key
     */
    private SecretKeySpec generateKey() throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = secretKey.getBytes("UTF-8");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16); // AES-128

        return new SecretKeySpec(key, ALGORITHM);
    }

    /**
     * Valider qu'une clé privée est correcte
     * @param privateKey Clé privée à valider
     * @return true si valide
     */
    public boolean isValidPrivateKey(String privateKey) {
        try {
            if (privateKey.startsWith("0x")) {
                privateKey = privateKey.substring(2);
            }

            // Vérifier la longueur (64 caractères hex)
            if (privateKey.length() != 64) {
                return false;
            }

            // Essayer de créer un wallet
            Credentials.create(privateKey);

            return true;

        } catch (Exception e) {
            log.error("❌ Clé privée invalide: {}", e.getMessage());
            return false;
        }
    }
}