// SPDX-License-Identifier: MIT
// C'est une licence open-source (gratuite, libre d'utilisation)
// Obligatoire dans tout contrat Solidity moderne

pragma solidity ^0.8.20;
// Utilise Solidity version 0.8.20 ou supérieure
// Le ^ signifie "compatible avec versions mineures" (0.8.21, 0.8.22... OK, mais pas 0.9.0)
// Pourquoi 0.8.x? Protection automatique contre les overflow/underflow

// ============================================
// IMPORTS - Bibliothèques de sécurité OpenZeppelin
// ============================================
import "@openzeppelin/contracts/utils/ReentrancyGuard.sol";
// ReentrancyGuard: Protection contre les attaques de réentrance
// Empêche qu'une fonction soit appelée plusieurs fois avant la fin de la première exécution

import "@openzeppelin/contracts/access/Ownable.sol";
// Ownable: Gestion des droits admin
// Permet de créer des fonctions accessibles uniquement par le propriétaire du contrat

/**
 * @title RentalPlatform
 * @dev Smart contract pour la location immobilière décentralisée
 * @notice Ce contrat gère les réservations et paiements escrow
 *
 * ============================================
 * POLITIQUE DES FRAIS
 * ============================================
 * - Les frais de plateforme (5%) sont prélevés à la création de la réservation
 * - Les frais ne sont PAS remboursables en cas d'annulation
 * - Le locataire paie: rentalAmount + platformFee
 * - Le propriétaire reçoit: rentalAmount (après check-out)
 *
 * ============================================
 * RÔLE DE L'ADMIN
 * ============================================
 * - Admin = Propriétaire de la plateforme (équipe du projet)
 * - Rôle: Gérer les paramètres globaux (frais, wallets, propriétaires)
 * - Admin NE PEUT PAS: annuler des réservations, voler des fonds, modifier le code
 * - Les paiements sont automatiques et immuables
 *
 * ============================================
 * PATTERN PULL WITHDRAWAL (Retrait Pull)
 * ============================================
 * - Au lieu d'envoyer l'argent directement au propriétaire (Push)
 * - Le contrat crédite le compte du propriétaire (pendingWithdrawals)
 * - Le propriétaire retire lui-même son argent quand il veut (Pull)
 * - Avantages: Plus sûr, évite les attaques de smart contracts malveillants
 */
