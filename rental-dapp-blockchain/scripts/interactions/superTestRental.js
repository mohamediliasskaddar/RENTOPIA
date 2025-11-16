// scripts/interactions/superTestRental.js
const hre = require("hardhat");

async function main() {
    // 1️⃣ Récupérer les comptes
    const signers = await hre.ethers.getSigners();
    const deployer = signers[0];
    const user1 = signers[1];
    const user2 = signers[2];
    const user3 = signers[3];

    console.log("Déploiement avec le compte:", deployer.address);
    const deployerBalance = await hre.ethers.provider.getBalance(deployer.address);
    console.log("Solde du compte déployeur:", deployerBalance.toString(), "wei");

    // 2️⃣ Déployer le contrat RentalPlatform
    const RentalPlatform = await hre.ethers.getContractFactory("RentalPlatform");
    const rental = await RentalPlatform.deploy(deployer.address);
    await rental.waitForDeployment(); // v6 ethers
    console.log("✅ Contrat déployé à:", rental.target);

    // 3️⃣ Créer plusieurs propriétés et assigner un propriétaire
    const properties = [
        { id: 1, owner: user1 },
        { id: 2, owner: user2 },
        { id: 3, owner: user3 }
    ];

    for (const p of properties) {
        const tx = await rental.setPropertyOwner(p.id, p.owner.address);
        await tx.wait();
        console.log(`Propriété #${p.id} assignée à ${p.owner.address}`);
    }

    // 4️⃣ Préparer des réservations pour tester
    const bookingsData = [
        { tenant: user1, propertyId: 2, startInSec: 70, durationSec: 3600, amountEth: "1" },
        { tenant: user2, propertyId: 1, startInSec: 120, durationSec: 3600, amountEth: "0.5" },
        { tenant: user3, propertyId: 3, startInSec: 150, durationSec: 7200, amountEth: "2" },
    ];

    const bookingIds = [];

    for (const b of bookingsData) {
        // ⚡ Récupérer le timestamp actuel de la blockchain
        const latestBlock = await hre.ethers.provider.getBlock("latest");
        const currentTimestamp = latestBlock.timestamp;

        const startDate = currentTimestamp + b.startInSec + 10; // +10 sec buffer
        const endDate = startDate + b.durationSec;

        const rentalAmount = hre.ethers.parseEther(b.amountEth);
        const platformFee = rentalAmount * 5n / 100n; // 5%
        const totalAmount = rentalAmount + platformFee;

        // Créer la réservation
        const txBooking = await rental.connect(b.tenant).createBooking(
            b.propertyId,
            startDate,
            endDate,
            rentalAmount,
            { value: totalAmount }
        );
        const receipt = await txBooking.wait();

        // Récupérer bookingId depuis l'événement
        const bookingEvent = receipt.logs
            .map(log => {
                try { return rental.interface.parseLog(log); }
                catch { return null; }
            })
            .find(e => e && e.name === "BookingCreated");

        if (!bookingEvent) {
            console.error(`❌ BookingCreated event non trouvé pour propriété #${b.propertyId}`);
            continue;
        }

        const bookingId = bookingEvent.args.bookingId;
        bookingIds.push({ bookingId, tenant: b.tenant, propertyId: b.propertyId, startDate, endDate });
        console.log(`✅ Réservation créée pour propriété #${b.propertyId} avec ID: ${bookingId}`);
    }

    // 5️⃣ Simuler check-in et check-out pour chaque réservation
    for (const b of bookingIds) {
        // Récupérer le timestamp actuel
        let block = await hre.ethers.provider.getBlock("latest");

        // Avancer le temps pour check-in
        let increaseCheckIn = b.startDate - block.timestamp;
        if (increaseCheckIn < 0) increaseCheckIn = 0;
        await hre.network.provider.send("evm_increaseTime", [increaseCheckIn]);
        await hre.network.provider.send("evm_mine");

        const txCheckIn = await rental.connect(b.tenant).checkIn(b.bookingId);
        await txCheckIn.wait();
        console.log(`✅ Check-in effectué pour réservation #${b.bookingId}`);

        // Récupérer le timestamp actuel
        block = await hre.ethers.provider.getBlock("latest");

        // Avancer le temps pour check-out
        let increaseCheckOut = b.endDate - block.timestamp;
        if (increaseCheckOut < 0) increaseCheckOut = 0;
        await hre.network.provider.send("evm_increaseTime", [increaseCheckOut]);
        await hre.network.provider.send("evm_mine");

        const txCheckOut = await rental.connect(b.tenant).checkOut(b.bookingId);
        await txCheckOut.wait();
        console.log(`✅ Check-out effectué pour réservation #${b.bookingId}`);

        // Libérer les fonds vers le propriétaire
        const txRelease = await rental.releaseFunds(b.bookingId);
        await txRelease.wait();
        console.log(`💰 Fonds libérés pour réservation #${b.bookingId}`);

        // Propriétaire retire les fonds
        const owner = properties.find(p => p.id === b.propertyId).owner;
        const ownerBalanceBefore = await hre.ethers.provider.getBalance(owner.address);
        const txWithdraw = await rental.connect(owner).withdraw();
        await txWithdraw.wait();
        const ownerBalanceAfter = await hre.ethers.provider.getBalance(owner.address);
        console.log(`💵 Retrait effectué pour propriété #${b.propertyId}, solde avant: ${ownerBalanceBefore}, après: ${ownerBalanceAfter}`);

        // Ajouter une note
        const txRating = await rental.connect(b.tenant).addRating(b.bookingId, 5);
        await txRating.wait();
        const avgRating = await rental.getAverageRating(b.propertyId);
        console.log(`⭐ Note moyenne pour la propriété #${b.propertyId}: ${Number(avgRating)/100}/5`);
    }

    console.log("🎉 Super test terminé !");
}

main().catch(error => {
    console.error(error);
    process.exitCode = 1;
});
