// src/app/store/messaging/messaging.actions.ts

import { createAction, props } from '@ngrx/store';

import {
  Conversation,
  ConversationSummary,
  Message,
  SendMessageRequest,
  CreateConversationRequest
} from '../../core/models/messaging.model';

// ==================== CONVERSATIONS ====================

export const loadConversations = createAction(
  '[Messaging] Load Conversations'
);

export const loadConversationsSuccess = createAction(
  '[Messaging] Load Conversations Success',
  props<{ conversations: ConversationSummary[] }>()
);

export const loadConversationsFailure = createAction(
  '[Messaging] Load Conversations Failure',
  props<{ error: string }>()
);

export const selectConversation = createAction(
  '[Messaging] Select Conversation',
  props<{ conversationId: number }>()
);

export const loadConversationDetails = createAction(
  '[Messaging] Load Conversation Details',
  props<{ conversationId: number }>()
);

export const loadConversationDetailsSuccess = createAction(
  '[Messaging] Load Conversation Details Success',
  props<{ conversation: Conversation }>()
);

export const createConversation = createAction(
  '[Messaging] Create Conversation',
  props<{ request: CreateConversationRequest }>()
);

export const createConversationSuccess = createAction(
  '[Messaging] Create Conversation Success',
  props<{ conversation: Conversation }>()
);

export const archiveConversation = createAction(
  '[Messaging] Archive Conversation',
  props<{ conversationId: number }>()
);

// ==================== MESSAGES ====================

export const loadMessages = createAction(
  '[Messaging] Load Messages',
  props<{ conversationId: number }>()
);

export const loadMessagesSuccess = createAction(
  '[Messaging] Load Messages Success',
  props<{ conversationId: number; messages: Message[] }>()
);

export const sendMessage = createAction(
  '[Messaging] Send Message',
  props<{ request: SendMessageRequest }>()
);

export const sendMessageSuccess = createAction(
  '[Messaging] Send Message Success',
  props<{ message: Message }>()
);

export const sendMessageFailure = createAction(
  '[Messaging] Send Message Failure',
  props<{ error: string }>()
);

export const markMessagesAsRead = createAction(
  '[Messaging] Mark Messages As Read',
  props<{ conversationId: number }>()
);

export const markMessagesAsReadSuccess = createAction(
  '[Messaging] Mark Messages As Read Success',
  props<{ conversationId: number }>()
);

// ==================== WEBSOCKET ====================

export const connectWebSocket = createAction(
  '[Messaging] Connect WebSocket',
  props<{ token: string }>()
);

export const disconnectWebSocket = createAction(
  '[Messaging] Disconnect WebSocket'
);

export const webSocketConnected = createAction(
  '[Messaging] WebSocket Connected'
);

export const webSocketDisconnected = createAction(
  '[Messaging] WebSocket Disconnected'
);

export const receiveWebSocketMessage = createAction(
  '[Messaging] Receive WebSocket Message',
  props<{ message: Message }>()
);

export const joinConversation = createAction(
  '[Messaging] Join Conversation',
  props<{ conversationId: number }>()
);


// ✅ NOUVEAU: Charger conversation par réservation
export const loadConversationByReservation = createAction(
  '[Messaging] Load Conversation By Reservation',
  props<{ reservationId: number }>()
);

export const loadConversationByReservationSuccess = createAction(
  '[Messaging] Load Conversation By Reservation Success',
  props<{ conversation: Conversation }>()
);

export const loadConversationByReservationFailure = createAction(
  '[Messaging] Load Conversation By Reservation Failure',
  props<{ error: string }>()
);
export const clearCurrentConversation = createAction(
  '[Messaging] Clear Current Conversation'
);