contract RentalPlatform is ReentrancyGuard, Ownable {

    // ============================================
    // 📊 STRUCTURES DE DONNÉES
    // ============================================

    /**
     * @dev Structure représentant une réservation
     * C'est comme un "objet" en programmation classique
     * Regroupe toutes les infos d'une réservation
     */
    struct Booking {
        uint256 bookingId;           // ID unique de la réservation (1, 2, 3...)
        address tenant;              // Adresse Ethereum du locataire (qui a réservé)
        uint256 propertyId;          // ID du logement (référence vers base de données off-chain)
        uint256 startDate;           // Date de début (timestamp Unix en secondes)
        uint256 endDate;             // Date de fin (timestamp Unix)
        uint256 rentalAmount;        // Montant de la location en Wei (1 ETH = 10^18 Wei)
        uint256 platformFee;         // Frais de plateforme (5% du rentalAmount)
        uint256 totalAmount;         // Montant total payé (rentalAmount + platformFee)
        BookingStatus status;        // Statut actuel (CONFIRMED, CHECKED_IN, etc.)
        uint256 createdAt;           // Date de création de la réservation
        uint256 checkInTime;         // Date d'arrivée réelle du locataire (0 si pas encore arrivé)
        uint256 checkOutTime;        // Date de départ réelle (0 si pas encore parti)
    }

    /**
     * @dev Énumération des statuts possibles d'une réservation
     * Comme un "menu déroulant" avec des choix fixes
     * Cycle de vie: CONFIRMED → CHECKED_IN → CHECKED_OUT → COMPLETED
     */
    enum BookingStatus {
        PENDING,        // En attente (pas utilisé dans cette version)
        CONFIRMED,      // Confirmée et payée (argent en escrow)
        CHECKED_IN,     // Locataire arrivé (séjour en cours)
        CHECKED_OUT,    // Locataire parti (en attente de libération des fonds)
        COMPLETED,      // Terminée et paiements libérés
        CANCELLED       // Annulée (remboursement selon politique)
    }

    /**
     * @dev Structure pour les notes (ratings)
     * Stocké sur blockchain = immuable (impossible de tricher sur les notes)
     */
    struct Rating {
        address tenant;         // Qui a donné la note (pour éviter qu'une personne note 10x)
        uint256 propertyId;     // Logement noté (référence vers BDD)
        uint8 stars;            // Note de 1 à 5 étoiles (uint8 pour économiser du gas)
        uint256 timestamp;      // Quand la note a été donnée (pour trier par date)
    }

    // ============================================
    // 💾 VARIABLES D'ÉTAT (Stockage permanent sur blockchain)
    // ============================================

    // ------------------------------------------
    // Mappings principaux (comme des dictionnaires/HashMap)
    // Clé → Valeur, stockage permanent
    // ------------------------------------------

    mapping(uint256 => Booking) public bookings;
    // bookingId => Booking
    // Registre principal de TOUTES les réservations
    // Exemple: bookings[1] = {bookingId: 1, tenant: 0x123..., ...}

    mapping(uint256 => Rating[]) public propertyRatings;
    // propertyId => [Rating, Rating, Rating...]
    // Liste des notes pour chaque logement
    // Un logement peut avoir plusieurs notes (array [])

    mapping(address => uint256[]) public tenantBookings;
    // tenantAddress => [bookingId1, bookingId2, ...]
    // Liste des réservations d'un locataire
    // Permet de récupérer l'historique d'un utilisateur

    mapping(uint256 => uint256[]) public propertyBookings;
    // propertyId => [bookingId1, bookingId2, ...]
    // Liste des réservations d'un logement
    // CRITIQUE pour vérifier les disponibilités (pas de double-booking)

    mapping(uint256 => address payable) public propertyOwners;
    // propertyId => ownerAddress
    // Stocke le propriétaire de chaque logement
    // address payable = peut recevoir de l'ETH (pour les paiements)
    // Sécurité: évite d'envoyer l'argent à la mauvaise personne par erreur

    // ------------------------------------------
    // Optimisation Rating (O(1) au lieu de O(n))
    // ------------------------------------------
    mapping(uint256 => uint256) public ratingSum;
    // propertyId => somme totale des notes
    // Exemple: 3 notes de 5★ + 4★ + 5★ = ratingSum[propertyId] = 14

    mapping(uint256 => uint256) public ratingCount;
    // propertyId => nombre de notes
    // Exemple: ratingCount[propertyId] = 3
    // Moyenne = ratingSum / ratingCount = 14/3 = 4.67★

    // ------------------------------------------
    // Compteurs
    // ------------------------------------------
    uint256 public bookingCounter;
    // Nombre total de réservations créées
    // Incrémente à chaque nouvelle réservation: 1, 2, 3, 4...

    // ------------------------------------------
    // Configuration générale de la plateforme
    // ------------------------------------------
    address payable public platformWallet;
    // Adresse du wallet de la plateforme (où vont les frais de 5%)
    // Défini une seule fois au déploiement, modifiable par l'admin

    uint256 public platformFeePercentage = 5;
    // Pourcentage des frais (5% par défaut)
    // Modifiable par l'admin (max 10%)

    // ------------------------------------------
    // Pull Pattern: Retraits en attente
    // ------------------------------------------
    mapping(address => uint256) public pendingWithdrawals;
    // address => montant en attente
    // Au lieu d'envoyer directement l'argent (risqué)
    // On crédite le compte, et la personne retire elle-même
    // Plus sûr contre les contrats malveillants

    // ============================================
    // 🔔 EVENTS (Notifications blockchain)
    // ============================================
    // Les events sont comme des "logs" sur la blockchain
    // Le backend/frontend peut les écouter pour savoir ce qui se passe
    // Exemple: Quand une réservation est créée → event BookingCreated → Backend met à jour la BDD

    event BookingCreated(
        uint256 indexed bookingId,
        address indexed tenant,
        uint256 propertyId,
        uint256 totalAmount
    );
    // Déclenché quand une réservation est créée
    // indexed = permet de filtrer les events (ex: toutes les résa d'un tenant)

    event BookingConfirmed(uint256 indexed bookingId);
    // Déclenché quand le paiement est validé

    event CheckInCompleted(uint256 indexed bookingId, uint256 timestamp);
    // Déclenché quand le locataire arrive

    event CheckOutCompleted(uint256 indexed bookingId, uint256 timestamp);
    // Déclenché quand le locataire part

    event BookingCompleted(uint256 indexed bookingId);
    // Déclenché quand la réservation est terminée (paiement libéré)

    event BookingCancelled(uint256 indexed bookingId, uint256 refundAmount);
    // Déclenché quand une réservation est annulée (avec montant remboursé)

    event PaymentReleased(uint256 indexed bookingId, address indexed owner, uint256 amount);
    // Déclenché quand les fonds sont crédités au propriétaire

    event RatingAdded(uint256 indexed propertyId, address indexed tenant, uint8 stars);
    // Déclenché quand une note est ajoutée

    event PlatformFeeUpdated(uint256 oldFee, uint256 newFee);
    // Déclenché quand l'admin change les frais (transparence)

    event PlatformWalletChanged(address indexed oldWallet, address indexed newWallet);
    // Déclenché quand l'admin change l'adresse du wallet plateforme

    event PropertyOwnerSet(uint256 indexed propertyId, address indexed owner);
    // Déclenché quand un propriétaire est assigné à un logement

    // ============================================
    // 🛡️ MODIFIERS (Conditions réutilisables)
    // ============================================
    // Les modifiers sont comme des "filtres" sur les fonctions
    // Ils vérifient des conditions AVANT d'exécuter la fonction

    /**
     * @dev Vérifie que l'appelant est le locataire de la réservation
     * msg.sender = adresse de celui qui appelle la fonction
     */
    modifier onlyTenant(uint256 _bookingId) {
        require(
            bookings[_bookingId].tenant == msg.sender,
            "Seul le locataire peut faire cette action"
        );
        _; // Continue l'exécution de la fonction
    }

    /**
     * @dev Vérifie que la réservation existe
     * Si bookingId = 0, c'est que la réservation n'existe pas
     */
    modifier bookingExists(uint256 _bookingId) {
        require(
            bookings[_bookingId].bookingId != 0,
            "Cette reservation n'existe pas"
        );
        _;
    }

    /**
     * @dev Vérifie que la réservation a un statut spécifique
     * Exemple: checkIn() seulement si status = CONFIRMED
     */
    modifier inStatus(uint256 _bookingId, BookingStatus _status) {
        require(
            bookings[_bookingId].status == _status,
            "Statut de reservation invalide"
        );
        _;
    }

    // ============================================
    // 🏗️ CONSTRUCTOR (Initialisation)
    // ============================================
    /**
     * @dev Constructeur appelé UNE SEULE FOIS au déploiement du contrat
     * @param _platformWallet Adresse du wallet de la plateforme (où vont les frais 5%)
     *
     * Ownable(msg.sender) = msg.sender devient le owner (admin)
     * msg.sender = adresse de celui qui déploie le contrat
     */
    constructor(address payable _platformWallet) Ownable(msg.sender) {
        require(_platformWallet != address(0), "Adresse wallet invalide");
        platformWallet = _platformWallet;
    }

    // ============================================
    // 📝 FONCTIONS PRINCIPALES
    // ============================================

    /**
     * @dev Créer une réservation avec paiement escrow
     *
     * ESCROW = Séquestre: l'argent est bloqué dans le contrat
     * - Pas encore au propriétaire (sécurité pour le locataire)
     * - Pas encore remboursable (sécurité pour le propriétaire)
     * - Libération automatique après check-out
     *
     * @param _propertyId ID du logement (référence BDD)
     * @param _startDate Date de début (timestamp Unix)
     * @param _endDate Date de fin (timestamp Unix)
     * @param _rentalAmount Montant de location en Wei
     * @return bookingId ID de la réservation créée
     *
     * external = fonction appelable de l'extérieur (frontend/backend)
     * payable = fonction qui peut recevoir de l'ETH
     * nonReentrant = protection contre attaques de réentrance
     */
    function createBooking(
        uint256 _propertyId,
        uint256 _startDate,
        uint256 _endDate,
        uint256 _rentalAmount
    ) external payable nonReentrant returns (uint256) {

        // ============================================
        // 1️⃣ VALIDATIONS (Checks)
        // ============================================

        // Vérifier que la date de début est dans le futur
        require(_startDate > block.timestamp, "Date debut doit etre future");
        // block.timestamp = timestamp actuel de la blockchain

        // Vérifier que la date de fin est après la date de début
        require(_endDate > _startDate, "Date fin apres date debut");

        // Vérifier que le montant est positif
        require(_rentalAmount > 0, "Montant location doit etre positif");

        // CRITIQUE: Vérifier que le logement est disponible (pas de double-booking)
        require(
            isPropertyAvailable(_propertyId, _startDate, _endDate),
            "Logement deja reserve pour ces dates"
        );

        // ============================================
        // 2️⃣ CALCULS
        // ============================================

        // Calculer les frais de plateforme (5%)
        uint256 platformFee = (_rentalAmount * platformFeePercentage) / 100;

        // Calculer le total à payer
        uint256 totalAmount = _rentalAmount + platformFee;

        // Vérifier que le locataire a envoyé le bon montant
        require(msg.value == totalAmount, "Montant paye incorrect");
        // msg.value = montant d'ETH envoyé avec la transaction

        // ============================================
        // 3️⃣ EFFECTS (Modifier l'état AVANT les interactions)
        // Pattern Checks-Effects-Interactions pour la sécurité
        // ============================================

        // Incrémenter le compteur de réservations
        bookingCounter++;

        // Créer la réservation dans le mapping
        bookings[bookingCounter] = Booking({
            bookingId: bookingCounter,
            tenant: msg.sender,              // Celui qui appelle = locataire
            propertyId: _propertyId,
            startDate: _startDate,
            endDate: _endDate,
            rentalAmount: _rentalAmount,
            platformFee: platformFee,
            totalAmount: totalAmount,
            status: BookingStatus.CONFIRMED, // Statut initial: confirmé et payé
            createdAt: block.timestamp,      // Date de création = maintenant
            checkInTime: 0,                  // Pas encore arrivé
            checkOutTime: 0                  // Pas encore parti
        });

        // Ajouter aux listes pour traçabilité
        tenantBookings[msg.sender].push(bookingCounter);       // Historique locataire
        propertyBookings[_propertyId].push(bookingCounter);    // Historique logement

        // ============================================
        // 4️⃣ INTERACTIONS (Transferts externes EN DERNIER)
        // ============================================

        // Transférer les frais à la plateforme (immédiatement)
        (bool feeSuccess, ) = platformWallet.call{value: platformFee}("");
        require(feeSuccess, "Echec transfert frais plateforme");

        // Note: Le rentalAmount reste bloqué dans le contrat (escrow)
        // Il sera libéré après le check-out

        // Émettre les events (notifications blockchain)
        emit BookingCreated(bookingCounter, msg.sender, _propertyId, totalAmount);
        emit BookingConfirmed(bookingCounter);

        return bookingCounter;
    }

    /**
     * @dev Vérifier si un logement est disponible pour des dates données
     *
     * CRITIQUE: Empêche le double-booking (2 personnes réservent les mêmes dates)
     *
     * @param _propertyId ID du logement
     * @param _startDate Date de début souhaitée
     * @param _endDate Date de fin souhaitée
     * @return true si disponible, false sinon
     *
     * internal = fonction privée (pas appelable de l'extérieur)
     * view = fonction en lecture seule (ne modifie pas l'état)
     */
    function isPropertyAvailable(
        uint256 _propertyId,
        uint256 _startDate,
        uint256 _endDate
    ) public view returns (bool) {

        // Récupérer toutes les réservations de ce logement
        uint256[] memory bookingsForProperty = propertyBookings[_propertyId];

        // Parcourir chaque réservation existante
        for (uint256 i = 0; i < bookingsForProperty.length; i++) {
            Booking storage booking = bookings[bookingsForProperty[i]];

            // Ignorer les réservations annulées ou terminées
            if (
                booking.status == BookingStatus.CANCELLED ||
                booking.status == BookingStatus.COMPLETED
            ) continue;

            // Vérifier s'il y a un chevauchement de dates
            // Chevauchement si: nouvelle début < existante fin ET nouvelle fin > existante début
            bool overlap = (_startDate < booking.endDate && _endDate > booking.startDate);

            if (overlap) {
                return false; // ❌ Pas disponible (dates se chevauchent)
            }
        }

        return true; // ✅ Disponible (aucun chevauchement)
    }

    /**
     * @dev Check-in: marquer l'arrivée du locataire
     *
     * Le locataire appelle cette fonction quand il arrive au logement
     * Change le statut: CONFIRMED → CHECKED_IN
     *
     * @param _bookingId ID de la réservation
     */
    function checkIn(uint256 _bookingId)
    external
    onlyTenant(_bookingId)                          // Seulement le locataire
    bookingExists(_bookingId)                       // Réservation existe
    inStatus(_bookingId, BookingStatus.CONFIRMED)   // Statut = CONFIRMED
    {
        Booking storage booking = bookings[_bookingId];

        // Vérifier que c'est bien la date de début (ou après)
        require(block.timestamp >= booking.startDate, "Trop tot pour check-in");

        // Modifier le statut
        booking.status = BookingStatus.CHECKED_IN;
        booking.checkInTime = block.timestamp; // Enregistrer l'heure d'arrivée réelle

        emit CheckInCompleted(_bookingId, block.timestamp);
    }

    /**
     * @dev Check-out: marquer le départ du locataire
     *
     * ✅ CORRECTION: Le locataire peut partir quand il veut après le check-in
     * Pas de restriction sur endDate (peut partir avant la fin si besoin)
     *
     * Change le statut: CHECKED_IN → CHECKED_OUT
     * Déclenche la possibilité de libérer les fonds
     *
     * @param _bookingId ID de la réservation
     */
    function checkOut(uint256 _bookingId)
    external
    onlyTenant(_bookingId)                         // Seulement le locataire
    bookingExists(_bookingId)                      // Réservation existe
    inStatus(_bookingId, BookingStatus.CHECKED_IN) // Statut = CHECKED_IN
    {
        Booking storage booking = bookings[_bookingId];

        // ✅ PAS de require sur endDate
        // Le locataire peut partir avant ou après endDate
        // (Tu peux ajouter: require(block.timestamp >= booking.startDate + 1 hours, "Minimum 1h");)

        // Modifier le statut
        booking.status = BookingStatus.CHECKED_OUT;
        booking.checkOutTime = block.timestamp; // Enregistrer l'heure de départ réelle

        emit CheckOutCompleted(_bookingId, block.timestamp);
    }

    /**
     * @dev Annuler une réservation (avant check-in uniquement)
     *
     * ✅ CORRECTION: Politique de remboursement ajoutée
     * - > 7 jours avant: 100% remboursement
     * - 3-7 jours avant: 50% remboursement
     * - < 3 jours avant: 0% remboursement
     * - Les frais de plateforme (5%) ne sont JAMAIS remboursés
     *
     * @param _bookingId ID de la réservation
     */
    function cancelBooking(uint256 _bookingId)
    external
    nonReentrant                   // Protection réentrance
    onlyTenant(_bookingId)         // Seulement le locataire
    bookingExists(_bookingId)      // Réservation existe
    {
        Booking storage booking = bookings[_bookingId];

        // Vérifier que le statut permet l'annulation
        require(
            booking.status == BookingStatus.CONFIRMED ||
            booking.status == BookingStatus.PENDING,
            "Annulation non permise"
        );

        // Vérifier qu'on est avant la date de début
        require(block.timestamp < booking.startDate, "Trop tard pour annuler");

        // ============================================
        // CALCUL DU REMBOURSEMENT selon politique
        // ============================================

        // Calculer combien de jours avant le check-in
        uint256 daysUntilCheckIn = (booking.startDate - block.timestamp) / 1 days;
        // 1 days = 86400 secondes

        uint256 refundAmount;

        if (daysUntilCheckIn > 7) {
            // Plus de 7 jours avant: 100% remboursement
            refundAmount = booking.rentalAmount;
        } else if (daysUntilCheckIn >= 3) {
            // Entre 3 et 7 jours avant: 50% remboursement
            refundAmount = booking.rentalAmount / 2;
        } else {
            // Moins de 3 jours avant: 0% remboursement
            refundAmount = 0;
        }

        // Note: Les frais de plateforme (platformFee) ne sont JAMAIS remboursés

        // Modifier le statut
        booking.status = BookingStatus.CANCELLED;

        // Rembourser si montant > 0
        if (refundAmount > 0) {
            (bool refundSuccess, ) = booking.tenant.call{value: refundAmount}("");
            require(refundSuccess, "Echec remboursement");
        }

        emit BookingCancelled(_bookingId, refundAmount);
    }

    /**
     * @dev Libérer les fonds après check-out (admin seulement)
     *
     * PULL PATTERN: Au lieu d'envoyer directement l'argent au propriétaire
     * On crédite son compte (pendingWithdrawals)
     * Le propriétaire retire ensuite avec la fonction withdraw()
     *
     * Avantages:
     * - Plus sûr (pas de risque si le propriétaire est un contrat malveillant)
     * - Le propriétaire contrôle quand il veut retirer
     *
     * @param _bookingId ID de la réservation
     *
     * onlyOwner = seulement l'admin peut appeler cette fonction
     */
    function releaseFunds(uint256 _bookingId)
    external
    onlyOwner                                      // Seulement admin
    nonReentrant                                   // Protection réentrance
    bookingExists(_bookingId)                      // Réservation existe
    inStatus(_bookingId, BookingStatus.CHECKED_OUT) // Statut = CHECKED_OUT
    {
        Booking storage booking = bookings[_bookingId];

        // Récupérer le propriétaire du logement
        address payable ownerAddress = propertyOwners[booking.propertyId];
        require(ownerAddress != address(0), "Proprietaire non enregistre");

        // Modifier le statut
        booking.status = BookingStatus.COMPLETED;

        // ✅ PULL PATTERN: Créditer le compte du propriétaire
        // Au lieu de: ownerAddress.transfer(booking.rentalAmount);
        pendingWithdrawals[ownerAddress] += booking.rentalAmount;

        // Le propriétaire devra appeler withdraw() pour récupérer son argent

        emit PaymentReleased(_bookingId, ownerAddress, booking.rentalAmount);
        emit BookingCompleted(_bookingId);
    }

    /**
     * @dev Retirer ses fonds (pour propriétaires)
     *
     * PULL PATTERN: Le propriétaire retire son argent quand il veut
     * Plus sûr que le transfert automatique
     *
     * Tout le monde peut appeler cette fonction (pas besoin d'être admin)
     * Chacun ne peut retirer QUE son propre argent
     */
    function withdraw() external nonReentrant {
        // Récupérer le montant en attente pour l'appelant
        uint256 amount = pendingWithdrawals[msg.sender];

        // Vérifier qu'il y a bien de l'argent à retirer
        require(amount > 0, "Rien a retirer");

        // ✅ SÉCURITÉ: Mettre le solde à 0 AVANT le transfert
        // Évite les attaques de réentrance
        pendingWithdrawals[msg.sender] = 0;

        // Transférer l'argent
        (bool ok, ) = msg.sender.call{value: amount}("");
        require(ok, "Echec retrait");

        // Pas d'event car c'est une action personnelle
    }

    /**
     * @dev Ajouter une note (rating) après un séjour
     *
     * Seulement après que la réservation soit COMPLETED
     * Une seule note par réservation
     * Note stockée sur blockchain = immuable (impossible de tricher)
     *
     * @param _bookingId ID de la réservation
     * @param _stars Note de 1 à 5 étoiles
     */
    function addRating(uint256 _bookingId, uint8 _stars)
    external
    bookingExists(_bookingId)                     // Réservation existe
    onlyTenant(_bookingId)                        // Seulement le locataire
    inStatus(_bookingId, BookingStatus.COMPLETED) // Statut = COMPLETED
    {
        // Vérifier que la note est entre 1 et 5
        require(_stars >= 1 && _stars <= 5, "Note doit etre entre 1 et 5");

        Booking storage booking = bookings[_bookingId];

        // Ajouter la note dans l'array
        propertyRatings[booking.propertyId].push(
            Rating({
                tenant: msg.sender,
                propertyId: booking.propertyId,
                stars: _stars,
                timestamp: block.timestamp
            })
        );

        // ✅ OPTIMISATION: Mettre à jour la somme et le compteur
        // Permet de calculer la moyenne en O(1) au lieu de O(n)
        ratingSum[booking.propertyId] += _stars;
        ratingCount[booking.propertyId] += 1;

        emit RatingAdded(booking.propertyId, msg.sender, _stars);
    }

    /**
     * @dev Obtenir la note moyenne d'un logement
     *
     * ✅ OPTIMISATION: O(1) au lieu de O(n)
     * Grâce aux mappings ratingSum et ratingCount
     *
     * @param _propertyId ID du logement
     * @return Note moyenne multipliée par 100 (ex: 467 = 4.67 étoiles)
     *
     * Pourquoi multiplier par 100?
     * Solidity ne gère pas les décimales
     * Donc on renvoie 467 au lieu de 4.67
     * Le frontend divise par 100 pour afficher: 4.67★
     */
    function getAverageRating(uint256 _propertyId)
    external
    view
    returns (uint256)
{// Si aucune note, retourner 0
    if (ratingCount[_propertyId] == 0) return 0;

    // Calculer la moyenne et multiplier par 100
    // Exemple: somme = 14, count = 3
    // (14 * 100) / 3 = 1400 / 3 = 466.67 ≈ 466 → 4.66★
    return (ratingSum[_propertyId] * 100) / ratingCount[_propertyId];
}

    // ============================================
    // 🔧 FONCTIONS ADMIN
    // ============================================
    // Ces fonctions ne peuvent être appelées QUE par l'admin (onlyOwner)
    // Permettent de gérer les paramètres globaux de la plateforme

    /**
     * @dev Enregistrer le propriétaire d'un logement (admin seulement)
     *
     * IMPORTANT: Cette fonction DOIT être appelée AVANT qu'un logement puisse recevoir des réservations
     * Sinon releaseFunds() échouera (pas de propriétaire enregistré)
     *
     * Workflow:
     * 1. Backend crée le logement en BDD (MySQL)
     * 2. Admin appelle setPropertyOwner() pour enregistrer sur blockchain
     * 3. Maintenant le logement peut recevoir des réservations
     *
     * @param _propertyId ID du logement (même ID que dans la BDD)
     * @param _owner Adresse Ethereum du propriétaire
     */
    function setPropertyOwner(uint256 _propertyId, address payable _owner)
    external
    onlyOwner
    {
        require(_owner != address(0), "Adresse invalide");
        propertyOwners[_propertyId] = _owner;
        emit PropertyOwnerSet(_propertyId, _owner);
    }

    /**
     * @dev Modifier le pourcentage de frais (admin seulement)
     *
     * Permet de changer les frais de plateforme
     * Maximum 10% (protection contre abus)
     *
     * Note: Ne change PAS les frais des réservations existantes
     * Les anciennes réservations gardent leur platformFee d'origine
     *
     * @param _newFee Nouveau pourcentage (0-10)
     */
    function setPlatformFee(uint256 _newFee) external onlyOwner {
        require(_newFee <= 10, "Frais max 10%");
        uint256 oldFee = platformFeePercentage;
        platformFeePercentage = _newFee;
        emit PlatformFeeUpdated(oldFee, _newFee);
    }

    /**
     * @dev Changer l'adresse du wallet plateforme (admin seulement)
     *
     * Permet de changer où vont les frais de plateforme
     * Utile si on veut changer de wallet ou en cas de compromission
     *
     * @param _newWallet Nouvelle adresse
     */
    function setPlatformWallet(address payable _newWallet) external onlyOwner {
        require(_newWallet != address(0), "Adresse invalide");
        address oldWallet = platformWallet;
        platformWallet = _newWallet;
        emit PlatformWalletChanged(oldWallet, _newWallet);
    }

    // ============================================
    // 📖 FONCTIONS DE LECTURE (View functions)
    // ============================================
    // Ces fonctions sont GRATUITES (pas de gas fees)
    // Elles permettent de lire les données du contrat
    // Appelables par le backend pour synchroniser avec la BDD

    /**
     * @dev Obtenir les détails complets d'une réservation
     * @param _bookingId ID de la réservation
     * @return Booking Structure complète de la réservation
     */
    function getBooking(uint256 _bookingId)
    external
    view
    returns (Booking memory)
    {
        return bookings[_bookingId];
    }

    /**
     * @dev Obtenir toutes les réservations d'un locataire
     * @param _tenant Adresse du locataire
     * @return Array des IDs de réservations
     *
     * Exemple: getTenantBookings(0x123...) → [1, 5, 12, 23]
     * Ensuite appeler getBooking(1), getBooking(5), etc.
     */
    function getTenantBookings(address _tenant)
    external
    view
    returns (uint256[] memory)
    {
        return tenantBookings[_tenant];
    }

    /**
     * @dev Obtenir toutes les réservations d'un logement
     * @param _propertyId ID du logement
     * @return Array des IDs de réservations
     */
    function getPropertyBookings(uint256 _propertyId)
    external
    view
    returns (uint256[] memory)
    {
        return propertyBookings[_propertyId];
    }

    /**
     * @dev Obtenir toutes les notes d'un logement
     * @param _propertyId ID du logement
     * @return Array des ratings
     */
    function getPropertyRatings(uint256 _propertyId)
    external
    view
    returns (Rating[] memory)
    {
        return propertyRatings[_propertyId];
    }

    /**
     * @dev Obtenir le propriétaire d'un logement
     * @param _propertyId ID du logement
     * @return Adresse du propriétaire
     */
    function getPropertyOwner(uint256 _propertyId)
    external
    view
    returns (address)
    {
        return propertyOwners[_propertyId];
    }

    /**
     * @dev Obtenir le montant en attente de retrait pour une adresse
     * @param _address Adresse à vérifier
     * @return Montant en Wei
     */
    function getPendingWithdrawal(address _address)
    external
    view
    returns (uint256)
    {
        return pendingWithdrawals[_address];
    }

    // ============================================
    // 💰 GESTION ETHER (Sécurité)
    // ============================================
    // Ces fonctions protègent contre les envois accidentels d'ETH

    /**
     * @dev Rejeter les envois directs d'ETH
     *
     * receive() est appelée quand quelqu'un envoie de l'ETH sans appeler de fonction
     * On rejette pour éviter que l'argent soit bloqué
     *
     * Les utilisateurs DOIVENT utiliser createBooking() pour payer
     */
    receive() external payable {
        revert("Envoi direct ETH non autorise");
    }

    /**
     * @dev Rejeter les appels de fonctions inexistantes
     *
     * fallback() est appelée quand quelqu'un appelle une fonction qui n'existe pas
     * On rejette pour éviter les erreurs
     */
    fallback() external payable {
        revert("Fonction inexistante");
    }
}