// src/app/core/guards/no-auth.guard.ts

import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map, take } from 'rxjs/operators';

/**
 * ============================
 * NO-AUTH GUARD
 * Empêche l'accès aux pages login/register si l'utilisateur est DÉJÀ connecté
 * Si connecté → Redirige vers la page d'accueil
 * ============================
 */
export const noAuthGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Vérifier si l'utilisateur est authentifié
  return authService.isAuthenticated$.pipe(
    take(1), // Prendre seulement la première valeur émise
    map(isAuthenticated => {
      if (!isAuthenticated) {
        // ✅ Utilisateur NON connecté → Peut accéder à login/register
        return true;
      }

      // ❌ Utilisateur DÉJÀ connecté → Redirection vers home
      console.info('ℹ️ Déjà connecté. Redirection vers /');
      router.navigate(['/']);
      return false;
    })
  );
};
