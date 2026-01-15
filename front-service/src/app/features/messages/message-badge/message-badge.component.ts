import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatBadgeModule } from '@angular/material/badge';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import * as MessagingSelectors from '../../../store/messaging/messaging.selectors';
import * as MessagingActions from '../../../store/messaging/messaging.actions';

@Component({
  selector: 'app-message-badge',
  standalone: true,
  imports: [
    CommonModule,
    MatBadgeModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule
  ],
  template: `
    <button
      class="message-badge-btn"
      [class.has-unread]="(unreadCount$ | async) > 0"
      [matTooltip]="getTooltipText(unreadCount$ | async)"
      matTooltipPosition="below"
      (click)="goToMessages()">

      <div class="icon-wrapper">
        <mat-icon class="message-icon">chat_bubble</mat-icon>

        <span
          *ngIf="(unreadCount$ | async) > 0"
          class="unread-badge"
          [@badgePulse]>
          {{ formatCount(unreadCount$ | async) }}
        </span>
      </div>

      <div class="ripple-effect"></div>
    </button>
  `,
  styles: [`
    .message-badge-btn {
      position: relative;
      width: 56px;
      height: 56px;
      border: none;
      border-radius: 50%;
      background: white;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      overflow: hidden;

      &:hover {
        transform: scale(1.1);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);

        .ripple-effect {
          transform: scale(1);
          opacity: 0.1;
        }
      }

      &:active {
        transform: scale(0.95);
      }

      &.has-unread {
        animation: attention 2s infinite;

        .icon-wrapper {
          animation: wiggle 0.5s ease-in-out;
        }
      }
    }

    .icon-wrapper {
      position: relative;
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 2;
    }

    .message-icon {
      font-size: 28px;
      width: 28px;
      height: 28px;
      color: #667eea;
      transition: color 0.3s;

      .message-badge-btn:hover & {
        color: #764ba2;
      }

      .message-badge-btn.has-unread & {
        color: #667eea;
      }
    }

    .unread-badge {
      position: absolute;
      top: -8px;
      right: -8px;
      min-width: 20px;
      height: 20px;
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      color: white;
      border-radius: 10px;
      padding: 0 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 11px;
      font-weight: 700;
      border: 2px solid white;
      box-shadow: 0 2px 8px rgba(240, 147, 251, 0.5);
      animation: badgeAppear 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
    }

    .ripple-effect {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 50%;
      transform: scale(0);
      opacity: 0;
      transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
    }

    /* Animations */
    @keyframes attention {
      0%, 100% {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
      50% {
        box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
      }
    }

    @keyframes wiggle {
      0%, 100% {
        transform: rotate(0deg);
      }
      25% {
        transform: rotate(-10deg);
      }
      75% {
        transform: rotate(10deg);
      }
    }

    @keyframes badgeAppear {
      0% {
        transform: scale(0);
        opacity: 0;
      }
      50% {
        transform: scale(1.2);
      }
      100% {
        transform: scale(1);
        opacity: 1;
      }
    }

    /* Variante compacte pour navbar */
    .message-badge-btn.compact {
      width: 44px;
      height: 44px;
      box-shadow: none;
      background: transparent;

      &:hover {
        background: rgba(102, 126, 234, 0.1);
        transform: scale(1.05);
      }

      .message-icon {
        font-size: 24px;
        width: 24px;
        height: 24px;
      }

      .unread-badge {
        top: -4px;
        right: -4px;
        min-width: 18px;
        height: 18px;
        font-size: 10px;
      }
    }

    /* Variante pour mode sombre */
    .dark-mode .message-badge-btn {
      background: #2d3748;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);

      .message-icon {
        color: #a0aec0;
      }

      &:hover {
        background: #374151;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);

        .message-icon {
          color: #e2e8f0;
        }
      }
    }
  `],
  animations: []
})
export class MessageBadgeComponent implements OnInit {
  private store = inject(Store);
  private router = inject(Router);

  unreadCount$: Observable<number>;

  constructor() {
    this.unreadCount$ = this.store.select(MessagingSelectors.selectTotalUnreadCount);
  }

  ngOnInit(): void {
    this.store.dispatch(MessagingActions.loadConversations());
  }

  goToMessages(): void {
    this.router.navigate(['/messages']);
  }

  getTooltipText(count: number | null): string {
    if (!count || count === 0) {
      return 'Messages';
    }
    return count === 1 ? '1 nouveau message' : `${count} nouveaux messages`;
  }

  formatCount(count: number | null): string {
    if (!count) return '0';
    return count > 99 ? '99+' : count.toString();
  }
}
