// src/app/features/profile/profile.component.ts

import {Component, OnInit, OnDestroy, inject} from '@angular/core';
import { CommonModule } from '@angular/common';
import {Observable, Subject, takeUntil} from 'rxjs';
import { Store } from '@ngrx/store';

// Material
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';

// Store
import * as ProfileActions from '../../store/profile/ profile.actions';
import {
  selectProfileUser,
  selectProfileLoading,
  selectProfileError,
  selectUserFullName,
  selectMemberSince,
  selectReviewsCount,
  selectLanguagesCount
} from '../../store/profile/profile.selectors';
import { selectCurrentUser } from '../../store/auth/auth.selectors';

// Child Components
import { ProfileInfoComponent } from '../../features/profile-info/profile-info.component';
import { ProfileLanguagesComponent } from '../../features/profile-languages/profile-languages.component';
import { ProfileReviewsComponent } from '../../features/profile-reviews/profile-reviews.component';
import {MatBadge} from "@angular/material/badge";

import { RouterLink } from '@angular/router'; // ✅ AJOUTER pour routerLink

import { MatBadgeModule } from '@angular/material/badge'; // ✅ AJOUTER pour matBadge

import * as MessagingSelectors from '../../store/messaging/messaging.selectors';
import * as MessagingActions from '../../store/messaging/messaging.actions';
/**
 * ============================
 * PROFILE COMPONENT
 * Page de profil avec onglets Material
 * ============================
 */
@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    MatTabsModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    ProfileInfoComponent,
    ProfileLanguagesComponent,
    ProfileReviewsComponent,
    MatBadge,
    RouterLink, // ✅ AJOUTER
    MatBadgeModule
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})

export class ProfileComponent implements OnInit, OnDestroy {

  // ✅ CORRECTION : Utiliser inject()
  private store = inject(Store);

  // Observables
  user$ = this.store.select(selectProfileUser);
  loading$ = this.store.select(selectProfileLoading);
  error$ = this.store.select(selectProfileError);
  fullName$ = this.store.select(selectUserFullName);
  memberSince$ = this.store.select(selectMemberSince);
  reviewsCount$ = this.store.select(selectReviewsCount);
  languagesCount$ = this.store.select(selectLanguagesCount);
  unreadCount$: Observable<number>;

  private destroy$ = new Subject<void>();
  private currentUserId: number | null = null;

  ngOnInit(): void {
    // Récupérer l'ID de l'utilisateur connecté
    this.store.select(selectCurrentUser)
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        if (user && user.id) {
          this.currentUserId = user.id;
          this.loadProfile(user.id);
        }
      });
    // ✅ AJOUTER: Charger les conversations pour le badge
    this.store.dispatch(MessagingActions.loadConversations());
  }
  constructor() {
    // ✅ AJOUTER ICI: Initialiser unreadCount$
    this.unreadCount$ = this.store.select(
      MessagingSelectors.selectTotalUnreadCount
    );
  }
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Charger le profil complet
   */
  private loadProfile(userId: number): void {
    console.log('📝 Loading profile for user:', userId);

    // Charger profil + langues + avis
    this.store.dispatch(ProfileActions.loadProfile({ userId }));
    this.store.dispatch(ProfileActions.loadUserLanguages({ userId }));
    this.store.dispatch(ProfileActions.loadUserReviews({ userId }));
  }

  /**
   * Recharger le profil
   */
  onRefresh(): void {
    if (this.currentUserId) {
      this.loadProfile(this.currentUserId);
    }
  }

  /**
   * Getter pour l'initial de l'avatar
   */
  get userInitial(): string {
    let initial = '';
    this.fullName$.pipe(takeUntil(this.destroy$)).subscribe(name => {
      initial = name ? name.charAt(0).toUpperCase() : 'U';
    });
    return initial;
  }
}







