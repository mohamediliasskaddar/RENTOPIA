// src/app/shared/components/navbar/navbar.component.ts

import {Component, OnInit, HostListener, OnDestroy, ViewChild} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, NavigationEnd } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, Subject, takeUntil } from 'rxjs';
import { filter } from 'rxjs/operators';

// Material Imports
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule, MatMenuTrigger } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';

// Store
import * as AuthActions from '../../../store/auth/auth.actions';
import {
  selectCurrentUser,
  selectIsAuthenticated,
  selectIsHost
} from '../../../store/auth/auth.selectors';

// Models
import { User } from '../../../core/models/user.model';
import { NotificationBellComponent } from "../notification-bell/notification-bell.component";

import * as MessagingActions from '../../../store/messaging/messaging.actions';
import * as MessagingSelectors from '../../../store/messaging/messaging.selectors';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatBadgeModule,
    MatDividerModule,
    MatTooltipModule,
    NotificationBellComponent
  ],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent implements OnInit, OnDestroy {
  // Observables
  currentUser$: Observable<User | null>;
  isAuthenticated$: Observable<boolean>;
  isHost$: Observable<boolean>;
  unreadMessagesCount$: Observable<number>;

  // État local
  userInitial = '';
  isHomePage = true;
  isScrolled = false;
  isHostMode = false; // 🔥 Détermine si on est en mode Host (basé sur l'URL)

  @ViewChild(MatMenuTrigger) menuTrigger!: MatMenuTrigger;

  private destroy$ = new Subject<void>();

  constructor(
    private store: Store,
    private router: Router
  ) {
    this.currentUser$ = this.store.select(selectCurrentUser);
    this.isAuthenticated$ = this.store.select(selectIsAuthenticated);
    this.isHost$ = this.store.select(selectIsHost);
    this.unreadMessagesCount$ = this.store.select(
      MessagingSelectors.selectTotalUnreadCount
    );
  }

  ngOnInit(): void {
    // S'abonner à currentUser pour récupérer l'initiale
    this.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        if (user && user.email) {
          this.userInitial = user.email.charAt(0).toUpperCase();
        } else {
          this.userInitial = '';
        }
      });

    // Écouter les changements de route
    this.checkIfHomePage();
    this.checkHostMode(); // 🔥 NOUVEAU

    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.checkIfHomePage();
        this.checkHostMode(); // 🔥 NOUVEAU
      });

    // Charger les conversations au démarrage

    this.isAuthenticated$.subscribe(isAuth => {
      if (isAuth) {
        this.store.dispatch(MessagingActions.loadConversations());
      }
    });


  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private checkIfHomePage(): void {
    this.isHomePage = this.router.url === '/' || this.router.url === '';
  }

  /**
   * 🔥 NOUVEAU: Vérifier si on est en mode Host
   */
  private checkHostMode(): void {
    this.isHostMode = this.router.url.startsWith('/host');
  }

  @HostListener('window:scroll', [])
  onWindowScroll(): void {
    this.isScrolled = window.scrollY > 50;
  }

  // ============================
  // 🔥 NOUVEAU: TOGGLE MODE (Host/Guest)
  // ============================
  toggleMode(): void {
    if (this.isHostMode) {
      // Si on est en mode Host, basculer vers Guest (listings)
      this.router.navigate(['/listings']);
    } else {
      // Si on est en mode Guest, basculer vers Host
      this.router.navigate(['/host']);
    }
  }

  // ============================
  // NAVIGATION
  // ============================
  navigateToHome(): void {
    this.router.navigate(['/']);
  }

  navigateToLogin(): void {
    this.router.navigate(['/login']);
  }

  navigateToRegister(): void {
    this.router.navigate(['/register']);
  }

  navigateToProfile(): void {
    this.router.navigate(['/profile']);
  }

  navigateToSettings(): void {
    this.router.navigate(['/settings']);
  }

  navigateToMessages(): void {
    this.router.navigate(['/messages']);
  }

  navigateToBecomeHost(): void {
    this.router.navigate(['/become-host']);
  }

  navigateToBookings(): void {
    this.router.navigate(['/my-bookings']);
  }

  // ============================
  // DÉCONNEXION
  // ============================
  logout(): void {
    this.store.dispatch(AuthActions.logout());
  }
}
