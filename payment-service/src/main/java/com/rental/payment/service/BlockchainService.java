package com.rental.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.BigInteger;

@Service
@Slf4j
public class BlockchainService {

    @Value("${blockchain.infura.endpoint}")
    private String infuraEndpoint;

    @Value("${blockchain.wallet.private-key}")
    private String privateKey;

    @Value("${blockchain.contract.address}")
    private String contractAddress;

    private Web3j web3j;
    private Credentials credentials;

    @PostConstruct
    public void init() {
        try {
            log.info("🔗 Initialisation de la connexion Web3...");
            this.web3j = Web3j.build(new HttpService(infuraEndpoint));
            this.credentials = Credentials.create(privateKey);

            // Test de connexion
            Web3ClientVersion version = web3j.web3ClientVersion().send();
            log.info("✅ Connecté au réseau Ethereum: {}", version.getWeb3ClientVersion());
            log.info("📍 Adresse du wallet: {}", credentials.getAddress());

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'initialisation Web3: {}", e.getMessage());
            throw new RuntimeException("Impossible de se connecter à Ethereum", e);
        }
    }

    /**
     * Vérifier le solde d'une adresse
     */
    public BigDecimal getBalance(String address) {
        try {
            EthGetBalance balance = web3j
                    .ethGetBalance(address, DefaultBlockParameterName.LATEST)
                    .send();

            BigInteger weiBalance = balance.getBalance();
            BigDecimal ethBalance = Convert.fromWei(weiBalance.toString(), Convert.Unit.ETHER);

            log.info("💰 Solde de {}: {} ETH", address, ethBalance);
            return ethBalance;

        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération du solde: {}", e.getMessage());
            throw new RuntimeException("Impossible de récupérer le solde", e);
        }
    }

    /**
     * Envoyer de l'ETH d'une adresse à une autre
     */
    public String sendEther(String toAddress, BigDecimal amountEth) {
        try {
            log.info("📤 Envoi de {} ETH vers {}", amountEth, toAddress);

            // Conversion ETH -> Wei
            BigInteger amountWei = Convert.toWei(amountEth, Convert.Unit.ETHER).toBigInteger();

            // Récupération du nonce
            EthGetTransactionCount transactionCount = web3j
                    .ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST)
                    .send();
            BigInteger nonce = transactionCount.getTransactionCount();

            // Préparation de la transaction
            org.web3j.tx.RawTransactionManager transactionManager =
                    new org.web3j.tx.RawTransactionManager(web3j, credentials);

            // Gas price et limit
            BigInteger gasPrice = DefaultGasProvider.GAS_PRICE;
            BigInteger gasLimit = DefaultGasProvider.GAS_LIMIT;

            // Création et envoi de la transaction
            EthSendTransaction transaction = transactionManager.sendTransaction(
                    gasPrice,
                    gasLimit,
                    toAddress,
                    "",  // data (vide pour transfer simple)
                    amountWei
            );

            String txHash = transaction.getTransactionHash();

            if (transaction.hasError()) {
                log.error("❌ Erreur transaction: {}", transaction.getError().getMessage());
                throw new RuntimeException("Transaction échouée: " + transaction.getError().getMessage());
            }

            log.info("✅ Transaction envoyée! Hash: {}", txHash);
            return txHash;

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi d'ETH: {}", e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'ETH", e);
        }
    }

    /**
     * Vérifier le statut d'une transaction
     */
    public TransactionReceipt getTransactionReceipt(String txHash) {
        try {
            EthGetTransactionReceipt receipt = web3j
                    .ethGetTransactionReceipt(txHash)
                    .send();

            return receipt.getTransactionReceipt().orElse(null);

        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération du reçu: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Vérifier si une transaction est confirmée
     */
    public boolean isTransactionConfirmed(String txHash) {
        TransactionReceipt receipt = getTransactionReceipt(txHash);
        return receipt != null && receipt.isStatusOK();
    }

    /**
     * Obtenir les frais de gas d'une transaction
     */
    public BigDecimal getTransactionGasFee(String txHash) {
        try {
            TransactionReceipt receipt = getTransactionReceipt(txHash);
            if (receipt == null) return BigDecimal.ZERO;

            BigInteger gasUsed = receipt.getGasUsed();

            EthTransaction transaction = web3j.ethGetTransactionByHash(txHash).send();
            BigInteger gasPrice = transaction.getTransaction().get().getGasPrice();

            BigInteger gasFeeWei = gasUsed.multiply(gasPrice);
            return Convert.fromWei(gasFeeWei.toString(), Convert.Unit.ETHER);

        } catch (Exception e) {
            log.error("❌ Erreur calcul gas fee: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    public Web3j getWeb3j() {
        return web3j;
    }

    public Credentials getCredentials() {
        return credentials;
    }
}