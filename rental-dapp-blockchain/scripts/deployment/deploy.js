const hre = require("hardhat");

async function main() {
    console.log("\n🚀 ========================================");
    console.log("   DÉPLOIEMENT - RENTAL PLATFORM");
    console.log("========================================\n");

    // 1️⃣ Récupérer le compte déployeur
    const [deployer] = await hre.ethers.getSigners();
    const deployerAddress = await deployer.getAddress();

    console.log("📍 Informations du déploiement:");
    console.log("   Réseau:", hre.network.name);
    console.log("   Déployeur:", deployerAddress);

    // Vérifier le solde
    const balance = await hre.ethers.provider.getBalance(deployerAddress);
    console.log("   Solde:", hre.ethers.formatEther(balance), "ETH");

    // Vérifier qu'il y a assez d'ETH
    if (balance === 0n) {
        throw new Error("❌ ERREUR: Solde insuffisant! Va chercher des ETH sur un faucet.");
    }

    console.log();

    // 2️⃣ Déployer le contrat
    console.log("📦 Déploiement du contrat RentalPlatform...");

    const RentalPlatform = await hre.ethers.getContractFactory("RentalPlatform");

    // Le platformWallet = adresse du déployeur
    const platformWallet = deployerAddress;

    console.log("   ⏳ Envoi de la transaction...");
    const rental = await RentalPlatform.deploy(platformWallet);

    console.log("   ⏳ Attente de la confirmation sur la blockchain...");
    console.log("   (Cela peut prendre 15-30 secondes...)");

    // Attendre le déploiement
    await rental.waitForDeployment();

    const contractAddress = await rental.getAddress();

    console.log("\n✅ ========================================");
    console.log("   DÉPLOIEMENT RÉUSSI!");
    console.log("========================================");
    console.log("📍 Adresse du contrat:", contractAddress);
    console.log("🔗 Voir sur Etherscan:");
    console.log("   ", `https://sepolia.etherscan.io/address/${contractAddress}`);
    console.log();

    // 3️⃣ Vérifier les paramètres du contrat
    console.log("🔍 Vérification des paramètres:");
    const owner = await rental.owner();
    const platformWalletCheck = await rental.platformWallet();
    const platformFee = await rental.platformFeePercentage();

    console.log("   Owner:", owner);
    console.log("   Platform Wallet:", platformWalletCheck);
    console.log("   Platform Fee:", platformFee.toString(), "%");
    console.log();

    // 4️⃣ Sauvegarder les informations dans un fichier
    const fs = require("fs");
    const deploymentInfo = {
        network: hre.network.name,
        contractAddress: contractAddress,
        deployer: deployerAddress,
        timestamp: new Date().toISOString(),
        txHash: rental.deploymentTransaction().hash
    };

    // Créer le dossier deployment si il n'existe pas
    const dir = "./scripts/deployment";
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
    }

    fs.writeFileSync(
        `${dir}/deployment-info.json`,
        JSON.stringify(deploymentInfo, null, 2)
    );

    console.log("💾 Informations sauvegardées dans: scripts/deployment/deployment-info.json");
    console.log();

    // 5️⃣ Prochaines étapes
    console.log("📋 ========================================");
    console.log("   PROCHAINES ÉTAPES");
    console.log("========================================");
    console.log("1. ✅ Vérifie le contrat sur Etherscan (lien ci-dessus)");
    console.log("2. ✅ Copie l'adresse du contrat:", contractAddress);
    console.log("3. ✅ Partage cette adresse avec ton collègue backend");
    console.log("4. ⚙️  Configure les propriétaires avec setPropertyOwner()");
    console.log();
    console.log("🎉 Déploiement terminé avec succès!\n");
}

// Gestion des erreurs
main()
    .then(() => process.exit(0))
    .catch((error) => {
        console.error("\n❌ ========================================");
        console.error("   ERREUR DE DÉPLOIEMENT");
        console.error("========================================");
        console.error(error);
        process.exitCode = 1;
    });

