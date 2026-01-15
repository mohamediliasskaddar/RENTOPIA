// ============================================================================
// SERVICE MESSAGING (API REST)
// ============================================================================
// src/app/core/services/messaging.service.ts

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  Conversation,
  ConversationSummary,
  Message,
  SendMessageRequest,
  CreateConversationRequest
} from '../models/messaging.model';

@Injectable({
  providedIn: 'root'
})
export class MessagingService {

  // ✅ CORRECTION : Utiliser le chemin de l'API Gateway
  private readonly BASE_PATH = 'messages';

  constructor(private apiService: ApiService) {}

  // ==================== CONVERSATIONS ====================

  createConversation(request: CreateConversationRequest): Observable<Conversation> {
    return this.apiService.post<Conversation>(
      `${this.BASE_PATH}/conversations`,
      null,
      { params: request }
    );
  }

  getConversationById(conversationId: number): Observable<Conversation> {
    return this.apiService.get<Conversation>(
      `${this.BASE_PATH}/conversations/${conversationId}`
    );
  }

  getConversationByReservationId(reservationId: number): Observable<Conversation> {
    return this.apiService.get<Conversation>(
      `${this.BASE_PATH}/conversations/reservation/${reservationId}`
    );
  }

  getMyConversations(): Observable<ConversationSummary[]> {
    return this.apiService.get<ConversationSummary[]>(
      `${this.BASE_PATH}/conversations/my-conversations`
    );
  }

  archiveConversation(conversationId: number): Observable<Conversation> {
    return this.apiService.patch<Conversation>(
      `${this.BASE_PATH}/conversations/${conversationId}/archive`
    );
  }

  getUnreadCount(conversationId: number): Observable<{ unreadCount: number }> {
    return this.apiService.get<{ unreadCount: number }>(
      `${this.BASE_PATH}/conversations/${conversationId}/unread-count`
    );
  }

  // ==================== MESSAGES ====================

  sendMessage(request: SendMessageRequest): Observable<Message> {
    return this.apiService.post<Message>(
      `${this.BASE_PATH}/messages`,
      request
    );
  }

  getConversationMessages(conversationId: number): Observable<Message[]> {
    return this.apiService.get<Message[]>(
      `${this.BASE_PATH}/messages/conversation/${conversationId}`
    );
  }

  getMessagesSince(conversationId: number, since: string): Observable<Message[]> {
    return this.apiService.get<Message[]>(
      `${this.BASE_PATH}/messages/conversation/${conversationId}/since`,
      { since }
    );
  }

  markMessagesAsRead(conversationId: number): Observable<{ message: string }> {
    return this.apiService.patch<{ message: string }>(
      `${this.BASE_PATH}/messages/conversation/${conversationId}/mark-read`
    );
  }

  getMessageUnreadCount(conversationId: number): Observable<{ unreadCount: number }> {
    return this.apiService.get<{ unreadCount: number }>(
      `${this.BASE_PATH}/messages/conversation/${conversationId}/unread-count`
    );
  }

  getTotalMessageCount(conversationId: number): Observable<{ totalCount: number }> {
    return this.apiService.get<{ totalCount: number }>(
      `${this.BASE_PATH}/messages/conversation/${conversationId}/total-count`
    );
  }
}
