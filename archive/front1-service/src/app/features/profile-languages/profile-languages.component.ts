// src/app/features/profile/components/profile-languages/profile-languages.component.ts
// ✅ AVEC CONFIGURATION DIALOG CENTRÉE

import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { Store } from '@ngrx/store';

// Material
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';

// Store
import * as ProfileActions from '../../store/profile/ profile.actions';
import {
  selectUserLanguages,
  selectProfileLoading
} from '../../store/profile/profile.selectors';
import { selectCurrentUser } from '../../store/auth/auth.selectors';

// Models
import { UserLanguageDTO, ProficiencyLevel } from '../../core/models/user.model';
import { AddLanguageDialogComponent } from '../add-language-dialog/add-language-dialog.component';

@Component({
  selector: 'app-profile-languages',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDialogModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './profile-languages.component.html',
  styleUrl: './profile-languages.component.scss'
})
export class ProfileLanguagesComponent implements OnInit, OnDestroy {

  private store = inject(Store);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private destroy$ = new Subject<void>();

  // Observables
  languages$ = this.store.select(selectUserLanguages);
  loading$ = this.store.select(selectProfileLoading);

  currentUserId: number | null = null;

  ngOnInit(): void {
    console.log('🎬 ProfileLanguagesComponent - Init');

    // Récupérer l'user ID
    this.store.select(selectCurrentUser)
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        console.log('👤 Current user:', user);
        this.currentUserId = user?.id || null;
        console.log('📌 Current user ID:', this.currentUserId);
      });

    // Debug languages
    this.languages$.pipe(takeUntil(this.destroy$)).subscribe(languages => {
      console.log('🌍 Languages in store:', languages);
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * ✨ Ouvrir dialog pour ajouter une langue - AVEC STYLES CENTRÉS
   */
  onAddLanguage(): void {
    console.log('➕ Add Language clicked');
    console.log('👤 User ID:', this.currentUserId);

    if (!this.currentUserId) {
      console.error('❌ No user ID!');
      this.snackBar.open('User not found', 'Close', {
        duration: 3000,
        panelClass: ['language-error-snackbar']
      });
      return;
    }

    // ✅ Configuration du dialog avec classes CSS personnalisées
    const dialogRef = this.dialog.open(AddLanguageDialogComponent, {
      width: '500px',
      maxWidth: '90vw',
      disableClose: false,
      autoFocus: true,
      restoreFocus: true,
      // ✨ Classes CSS pour centrer et styliser
      panelClass: 'add-language-dialog-container',
      backdropClass: 'add-language-dialog-backdrop',
      // Animation
      enterAnimationDuration: '300ms',
      exitAnimationDuration: '200ms'
    });

    console.log('📂 Dialog opened');

    dialogRef.afterClosed().subscribe(result => {
      console.log('🔙 Dialog closed with result:', result);

      if (result && this.currentUserId) {
        console.log('✅ Dispatching addLanguage action');
        console.log('📦 Payload:', {
          userId: this.currentUserId,
          languageData: result
        });

        this.store.dispatch(ProfileActions.addLanguage({
          userId: this.currentUserId,
          languageData: {
            languageId: result.languageId,
            proficiencyLevel: result.proficiencyLevel
          }
        }));

        // ✅ Snackbar de succès personnalisé
        this.snackBar.open(
          `✅ ${result.languageName || 'Language'} added successfully!`,
          'Close',
          {
            duration: 3000,
            panelClass: ['language-success-snackbar']
          }
        );
      } else {
        console.log('❌ No result or no user ID');
      }
    });
  }

  /**
   * 🗑️ Supprimer une langue - AVEC CONFIRMATION AMÉLIORÉE
   */
  onRemoveLanguage(language: UserLanguageDTO): void {
    console.log('🗑️ Remove language clicked:', language);

    if (!this.currentUserId) {
      console.error('❌ No user ID!');
      return;
    }

    // ✅ Confirmation plus user-friendly
    const confirmed = confirm(
      `Are you sure you want to remove ${language.languageName}?\n\n` +
      `This action cannot be undone.`
    );

    console.log('❓ Confirmed:', confirmed);

    if (confirmed) {
      console.log('✅ Dispatching removeLanguage action');

      this.store.dispatch(ProfileActions.removeLanguage({
        userId: this.currentUserId,
        languageId: language.languageId
      }));

      // ✅ Snackbar de succès
      this.snackBar.open(
        `🗑️ ${language.languageName} removed`,
        'Undo',
        {
          duration: 3000,
          panelClass: ['language-success-snackbar']
        }
      ).onAction().subscribe(() => {
        // Si l'utilisateur clique sur "Undo", re-ajouter la langue
        this.store.dispatch(ProfileActions.addLanguage({
          userId: this.currentUserId!,
          languageData: {
            languageId: language.languageId,
            proficiencyLevel: language.proficiencyLevel
          }
        }));
      });
    }
  }

  /**
   * 📊 Labels pour les niveaux de maîtrise avec emojis
   */
  getProficiencyLabel(level: ProficiencyLevel): string {
    const labels: Record<ProficiencyLevel, string> = {
      [ProficiencyLevel.BASIC]: 'Basic',
      [ProficiencyLevel.INTERMEDIATE]: 'Intermediate',
      [ProficiencyLevel.ADVANCED]: 'Advanced',
      [ProficiencyLevel.NATIVE]: 'Native'
    };
    return labels[level] || level;
  }

  /**
   * 🎨 Obtenir la couleur pour un niveau
   */
  getProficiencyColor(level: ProficiencyLevel): string {
    const colors: Record<ProficiencyLevel, string> = {
      [ProficiencyLevel.BASIC]: '#e67e22',
      [ProficiencyLevel.INTERMEDIATE]: '#031b30',
      [ProficiencyLevel.ADVANCED]: '#10345B',
      [ProficiencyLevel.NATIVE]: '#0a0e1a'
    };
    return colors[level] || '#666';
  }

  /**
   * ⭐ Obtenir le nombre d'étoiles pour un niveau
   */
  getProficiencyStars(level: ProficiencyLevel): number {
    const stars: Record<ProficiencyLevel, number> = {
      [ProficiencyLevel.BASIC]: 1,
      [ProficiencyLevel.INTERMEDIATE]: 2,
      [ProficiencyLevel.ADVANCED]: 3,
      [ProficiencyLevel.NATIVE]: 4
    };
    return stars[level] || 0;
  }
}
