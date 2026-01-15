import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Store } from '@ngrx/store';

import { MessagingService } from '../../../core/services/messaging.service';

@Component({
  selector: 'app-start-conversation-button',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  template: `
    <button
      class="modern-message-btn"
      [class.loading]="loading"
      [class.compact]="variant === 'compact'"
      [class.icon-only]="variant === 'icon'"
      (click)="startConversation()"
      [disabled]="loading">

      <span class="btn-content">
        <mat-spinner *ngIf="loading" diameter="20" class="btn-spinner"></mat-spinner>
        <mat-icon *ngIf="!loading" class="btn-icon">{{ icon }}</mat-icon>
        <span *ngIf="variant !== 'icon'" class="btn-label">{{ label }}</span>
      </span>
    </button>
  `,
  styles: [`
    .modern-message-btn {
      position: relative;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      border-radius: 12px;
      padding: 14px 28px;
      color: white;
      font-size: 15px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: linear-gradient(135deg, rgba(255,255,255,0.2) 0%, transparent 100%);
        opacity: 0;
        transition: opacity 0.3s;
      }

      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgba(102, 126, 234, 0.5);

        &::before {
          opacity: 1;
        }
      }

      &:active:not(:disabled) {
        transform: translateY(0);
        box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
      }

      &:disabled {
        cursor: not-allowed;
        opacity: 0.6;
        transform: none;
      }

      &.loading {
        pointer-events: none;
      }

      &.compact {
        padding: 10px 20px;
        font-size: 14px;
        border-radius: 10px;
      }

      &.icon-only {
        width: 48px;
        height: 48px;
        padding: 0;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;

        .btn-icon {
          margin: 0;
        }
      }
    }

    .btn-content {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      position: relative;
      z-index: 1;
    }

    .btn-spinner {
      ::ng-deep circle {
        stroke: white !important;
      }
    }

    .btn-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .btn-label {
      line-height: 1;
    }

    /* Variantes de couleur alternatives */
    .modern-message-btn.accent {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      box-shadow: 0 4px 16px rgba(240, 147, 251, 0.4);

      &:hover:not(:disabled) {
        box-shadow: 0 8px 24px rgba(240, 147, 251, 0.5);
      }
    }

    .modern-message-btn.success {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      box-shadow: 0 4px 16px rgba(67, 233, 123, 0.4);

      &:hover:not(:disabled) {
        box-shadow: 0 8px 24px rgba(67, 233, 123, 0.5);
      }
    }

    /* Animation pulse pour attirer l'attention */
    @keyframes pulse {
      0% {
        box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
      }
      50% {
        box-shadow: 0 4px 24px rgba(102, 126, 234, 0.6);
      }
      100% {
        box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
      }
    }

    .modern-message-btn.pulse {
      animation: pulse 2s infinite;
    }
  `]
})
export class StartConversationButtonComponent {
  @Input() reservationId!: number;
  @Input() tenantId!: number;
  @Input() hostId!: number;
  @Input() label: string = 'Envoyer un message';
  @Input() icon: string = 'chat';
  @Input() variant: 'default' | 'compact' | 'icon' = 'default';

  private messagingService = inject(MessagingService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  loading = false;

  startConversation(): void {
    if (!this.reservationId || !this.tenantId || !this.hostId) {
      this.showNotification('Informations manquantes pour démarrer la conversation', 'error');
      return;
    }

    this.loading = true;

    this.messagingService.getConversationByReservationId(this.reservationId).subscribe({
      next: (conversation) => {
        this.loading = false;
        this.router.navigate(['/messages', conversation.id]);
      },
      error: (err) => {
        if (err.status === 404) {
          this.createNewConversation();
        } else {
          this.loading = false;
          this.showNotification('Erreur lors de l\'accès à la conversation', 'error');
        }
      }
    });
  }

  private createNewConversation(): void {
    this.messagingService.createConversation({
      reservationId: this.reservationId,
      tenantId: this.tenantId,
      hostId: this.hostId
    }).subscribe({
      next: (conversation) => {
        this.loading = false;
        this.showNotification('Conversation démarrée !', 'success');
        this.router.navigate(['/messages', conversation.id]);
      },
      error: () => {
        this.loading = false;
        this.showNotification('Échec du démarrage de la conversation', 'error');
      }
    });
  }

  private showNotification(message: string, type: 'success' | 'error'): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 3000,
      horizontalPosition: 'center',
      verticalPosition: 'bottom',
      panelClass: type === 'success' ? 'success-snackbar' : 'error-snackbar'
    });
  }
}
