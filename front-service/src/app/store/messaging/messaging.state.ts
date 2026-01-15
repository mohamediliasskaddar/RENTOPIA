// src/app/store/messaging/messaging.state.ts

import { Conversation, ConversationSummary, Message } from '../../core/models/messaging.model';

export interface MessagingState {
  conversations: ConversationSummary[];
  currentConversation: Conversation | null;
  messages: { [conversationId: number]: Message[] };
  selectedConversationId: number | null;
  loading: boolean;
  sending: boolean;
  error: string | null;
  wsConnected: boolean;
}

export const initialMessagingState: MessagingState = {
  conversations: [],
  currentConversation: null,
  messages: {},
  selectedConversationId: null,
  loading: false,
  sending: false,
  error: null,
  wsConnected: false
};
