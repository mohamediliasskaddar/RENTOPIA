package com.rentaldapp.userservice.repository;

import com.rentaldapp.userservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Optional<User> findByWalletAdresse(String walletAdresse);

    Boolean existsByWalletAdresse(String walletAdresse);

    // ✅ NOUVEAU : Filtrer les utilisateurs par langue
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN UserLanguage ul ON u.id = ul.userId " +
            "JOIN Language l ON ul.languageId = l.id " +
            "WHERE l.code = :languageCode")
    List<User> findByLanguageCode(@Param("languageCode") String languageCode);

    // ✅ NOUVEAU : Filtrer les utilisateurs qui parlent plusieurs langues
    @Query("SELECT u FROM User u " +
            "JOIN UserLanguage ul ON u.id = ul.userId " +
            "WHERE ul.languageId IN :languageIds " +
            "GROUP BY u.id " +
            "HAVING COUNT(DISTINCT ul.languageId) = :count")
    List<User> findByAllLanguages(@Param("languageIds") List<Integer> languageIds,
                                  @Param("count") Long count);
}