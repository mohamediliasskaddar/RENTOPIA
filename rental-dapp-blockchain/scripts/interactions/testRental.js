// ============================================
// SCRIPT DE TEST - RENTAL PLATFORM
// ============================================
// Ce script teste le cycle complet d'une réservation:
// 1. Déploiement du contrat
// 2. Configuration du propriétaire
// 3. Création d'une réservation
// 4. Check-in
// 5. Check-out
// 6. Libération des fonds (Pull Pattern)
// 7. Retrait des fonds par le propriétaire
// 8. Ajout d'une note

const hre = require("hardhat");

async function main() {
    console.log("\n🚀 ========================================");
    console.log("   DÉBUT DU TEST - RENTAL PLATFORM");
    console.log("========================================\n");

    // ============================================
    // 1️⃣ RÉCUPÉRER LES COMPTES DE TEST
    // ============================================
    // Hardhat fournit 20 comptes de test avec 10,000 ETH chacun
    const signers = await hre.ethers.getSigners();
    const deployer = signers[0];  // Admin de la plateforme
    const user1 = signers[1];     // Locataire
    const user2 = signers[2];     // Propriétaire

    console.log("👤 Comptes utilisés:");
    console.log("   Deployer (Admin):", deployer.address);
    console.log("   User1 (Locataire):", user1.address);
    console.log("   User2 (Propriétaire):", user2.address);

    // Afficher le solde du déployeur
    const deployerBalance = await hre.ethers.provider.getBalance(deployer.address);
    console.log("   Solde déployeur:", hre.ethers.formatEther(deployerBalance), "ETH\n");

    // ============================================
    // 2️⃣ DÉPLOYER LE CONTRAT RENTALPLATFORM
    // ============================================
    console.log("📦 Déploiement du contrat...");

    // Récupérer la factory du contrat
    const RentalPlatform = await hre.ethers.getContractFactory("RentalPlatform");

    // Déployer avec le deployer comme wallet de la plateforme
    // Les frais de 5% iront sur ce wallet
    const rental = await RentalPlatform.deploy(deployer.address);

    // Attendre que le déploiement soit confirmé sur la blockchain
    await rental.waitForDeployment();

    console.log("✅ Contrat RentalPlatform déployé à l'adresse:", rental.target);
    console.log("   Owner du contrat:", await rental.owner());
    console.log("   Platform wallet:", await rental.platformWallet());
    console.log("   Platform fee:", await rental.platformFeePercentage(), "%\n");

    // ============================================
    // 3️⃣ DÉFINIR UN PROPRIÉTAIRE POUR UN LOGEMENT
    // ============================================
    console.log("🏠 Configuration du logement...");

    const propertyId = 1; // ID du logement (même ID que dans la BDD MySQL)

    // Admin enregistre que user2 est le propriétaire du logement #1
    const txOwner = await rental.setPropertyOwner(propertyId, user2.address);
    await txOwner.wait(); // Attendre la confirmation

    console.log(`   Propriétaire du logement #${propertyId}:`, user2.address);

    // Vérifier que c'est bien enregistré
    const registeredOwner = await rental.getPropertyOwner(propertyId);
    console.log("   Vérification:", registeredOwner === user2.address ? "✅" : "❌");
    console.log();

    // ============================================
    // 4️⃣ CRÉER UNE RÉSERVATION
    // ============================================
    console.log("📅 Création d'une réservation...");

    // Calculer les timestamps
    // Date.now() = millisecondes, on divise par 1000 pour avoir des secondes
    const startDate = Math.floor(Date.now() / 1000) + 60; // Dans 60 secondes
    const endDate = startDate + 3600; // 1 heure après le début (3600 sec)

    console.log("   Date début:", new Date(startDate * 1000).toLocaleString());
    console.log("   Date fin:", new Date(endDate * 1000).toLocaleString());

    // Montants
    const rentalAmount = hre.ethers.parseEther("1"); // 1 ETH en Wei
    const platformFee = rentalAmount * 5n / 100n;    // 5% = 0.05 ETH
    const totalAmount = rentalAmount + platformFee;  // 1.05 ETH

    console.log("   Montant location:", hre.ethers.formatEther(rentalAmount), "ETH");
    console.log("   Frais plateforme:", hre.ethers.formatEther(platformFee), "ETH");
    console.log("   Total à payer:", hre.ethers.formatEther(totalAmount), "ETH");

    // User1 (locataire) crée une réservation
    // connect(user1) = appeler la fonction en tant que user1
    // { value: totalAmount } = envoyer totalAmount ETH avec la transaction
    const txBooking = await rental.connect(user1).createBooking(
        propertyId,
        startDate,
        endDate,
        rentalAmount,
        { value: totalAmount }
    );

    // Attendre la confirmation et récupérer le reçu
    const receiptBooking = await txBooking.wait();

    console.log("   Transaction hash:", receiptBooking.hash);

    // ============================================
    // 5️⃣ RÉCUPÉRER LE BOOKING ID DEPUIS L'EVENT
    // ============================================
    // Chercher l'event "BookingCreated" dans les logs
    const bookingCreatedEvent = receiptBooking.logs
        .map(log => {
            try {
                return rental.interface.parseLog(log);
            } catch {
                return null;
            }
        })
        .find(e => e && e.name === "BookingCreated");

    if (!bookingCreatedEvent) {
        console.error("❌ Event BookingCreated non trouvé dans la transaction");
        return;
    }

    // Récupérer le bookingId depuis l'event
    const bookingId = bookingCreatedEvent.args.bookingId;
    console.log(`✅ Réservation créée avec succès! ID: ${bookingId}\n`);

    // Vérifier les détails de la réservation
    const booking = await rental.getBooking(bookingId);
    console.log("📋 Détails de la réservation:");
    console.log("   Locataire:", booking.tenant);
    console.log("   Logement ID:", booking.propertyId.toString());
    console.log("   Montant:", hre.ethers.formatEther(booking.rentalAmount), "ETH");
    console.log("   Statut:", booking.status, "(1 = CONFIRMED)");
    console.log();

    // ============================================
    // 6️⃣ SIMULER LE CHECK-IN
    // ============================================
    console.log("🔑 Simulation du check-in...");

    // Avancer le temps de la blockchain de 61 secondes
    // Pour que block.timestamp soit >= startDate
    await hre.network.provider.send("evm_increaseTime", [61]);
    await hre.network.provider.send("evm_mine"); // Miner un nouveau bloc

    console.log("   ⏰ Temps avancé de 61 secondes");

    // User1 fait le check-in
    const txCheckIn = await rental.connect(user1).checkIn(bookingId);
    await txCheckIn.wait();

    console.log("✅ Check-in effectué pour la réservation #" + bookingId);

    // Vérifier le nouveau statut
    const bookingAfterCheckIn = await rental.getBooking(bookingId);
    console.log("   Nouveau statut:", bookingAfterCheckIn.status, "(2 = CHECKED_IN)");
    console.log("   Check-in time:", new Date(Number(bookingAfterCheckIn.checkInTime) * 1000).toLocaleString());
    console.log();

    // ============================================
    // 7️⃣ SIMULER LE CHECK-OUT
    // ============================================
    console.log("🚪 Simulation du check-out...");

    // Avancer le temps de 3601 secondes (1h + 1 sec)
    await hre.network.provider.send("evm_increaseTime", [3601]);
    await hre.network.provider.send("evm_mine");

    console.log("   ⏰ Temps avancé de 3601 secondes (1h)");

    // User1 fait le check-out
    const txCheckOut = await rental.connect(user1).checkOut(bookingId);
    await txCheckOut.wait();

    console.log("✅ Check-out effectué pour la réservation #" + bookingId);

    // Vérifier le nouveau statut
    const bookingAfterCheckOut = await rental.getBooking(bookingId);
    console.log("   Nouveau statut:", bookingAfterCheckOut.status, "(3 = CHECKED_OUT)");
    console.log("   Check-out time:", new Date(Number(bookingAfterCheckOut.checkOutTime) * 1000).toLocaleString());
    console.log();

    // ============================================
    // 8️⃣ LIBÉRER LES FONDS (ADMIN)
    // ============================================
    console.log("💰 Libération des fonds...");

    // L'admin libère les fonds vers le propriétaire
    // Avec le Pull Pattern, ça crédite juste pendingWithdrawals
    const txRelease = await rental.releaseFunds(bookingId);
    await txRelease.wait();

    console.log("✅ Fonds libérés par l'admin");

    // Vérifier le montant en attente pour le propriétaire
    const pendingAmount = await rental.getPendingWithdrawal(user2.address);
    console.log("   Montant en attente pour le propriétaire:", hre.ethers.formatEther(pendingAmount), "ETH");

    // Vérifier le nouveau statut
    const bookingAfterRelease = await rental.getBooking(bookingId);
    console.log("   Nouveau statut:", bookingAfterRelease.status, "(4 = COMPLETED)");
    console.log();

    // ============================================
    // 9️⃣ PROPRIÉTAIRE RETIRE LES FONDS
    // ============================================
    console.log("💵 Retrait des fonds par le propriétaire...");

    // Vérifier le solde du propriétaire avant retrait
    const ownerBalanceBefore = await hre.ethers.provider.getBalance(user2.address);
    console.log("   Solde avant:", hre.ethers.formatEther(ownerBalanceBefore), "ETH");

    // User2 (propriétaire) retire son argent
    const txWithdraw = await rental.connect(user2).withdraw();
    const receiptWithdraw = await txWithdraw.wait();

    // Calculer les gas fees payés
    const gasUsed = receiptWithdraw.gasUsed * receiptWithdraw.gasPrice;

    // Vérifier le solde du propriétaire après retrait
    const ownerBalanceAfter = await hre.ethers.provider.getBalance(user2.address);
    console.log("   Solde après:", hre.ethers.formatEther(ownerBalanceAfter), "ETH");

    // Calculer le gain net (en enlevant les gas fees)
    const netGain = ownerBalanceAfter - ownerBalanceBefore + gasUsed;
    console.log("   Gain net:", hre.ethers.formatEther(netGain), "ETH");
    console.log("   Gas payé:", hre.ethers.formatEther(gasUsed), "ETH");

    // Vérifier que pendingWithdrawals est à 0 maintenant
    const pendingAfter = await rental.getPendingWithdrawal(user2.address);
    console.log("   Montant en attente après retrait:", hre.ethers.formatEther(pendingAfter), "ETH");
    console.log();

    // ============================================
    // 🔟 AJOUTER UNE NOTE (RATING)
    // ============================================
    console.log("⭐ Ajout d'une note...");

    // User1 (locataire) donne une note de 5 étoiles
    const txRating = await rental.connect(user1).addRating(bookingId, 5);
    await txRating.wait();

    console.log("✅ Note ajoutée: 5 étoiles");

    // Récupérer la note moyenne du logement
    const avgRating = await rental.getAverageRating(propertyId);
    const avgRatingDecimal = Number(avgRating) / 100; // Diviser par 100 pour avoir les décimales

    console.log(`   Note moyenne du logement #${propertyId}: ${avgRatingDecimal}/5 étoiles`);

    // Récupérer toutes les notes du logement
    const allRatings = await rental.getPropertyRatings(propertyId);
    console.log("   Nombre total de notes:", allRatings.length);
    console.log();

    // ============================================
    // 📊 RÉCAPITULATIF FINAL
    // ============================================
    console.log("📊 ========================================");
    console.log("   RÉCAPITULATIF DU TEST");
    console.log("========================================");
    console.log(`✅ Contrat déployé: ${rental.target}`);
    console.log(`✅ Propriétaire configuré pour logement #${propertyId}`);
    console.log(`✅ Réservation créée: #${bookingId}`);
    console.log(`✅ Check-in effectué`);
    console.log(`✅ Check-out effectué`);
    console.log(`✅ Fonds libérés`);
    console.log(`✅ Propriétaire a retiré: ${hre.ethers.formatEther(netGain)} ETH`);
    console.log(`✅ Note ajoutée: ${avgRatingDecimal}/5 étoiles`);
    console.log("========================================");
    console.log("🎉 TEST COMPLET TERMINÉ AVEC SUCCÈS!\n");
}

// ============================================
// GESTION DES ERREURS
// ============================================
main()
    .then(() => process.exit(0))
    .catch((error) => {
        console.error("\n❌ ========================================");
        console.error("   ERREUR LORS DU TEST");
        console.error("========================================");
        console.error(error);
        process.exitCode = 1;
    });
