package com.rentaldapp.bookingservice.service;

import com.rentaldapp.bookingservice.client.PaymentServiceClient;
import com.rentaldapp.bookingservice.client.PropertyServiceClient;
import com.rentaldapp.bookingservice.client.UserServiceClient;
import com.rentaldapp.bookingservice.exception.InvalidBookingException;
import com.rentaldapp.bookingservice.exception.PropertyNotAvailableException;
import com.rentaldapp.bookingservice.exception.ReservationNotFoundException;
import com.rentaldapp.bookingservice.model.dto.*;
import com.rentaldapp.bookingservice.model.entity.PropertyVersion;
import com.rentaldapp.bookingservice.model.entity.Reservation;
import com.rentaldapp.bookingservice.model.entity.ReservationStatusHistory;
import com.rentaldapp.bookingservice.model.enums.ReservationStatus;
import com.rentaldapp.bookingservice.repository.PropertyVersionRepository;
import com.rentaldapp.bookingservice.repository.ReservationRepository;
import com.rentaldapp.bookingservice.repository.ReservationStatusHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private PropertyVersionRepository propertyVersionRepository;

    @Autowired
    private PriceCalculationService priceCalculationService;

    // Feign Clients
    @Autowired
    private PropertyServiceClient propertyServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private PaymentServiceClient paymentServiceClient;

    // ✅ CHANGEMENT : Utilise RabbitMQ au lieu de Feign pour les notifications
    @Autowired
    private NotificationEventPublisher notificationEventPublisher;

    // Event Publisher pour les événements de booking
    @Autowired
    private BookingEventPublisher eventPublisher;

    @Transactional
    public ReservationResponseDTO createBooking(CreateBookingDTO createBookingDTO, Integer userId) {
        logger.info("📝 Creating booking for user {} - Property {}", userId, createBookingDTO.getPropertyId());

        // 1. Vérifier que l'utilisateur existe
        try {
            Map<String, Object> userResponse = userServiceClient.getUserById(userId);
            if (userResponse == null || userResponse.isEmpty()) {
                throw new InvalidBookingException("Utilisateur non trouvé");
            }
            logger.info("✅ User verified: {} {}", userResponse.get("prenom"), userResponse.get("nom"));
        } catch (Exception e) {
            logger.error("❌ Failed to verify user", e);
            throw new InvalidBookingException("Impossible de vérifier l'utilisateur");
        }

        // 2. Récupérer les détails de la propriété
        PropertyDTO property;
        try {
            property = propertyServiceClient.getPropertyById(createBookingDTO.getPropertyId());
            if (property == null) {
                throw new InvalidBookingException("Propriété non trouvée");
            }
            logger.info("✅ Property found: {}", property.getTitle());
        } catch (Exception e) {
            logger.error("❌ Failed to fetch property", e);
            throw new InvalidBookingException("Impossible de récupérer les détails de la propriété");
        }

        // 3. Validation des dates
        validateDates(createBookingDTO.getCheckInDate(), createBookingDTO.getCheckOutDate());

        // 4. Vérifier la disponibilité via Property Service
        try {
            Boolean isAvailable = propertyServiceClient.checkAvailability(
                    createBookingDTO.getPropertyId(),
                    createBookingDTO.getCheckInDate(),
                    createBookingDTO.getCheckOutDate()
            );

            if (!isAvailable) {
                throw new PropertyNotAvailableException("La propriété n'est pas disponible pour ces dates");
            }
        } catch (PropertyNotAvailableException e) {
            throw e;
        } catch (Exception e) {
            logger.error("❌ Failed to check availability", e);
        }

        // 5. Vérifier les chevauchements locaux
        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(
                createBookingDTO.getPropertyId(),
                createBookingDTO.getCheckInDate(),
                createBookingDTO.getCheckOutDate()
        );

        if (!overlapping.isEmpty()) {
            throw new PropertyNotAvailableException("La propriété n'est pas disponible (chevauchement détecté)");
        }

        // 6. Vérifier que l'utilisateur n'a pas déjà une réservation
        boolean hasOverlapping = reservationRepository.existsOverlappingReservationForUser(
                userId,
                createBookingDTO.getPropertyId(),
                createBookingDTO.getCheckInDate(),
                createBookingDTO.getCheckOutDate()
        );

        if (hasOverlapping) {
            throw new InvalidBookingException("Vous avez déjà une réservation pour cette propriété pendant ces dates");
        }

        // 7. Déterminer la version de la propriété
        Integer versionId = determinePropertyVersion(
                createBookingDTO.getPropertyId(),
                createBookingDTO.getVersionId()
        );

        // 8. Calculer le nombre de nuits
        long totalNights = ChronoUnit.DAYS.between(
                createBookingDTO.getCheckInDate(),
                createBookingDTO.getCheckOutDate()
        );

        // 9. Récupérer les prix depuis la propriété
        Double pricePerNight = property.getWeekendPricePerNight();
        Double cleaningFee = property.getCleaningFee();
        Double petFee = createBookingDTO.getHasPets() ? property.getPetFee() : 0.0;

        // 10. Calculer le prix total
        PriceBreakdownDTO priceBreakdown = priceCalculationService.calculatePrice(
                createBookingDTO.getCheckInDate(),
                createBookingDTO.getCheckOutDate(),
                pricePerNight,
                property.getWeeklyPrice(),
                property.getMonthlyPrice(),
                cleaningFee,
                petFee,
                null
        );

        // 11. Créer la réservation
        Reservation reservation = new Reservation();
        reservation.setPropertyId(createBookingDTO.getPropertyId());
        reservation.setVersionId(versionId);
        reservation.setUserId(userId);
        reservation.setCheckInDate(createBookingDTO.getCheckInDate());
        reservation.setCheckOutDate(createBookingDTO.getCheckOutDate());
        reservation.setTotalNights((int) totalNights);
        reservation.setNumGuests(createBookingDTO.getNumGuests());
        reservation.setStatus(ReservationStatus.PENDING);

        // Prix
        reservation.setLockedPricePerNight(priceBreakdown.getLockedPricePerNight());
        reservation.setBaseAmount(priceBreakdown.getBaseAmount());
        reservation.setDiscountAmount(priceBreakdown.getDiscountAmount());
        reservation.setCleaningFee(priceBreakdown.getCleaningFee());
        reservation.setPetFee(priceBreakdown.getPetFee());
        reservation.setServiceFee(priceBreakdown.getServiceFee());
        reservation.setTotalAmount(priceBreakdown.getTotalAmount());
        reservation.setPlatformFeePercentage(priceBreakdown.getPlatformFeePercentage());

        // 12. Sauvegarder
        Reservation savedReservation = reservationRepository.save(reservation);
        logger.info("✅ Reservation {} created successfully", savedReservation.getId());

        // 13. Enregistrer l'historique
        saveStatusHistory(savedReservation.getId(), null, ReservationStatus.PENDING.name(), userId, "Réservation créée");

        // 14. Publier l'événement
        ReservationResponseDTO responseDTO = convertToDTO(savedReservation);
        eventPublisher.publishBookingCreated(responseDTO);

        return responseDTO;
    }

    private Integer determinePropertyVersion(Integer propertyId, Integer requestedVersionId) {
        if (requestedVersionId != null) {
            PropertyVersion version = propertyVersionRepository.findById(requestedVersionId)
                    .orElseThrow(() -> new InvalidBookingException("Version de propriété invalide"));

            if (!version.getPropertyId().equals(propertyId)) {
                throw new InvalidBookingException("La version ne correspond pas à la propriété");
            }

            return requestedVersionId;
        }

        PropertyVersion latestVersion = propertyVersionRepository
                .findLatestVersionByPropertyId(propertyId)
                .orElseThrow(() -> new InvalidBookingException("Aucune version trouvée pour cette propriété"));

        return latestVersion.getVersionId();
    }

    @Transactional(readOnly = true)
    public ReservationResponseDTO getReservationById(Integer id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Réservation non trouvée avec l'ID: " + id));
        return convertToDTO(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getUserReservations(Integer userId) {
        return reservationRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getPropertyReservations(Integer propertyId) {
        return reservationRepository.findByPropertyId(propertyId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getUpcomingReservations(Integer userId) {
        return reservationRepository.findUpcomingReservationsByUser(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getPastReservations(Integer userId) {
        return reservationRepository.findPastReservationsByUser(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationResponseDTO confirmReservation(Integer reservationId, String blockchainTxHash) {
        logger.info("📝 Confirming reservation {} with tx {}", reservationId, blockchainTxHash);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Réservation non trouvée"));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new InvalidBookingException("Seules les réservations en attente peuvent être confirmées");
        }

        ReservationStatus oldStatus = reservation.getStatus();
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setBlockchainTxHash(blockchainTxHash);

        Reservation updated = reservationRepository.save(reservation);

        saveStatusHistory(reservationId, oldStatus.name(), ReservationStatus.CONFIRMED.name(),
                reservation.getUserId(), "Paiement confirmé");

        // Bloquer les dates dans Property Service
        try {
            propertyServiceClient.blockDates(
                    reservation.getPropertyId(),
                    reservation.getCheckInDate(),
                    reservation.getCheckOutDate(),
                    reservationId
            );
            logger.info("✅ Dates blocked in Property Service");
        } catch (Exception e) {
            logger.error("❌ Failed to block dates", e);
        }

        ReservationResponseDTO responseDTO = convertToDTO(updated);
        eventPublisher.publishBookingConfirmed(responseDTO);

        // ✅ CHANGEMENT : Notification ASYNCHRONE via RabbitMQ
        try {
            Map<String, Object> user = userServiceClient.getUserById(reservation.getUserId());
            if (user != null && user.get("email") != null) {
                notificationEventPublisher.sendBookingConfirmation(
                        reservation.getUserId(),
                        reservationId,
                        (String) user.get("email")
                );
            }
        } catch (Exception e) {
            logger.error("❌ Failed to send notification", e);
        }

        return responseDTO;
    }

    @Transactional
    public ReservationResponseDTO checkIn(Integer reservationId, Integer userId) {
        logger.info("📝 Check-in for reservation {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Réservation non trouvée"));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new InvalidBookingException("Seules les réservations confirmées peuvent être check-in");
        }

        if (LocalDateTime.now().isBefore(reservation.getCheckInDate())) {
            throw new InvalidBookingException("Le check-in ne peut être effectué avant la date prévue");
        }

        ReservationStatus oldStatus = reservation.getStatus();
        reservation.setStatus(ReservationStatus.CHECKED_IN);

        Reservation updated = reservationRepository.save(reservation);

        saveStatusHistory(reservationId, oldStatus.name(), ReservationStatus.CHECKED_IN.name(),
                userId, "Check-in effectué");

        // ✅ CHANGEMENT : Notification ASYNCHRONE via RabbitMQ
        try {
            notificationEventPublisher.sendCheckInCompleted(reservation.getUserId(), reservationId);
        } catch (Exception e) {
            logger.error("❌ Failed to send notification", e);
        }

        return convertToDTO(updated);
    }

    @Transactional
    public ReservationResponseDTO checkOut(Integer reservationId, Integer userId) {
        logger.info("📝 Check-out for reservation {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Réservation non trouvée"));

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new InvalidBookingException("Seules les réservations avec check-in peuvent être check-out");
        }

        if (LocalDateTime.now().isBefore(reservation.getCheckOutDate())) {
            throw new InvalidBookingException("Le check-out ne peut être effectué avant la date prévue");
        }

        ReservationStatus oldStatus = reservation.getStatus();
        reservation.setStatus(ReservationStatus.COMPLETED);

        Reservation updated = reservationRepository.save(reservation);

        saveStatusHistory(reservationId, oldStatus.name(), ReservationStatus.COMPLETED.name(),
                userId, "Check-out effectué");

        ReservationResponseDTO responseDTO = convertToDTO(updated);
        eventPublisher.publishBookingCompleted(responseDTO);

        // Déclencher la libération de l'escrow via Payment Service
        try {
            PropertyDTO property = propertyServiceClient.getPropertyById(reservation.getPropertyId());
            Map<String, Object> host = userServiceClient.getUserById(property.getUserId());

            if (host != null && host.get("walletAdresse") != null) {
                paymentServiceClient.releaseEscrow(reservationId, (String) host.get("walletAdresse"));
                logger.info("✅ Escrow release initiated");
            }
        } catch (Exception e) {
            logger.error("❌ Failed to release escrow", e);
        }

        // ✅ CHANGEMENT : Notification ASYNCHRONE via RabbitMQ
        try {
            notificationEventPublisher.sendCheckOutCompleted(reservation.getUserId(), reservationId);
        } catch (Exception e) {
            logger.error("❌ Failed to send notification", e);
        }

        return responseDTO;
    }

    @Transactional
    public ReservationResponseDTO cancelReservation(Integer reservationId, Integer userId, String reason) {
        logger.info("📝 Cancelling reservation {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Réservation non trouvée"));

        if (reservation.getStatus() == ReservationStatus.COMPLETED ||
                reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new InvalidBookingException("Cette réservation ne peut pas être annulée");
        }

        ReservationStatus oldStatus = reservation.getStatus();
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());

        Reservation updated = reservationRepository.save(reservation);

        saveStatusHistory(reservationId, oldStatus.name(), ReservationStatus.CANCELLED.name(),
                userId, reason != null ? reason : "Annulation demandée");

        // Débloquer les dates
        try {
            propertyServiceClient.unblockDates(reservation.getPropertyId(), reservationId);
            logger.info("✅ Dates unblocked");
        } catch (Exception e) {
            logger.error("❌ Failed to unblock dates", e);
        }

        ReservationResponseDTO responseDTO = convertToDTO(updated);
        eventPublisher.publishBookingCancelled(responseDTO, reason);

        // Initier le remboursement
        try {
            paymentServiceClient.initiateRefund(reservationId, reason);
            logger.info("✅ Refund initiated");
        } catch (Exception e) {
            logger.error("❌ Failed to initiate refund", e);
        }

        // ✅ CHANGEMENT : Notification ASYNCHRONE via RabbitMQ
        try {
            Map<String, Object> user = userServiceClient.getUserById(reservation.getUserId());
            if (user != null && user.get("email") != null) {
                notificationEventPublisher.sendBookingCancellation(
                        reservation.getUserId(),
                        reservationId,
                        (String) user.get("email"),
                        reason
                );
            }
        } catch (Exception e) {
            logger.error("❌ Failed to send notification", e);
        }

        return responseDTO;
    }

    @Transactional
    public ReservationResponseDTO releaseEscrow(Integer reservationId, String txHash) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Réservation non trouvée"));

        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new InvalidBookingException("L'escrow ne peut être libéré que pour les réservations terminées");
        }

        if (reservation.getEscrowReleased()) {
            throw new InvalidBookingException("L'escrow a déjà été libéré");
        }

        reservation.setEscrowReleased(true);
        reservation.setEscrowReleaseTxHash(txHash);

        Reservation updated = reservationRepository.save(reservation);

        return convertToDTO(updated);
    }

    private void validateDates(LocalDateTime checkInDate, LocalDateTime checkOutDate) {
        if (checkInDate.isBefore(LocalDateTime.now())) {
            throw new InvalidBookingException("La date d'arrivée doit être dans le futur");
        }

        if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
            throw new InvalidBookingException("La date de départ doit être après la date d'arrivée");
        }
    }

    private void saveStatusHistory(Integer reservationId, String oldStatus, String newStatus,
                                   Integer changedBy, String reason) {
        ReservationStatusHistory history = new ReservationStatusHistory();
        history.setReservationId(reservationId);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setReason(reason);
        statusHistoryRepository.save(history);
    }

    private ReservationResponseDTO convertToDTO(Reservation reservation) {
        ReservationResponseDTO dto = new ReservationResponseDTO();
        dto.setId(reservation.getId());
        dto.setPropertyId(reservation.getPropertyId());
        dto.setVersionId(reservation.getVersionId());
        dto.setUserId(reservation.getUserId());
        dto.setCheckInDate(reservation.getCheckInDate());
        dto.setCheckOutDate(reservation.getCheckOutDate());
        dto.setTotalNights(reservation.getTotalNights());
        dto.setNumGuests(reservation.getNumGuests());
        dto.setStatus(reservation.getStatus());
        dto.setCancelledAt(reservation.getCancelledAt());
        dto.setCreatedAt(reservation.getCreatedAt());
        dto.setBlockchainTxHash(reservation.getBlockchainTxHash());
        dto.setEscrowReleased(reservation.getEscrowReleased());
        dto.setEscrowReleaseTxHash(reservation.getEscrowReleaseTxHash());

        PriceBreakdownDTO priceBreakdown = new PriceBreakdownDTO(
                reservation.getLockedPricePerNight(),
                reservation.getBaseAmount(),
                reservation.getDiscountAmount(),
                reservation.getCleaningFee(),
                reservation.getPetFee(),
                reservation.getServiceFee(),
                reservation.getTotalAmount(),
                reservation.getPlatformFeePercentage()
        );
        dto.setPriceBreakdown(priceBreakdown);

        return dto;
    }
}