// src/app/features/auth/register/register.component.ts

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Store } from '@ngrx/store';
import { Observable, Subject, takeUntil } from 'rxjs';

// Material Imports
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatStepperModule } from '@angular/material/stepper';

// Services
import { Web3Service } from '../../../core/services/web3.service';

// Store
import * as AuthActions from '../../../store/auth/auth.actions';
import { selectAuthLoading, selectAuthError } from '../../../store/auth/auth.selectors';

// Models
import { RegisterDTO } from '../../../core/models/auth.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatStepperModule
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent implements OnInit, OnDestroy {
  // Observables du store
  loading$: Observable<boolean>;
  error$: Observable<string | null>;

  // Formulaire
  registerForm!: FormGroup;

  // État local
  metaMaskInstalled = false;
  isConnecting = false;
  walletConnected = false;
  walletAddress: string | null = null;
  signature: string | null = null;
  hidePassword = true;

  // Stepper
  isLinear = true;
  firstStepCompleted = false;

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private store: Store,
    private web3Service: Web3Service,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.loading$ = this.store.select(selectAuthLoading);
    this.error$ = this.store.select(selectAuthError);
  }

  ngOnInit(): void {
    // Vérifier MetaMask
    this.metaMaskInstalled = this.web3Service.isMetaMaskInstalled();

    // Initialiser le formulaire
    this.initForm();

    // ✅ CORRECTION: Écouter les erreurs du store (backend)
    this.error$.pipe(takeUntil(this.destroy$)).subscribe(error => {
      if (error) {
        // ✅ Afficher l'erreur à l'utilisateur
        this.showError(error);
      }
    });

    // ✅ CORRECTION: Surveiller le succès de l'inscription
    // (optionnel, mais utile pour debug)
    this.loading$.pipe(takeUntil(this.destroy$)).subscribe(loading => {
      if (!loading) {
        console.log('✅ Loading terminé (succès ou erreur)');
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * ============================
   * INITIALISER LE FORMULAIRE
   * ============================
   */
  private initForm(): void {
    this.registerForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      prenom: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email]],
      tel: ['', [Validators.maxLength(20)]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(100)]]
    });
  }

  /**
   * ============================
   * ÉTAPE 1 : CONNECTER METAMASK
   * ============================
   */
  async connectMetaMask(): Promise<void> {
    if (!this.metaMaskInstalled) {
      this.showError('MetaMask is not installed');
      return;
    }

    this.isConnecting = true;

    try {
      // 1. Connecter wallet
      console.log('🔗 Connecting to MetaMask...');
      const wallet = await this.web3Service.connectWallet().toPromise();

      if (!wallet) {
        throw new Error('Unable to retrieve wallet');
      }

      this.walletAddress = wallet;
      console.log('✅ Wallet connected:', wallet);

      // 2. Générer message
      const message = this.web3Service.generateAuthMessage(wallet);

      // 3. Demander signature
      this.showInfo('Please sign the message in MetaMask...');
      const sig = await this.web3Service.signMessage(message).toPromise();

      if (!sig) {
        throw new Error('Signature denied');
      }

      this.signature = sig;
      console.log('✍️ Signature received');

      // Marquer comme complété
      this.walletConnected = true;
      this.firstStepCompleted = true;
      this.showSuccess('Wallet connected successfully!');

    } catch (error: any) {
      console.error('❌ MetaMask error:', error);
      this.showError(error.message || 'Error during MetaMask connection');
      this.walletConnected = false;
      this.firstStepCompleted = false;
    } finally {
      this.isConnecting = false;
    }
  }

  /**
   * ============================
   * ÉTAPE 2 : SOUMETTRE LE FORMULAIRE
   * ============================
   */
  onSubmit(): void {
    // Vérifier que le formulaire est valide
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      this.showError('Please fill in all required fields correctly');
      return;
    }

    // Vérifier que le wallet est connecté
    if (!this.walletAddress || !this.signature) {
      this.showError('Please connect your MetaMask wallet first');
      return;
    }

    // Créer l'objet RegisterDTO
    const registerData: RegisterDTO = {
      nom: this.registerForm.value.nom.trim(),
      prenom: this.registerForm.value.prenom.trim(),
      email: this.registerForm.value.email.trim(),
      password: this.registerForm.value.password,
      walletAdresse: this.walletAddress,
      tel: this.registerForm.value.tel?.trim() || undefined
    };

    console.log('📤 Sending registration:', { ...registerData, password: '***' });

    // ✅ Dispatcher l'action register
    // Les erreurs backend (wallet déjà utilisé, email déjà utilisé, etc.)
    // seront automatiquement affichées via error$
    this.store.dispatch(AuthActions.register({ registerData }));
  }

  /**
   * ============================
   * NAVIGUER VERS LOGIN
   * ============================
   */
  goToLogin(): void {
    this.router.navigate(['/login']);
  }

  /**
   * ============================
   * GETTER POUR LES ERREURS DE FORMULAIRE
   * ============================
   */
  getErrorMessage(field: string): string {
    const control = this.registerForm.get(field);

    if (!control || !control.errors || !control.touched) {
      return '';
    }

    if (control.errors['required']) {
      return 'This field is required';
    }
    if (control.errors['minlength']) {
      return `Minimum ${control.errors['minlength'].requiredLength} characters`;
    }
    if (control.errors['maxlength']) {
      return `Maximum ${control.errors['maxlength'].requiredLength} characters`;
    }
    if (control.errors['email']) {
      return 'Invalid email';
    }

    return 'Validation error';
  }

  /**
   * ============================
   * MESSAGES SNACKBAR
   * ============================
   */
  private showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 6000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    });
  }

  private showInfo(message: string): void {
    this.snackBar.open(message, '', {
      duration: 3000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['info-snackbar']
    });
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, '', {
      duration: 3000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['success-snackbar']
    });
  }
}
