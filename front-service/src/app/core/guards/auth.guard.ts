// src/app/core/guards/auth.guard.ts

import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router'; // CanActivateFn Type fourni par Angular pour créer un “guard”
import { AuthService } from '../services/auth.service';
import { map, take } from 'rxjs/operators';
// map pour Transformer une valeur en une autre
//take : dans un observable il sert a Prendre seulement UNE valeur puis arrêter
/**
 * ============================
 * AUTH GUARD
 * Protège les routes qui nécessitent une authentification
 * Si l'utilisateur n'est PAS connecté → Redirige vers /login
 * ============================
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Vérifier si l'utilisateur est authentifié
  return authService.isAuthenticated$.pipe(
    take(1), // Prendre seulement la première valeur émise
    map(isAuthenticated => {
      if (isAuthenticated) {
        // ✅ Utilisateur connecté → Accès autorisé
        return true;
      }

      // ❌ Utilisateur NON connecté → Redirection vers login
      // On sauvegarde l'URL demandée pour rediriger après connexion
      console.warn('🔒 Accès refusé. Redirection vers /login');
      router.navigate(['/login'], {
        queryParams: { returnUrl: state.url } // Ex: returnUrl=/profile
      });
      return false;
    })
  );
};
