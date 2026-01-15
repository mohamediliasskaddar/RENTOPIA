// src/app/store/messaging/messaging.reducer.ts

import { createReducer, on } from '@ngrx/store';
import * as MessagingActions from './messaging.actions';
import { initialMessagingState } from './messaging.state';

export const messagingReducer = createReducer(
  initialMessagingState,

  // ==================== CONVERSATIONS ====================

  on(MessagingActions.loadConversations, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(MessagingActions.loadConversationsSuccess, (state, { conversations }) => ({
    ...state,
    conversations,
    loading: false
  })),

  on(MessagingActions.loadConversationsFailure, (state, { error }) => ({
    ...state,
    error,
    loading: false
  })),

  on(MessagingActions.selectConversation, (state, { conversationId }) => ({
    ...state,
    selectedConversationId: conversationId
  })),

  on(MessagingActions.loadConversationDetailsSuccess, (state, { conversation }) => ({
    ...state,
    currentConversation: conversation
  })),

  on(MessagingActions.createConversationSuccess, (state, { conversation }) => ({
    ...state,
    currentConversation: conversation,
    selectedConversationId: conversation.id
  })),

  // ==================== MESSAGES ====================

  on(MessagingActions.loadMessages, (state) => ({
    ...state,
    loading: true
  })),

  on(MessagingActions.loadMessagesSuccess, (state, { conversationId, messages }) => ({
    ...state,
    messages: {
      ...state.messages,
      [conversationId]: messages
    },
    loading: false
  })),

  on(MessagingActions.sendMessage, (state) => ({
    ...state,
    sending: true,
    error: null
  })),

  on(MessagingActions.sendMessageSuccess, (state, { message }) => {
    const conversationMessages = state.messages[message.conversationId] || [];
    return {
      ...state,
      messages: {
        ...state.messages,
        [message.conversationId]: [...conversationMessages, message]
      },
      sending: false
    };
  }),

  on(MessagingActions.sendMessageFailure, (state, { error }) => ({
    ...state,
    sending: false,
    error
  })),

  on(MessagingActions.markMessagesAsReadSuccess, (state, { conversationId }) => {
    const conversationMessages = state.messages[conversationId] || [];
    return {
      ...state,
      messages: {
        ...state.messages,
        [conversationId]: conversationMessages.map(msg => ({
          ...msg,
          isRead: true
        }))
      }
    };
  }),

  // ==================== WEBSOCKET ====================

  on(MessagingActions.webSocketConnected, (state) => ({
    ...state,
    wsConnected: true
  })),

  on(MessagingActions.webSocketDisconnected, (state) => ({
    ...state,
    wsConnected: false
  })),

  on(MessagingActions.receiveWebSocketMessage, (state, { message }) => {
    const conversationMessages = state.messages[message.conversationId] || [];
    const messageExists = conversationMessages.some(m => m.id === message.id);

    if (messageExists) {
      return state;
    }

    return {
      ...state,
      messages: {
        ...state.messages,
        [message.conversationId]: [...conversationMessages, message]
      }
    };
  }),

  // ✅ NOUVEAU: Gérer loadConversationByReservation
  on(MessagingActions.loadConversationByReservation, (state) => ({
    ...state,
    loading: true,
    currentConversation: null // ⚠️ Reset à null, pas undefined
  })),

  on(MessagingActions.loadConversationByReservationSuccess, (state, { conversation }) => ({
    ...state,
    currentConversation: conversation, // Peut être null si pas trouvée
    loading: false
  })),

  on(MessagingActions.loadConversationByReservationFailure, (state, { error }) => ({
    ...state,
    currentConversation: null, // ⚠️ Important: null, pas undefined
    loading: false,
    error
  })),

  // ✅ OPTIONNEL: Clear current conversation
  on(MessagingActions.clearCurrentConversation, (state) => ({
    ...state,
    currentConversation: null // ⚠️ null, pas undefined
  }))
);
