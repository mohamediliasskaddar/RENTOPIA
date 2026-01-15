import { Component, OnInit, OnDestroy, ViewChild, ElementRef, inject, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { Store } from '@ngrx/store';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

import * as MessagingActions from '../../../store/messaging/messaging.actions';
import * as MessagingSelectors from '../../../store/messaging/messaging.selectors';
import * as AuthSelectors from '../../../store/auth/auth.selectors';
import {ConversationStatus, Message, MessageWithSender} from '../../../core/models/messaging.model';
import {UserService} from "../../../core/services/user.service";

@Component({
  selector: 'app-chat-view',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatToolbarModule,
    MatTooltipModule,
    MatMenuModule
  ],
  template: `
    <div class="chat-container">
      <!-- Header moderne avec gradient -->
      <div class="chat-header">
        <button mat-icon-button class="back-btn" (click)="goBack()">
          <mat-icon>arrow_back</mat-icon>
        </button>

        <div class="header-info" (click)="showParticipantInfo()">
          <div class="participant-avatar" [style.background]="getAvatarColor(otherParticipantName)">
            <span>{{ getInitials(otherParticipantName) }}</span>
          </div>
          <div class="participant-details">
            <h2 class="participant-name">{{ otherParticipantName }}</h2>
            <span class="participant-status" [class.online]="wsConnected">
              <span class="status-dot"></span>
              {{ wsConnected ? 'En ligne' : 'Hors ligne' }}
            </span>
          </div>
        </div>

        <div class="header-actions">
          <button mat-icon-button [matMenuTriggerFor]="menu" class="more-btn">
            <mat-icon>more_vert</mat-icon>
          </button>
        </div>

        <mat-menu #menu="matMenu">
          <button mat-menu-item>
            <mat-icon>info</mat-icon>
            <span>Informations</span>
          </button>
          <button mat-menu-item>
            <mat-icon>notifications_off</mat-icon>
            <span>Désactiver les notifications</span>
          </button>
        </mat-menu>
      </div>

      <!-- Zone de messages -->
      <div class="messages-area" #messagesContainer>
        <!-- Loading -->
        <div *ngIf="loading" class="loading-container">
          <mat-spinner diameter="48"></mat-spinner>
          <p>Chargement des messages...</p>
        </div>

        <!-- Empty state -->
        <div *ngIf="!loading && messages.length === 0" class="empty-chat">
          <div class="empty-icon">
            <mat-icon>chat_bubble_outline</mat-icon>
          </div>
          <h3>Commencez la conversation</h3>
          <p>Envoyez votre premier message à {{ otherParticipantName }}</p>
        </div>

        <!-- Messages -->
        <div *ngIf="!loading && messages.length > 0" class="messages-flow">
          <ng-container *ngFor="let message of messages; let i = index">
            <!-- Date separator -->
            <div *ngIf="shouldShowDate(i)" class="date-divider">
              <span class="date-label">{{ formatMessageDate(message.createdAt) }}</span>
            </div>

            <!-- Message bubble -->
            <div class="message-row" [class.mine]="message.isMine">
              <div class="message-bubble" [class.mine]="message.isMine">
                <p class="message-text">{{ message.messageText }}</p>
                <div class="message-footer">
                  <span class="message-time">{{ formatMessageTime(message.createdAt) }}</span>
                  <mat-icon *ngIf="message.isMine" class="read-status"
                    [class.read]="message.isRead"
                    [matTooltip]="message.isRead ? 'Lu' : 'Envoyé'">
                    {{ message.isRead ? 'done_all' : 'done' }}
                  </mat-icon>
                </div>
              </div>
            </div>
          </ng-container>

          <!-- Typing indicator (optionnel) -->
          <div class="message-row typing-indicator" *ngIf="false">
            <div class="message-bubble typing">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
          </div>
        </div>
      </div>
      <div *ngIf="!isConversationActive" class="conversation-disabled-banner">
        <mat-icon>lock</mat-icon>
        <span>
    Cette conversation n'est plus active.
    <ng-container *ngIf="conversationStatus === 'EXPIRED'">
      Elle a expiré.
    </ng-container>
    <ng-container *ngIf="conversationStatus === 'ARCHIVED'">
      Elle a été archivée.
    </ng-container>
  </span>
      </div>
      <!-- Input moderne -->
      <div class="message-input-area">
        <form [formGroup]="messageForm" (ngSubmit)="sendMessage()" class="input-form">
          <button type="button" mat-icon-button class="attach-btn">
            <mat-icon>add_circle_outline</mat-icon>
          </button>

          <div class="input-wrapper">
            <input
              type="text"
              formControlName="messageText"
              placeholder="Tapez votre message..."
              class="message-input"
              (keydown.enter)="$event.preventDefault(); sendMessage()"
              [disabled]="!isConversationActive || sending"
              #messageInput>
          </div>

          <button
            type="submit"
            mat-mini-fab
            color="primary"
            [disabled]="!isConversationActive || messageForm.invalid || sending"
            class="send-btn">
            <mat-icon>{{ sending ? 'hourglass_empty' : 'send' }}</mat-icon>
          </button>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .chat-container {
      display: flex;
      flex-direction: column;
      height: 100vh;
      background: #f8f9fa;
    }

    /* ===== HEADER ===== */
    .chat-header {
      background: var(--color-bg-dark, #16243e);
      color: white;
      padding: 12px 16px;
      display: flex;
      align-items: center;
      gap: 12px;
      box-shadow: 0 2px 12px rgba(7, 19, 74, 0.3);
      position: sticky;
      top: 0;
      z-index: 10;
    }

    .back-btn, .more-btn {
      color: white !important;

      mat-icon {
        color: white;
      }
    }

    .header-info {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 12px;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 8px;
      transition: background 0.2s;

      &:hover {
        background: rgba(255, 255, 255, 0.1);
      }
    }

    .participant-avatar {
      width: 44px;
      height: 44px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      font-size: 16px;
      color: white;
      border: 3px solid rgba(255, 255, 255, 0.3);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    }

    .participant-details {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .participant-name {
      margin: 0;
      font-size: 17px;
      font-weight: 600;
      line-height: 1.2;
    }

    .participant-status {
      font-size: 13px;
      display: flex;
      align-items: center;
      gap: 6px;
      opacity: 0.9;
    }

    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #9ca3af;
      display: inline-block;
    }

    .participant-status.online .status-dot {
      background: #10b981;
      box-shadow: 0 0 8px rgba(16, 185, 129, 0.6);
    }

    .header-actions {
      display: flex;
      gap: 4px;
    }

    /* ===== MESSAGES AREA ===== */
    .messages-area {
      flex: 1;
      overflow-y: auto;
      padding: 20px 16px;
      background: #f8f9fa;
      scroll-behavior: smooth;

      &::-webkit-scrollbar {
        width: 6px;
      }

      &::-webkit-scrollbar-track {
        background: transparent;
      }

      &::-webkit-scrollbar-thumb {
        background: #d1d5db;
        border-radius: 3px;

        &:hover {
          background: #9ca3af;
        }
      }
    }

    /* ===== LOADING ===== */
    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      gap: 20px;

      p {
        margin: 0;
        color: #6b7280;
        font-size: 15px;
      }
    }

    /* ===== EMPTY CHAT ===== */
    .empty-chat {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      text-align: center;
      padding: 40px;
    }

    .empty-icon {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      background: var(--color-bg-dark, #16243e);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 24px;
      box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);

      mat-icon {
        font-size: 40px;
        width: 40px;
        height: 40px;
        color: white;
      }
    }

    .empty-chat h3 {
      margin: 0 0 8px 0;
      font-size: 20px;
      font-weight: 600;
      color: #1a1a1a;
    }

    .empty-chat p {
      margin: 0;
      color: #6b7280;
      font-size: 15px;
    }

    /* ===== MESSAGES FLOW ===== */
    .messages-flow {
      display: flex;
      flex-direction: column;
      gap: 8px;
      max-width: 900px;
      margin: 0 auto;
      width: 100%;
    }

    /* ===== DATE DIVIDER ===== */
    .date-divider {
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 16px 0;
    }

    .date-label {
      background: white;
      color: #6b7280;
      padding: 6px 16px;
      border-radius: 20px;
      font-size: 13px;
      font-weight: 600;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    /* ===== MESSAGE ROW ===== */
    .message-row {
      display: flex;
      animation: messageSlideIn 0.3s ease;

      &.mine {
        justify-content: flex-end;
      }
    }

    @keyframes messageSlideIn {
      from {
        opacity: 0;
        transform: translateY(10px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    /* ===== MESSAGE BUBBLE ===== */
    .message-bubble {
      max-width: 70%;
      padding: 12px 16px;
      border-radius: 18px;
      background: white;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
      position: relative;
      word-wrap: break-word;

      &.mine {
        background: var(--color-bg-dark, #16243e);
        color: white;
        border-bottom-right-radius: 4px;
      }

      &:not(.mine) {
        background: white;
        color: #1a1a1a;
        border-bottom-left-radius: 4px;
      }

      &.typing {
        padding: 16px 20px;
        display: flex;
        gap: 6px;
        align-items: center;
      }
    }

    .message-text {
      margin: 0 0 6px 0;
      font-size: 15px;
      line-height: 1.5;
      word-break: break-word;
    }

    .message-footer {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: 6px;
      margin-top: 4px;
    }

    .message-time {
      font-size: 11px;
      opacity: 0.7;
      font-weight: 500;
    }

    .read-status {
      font-size: 16px;
      width: 16px;
      height: 16px;
      opacity: 0.7;

      &.read {
        color: #10b981;
        opacity: 1;
      }
    }

    /* ===== TYPING INDICATOR ===== */
    .typing-indicator .dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #9ca3af;
      display: inline-block;
      animation: typingBounce 1.4s infinite;

      &:nth-child(2) {
        animation-delay: 0.2s;
      }

      &:nth-child(3) {
        animation-delay: 0.4s;
      }
    }

    @keyframes typingBounce {
      0%, 60%, 100% {
        transform: translateY(0);
      }
      30% {
        transform: translateY(-8px);
      }
    }

    /* ===== INPUT AREA ===== */
    .message-input-area {
      background: white;
      border-top: 1px solid #e5e7eb;
      padding: 16px;
      box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.05);
    }

    .input-form {
      max-width: 900px;
      margin: 0 auto;
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .attach-btn {
      color: #6b7280;
      flex-shrink: 0;

      &:hover {
        background: #f3f4f6;
        color: #1a1a1a;
      }
    }

    .input-wrapper {
      flex: 1;
      background: #f3f4f6;
      border-radius: 24px;
      padding: 0 20px;
      transition: all 0.2s;

      &:focus-within {
        background: #e5e7eb;
        box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
      }
    }

    .message-input {
      width: 100%;
      border: none;
      background: transparent;
      padding: 14px 0;
      font-size: 15px;
      color: #1a1a1a;
      outline: none;

      &::placeholder {
        color: #9ca3af;
      }

      &:disabled {
        cursor: not-allowed;
        opacity: 0.6;
      }
    }

    .send-btn {
      flex-shrink: 0;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4) !important;
      transition: all 0.2s;

      &:hover:not(:disabled) {
        transform: scale(1.05);
        box-shadow: 0 6px 16px rgba(102, 126, 234, 0.5) !important;
      }

      &:active:not(:disabled) {
        transform: scale(0.95);
      }

      &:disabled {
        opacity: 0.5;
        box-shadow: none !important;
      }
    }

    /* ===== RESPONSIVE ===== */
    @media (max-width: 768px) {
      .chat-header {
        padding: 10px 12px;
      }

      .participant-avatar {
        width: 40px;
        height: 40px;
        font-size: 15px;
      }

      .participant-name {
        font-size: 16px;
      }

      .messages-area {
        padding: 16px 12px;
      }

      .message-bubble {
        max-width: 85%;
        padding: 10px 14px;
      }

      .message-input-area {
        padding: 12px;
      }
      .conversation-disabled-banner {
        background: #fef3c7;
        color: #92400e;
        border-top: 1px solid #fde68a;
        padding: 12px 16px;
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 14px;
        font-weight: 500;
      }

    }
  `]
})
export class ChatViewComponent implements OnInit, OnDestroy, AfterViewChecked {
  private store = inject(Store);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private destroy$ = new Subject<void>();

