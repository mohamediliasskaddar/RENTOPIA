package com.rentaldapp.messagingservice.controller;

import com.rentaldapp.messagingservice.model.dto.MessageDTO;
import com.rentaldapp.messagingservice.model.dto.SendMessageDTO;
import com.rentaldapp.messagingservice.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages/messages")

public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageDTO> sendMessage(
            @Valid @RequestBody SendMessageDTO sendMessageDTO,
            Authentication authentication) {
        Integer senderId = (Integer) authentication.getPrincipal();
        MessageDTO message = messageService.sendMessage(sendMessageDTO, senderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<MessageDTO>> getConversationMessages(
            @PathVariable Integer conversationId,
            Authentication authentication) {
        Integer userId = (Integer) authentication.getPrincipal();
        List<MessageDTO> messages = messageService.getConversationMessages(conversationId, userId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/conversation/{conversationId}/since")
    public ResponseEntity<List<MessageDTO>> getMessagesSince(
            @PathVariable Integer conversationId,
            @RequestParam String since,
            Authentication authentication) {
        Integer userId = (Integer) authentication.getPrincipal();
        LocalDateTime sinceDateTime = LocalDateTime.parse(since);
        List<MessageDTO> messages = messageService.getMessagesSince(conversationId, userId, sinceDateTime);
        return ResponseEntity.ok(messages);
    }

    @PatchMapping("/conversation/{conversationId}/mark-read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable Integer conversationId,
            Authentication authentication) {
        Integer userId = (Integer) authentication.getPrincipal();
        messageService.markMessagesAsRead(conversationId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Messages marqués comme lus");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversation/{conversationId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @PathVariable Integer conversationId,
            Authentication authentication) {
        Integer userId = (Integer) authentication.getPrincipal();
        Long count = messageService.getUnreadCount(conversationId, userId);

        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversation/{conversationId}/total-count")
    public ResponseEntity<Map<String, Long>> getTotalCount(
            @PathVariable Integer conversationId,
            Authentication authentication) {
        Integer userId = (Integer) authentication.getPrincipal();
        Long count = messageService.getTotalMessageCount(conversationId, userId);

        Map<String, Long> response = new HashMap<>();
        response.put("totalCount", count);
        return ResponseEntity.ok(response);
    }
}