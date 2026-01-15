// src/app/core/services/auth.service.ts

import { Injectable } from '@angular/core';

//Observable normal : on abonnes et on reçois seulement ce qui arrive après l'abonnement.
// BehaviorSubject :  il garde en mémoire la dernière valeur émise et la donne immédiatement à tout nouvel abonné.
import { Observable, BehaviorSubject, throwError } from 'rxjs';

import { tap, catchError } from 'rxjs/operators'; // tap permet d’exécuter du code sur les données qui passent dans l’Observable sans modifier ces données.
import { ApiService } from './api.service';
import { RegisterDTO, LoginDTO, JwtResponseDTO } from '../models/auth.model';
import { User } from '../models/user.model';
import { environment } from '../../../environments/environment';

// pipe() sert à enchaîner plusieurs opérations sur un Observable.

/**
 * ============================
 * SERVICE AUTHENTIFICATION
 * Gère la connexion, inscription, déconnexion et l'état utilisateur
 * ============================
 */
@Injectable({
  providedIn: 'root' // Service singleton disponible partout
})
export class AuthService {
  // BehaviorSubject = Observable qui garde la dernière valeur émise
  // Permet aux composants de s'abonner et recevoir immédiatement la valeur actuelle

  private currentUserSubject = new BehaviorSubject<User | null>(this.getUserFromStorage()); // garde la valeur de l’utilisateur connecté
  public currentUser$ = this.currentUserSubject.asObservable(); // Observable public (lecture seule)

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();


  //Le constructeur s’exécute au démarrage de l’application.
  // Si un token est déjà présent, il charge l’utilisateur depuis le backend.
  // Si ça échoue, on déconnecte l’utilisateur automatiquement.
  constructor(private apiService: ApiService) {
    if (this.hasToken()) {
      this.loadCurrentUser().subscribe({
        error: () => this.logout() // Si erreur, déconnecter
      });
    }
  }

  /**
   * ============================
   * INSCRIPTION AVEC METAMASK
   * 1. User connecte MetaMask → récupère walletAddress
   * 2. User signe un message → récupère signature
   * 3. User remplit le formulaire (nom, prenom, email, password, tel)
   * 4. Envoie tout au backend → POST /auth/register
   * ============================
   */

  register(registerData: RegisterDTO): Observable<JwtResponseDTO> {
    return this.apiService.post<JwtResponseDTO>(`${environment.services.user}/auth/register`, registerData).pipe(
      tap(response => this.setSession(response)), // tap => met en session le token et user pour garder l’utilisateur connecté.
      catchError(error => {
        console.error('❌ Erreur inscription:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * ============================
   * CONNEXION AVEC METAMASK
   * 1. User connecte MetaMask → récupère walletAddress
   * 2. User signe un message → récupère signature
   * 3. Envoie au backend → POST /auth/login
   * ============================
   */
  login(loginData: LoginDTO): Observable<JwtResponseDTO> {
    return this.apiService.post<JwtResponseDTO>(`${environment.services.user}/auth/login`, loginData).pipe(
      tap(response => this.setSession(response)), // Sauvegarder token + user
      catchError(error => {
        console.error('❌ Erreur connexion:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * ============================
   * DEMANDER LA VÉRIFICATION D'EMAIL
   * Envoie un email avec un lien de vérification
   * POST /auth/verify-email/request?email=...
   * ============================
   */
  requestEmailVerification(email: string): Observable<{ message: string }> {
    return this.apiService.post<{ message: string }>(
      `${environment.services.user}/auth/verify-email/request`,
      null,
      { params: { email } }
    );
  }

  /**
   * ============================
   * CONFIRMER LA VÉRIFICATION D'EMAIL
   * User clique sur le lien dans l'email
   * POST /auth/verify-email/confirm?token=...
   * ============================
   */
  confirmEmailVerification(token: string): Observable<{ message: string }> {
    return this.apiService.post<{ message: string }>(
      `${environment.services.user}/auth/verify-email/confirm`,
      null,
      { params: { token } }  // paramètres dans l’URL
    );
  }

  /**
   * ============================
   * CHARGER L'UTILISATEUR ACTUEL DEPUIS LE BACKEND
   * GET /users/me
   * Appelé au démarrage si un token existe
   * ============================
   */
  loadCurrentUser(): Observable<User> {
    return this.apiService.get<User>(`${environment.services.user}/users/me`).pipe(
      tap(user => {
        this.saveUserToStorage(user);
        this.currentUserSubject.next(user); // next pour emmet la nouvel valeur cad tous les composants abonnés reçoivent la nouvelle valeur
      }),
      catchError(error => {
        console.error('❌ Erreur chargement utilisateur:', error);
        this.logout();
        return throwError(() => error);
      })
    );
  }

  /**
   * ============================
   * DÉCONNEXION
   * Supprime token et user du localStorage
   * Réinitialise les BehaviorSubjects
   * ============================
   */
  logout(): void {
    localStorage.removeItem(environment.tokenKey);
    localStorage.removeItem(environment.userKey);
    this.currentUserSubject.next(null); // envoyer null a tous les abonnes cad Il n’y a plus d’utilisateur
    this.isAuthenticatedSubject.next(false);
  }

  /**
   * ============================
   * SAUVEGARDER LA SESSION (token + user)
   * Appelée après login ou register réussi
   * ============================
   */
  private setSession(response: JwtResponseDTO): void {
    // Sauvegarder le token JWT
    localStorage.setItem(environment.tokenKey, response.token);

    // Sauvegarder l'utilisateur
    this.saveUserToStorage(response.user);

    // Mettre à jour les Observables
    this.currentUserSubject.next(response.user as User);
    this.isAuthenticatedSubject.next(true);
  }

  /**
   * ============================
   * RÉCUPÉRER LE TOKEN JWT
   * Utilisé par l'intercepteur Axios dans api.service.ts
   * ============================
   */
  getToken(): string | null {
    return localStorage.getItem(environment.tokenKey);
  }

  /**
   * ============================
   * VÉRIFIER SI UN TOKEN EXISTE
   * ============================
   */
  hasToken(): boolean {
    return !!this.getToken();
  }

  /**
   * ============================
   * RÉCUPÉRER L'UTILISATEUR ACTUEL
   * Retourne la valeur actuelle du BehaviorSubject
   * ============================
   */
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * ============================
   * VÉRIFIER SI L'UTILISATEUR EST AUTHENTIFIÉ
   * ============================
   */
  isAuthenticated(): boolean {
    return this.isAuthenticatedSubject.value;
  }

  /**
   * ============================
   * SAUVEGARDER L'UTILISATEUR DANS LOCALSTORAGE
   * ============================
   */
  private saveUserToStorage(user: any): void {
    localStorage.setItem(environment.userKey, JSON.stringify(user));
  }

  /**
   * ============================
   * RÉCUPÉRER L'UTILISATEUR DEPUIS LOCALSTORAGE
   * ============================
   */
  private getUserFromStorage(): User | null {
    const userStr = localStorage.getItem(environment.userKey);
    if (!userStr) return null;
    try {
      return JSON.parse(userStr) as User;
    } catch {
      return null;
    }
  }
}