  @ViewChild('messagesContainer') messagesContainer!: ElementRef;
  @ViewChild('messageInput') messageInput!: ElementRef;

  private shouldScrollToBottom = false;
  private userService = inject(UserService);
  conversationId!: number;
  currentUserId!: number;
  messages: MessageWithSender[] = [];
  otherParticipantName = 'Chargement...';
  messageForm: FormGroup;
  loading = false;
  sending = false;
  wsConnected = false;

  conversationStatus!: ConversationStatus;

  private userCache = new Map<number, string>();
  constructor() {
    this.messageForm = this.fb.group({
      messageText: ['', [Validators.required, Validators.maxLength(5000)]]
    });
  }

  ngOnInit(): void {
    this.route.params.pipe(takeUntil(this.destroy$)).subscribe(params => {
      this.conversationId = +params['id'];
      this.loadConversation();
    });

    this.store.select(AuthSelectors.selectUserId)
      .pipe(takeUntil(this.destroy$))
      .subscribe(userId => {
        if (userId) {
          this.currentUserId = userId;
        }
      });



    this.store.select(MessagingSelectors.selectMessages)
      .pipe(takeUntil(this.destroy$))
      .subscribe(messages => {
        const oldLength = this.messages.length;
        this.messages = messages.map(msg => this.enrichMessage(msg));
        if (this.messages.length > oldLength) {
          this.shouldScrollToBottom = true;
        }
      });


    this.store.select(MessagingSelectors.selectLoading)
      .pipe(takeUntil(this.destroy$))
      .subscribe(loading => {
        this.loading = loading;
      });


    this.store.select(MessagingSelectors.selectSending)
      .pipe(takeUntil(this.destroy$))
      .subscribe(sending => {
        this.sending = sending;
      });

    this.store.select(MessagingSelectors.selectWebSocketConnected)
      .pipe(takeUntil(this.destroy$))
      .subscribe(connected => {
        this.wsConnected = connected;
      });

    this.store.select(MessagingSelectors.selectCurrentConversation)
      .pipe(
        takeUntil(this.destroy$),
        filter(conv => !!conv)
      )
      .subscribe(conversation => {
        this.conversationStatus = conversation.status;

        const otherParticipant = conversation.participants.find(
          p => p.userId !== this.currentUserId
        );

        if (otherParticipant) {
          this.loadUserName(otherParticipant.userId);
        }
      });

  }
  get isConversationActive(): boolean {
    return this.conversationStatus === ConversationStatus.ACTIVE;
  }

