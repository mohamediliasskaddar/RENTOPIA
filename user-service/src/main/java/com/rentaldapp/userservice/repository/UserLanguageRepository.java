package com.rentaldapp.userservice.repository;

import com.rentaldapp.userservice.model.entity.UserLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLanguageRepository extends JpaRepository<UserLanguage, Integer> {

    List<UserLanguage> findByUserId(Integer userId);

    Optional<UserLanguage> findByUserIdAndLanguageId(Integer userId, Integer languageId);

    void deleteByUserIdAndLanguageId(Integer userId, Integer languageId);

    boolean existsByUserIdAndLanguageId(Integer userId, Integer languageId);

    // Trouver tous les utilisateurs qui parlent une langue donnée
    @Query("SELECT ul.userId FROM UserLanguage ul WHERE ul.languageId = :languageId")
    List<Integer> findUserIdsByLanguageId(@Param("languageId") Integer languageId);

    // Trouver tous les utilisateurs qui parlent une langue par code
    @Query("SELECT ul.userId FROM UserLanguage ul JOIN ul.language l WHERE l.code = :languageCode")
    List<Integer> findUserIdsByLanguageCode(@Param("languageCode") String languageCode);
}