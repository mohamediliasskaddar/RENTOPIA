import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { Store } from '@ngrx/store';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import * as MessagingActions from '../../../store/messaging/messaging.actions';
import * as MessagingSelectors from '../../../store/messaging/messaging.selectors';
import { ConversationSummary } from '../../../core/models/messaging.model';
import {UserService} from "../../../core/services/user.service";

@Component({
  selector: 'app-conversation-list',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatListModule,
    MatIconModule,
    MatBadgeModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatInputModule,
    MatFormFieldModule
  ],
  template: `
    <div class="conversations-page">
      <!-- Header moderne -->
      <div class="conversations-header">
        <div class="header-content">
          <h1 class="header-title">Messages</h1>
          <div class="header-actions">
            <button mat-icon-button class="search-btn">
              <mat-icon>search</mat-icon>
            </button>
          </div>
        </div>
      </div>

      <!-- Conteneur principal -->
      <div class="conversations-wrapper">
        <!-- Loading -->
        <div *ngIf="loading" class="loading-state">
          <mat-spinner diameter="48"></mat-spinner>
          <p class="loading-text">Chargement des conversations...</p>
        </div>

        <!-- Liste vide -->
        <div *ngIf="!loading && conversations.length === 0" class="empty-state">
          <div class="empty-icon-wrapper">
            <mat-icon>chat_bubble_outline</mat-icon>
          </div>
          <h3>Aucune conversation</h3>
          <p>Vos messages apparaîtront ici</p>
        </div>

        <!-- Liste des conversations -->
        <div *ngIf="!loading && conversations.length > 0" class="conversations-list">
          <div
            *ngFor="let conv of conversations"
            class="conversation-card"
            [class.unread]="conv.unreadCount > 0"
            (click)="openConversation(conv.id)">

            <!-- Avatar avec statut -->
            <div class="avatar-container">
              <div class="avatar" [style.background]="getAvatarColor(conv.otherParticipantName)">
                <span class="avatar-text">{{ getInitials(conv.otherParticipantName) }}</span>
              </div>
              <div class="status-indicator online"></div>
            </div>

            <!-- Contenu de la conversation -->
            <div class="conversation-body">
              <div class="conversation-header-row">
                <h3 class="participant-name">
                  {{ conv.otherParticipantName || 'Chargement...' }}
                </h3>
                <span class="timestamp">{{ formatTime(conv.lastActivity) }}</span>
              </div>

              <div class="conversation-preview-row">
                <p class="message-preview" [class.unread]="conv.unreadCount > 0">
                  <mat-icon *ngIf="isLastMessageMine(conv)" class="check-icon">
                    {{ conv.lastMessage?.isRead ? 'done_all' : 'done' }}
                  </mat-icon>
                  {{ conv.lastMessage?.messageText || 'Nouvelle conversation' }}
                </p>
                <span *ngIf="conv.unreadCount > 0" class="unread-badge">
                  {{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .conversations-page {
      height: 100vh;
      display: flex;
      flex-direction: column;
      background: #f8f9fa;
    }

    /* ===== HEADER ===== */
    .conversations-header {
      background: white;
      border-bottom: 1px solid #e5e7eb;
      padding: 0 24px;
      position: sticky;
      top: 0;
      z-index: 10;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    }

    .header-content {
      max-width: 900px;
      margin: 0 auto;
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 72px;
    }

    .header-title {
      margin: 0;
      font-size: 28px;
      font-weight: 700;
      color: #1a1a1a;
      letter-spacing: -0.5px;
    }

    .header-actions {
      display: flex;
      gap: 8px;
    }

    .search-btn {
      color: #6b7280;

      &:hover {
        background: #f3f4f6;
        color: #1a1a1a;
      }
    }

    /* ===== WRAPPER ===== */
    .conversations-wrapper {
      flex: 1;
      overflow-y: auto;
      padding: 16px;
      max-width: 900px;
      margin: 0 auto;
      width: 100%;
    }

    /* ===== LOADING STATE ===== */
    .loading-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 400px;
      gap: 24px;
    }

    .loading-text {
      margin: 0;
      color: #6b7280;
      font-size: 15px;
    }

    /* ===== EMPTY STATE ===== */
    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 400px;
      text-align: center;
    }

    .empty-icon-wrapper {
      width: 96px;
      height: 96px;
      border-radius: 50%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 24px;
      box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);

      mat-icon {
        font-size: 48px;
        width: 48px;
        height: 48px;
        color: white;
      }
    }

    .empty-state h3 {
      margin: 0 0 8px 0;
      font-size: 22px;
      font-weight: 600;
      color: #1a1a1a;
    }

    .empty-state p {
      margin: 0;
      color: #6b7280;
      font-size: 15px;
    }

    /* ===== CONVERSATIONS LIST ===== */
    .conversations-list {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    /* ===== CONVERSATION CARD ===== */
    .conversation-card {
      background: white;
      border-radius: 12px;
      padding: 16px;
      display: flex;
      gap: 16px;
      cursor: pointer;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      border: 1px solid transparent;

      &:hover {
        background: #f9fafb;
        transform: translateX(4px);
        border-color: #e5e7eb;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
      }

      &:active {
        transform: translateX(4px) scale(0.99);
      }

      &.unread {
        background: linear-gradient(to right, #f0f9ff 0%, white 100%);
        border-color: #bfdbfe;

        &:hover {
          background: linear-gradient(to right, #e0f2fe 0%, #f9fafb 100%);
        }
      }
    }

    /* ===== AVATAR ===== */
    .avatar-container {
      position: relative;
      flex-shrink: 0;
    }

    .avatar {
      width: 56px;
      height: 56px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 600;
      font-size: 20px;
      color: white;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    }

    .avatar-text {
      text-transform: uppercase;
    }

    .status-indicator {
      position: absolute;
      bottom: 2px;
      right: 2px;
      width: 14px;
      height: 14px;
      border-radius: 50%;
      border: 3px solid white;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);

      &.online {
        background: #10b981;
      }

      &.offline {
        background: #9ca3af;
      }
    }

    /* ===== CONVERSATION BODY ===== */
    .conversation-body {
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      gap: 6px;
    }

    .conversation-header-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
    }

    .participant-name {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #1a1a1a;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      flex: 1;
    }

    .timestamp {
      font-size: 13px;
      color: #9ca3af;
      font-weight: 500;
      flex-shrink: 0;
    }

    .conversation-preview-row {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .message-preview {
      margin: 0;
      font-size: 14px;
      color: #6b7280;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      flex: 1;
      display: flex;
      align-items: center;
      gap: 6px;

      &.unread {
        color: #1a1a1a;
        font-weight: 600;
      }
    }

    .check-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
      color: #9ca3af;
      flex-shrink: 0;
    }

    .unread-badge {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 12px;
      padding: 2px 10px;
      font-size: 12px;
      font-weight: 700;
      min-width: 20px;
      text-align: center;
      flex-shrink: 0;
      box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
    }

    /* ===== RESPONSIVE ===== */
    @media (max-width: 768px) {
      .conversations-wrapper {
        padding: 8px;
      }

      .conversations-header {
        padding: 0 16px;
      }

      .header-content {
        height: 64px;
      }

      .header-title {
        font-size: 24px;
      }

      .conversation-card {
        padding: 12px;
      }

      .avatar {
        width: 48px;
        height: 48px;
        font-size: 18px;
      }
    }
  `]
})
export class ConversationListComponent implements OnInit, OnDestroy {
  private store = inject(Store);
  private router = inject(Router);
  private destroy$ = new Subject<void>();

