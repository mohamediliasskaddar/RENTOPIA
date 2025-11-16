// scripts/interactions/stressTestRental.js
const hre = require("hardhat");

async function main() {
    console.log("🚀 Lancement du stress test blockchain...");

    const [owner, ...users] = await hre.ethers.getSigners();
    const TEST_USERS = 20; // nombre d'utilisateurs simulés

    // 1️⃣ Déployer le contrat RentalPlatform
    const RentalPlatform = await hre.ethers.getContractFactory("RentalPlatform");
    const rental = await RentalPlatform.deploy(owner.address); // passer wallet plateforme
    await rental.waitForDeployment();
    console.log(`✅ Contrat déployé à : ${await rental.getAddress()}`);
    console.log(`👤 Wallet plateforme : ${owner.address}`);
    console.log(`👥 Nombre d’utilisateurs simulés : ${users.length}`);

    let successCount = 0;
    let failCount = 0;

    // 2️⃣ Assigner des propriétaires aux logements
    console.log("\n🏠 Assignation des logements aux utilisateurs...");
    for (let i = 0; i < TEST_USERS; i++) {
        try {
            const tx = await rental.setPropertyOwner(i + 1, users[i % users.length].address);
            await tx.wait();
            console.log(`🏘️ Logement #${i + 1} assigné à ${users[i % users.length].address}`);
            successCount++;
        } catch (err) {
            console.error(`❌ Échec assignation logement #${i + 1}:`, err.message);
            failCount++;
        }
    }

    // 3️⃣ Création des réservations
    console.log("\n📅 Création des réservations...");
    for (let i = 0; i < TEST_USERS; i++) {
        try {
            const latestBlock = await hre.ethers.provider.getBlock("latest");
            const currentTimestamp = latestBlock.timestamp;
            const startDate = currentTimestamp + 60 + i * 10; // futur + petit décalage
            const endDate = startDate + 3600; // 1h plus tard

            const rentalAmount = hre.ethers.parseEther("1.0");
            const platformFee = rentalAmount * 5n / 100n;
            const totalAmount = rentalAmount + platformFee;

            const tx = await rental.connect(users[i % users.length]).createBooking(
                i + 1,          // propertyId
                startDate,
                endDate,
                rentalAmount,
                { value: totalAmount }
            );
            await tx.wait();
            console.log(`✅ Réservation créée pour logement #${i + 1}`);
            successCount++;
        } catch (err) {
            console.error(`⚠️ Réservation échouée #${i + 1}:`, err.message);
            failCount++;
        }
    }

    // 4️⃣ Check-in / Check-out
    console.log("\n🔑 Check-in / Check-out...");
    for (let i = 0; i < TEST_USERS; i++) {
        try {
            // Avancer le temps pour check-in
            const block = await hre.ethers.provider.getBlock("latest");
            await hre.network.provider.send("evm_increaseTime", [70 + i * 10]);
            await hre.network.provider.send("evm_mine");

            const txCheckIn = await rental.connect(users[i % users.length]).checkIn(i + 1);
            await txCheckIn.wait();

            // Avancer le temps pour check-out
            await hre.network.provider.send("evm_increaseTime", [3601]);
            await hre.network.provider.send("evm_mine");

            const txCheckOut = await rental.connect(users[i % users.length]).checkOut(i + 1);
            await txCheckOut.wait();

            // Libérer les fonds vers le propriétaire
            const txRelease = await rental.releaseFunds(i + 1);
            await txRelease.wait();

            // Propriétaire retire les fonds
            const ownerAddress = users[i % users.length];
            const txWithdraw = await rental.connect(ownerAddress).withdraw();
            await txWithdraw.wait();

            console.log(`✅ Check-in/out + retrait effectué pour logement #${i + 1}`);
            successCount++;
        } catch (err) {
            console.error(`⚠️ Échec check-in/out pour logement #${i + 1}:`, err.message);
            failCount++;
        }
    }

    console.log("\n📊 Résultats du test :");
    console.log(`✅ Succès : ${successCount}`);
    console.log(`❌ Échecs : ${failCount}`);
    console.log("🎉 Test terminé !");
}

main().catch((error) => {
    console.error("Erreur critique :", error);
    process.exitCode = 1;
});

