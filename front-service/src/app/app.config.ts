// src/app/app.config.ts

import {APP_INITIALIZER, ApplicationConfig, provideZoneChangeDetection} from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideStore } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { provideStoreDevtools } from '@ngrx/store-devtools';
import { provideHttpClient } from '@angular/common/http';
import { Store } from '@ngrx/store';

// Routes
import { routes } from './app.routes';
import { environment } from '../environments/environment';

// Auth
import { authReducer } from './store/auth/auth.reducer';
import { AuthEffects } from './store/auth/auth.effects';
import * as AuthActions from './store/auth/auth.actions';

// Listings
import { listingsReducer } from "./store/listings/listing.reducer";
import { ListingsEffects } from "./store/listings/listing.effects";

// Reviews
import { reviewReducer } from "./store/review/review.reducer";
import { ReviewEffects } from "./store/review/review.effects";

// notifications
import { notificationReducer } from './store/notification/notification.reducer';
import { NotificationEffects } from './store/notification/notification.effects';
// ✅ NOUVEAU - Booking
import { bookingReducer } from "./store/booking/booking.reducer";
import { BookingEffects } from "./store/booking/booking.effects";
import { profileReducer } from './store/profile/profile.reducer';
import { ProfileEffects } from './store/profile/profile.effects';
// ✅ NOUVEAU - Payment
import { paymentReducer } from "./store/payment/payment.reducer";
import { PaymentEffects } from "./store/payment/payment.effects";
import {messagingReducer} from "./store/messaging/messaging.reducer";
import {MessagingEffects} from "./store/messaging/messaging.effects";

/**
 * ============================
 * FONCTION D'INITIALISATION
 * Exécutée au démarrage de l'app AVANT le rendu
 * Restaure la session si un token existe
 * ============================
 */
export function initializeAuth(store: Store) {
  return () => {
    store.dispatch(AuthActions.initAuth());
    return Promise.resolve();
  };
}

/**
 * ============================
 * CONFIGURATION DE L'APPLICATION (STANDALONE)
 *
 * Providers globaux :
 * - Routing
 * - HTTP Client
 * - Animations Material
 * - NgRx Store + Effects
 * - DevTools (dev only)
 * - Auth Initializer
 * ============================
 */
export const appConfig: ApplicationConfig = {
  providers: [

    // ========================================
    // ZONE.JS
    // Détection automatique des changements Angular
    // eventCoalescing: regroupe les changements pour performance
    // ========================================
    provideZoneChangeDetection({ eventCoalescing: true }),

    // ========================================
    // ROUTING
    // ========================================
    provideRouter(routes),

    // ========================================
    // HTTP CLIENT
    // Nécessaire pour les appels API
    // ========================================
    provideHttpClient(),

    // ========================================
    // ANGULAR MATERIAL ANIMATIONS
    // Active les animations Material UI
    // ========================================
    provideAnimationsAsync(),

    // ========================================
    // NGRX STORE
    // State management global
    // Tous les reducers de l'application
    // ========================================
    provideStore({
      auth: authReducer,           // État d'authentification
      listings: listingsReducer,   // État des propriétés
      reviews: reviewReducer,       // État des avis
      booking: bookingReducer,      // ✅ NOUVEAU - État des réservations
      payment: paymentReducer ,      // ✅ NOUVEAU - État des paiements
      profile: profileReducer,
      notification: notificationReducer,
      messaging: messagingReducer,
    }),

    // ========================================
    // NGRX EFFECTS
    // Side effects (appels API, navigation, etc.)
    // ========================================
    provideEffects([
      AuthEffects,        // Effects d'authentification
      ListingsEffects,    // Effects des propriétés
      ReviewEffects,      // Effects des avis
      BookingEffects,     // ✅ NOUVEAU - Effects des réservations
      PaymentEffects  ,    // ✅ NOUVEAU - Effects des paiements
      ProfileEffects,
      NotificationEffects,
      MessagingEffects
    ]),

    // ========================================
    // NGRX DEVTOOLS (Redux DevTools Extension)
    // Pour déboguer le store dans le navigateur
    //
    // Installation :
    // Chrome: https://chrome.google.com/webstore/detail/redux-devtools
    // Firefox: https://addons.mozilla.org/firefox/addon/reduxdevtools/
    //
    // Options:
    // - maxAge: nombre d'actions à garder en mémoire (25 max)
    // - logOnly: en production, lecture seule (pas de time-travel)
    // - connectInZone: nécessaire pour Angular 18+
    // ========================================
    provideStoreDevtools({
      maxAge: 25,
      logOnly: environment.production,
      connectInZone: true
    }),

    // ========================================
    // APP INITIALIZER
    // Restaure la session au démarrage
    // CRITIQUE: résout le problème de déconnexion au refresh
    // ========================================
    {
      provide: APP_INITIALIZER,
      useFactory: initializeAuth,
      deps: [Store],
      multi: true
    }
  ]
};
