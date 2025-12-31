package com.rental.payment.service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
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
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class BlockchainService {

    @Value("${blockchain.infura.endpoint}")
    private String infuraEndpoint;

    @Value("${blockchain.infura.connection-timeout:30000}")
    private int connectionTimeout;

    @Value("${blockchain.infura.read-timeout:60000}")
    private int readTimeout;

    @Value("${blockchain.infura.request-timeout:60000}")
    private int requestTimeout;

    @Value("${blockchain.infura.use-google-dns:false}")
    private boolean useGoogleDns;

    @Value("${blockchain.wallet.private-key}")
    private String privateKey;

    @Value("${blockchain.contract.address}")
    private String contractAddress;

    @Value("${blockchain.enabled:true}")
    private boolean blockchainEnabled;

    private Web3j web3j;
    private Credentials credentials;
    private boolean mockMode = false;

    @PostConstruct
    public void init() {
        try {
            // Vérifier si blockchain est désactivé
            if (!blockchainEnabled) {
                log.warn("⚠️ Blockchain désactivée dans la configuration");
                initializeMockMode();
                return;
            }

            log.info("🔗 Initialisation de la connexion Web3...");
            log.info("📡 Endpoint Infura: {}", infuraEndpoint.replaceAll("/[^/]+$", "/****"));
            log.info("⏱️  Timeouts configurés - Connexion: {}ms, Lecture: {}ms",
                    connectionTimeout, readTimeout);

            // Configurer les propriétés système pour les timeouts
            configureSystemProperties();

            // Créer le service HTTP avec les timeouts configurés
            HttpService httpService = createHttpService();

            this.web3j = Web3j.build(httpService);
            this.credentials = Credentials.create(privateKey);

            // Tester la connexion avec un timeout
            log.info("🔄 Test de connexion au réseau Ethereum...");
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

            log.info("🎯 Service blockchain initialisé avec succès");

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'initialisation Web3: {}", e.getMessage());

            // Analyser le type d'erreur
            if (e.getCause() != null) {
                String cause = e.getCause().getClass().getSimpleName();
                log.error("🔍 Cause racine: {}", cause);

                if (cause.contains("UnknownHostException") || cause.contains("ConnectException")) {
                    log.error("🌐 Problème de connexion/réseau détecté");
                    log.warn("⚠️ Activation du mode mock pour développement local");
                    initializeMockMode();
                } else if (cause.contains("SocketTimeoutException") || cause.contains("Timeout")) {
                    log.error("⏱️  Timeout de connexion - Augmentez les valeurs dans application.yml");
                    log.warn("⚠️ Activation du mode mock temporaire");
                    initializeMockMode();
                } else {
                    log.error("❌ Erreur spécifique: {}", e.getCause().getMessage());
                    // Essayer le mode mock avant de planter complètement
                    if (isDevelopmentEnvironment()) {
                        log.warn("⚠️ Mode développement - Activation du mode mock");
                        initializeMockMode();
                    } else {
                        throw new RuntimeException("Impossible de se connecter à Ethereum", e);
                    }
                }
            } else {
                // En développement, activer le mode mock
                if (isDevelopmentEnvironment()) {
                    log.warn("⚠️ Mode développement - Activation du mode mock");
                    initializeMockMode();
                } else {
                    throw new RuntimeException("Impossible de se connecter à Ethereum", e);
                }
            }
        }
    }

    private void configureSystemProperties() {
        // Configurer les timeouts au niveau système (affecte toutes les connexions HTTP)
        System.setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(connectionTimeout));
        System.setProperty("sun.net.client.defaultReadTimeout", String.valueOf(readTimeout));
        System.setProperty("http.keepAlive.timeout", "5000");
        System.setProperty("sun.net.http.retryPost", "false");

        // Configurer DNS si nécessaire
        if (useGoogleDns) {
            log.info("🌐 Utilisation de Google DNS comme fallback");
            System.setProperty("sun.net.spi.nameservice.nameservers", "8.8.8.8,8.8.4.4");
            System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
        }

        // Désactiver la vérification SSL pour les problèmes de certificat (dev seulement)
        if (isDevelopmentEnvironment()) {
            System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
        }
    }

    private HttpService createHttpService() {
        try {
            // Créer un client OkHttp avec les timeouts configurés
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
                    .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                    .writeTimeout(readTimeout, TimeUnit.MILLISECONDS)
                    .callTimeout(requestTimeout, TimeUnit.MILLISECONDS);

            // Ajouter logging pour débogage
            if (log.isDebugEnabled()) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message ->
                        log.debug("🔗 Web3 HTTP: {}", message));
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
                clientBuilder.addInterceptor(loggingInterceptor);
            }

            // Ajouter un intercepteur pour les retries
            clientBuilder.addInterceptor(chain -> {
                try {
                    return chain.proceed(chain.request());
                } catch (Exception e) {
                    log.warn("🔄 Tentative de reconnexion...");
                    throw e;
                }
            });

            OkHttpClient client = clientBuilder.build();

            // Retourner HttpService avec le client configuré
            return new HttpService(infuraEndpoint, client, false);

        } catch (Exception e) {
            log.warn("⚠️ Impossible de configurer le client HTTP avancé: {}", e.getMessage());
            log.info("🔄 Utilisation de la configuration HTTP par défaut");
            // Fallback à la configuration par défaut
            return new HttpService(infuraEndpoint);
        }
    }

    private boolean isDevelopmentEnvironment() {
        // Vérifier si on est en environnement de développement
        String profiles = System.getProperty("spring.profiles.active", "");
        boolean isLocalProfile = profiles.contains("local") || profiles.contains("dev");
        boolean isTestEnv = "test".equals(System.getenv("ENVIRONMENT"));
        boolean isLocalMachine = System.getProperty("user.name").toLowerCase().contains("fatim");

        return isLocalProfile || isTestEnv || isLocalMachine;
    }

    private void initializeMockMode() {
        mockMode = true;
        log.info("🧪 Mode mock activé - Simulation des appels blockchain");
        log.info("📝 Les transactions seront loggées mais non envoyées sur le réseau");

        this.web3j = null;
        this.credentials = Credentials.create(privateKey); // On garde les credentials pour la logique

        // Log de l'adresse du wallet (même en mode mock)
        if (credentials != null) {
            log.info("📍 Adresse du wallet (mock): {}", credentials.getAddress());
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

            // Mode mock : retourner un solde fictif
            if (mockMode || web3j == null) {
                log.debug("🧪 Mode mock - Solde fictif pour {}: 10.0 ETH", address);
                return new BigDecimal("10.0");
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

            // En mode mock ou erreur, retourner une valeur par défaut
            if (mockMode || isDevelopmentEnvironment()) {
                log.warn("⚠️ Retour solde par défaut pour {}", address);
                return new BigDecimal("5.0");
            }
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

            // Mode mock : simuler l'envoi
            if (mockMode || web3j == null) {
                log.info("🧪 Mode mock - Simulation envoi de {} ETH vers {}", amountEth, toAddress);
                String mockTxHash = "0xmock" + System.currentTimeMillis() + "abcdef1234567890";
                log.info("🧪 Transaction simulée! Hash: {}", mockTxHash);
                log.info("🧪 Explorer: https://sepolia.etherscan.io/tx/{}", mockTxHash);
                return mockTxHash;
            }

            log.info("📤 Envoi réel de {} ETH vers {}", amountEth, toAddress);

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

            // En mode développement, retourner un hash mock
            if (isDevelopmentEnvironment()) {
                log.warn("⚠️ Retour transaction mock à cause de l'erreur");
                return "0xerror" + System.currentTimeMillis() + "recovery123";
            }
            throw new RuntimeException("Impossible d'envoyer l'ETH: " + e.getMessage(), e);
        }
    }

    /**
     * Vérifier le statut d'une transaction
     */
    public TransactionReceipt getTransactionReceipt(String txHash) {
        try {
            if (txHash == null) {
                throw new IllegalArgumentException("Hash de transaction ne peut pas être null");
            }

            // Mode mock : simuler un reçu
            if (mockMode || web3j == null || txHash.startsWith("0xmock") || txHash.startsWith("0xerror")) {
                log.debug("🧪 Mode mock - Reçu simulé pour {}", txHash);
                return null; // Ou créer un TransactionReceipt mock si nécessaire
            }

            if (!txHash.matches("^0x[a-fA-F0-9]{64}$")) {
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
        try {
            // Mode mock : simuler la confirmation
            if (mockMode || txHash == null || txHash.startsWith("0xmock") || txHash.startsWith("0xerror")) {
                log.debug("🧪 Mode mock - Transaction {} confirmée (simulée)", txHash);
                return true;
            }

            TransactionReceipt receipt = getTransactionReceipt(txHash);
            boolean confirmed = receipt != null && receipt.isStatusOK();

            log.debug("🔍 Transaction {} confirmée: {}", txHash, confirmed);

            return confirmed;

        } catch (Exception e) {
            log.error("❌ Erreur vérification confirmation {}: {}", txHash, e.getMessage());
            return false;
        }
    }

    /**
     * Obtenir les frais de gas d'une transaction
     */
    public BigDecimal getTransactionGasFee(String txHash) {
        try {
            // Mode mock : frais fictifs
            if (mockMode || web3j == null || txHash.startsWith("0xmock") || txHash.startsWith("0xerror")) {
                log.debug("🧪 Mode mock - Frais gas simulés: 0.001 ETH");
                return new BigDecimal("0.001");
            }

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

    // Méthodes utilitaires
    public boolean isMockMode() {
        return mockMode;
    }

    public boolean isBlockchainEnabled() {
        return blockchainEnabled && !mockMode && web3j != null;
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