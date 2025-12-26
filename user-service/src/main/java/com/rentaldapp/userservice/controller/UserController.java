package com.rentaldapp.userservice.controller;

import com.rentaldapp.userservice.model.dto.AddLanguageDTO;
import com.rentaldapp.userservice.model.dto.UserLanguageDTO;
import com.rentaldapp.userservice.model.dto.UserResponseDTO;
import com.rentaldapp.userservice.model.entity.UserLanguage.ProficiencyLevel;
import com.rentaldapp.userservice.service.UserLanguageService;
import com.rentaldapp.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.rentaldapp.userservice.model.dto.BookingUserInfoDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")

public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserLanguageService userLanguageService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserResponseDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Integer id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(
            @RequestParam(required = false) String language) {

        List<UserResponseDTO> users;

        // ✅ NOUVEAU : Filtrer par langue si paramètre fourni
        if (language != null && !language.isEmpty()) {
            users = userService.getUsersByLanguage(language);
        } else {
            users = userService.getAllUsers();
        }

        return ResponseEntity.ok(users);
    }
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Integer id,
            @RequestBody UserResponseDTO updateDTO) {
        UserResponseDTO updatedUser = userService.updateUser(id, updateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Utilisateur supprimé avec succès");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/toggle-host")
    public ResponseEntity<UserResponseDTO> toggleHostRole(@PathVariable Integer id) {
        UserResponseDTO user = userService.toggleHostRole(id);
        return ResponseEntity.ok(user);
    }

    // ✅ NOUVEAU : Gestion des langues de l'utilisateur

    @GetMapping("/{id}/languages")
    public ResponseEntity<List<UserLanguageDTO>> getUserLanguages(@PathVariable Integer id) {
        List<UserLanguageDTO> languages = userLanguageService.getUserLanguages(id);
        return ResponseEntity.ok(languages);
    }

    @PostMapping("/{id}/languages")
    public ResponseEntity<UserLanguageDTO> addLanguageToUser(
            @PathVariable Integer id,
            @Valid @RequestBody AddLanguageDTO addLanguageDTO) {
        UserLanguageDTO language = userLanguageService.addLanguageToUser(id, addLanguageDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(language);
    }

    @DeleteMapping("/{id}/languages/{languageId}")
    public ResponseEntity<Map<String, String>> removeLanguageFromUser(
            @PathVariable Integer id,
            @PathVariable Integer languageId) {
        userLanguageService.removeLanguageFromUser(id, languageId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Langue supprimée avec succès");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/languages/{languageId}/proficiency")
    public ResponseEntity<UserLanguageDTO> updateLanguageProficiency(
            @PathVariable Integer id,
            @PathVariable Integer languageId,
            @RequestParam ProficiencyLevel proficiencyLevel) {
        UserLanguageDTO updated = userLanguageService.updateUserLanguageProficiency(
                id, languageId, proficiencyLevel);
        return ResponseEntity.ok(updated);
    }

    // ✅ NOUVEAU : Endpoint pour Booking Service
    @GetMapping("/{id}/booking-info")
    public ResponseEntity<BookingUserInfoDTO> getUserInfoForBooking(@PathVariable Integer id) {
        UserResponseDTO user = userService.getUserById(id);

        BookingUserInfoDTO bookingInfo = new BookingUserInfoDTO();
        bookingInfo.setUserId(user.getId());
        bookingInfo.setFullName(user.getNom() + " " + user.getPrenom());
        bookingInfo.setEmail(user.getEmail());
        bookingInfo.setPhone(user.getTel());
        bookingInfo.setWalletAddress(user.getWalletAdresse());
        bookingInfo.setIsHost(user.getIsHost());
        bookingInfo.setIsGuest(user.getIsGuest());

        return ResponseEntity.ok(bookingInfo);
    }

    // ✅ NOUVEAU : Endpoint batch pour Booking Service
    @PostMapping("/batch/booking-info")
    public ResponseEntity<List<BookingUserInfoDTO>> getUsersInfoForBooking(@RequestBody List<Integer> userIds) {
        List<BookingUserInfoDTO> result = userService.getUsersBookingInfo(userIds);
        return ResponseEntity.ok(result);
    }
    /**
     * ✅ NOUVEAU : Récupérer uniquement l'email d'un utilisateur
     * Endpoint: GET /api/v1/users/{id}/email
     */
    @GetMapping("/{id}/email")
    public ResponseEntity<String> getUserEmail(@PathVariable Integer id) {
        String email = userService.getUserEmail(id);
        return ResponseEntity.ok(email);
    }

    /**
     * ✅ NOUVEAU : Récupérer l'adresse wallet d'un utilisateur
     * Endpoint: GET /api/v1/users/{id}/wallet
     */
    @GetMapping("/{id}/wallet")
    public ResponseEntity<String> getUserWallet(@PathVariable Integer id) {
        String wallet = userService.getUserWallet(id);
        return ResponseEntity.ok(wallet);
    }

    /**
     * ✅ NOUVEAU : Vérifier si un utilisateur existe
     * Endpoint: GET /api/v1/users/{id}/exists
     */
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> userExists(@PathVariable Integer id) {
        boolean exists = userService.userExists(id);
        return ResponseEntity.ok(exists);
    }
}
