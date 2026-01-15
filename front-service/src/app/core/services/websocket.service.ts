// ============================================================================
// 2. SERVICE WEBSOCKET (TEMPS RÉEL)
// ============================================================================
// src/app/core/services/websocket.service.ts

import { Injectable } from '@angular/core';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Message } from '../models/messaging.model';

export enum WebSocketMessageType {
  NEW_MESSAGE = 'new_message',
  MESSAGES_READ = 'messages_read',
  ERROR = 'error'
}

export interface WebSocketMessage {
  type: WebSocketMessageType;
  data?: any;
  message?: string;
}

export interface WebSocketAction {
  action: 'send_message' | 'mark_read' | 'join_conversation';
  conversationId?: number;
  messageText?: string;
}

@Injectable({

  providedIn: 'root'
})
export class WebSocketService {
  private ws: WebSocket | null = null;
  private messageSubject = new Subject<WebSocketMessage>();
  private connectionStatus = new BehaviorSubject<boolean>(false);
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectInterval = 3000;

  public messages$ = this.messageSubject.asObservable();
  public connected$ = this.connectionStatus.asObservable();

  /**
   * Se connecter au WebSocket avec le token JWT
   */
  connect(token: string): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      console.log('⚡ WebSocket already connected');
      return;
    }

    // ✅ Utilisation de environment.wsUrl
    const wsUrl = environment.wsUrl;
    const urlWithToken = `${wsUrl}?token=${token}`;

    console.log('🔌 Connecting to WebSocket...', wsUrl);

    try {
      this.ws = new WebSocket(urlWithToken);

      this.ws.onopen = () => {
        console.log('✅ WebSocket connected');
        this.connectionStatus.next(true);
        this.reconnectAttempts = 0;
      };

      this.ws.onmessage = (event) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data);
          console.log('📨 WebSocket message received:', message);
          this.messageSubject.next(message);
        } catch (error) {
          console.error('❌ Error parsing WebSocket message:', error);
        }
      };

      this.ws.onerror = (error) => {
        console.error('❌ WebSocket error:', error);
      };

      this.ws.onclose = (event) => {
        console.log('🔌 WebSocket closed:', event.code, event.reason);
        this.connectionStatus.next(false);
        this.attemptReconnect(token);
      };

    } catch (error) {
      console.error('❌ Error creating WebSocket:', error);
    }
  }

  /**
   * Tentative de reconnexion automatique
   */
  private attemptReconnect(token: string): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`🔄 Attempting reconnect ${this.reconnectAttempts}/${this.maxReconnectAttempts}...`);

      setTimeout(() => {
        this.connect(token);
      }, this.reconnectInterval);
    } else {
      console.error('❌ Max reconnect attempts reached');
    }
  }

  /**
   * Envoyer un message via WebSocket
   */
  send(action: WebSocketAction): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(action));
      console.log('📤 Sent WebSocket action:', action);
    } else {
      console.error('❌ WebSocket not connected');
    }
  }

  /**
   * Rejoindre une conversation
   */
  joinConversation(conversationId: number): void {
    this.send({
      action: 'join_conversation',
      conversationId
    });
  }

  /**
   * Envoyer un message (via WebSocket pour temps réel)
   */
  sendMessage(conversationId: number, messageText: string): void {
    this.send({
      action: 'send_message',
      conversationId,
      messageText
    });
  }

  /**
   * Marquer comme lu
   */
  markAsRead(conversationId: number): void {
    this.send({
      action: 'mark_read',
      conversationId
    });
  }

  /**
   * Déconnecter le WebSocket
   */
  disconnect(): void {
    if (this.ws) {
      console.log('🔌 Disconnecting WebSocket...');
      this.ws.close();
      this.ws = null;
      this.connectionStatus.next(false);
    }
  }

  /**
   * Vérifier si connecté
   */
  isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN;
  }
}