  private loadUserName(userId: number): void {
    if (this.userCache.has(userId)) return;

    this.userService.getUserById(userId).subscribe({
      next: user => {
        const fullName = `${user.prenom} ${user.nom}`;
        this.userCache.set(userId, fullName);

        // mettre à jour le header si besoin
        if (userId !== this.currentUserId) {
          this.otherParticipantName = fullName;
        }

        // forcer refresh messages
        this.messages = this.messages.map(msg => this.enrichMessage(msg));
      },
      error: () => {
        this.userCache.set(userId, `Utilisateur ${userId}`);
      }
    });
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadConversation(): void {
    this.store.dispatch(MessagingActions.selectConversation({
      conversationId: this.conversationId
    }));
    this.store.dispatch(MessagingActions.loadConversationDetails({
      conversationId: this.conversationId
    }));
    this.store.dispatch(MessagingActions.loadMessages({
      conversationId: this.conversationId
    }));
    this.store.dispatch(MessagingActions.joinConversation({
      conversationId: this.conversationId
    }));
    this.store.dispatch(MessagingActions.markMessagesAsRead({
      conversationId: this.conversationId
    }));
  }

  private enrichMessage(message: Message): MessageWithSender {
    if (message.senderId !== this.currentUserId) {
      this.loadUserName(message.senderId);
    }

    return {
      ...message,
      isMine: message.senderId === this.currentUserId,
      senderName:
        message.senderId === this.currentUserId
          ? 'Vous'
          : this.userCache.get(message.senderId) || `Utilisateur ${message.senderId}`
    };
  }


  sendMessage(): void {
    if (!this.isConversationActive) {
      return;
    }

    if (this.messageForm.invalid || this.sending) {
      return;
    }

    const messageText = this.messageForm.value.messageText.trim();
    if (!messageText) return;

    this.store.dispatch(MessagingActions.sendMessage({
      request: {
        conversationId: this.conversationId,
        messageText
      }
    }));

    this.messageForm.reset();
  }

  goBack(): void {
    this.router.navigate(['/messages']);
  }

  showParticipantInfo(): void {
    // Implémenter la logique pour afficher les infos du participant
    console.log('Show participant info');
  }

  getInitials(name: string): string {
    return name
      .split(' ')
      .map(word => word[0])
      .join('')
      .substring(0, 2)
      .toUpperCase();
  }

  getAvatarColor(name: string): string {
    const colors = [
      'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
      'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
      'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
    ];
    const index = name.charCodeAt(0) % colors.length;
    return colors[index];
  }

  formatMessageTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatMessageDate(dateString: string): string {
    const date = new Date(dateString);
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    if (date.toDateString() === today.toDateString()) {
      return "Aujourd'hui";
    } else if (date.toDateString() === yesterday.toDateString()) {
      return 'Hier';
    } else {
      return date.toLocaleDateString('fr-FR', {
        day: 'numeric',
        month: 'long',
        year: 'numeric'
      });
    }
  }

  shouldShowDate(index: number): boolean {
    if (index === 0) return true;
    const current = new Date(this.messages[index].createdAt).toDateString();
    const previous = new Date(this.messages[index - 1].createdAt).toDateString();
    return current !== previous;
  }

  private scrollToBottom(): void {
    if (this.messagesContainer) {
      const element = this.messagesContainer.nativeElement;
      element.scrollTop = element.scrollHeight;
    }
  }
}
