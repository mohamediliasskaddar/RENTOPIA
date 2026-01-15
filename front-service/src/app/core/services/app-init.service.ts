// src/app/core/services/app-init.service.ts

import { Injectable, inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { filter, take } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import * as MessagingActions from '../../store/messaging/messaging.actions';
import * as AuthSelectors from '../../store/auth/auth.selectors';

@Injectable({
  providedIn: 'root'
})
export class AppInitService {
  private store = inject(Store);

  /**
   * Initialiser le WebSocket au démarrage de l'app
   */
  initializeWebSocket(): void {
    // Attendre que l'utilisateur soit connecté
    this.store.select(AuthSelectors.selectIsAuthenticated)
      .pipe(
        filter(isAuth => isAuth === true),
        take(1)
      )
      .subscribe(() => {
        const token = localStorage.getItem(environment.tokenKey);
        if (token) {
          console.log('🚀 Initializing WebSocket connection...');
          this.store.dispatch(MessagingActions.connectWebSocket({ token }));
        }
      });
  }

  /**
   * Déconnecter le WebSocket
   */
  disconnectWebSocket(): void {
    this.store.dispatch(MessagingActions.disconnectWebSocket());
  }
}
