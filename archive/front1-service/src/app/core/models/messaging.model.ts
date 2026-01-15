// src/app/core/models/messaging.model.ts

export enum ConversationStatus {
  ACTIVE = 'ACTIVE',
  ARCHIVED = 'ARCHIVED',
  EXPIRED = 'EXPIRED'
}

export enum ParticipantRole {
  TENANT = 'TENANT',
  HOST = 'HOST'
}

export interface Message {
  id: number;
  conversationId: number;
  senderId: number;
  messageText: string;
  isRead: boolean;
  readAt: string | null;
  createdAt: string;
}

export interface Participant {
  userId: number;
  role: ParticipantRole;
  lastReadAt: string | null;
  joinedAt: string;
}

export interface Conversation {
  id: number;
  reservationId: number;
  status: ConversationStatus;
  createdAt: string;
  expiresAt: string;
  participants: Participant[];
  lastMessage: Message | null;
  unreadCount: number;
}

export interface ConversationSummary {
  id: number;
  reservationId: number;
  status: ConversationStatus;
  otherParticipantId: number;
  otherParticipantName: string;
  lastMessage: Message | null;
  unreadCount: number;
  lastActivity: string;
}

export interface SendMessageRequest {
  conversationId: number;
  messageText: string;
}

export interface CreateConversationRequest {
  reservationId: number;
  tenantId: number;
  hostId: number;
}

/**
 * Message enrichi avec infos de l'expéditeur
 */
export interface MessageWithSender extends Message {
  senderName?: string;
  senderAvatar?: string;
  isMine: boolean;
}

/**
 * Conversation enrichie avec property info
 */
export interface ConversationWithProperty extends Conversation {
  propertyTitle?: string;
  propertyPhoto?: string;
  otherParticipantName?: string;
}
