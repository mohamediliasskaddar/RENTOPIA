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
            log.info("📡 Endpoint Infura: {}", infuraEndpoint.replaceAll("/[^/]+$", "/****")); // Masquer le project ID

            this.web3j = Web3j.build(new HttpService(infuraEndpoint));
            this.credentials = Credentials.create(privateKey);

            // Test de connexion
            Web3ClientVersion version = web3j.web3ClientVersion().send();
            log.info("✅ Connecté au réseau Ethereum: {}", version.getWeb3ClientVersion());
            log.info("📍 Adresse du wallet: {}", credentials.getAddress());

            // Vérifier le solde du wallet admin
            BigDecimal adminBalance = getBalance(credentials.getAddress());
            if (adminBalance.compareTo(new BigDecimal("0.01")) < 0) {
                log.warn("⚠️ ATTENTION: Le solde du wallet admin est faible: {} ETH", adminBalance);
                log.warn("   Rechargez le wallet sur: https://sepoliafaucet.com");
            } else {
                log.info("💰 Solde wallet admin: {} ETH", adminBalance);
            }

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'initialisation Web3: {}", e.getMessage());
            log.error("   Vérifiez votre configuration blockchain dans application.yml");
            throw new RuntimeException("Impossible de se connecter à Ethereum", e);
        }
    }

    /**
     * Vérifier le solde d'une adresse
     */
    public BigDecimal getBalance(String address) {
        try {
            // Validation de l'adresse
            if (address == null || !address.matches("^0x[a-fA-F0-9]{40}$")) {
                throw new IllegalArgumentException("Adresse Ethereum invalide: " + address);
            }

            EthGetBalance balance = web3j
                    .ethGetBalance(address, DefaultBlockParameterName.LATEST)
                    .send();

            BigInteger weiBalance = balance.getBalance();
            BigDecimal ethBalance = Convert.fromWei(weiBalance.toString(), Convert.Unit.ETHER);

            log.debug("💰 Solde de {}: {} ETH", address, ethBalance);
            return ethBalance;

        } catch (IllegalArgumentException e) {
            log.error("❌ Adresse invalide: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération du solde de {}: {}", address, e.getMessage());
            throw new RuntimeException("Impossible de récupérer le solde", e);
        }
    }

    /**
     * Envoyer de l'ETH d'une adresse à une autre
     */
    public String sendEther(String toAddress, BigDecimal amountEth) {
        try {
            // Validation de l'adresse de destination
            if (toAddress == null || !toAddress.matches("^0x[a-fA-F0-9]{40}$")) {
                throw new IllegalArgumentException("Adresse de destination invalide: " + toAddress);
            }

            // Validation du montant
            if (amountEth == null || amountEth.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le montant doit être supérieur à 0");
            }

            log.info("📤 Envoi de {} ETH vers {}", amountEth, toAddress);

            // Vérifier le solde avant l'envoi
            BigDecimal senderBalance = getBalance(credentials.getAddress());
            if (senderBalance.compareTo(amountEth) < 0) {
                throw new RuntimeException(
                        String.format("Solde insuffisant. Disponible: %s ETH, Requis: %s ETH",
                                senderBalance, amountEth)
                );
            }

            // Conversion ETH -> Wei
            BigInteger amountWei = Convert.toWei(amountEth, Convert.Unit.ETHER).toBigInteger();

            // Récupération du nonce
            EthGetTransactionCount transactionCount = web3j
                    .ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST)
                    .send();
            BigInteger nonce = transactionCount.getTransactionCount();
            log.debug("🔢 Nonce: {}", nonce);

            // Préparation de la transaction
            org.web3j.tx.RawTransactionManager transactionManager =
                    new org.web3j.tx.RawTransactionManager(web3j, credentials);

            // Gas price et limit
            BigInteger gasPrice = DefaultGasProvider.GAS_PRICE;
            BigInteger gasLimit = DefaultGasProvider.GAS_LIMIT;

            log.debug("⛽ Gas Price: {} Gwei, Gas Limit: {}",
                    Convert.fromWei(gasPrice.toString(), Convert.Unit.GWEI), gasLimit);

            // Création et envoi de la transaction
            EthSendTransaction transaction = transactionManager.sendTransaction(
                    gasPrice,
                    gasLimit,
                    toAddress,
                    "",  // data (vide pour transfer simple)
                    amountWei
            );

            // Vérification des erreurs
            if (transaction.hasError()) {
                String errorMsg = transaction.getError().getMessage();
                log.error("❌ Erreur transaction: {}", errorMsg);
                throw new RuntimeException("Transaction échouée: " + errorMsg);
            }

            String txHash = transaction.getTransactionHash();

            if (txHash == null || txHash.isEmpty()) {
                throw new RuntimeException("La transaction n'a pas retourné de hash");
            }

            log.info("✅ Transaction envoyée! Hash: {}", txHash);
            log.info("🔗 Explorer: https://sepolia.etherscan.io/tx/{}", txHash);

            return txHash;

        } catch (IllegalArgumentException e) {
            log.error("❌ Paramètre invalide: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi d'ETH: {}", e.getMessage(), e);
            throw new RuntimeException("Impossible d'envoyer l'ETH: " + e.getMessage(), e);
        }
    }

    /**
     * Vérifier le statut d'une transaction
     */
    public TransactionReceipt getTransactionReceipt(String txHash) {
        try {
            if (txHash == null || !txHash.matches("^0x[a-fA-F0-9]{64}$")) {
                throw new IllegalArgumentException("Hash de transaction invalide: " + txHash);
            }

            EthGetTransactionReceipt receipt = web3j
                    .ethGetTransactionReceipt(txHash)
                    .send();

            TransactionReceipt transactionReceipt = receipt.getTransactionReceipt().orElse(null);

            if (transactionReceipt != null) {
                log.debug("📄 Transaction Receipt trouvé pour {}", txHash);
                log.debug("   Bloc: {}, Gas utilisé: {}, Statut: {}",
                        transactionReceipt.getBlockNumber(),
                        transactionReceipt.getGasUsed(),
                        transactionReceipt.isStatusOK() ? "SUCCESS" : "FAILED");
            } else {
                log.debug("⏳ Transaction {} en attente de confirmation", txHash);
            }

            return transactionReceipt;

        } catch (IllegalArgumentException e) {
            log.error("❌ Hash invalide: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération du reçu de {}: {}", txHash, e.getMessage());
            return null;
        }
    }

    /**
     * Vérifier si une transaction est confirmée
     */
    public boolean isTransactionConfirmed(String txHash) {
        TransactionReceipt receipt = getTransactionReceipt(txHash);
        boolean confirmed = receipt != null && receipt.isStatusOK();

        log.debug("🔍 Transaction {} confirmée: {}", txHash, confirmed);

        return confirmed;
    }

    /**
     * Obtenir les frais de gas d'une transaction
     */
    public BigDecimal getTransactionGasFee(String txHash) {
        try {
            TransactionReceipt receipt = getTransactionReceipt(txHash);
            if (receipt == null) {
                log.debug("⚠️ Pas de reçu pour la transaction {}", txHash);
                return BigDecimal.ZERO;
            }

            BigInteger gasUsed = receipt.getGasUsed();

            EthTransaction transaction = web3j.ethGetTransactionByHash(txHash).send();

            if (!transaction.getTransaction().isPresent()) {
                log.warn("⚠️ Transaction {} introuvable", txHash);
                return BigDecimal.ZERO;
            }

            BigInteger gasPrice = transaction.getTransaction().get().getGasPrice();

            BigInteger gasFeeWei = gasUsed.multiply(gasPrice);
            BigDecimal gasFeeEth = Convert.fromWei(gasFeeWei.toString(), Convert.Unit.ETHER);

            log.debug("⛽ Gas Fee pour {}: {} ETH (Gas utilisé: {}, Prix: {} Gwei)",
                    txHash, gasFeeEth, gasUsed,
                    Convert.fromWei(gasPrice.toString(), Convert.Unit.GWEI));

            return gasFeeEth;

        } catch (Exception e) {
            log.error("❌ Erreur calcul gas fee pour {}: {}", txHash, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    // Getters
    public Web3j getWeb3j() {
        return web3j;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public String getContractAddress() {
        return contractAddress;
    }
}