  conversations: ConversationSummary[] = [];
  loading = false;
  totalUnreadCount = 0;
  private userService = inject(UserService);
  conversationId!: number;
  currentUserId!: number;
  private userCache = new Map<number, string>();
  ngOnInit(): void {
    this.store.dispatch(MessagingActions.loadConversations());

    this.store.select(MessagingSelectors.selectAllConversations)
      .pipe(takeUntil(this.destroy$))
      .subscribe(conversations => {

        // 🔥 CLONE COMPLET DES OBJETS
        this.conversations = conversations.map(conv => ({
          ...conv
        }));

        // 🔥 Maintenant on peut modifier
        this.conversations.forEach(conv => this.loadParticipantName(conv));
      });

    this.store.select(MessagingSelectors.selectLoading)
      .pipe(takeUntil(this.destroy$))
      .subscribe(loading => {
        this.loading = loading;
      });

    this.store.select(MessagingSelectors.selectTotalUnreadCount)
      .pipe(takeUntil(this.destroy$))
      .subscribe(count => {
        this.totalUnreadCount = count;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  openConversation(conversationId: number): void {
    this.store.dispatch(MessagingActions.selectConversation({ conversationId }));
    this.router.navigate(['/messages', conversationId]);
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
      'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
      'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
      'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
      'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
      'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)'
    ];
    const index = name.charCodeAt(0) % colors.length;
    return colors[index];
  }

  formatTime(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffInMs = now.getTime() - date.getTime();
    const diffInHours = diffInMs / (1000 * 60 * 60);

    if (diffInHours < 1) {
      const minutes = Math.floor(diffInMs / (1000 * 60));
      return minutes < 1 ? 'À l\'instant' : `${minutes}min`;
    } else if (diffInHours < 24) {
      return date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
    } else if (diffInHours < 48) {
      return 'Hier';
    } else if (diffInHours < 168) {
      return date.toLocaleDateString('fr-FR', { weekday: 'short' });
    } else {
      return date.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' });
    }
  }

  isLastMessageMine(conv: ConversationSummary): boolean {
    // Vous devrez ajouter cette logique selon votre modèle
    return false;
  }



  loadParticipantName(conv: any): void {
    const userId = conv.otherParticipantId;

    if (!userId) {
      conv.otherParticipantName = 'Utilisateur';
      return;
    }

    if (this.userCache.has(userId)) {
      conv.otherParticipantName = this.userCache.get(userId);
      return;
    }

    this.userService.getUserById(userId).subscribe({
      next: user => {
        const fullName = `${user.nom} ${user.prenom}`;
        this.userCache.set(userId, fullName);
        conv.otherParticipantName = fullName;
      },
      error: () => {
        conv.otherParticipantName = `Utilisateur ${userId}`;
      }
    });
  }

}


