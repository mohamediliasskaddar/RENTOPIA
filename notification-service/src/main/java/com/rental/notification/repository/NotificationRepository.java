package com.rental.notification.repository;

import com.rental.notification.entity.Notification;
import com.rental.notification.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Trouver toutes les notifications d'un utilisateur
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);

    // Trouver les notifications non lues d'un utilisateur
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Integer userId);

    // Trouver les notifications par réservation
    List<Notification> findByReservationId(Integer reservationId);

    // Trouver les notifications par type
    List<Notification> findByNotificationType(NotificationType type);

    // Compter les notifications non lues
    long countByUserIdAndIsReadFalse(Integer userId);

    // Marquer comme lu
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id")
    void markAsRead(Long id);

    // Marquer toutes les notifications d'un user comme lues
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId")
    void markAllAsReadForUser(Integer userId);

    // Supprimer les anciennes notifications
    void deleteByCreatedAtBefore(LocalDateTime date);
}