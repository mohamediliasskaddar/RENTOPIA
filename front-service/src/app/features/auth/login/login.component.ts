// src/app/features/auth/login/login.component.ts

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, Subject, takeUntil } from 'rxjs';

// Material Imports
import { MatCardModule } from '@angular/material/card'; // card
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';// Pour afficher des messages temporaires en bas de l’écran

// Services
import { Web3Service } from '../../../core/services/web3.service';

// Store
import * as AuthActions from '../../../store/auth/auth.actions';
import { selectAuthLoading, selectAuthError } from '../../../store/auth/auth.selectors';

// Models
import { LoginDTO } from '../../../core/models/auth.model';

/**
 * ============================
 * COMPOSANT LOGIN
 * Permet la connexion avec MetaMask uniquement
 * ============================
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatSnackBarModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit, OnDestroy {
  // Observables du store
  loading$: Observable<boolean>;
  error$: Observable<string | null>;

  // État local
  isConnecting = false; // vrai quand on connecte le wallet.
  metaMaskInstalled = false;

  // Pour unsubscribe automatiquement
  // on lútilise pour arrêter l’écoute des observables quand le composant est détruit
  private destroy$ = new Subject<void>();

  constructor(
    private store: Store,
    private web3Service: Web3Service,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    // Sélectionner les données du store
    this.loading$ = this.store.select(selectAuthLoading);
    this.error$ = this.store.select(selectAuthError);
  }

  ngOnInit(): void {
    // Vérifier si MetaMask est installé
    this.metaMaskInstalled = this.web3Service.isMetaMaskInstalled();

    // Écouter les erreurs venant du store et arreter l'ecoute quand  destroy$ envoie un signal
    this.error$.pipe(takeUntil(this.destroy$)).subscribe(error => {
      if (error) {
        this.showError(error);
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();  // envoie un "signal" pour dire :arrêtez tous les Observables liés à takeUntil(this.destroy$)
    this.destroy$.complete(); // complete cad l’Observable est terminé=>  plus possible d’envoyer des valeurs avec next()
  }

  /**
   * ============================
   * CONNEXION AVEC METAMASK
   * Flow :
   * 1. Connecter MetaMask → récupérer walletAddress
   * 2. Générer un message d'authentification
   * 3. Demander la signature du message
   * 4. Envoyer walletAddress + signature au backend
   * ============================
   */
  async loginWithMetaMask(): Promise<void> {
    // Vérifier si MetaMask est installé
    if (!this.metaMaskInstalled) {
      this.showError('MetaMask is not installed. Please install it from metamask.io');
      return;
    }

    this.isConnecting = true;

    try {
      // 1. Connecter MetaMask et récupérer l'adresse wallet
      console.log('🔗 Connecting to MetaMask...');
      const walletAddress = await this.web3Service.connectWallet().toPromise();

      if (!walletAddress) {
        throw new Error('Unable to retrieve wallet address');
      }

      console.log('✅ Wallet connected:', walletAddress);

      // 2. Générer le message d'authentification
      const message = this.web3Service.generateAuthMessage(walletAddress);
      console.log('📝 Message to sign::', message);

      // 3. Demander la signature
      this.showInfo('Please sign the message in MetaMask...');
      const signature = await this.web3Service.signMessage(message).toPromise();

      if (!signature) {
        throw new Error('Signature denied');
      }

      console.log('✍️ Signature received:', signature);

      // 4. Créer l'objet LoginDTO
      const loginData: LoginDTO = {
        walletAdresse: walletAddress,
        signature: signature
      };

      // 5. Dispatcher l'action login (NgRx Effect gère l'appel API)
      this.store.dispatch(AuthActions.login({ loginData }));

    } catch (error: any) {
      console.error('❌ MetaMask login error:', error);
      this.showError(error.message || '\'Error during login');
      this.isConnecting = false;
    }
  }

  /**
   * ============================
   * NAVIGUER VERS L'INSCRIPTION
   * ============================
   */
  goToRegister(): void {
    this.router.navigate(['/register']);
  }

  /**
   * ============================
   * AFFICHER UN MESSAGE D'ERREUR
   * ============================
   */
  private showError(message: string): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 5000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    });
  }

  /**
   * ============================
   * AFFICHER UN MESSAGE D'INFO
   * ============================
   */
  private showInfo(message: string): void {
    this.snackBar.open(message, '', {
      duration: 3000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['info-snackbar']
    });
  }
}
