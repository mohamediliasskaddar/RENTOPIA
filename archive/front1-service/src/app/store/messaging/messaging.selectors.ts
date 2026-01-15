// src/app/store/messaging/messaging.selectors.ts

import { createFeatureSelector, createSelector } from '@ngrx/store';
import { MessagingState } from './messaging.state';

export const selectMessagingState = createFeatureSelector<MessagingState>('messaging');

export const selectAllConversations = createSelector(
  selectMessagingState,
  (state) => state.conversations
);

export const selectCurrentConversation = createSelector(
  selectMessagingState,
  (state) => state.currentConversation
);

export const selectSelectedConversationId = createSelector(
  selectMessagingState,
  (state) => state.selectedConversationId
);

export const selectMessages = createSelector(
  selectMessagingState,
  selectSelectedConversationId,
  (state, conversationId) =>
    conversationId ? state.messages[conversationId] || [] : []
);

export const selectLoading = createSelector(
  selectMessagingState,
  (state) => state.loading
);

export const selectSending = createSelector(
  selectMessagingState,
  (state) => state.sending
);

export const selectError = createSelector(
  selectMessagingState,
  (state) => state.error
);

export const selectWebSocketConnected = createSelector(
  selectMessagingState,
  (state) => state.wsConnected
);

export const selectTotalUnreadCount = createSelector(
  selectAllConversations,
  (conversations) =>
    conversations.reduce((total, conv) => total + (conv.unreadCount || 0), 0)
);

export const selectConversationById = (conversationId: number) =>
  createSelector(
    selectAllConversations,
    (conversations) => conversations.find(c => c.id === conversationId)
  );
export const selectMessagingLoading = createSelector(
  selectMessagingState,
  (state) => state.loading
);
