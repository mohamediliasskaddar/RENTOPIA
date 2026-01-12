package com.rentaldapp.messagingservice.controller;

import com.rentaldapp.messagingservice.model.dto.ConversationDTO;
import com.rentaldapp.messagingservice.model.dto.ConversationSummaryDTO;
import com.rentaldapp.messagingservice.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages/conversations")

public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @PostMapping
    public ResponseEntity<ConversationDTO> createConversation(
            @RequestParam Integer reservationId,
            @RequestParam Integer tenantId,
            @RequestParam Integer hostId) {
        ConversationDTO conversation = conversationService.createConversation(reservationId, tenantId, hostId);
        return ResponseEntity.status(HttpStatus.CREATED).body(conversation);
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationDTO> getConversationById(
            @PathVariable Integer conversationId,
            Authentication authentication) {
        Integer userId = (Integer) authentication.getPrincipal();
        ConversationDTO conversation = conversationService.getConversationById(conversationId, userId);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<ConversationDTO> getConversationByReservationId(
            @PathVariable Integer reservationId,
            Authentication authentication) {
        Integer userId = (Integer) authentication.getPrincipal();
        ConversationDTO conversation = conversationService.getConversationByReservationId(reservationId, userId);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/my-conversations")
    public ResponseEntity<List<ConversationSummaryDTO>> getMyConversations(Authentication authentication) {
        Integer userId = (Integer) authentication.getPrincipal();
        List<ConversationSummaryDTO> conversations = conversationService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    @PatchMapping("/{conversationId}/archive")
    public ResponseEntity<ConversationDTO> archiveConversation(
            @PathVariable Integer conversationId,
            Authentication authentication) {
        Integer userId = (Integer) authentication.getPrincipal();
        ConversationDTO conversation = conversationService.archiveConversation(conversationId, userId);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/{conversationId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @PathVariable Integer conversationId,
            Authentication authentication) {
        Integer userId = (Integer) authentication.getPrincipal();
        Long count = conversationService.getUnreadCount(conversationId, userId);

        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "messaging-service");
        return ResponseEntity.ok(response);
    }
}