// src/app/features/profile/components/profile-info/profile-info.component.ts
// ✅ VERSION CORRIGÉE

import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { Store } from '@ngrx/store';

// Material
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

// Store
import * as ProfileActions from '../../store/profile/ profile.actions';
import {
  selectProfileUser,
  selectIsEditMode,
  selectProfileLoading,
  selectUploadingPhoto,
  selectCanEdit
} from '../../store/profile/profile.selectors';
import { selectCurrentUser } from '../../store/auth/auth.selectors';

/**
 * ============================
 * PROFILE INFO COMPONENT
 * Onglet "Informations personnelles" avec édition inline
 * ============================
 */
@Component({
  selector: 'app-profile-info',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './profile-info.component.html',
  styleUrl: './profile-info.component.scss'
})
export class ProfileInfoComponent implements OnInit, OnDestroy {

  // ✅ CORRECTION : Utiliser inject() au lieu du constructeur
  private fb = inject(FormBuilder);
  private store = inject(Store);
  private snackBar = inject(MatSnackBar);

  // Observables
  user$ = this.store.select(selectProfileUser);
  isEditMode$ = this.store.select(selectIsEditMode);
  loading$ = this.store.select(selectProfileLoading);
  uploadingPhoto$ = this.store.select(selectUploadingPhoto);
  canEdit$ = this.store.select(selectCanEdit);

  // Form
  profileForm!: FormGroup;

  // File upload
  selectedFile: File | null = null;
  previewUrl: string | null = null;

  private destroy$ = new Subject<void>();
  private currentUserId: number | null = null;

  ngOnInit(): void {
    this.initForm();
    this.loadUserData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Initialiser le formulaire
   */
  private initForm(): void {
    this.profileForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      prenom: ['', [Validators.required, Validators.minLength(2)]],
      photoUrl: ['']
    });

    // Désactiver par défaut
    this.profileForm.disable();
  }

  /**
   * Charger les données utilisateur
   */
  private loadUserData(): void {
    // Récupérer l'ID
    this.store.select(selectCurrentUser)
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        if (user?.id) {
          this.currentUserId = user.id;
        }
      });

    // Remplir le formulaire
    this.user$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        if (user) {
          this.profileForm.patchValue({
            nom: user.nom,
            prenom: user.prenom,
            photoUrl: user.photoUrl || ''
          });
        }
      });

    // Activer/désactiver selon le mode
    this.isEditMode$
      .pipe(takeUntil(this.destroy$))
      .subscribe(isEdit => {
        if (isEdit) {
          this.profileForm.enable();
        } else {
          this.profileForm.disable();
          this.selectedFile = null;
          this.previewUrl = null;
        }
      });
  }

  /**
   * Basculer mode édition
   */
  onToggleEdit(): void {
    this.store.dispatch(ProfileActions.toggleEditMode());
  }

  /**
   * Sauvegarder les modifications
   */
  onSave(): void {
    if (this.profileForm.invalid || !this.currentUserId) {
      this.snackBar.open('Formulaire invalide', 'Fermer', { duration: 3000 });
      return;
    }

    const updateData = {
      nom: this.profileForm.value.nom,
      prenom: this.profileForm.value.prenom
    };

    console.log('💾 Saving profile:', updateData);

    // Mettre à jour le profil
    this.store.dispatch(ProfileActions.updateProfile({
      userId: this.currentUserId,
      updateData
    }));

    // Si une photo est sélectionnée, l'uploader
    if (this.selectedFile) {
      this.uploadPhoto();
    }
  }

  /**
   * Annuler les modifications
   */
  onCancel(): void {
    this.loadUserData();
    this.store.dispatch(ProfileActions.toggleEditMode());
  }

  /**
   * Sélectionner une photo
   */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];

      // Validation
      if (!file.type.startsWith('image/')) {
        this.snackBar.open('Veuillez sélectionner une image', 'Fermer', { duration: 3000 });
        return;
      }

      if (file.size > 5 * 1024 * 1024) {
        this.snackBar.open('L\'image ne doit pas dépasser 5MB', 'Fermer', { duration: 3000 });
        return;
      }

      this.selectedFile = file;

      // Preview
      const reader = new FileReader();
      reader.onload = () => {
        this.previewUrl = reader.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  /**
   * Upload la photo
   */
  private uploadPhoto(): void {
    if (!this.selectedFile || !this.currentUserId) return;

    console.log('📸 Uploading photo...');

    this.store.dispatch(ProfileActions.uploadPhoto({
      userId: this.currentUserId,
      file: this.selectedFile
    }));

    // Reset après upload
    this.selectedFile = null;
    this.previewUrl = null;
  }

  /**
   * Trigger file input
   */
  triggerFileInput(): void {
    const fileInput = document.getElementById('photoInput') as HTMLInputElement;
    fileInput?.click();
  }
}

















































































































































































































