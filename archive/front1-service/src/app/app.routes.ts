// src/app/app.routes.ts
// ✅ AJOUTER LA ROUTE /profile

import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { noAuthGuard } from './core/guards/no-auth.guard';

export const routes: Routes = [
  // ========================================
  // PAGE D'ACCUEIL
  // ========================================
  {
    path: '',
    loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent),
    data: { title: 'Accueil' }
  },

  // ========================================
  // ROUTES PUBLIQUES (AUTH)
  // ========================================
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent),
    canActivate: [noAuthGuard],
    data: { title: 'Connexion' }
  },

  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent),
    canActivate: [noAuthGuard],
    data: { title: 'Inscription' }
  },

  // ========================================
  // ROUTES PROTÉGÉES (NÉCESSITE AUTHENTIFICATION)
  // ========================================

  // ✅ AJOUTER LA ROUTE PROFILE
  {
    path: 'profile',
    loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent),
    canActivate: [authGuard],
    data: { title: 'Mon Profil' }
  },

  {
    path: 'my-bookings',
    loadComponent: () => import('./features/my-bookings/my-bookings.component')
      .then(m => m.MyBookingsComponent),
    canActivate: [authGuard]  // Protection: utilisateur connecté uniquement
  },

  // ========================================
  // ROUTES HOST (PROPRIÉTAIRE)
  // ========================================
  {
    path: 'host',
    loadComponent: () => import('./features/host/host-layout/host-layout.component').then(m => m.HostLayoutComponent),
    canActivate: [authGuard],
    children: [
      {
        path: '',
        redirectTo: 'properties',
        pathMatch: 'full'
      },
      {
        path: 'properties',
        loadComponent: () => import('./features/host/host-properties/host-properties.component').then(m => m.HostPropertiesComponent),
        data: { title: 'My Properties' }
      },
      {
        path: 'properties/new',
        loadComponent: () => import('./features/host/property-wizard/property-wizard.component').then(m => m.PropertyWizardComponent),
        data: { title: 'Create Property' }
      },
      {
        path: 'properties/:id',
        loadComponent: () => import('./features/host/host-property-detail/host-property-detail.component').then(m => m.HostPropertyDetailComponent),
        data: { title: 'Property Details' }
      },
      {
        path: 'bookings',
        loadComponent: () => import('./features/host/host-bookings/host-bookings.component').then(m => m.HostBookingsComponent),
        data: { title: 'Reservations' }
      },
      {
        path: 'bookings/:id',
        loadComponent: () => import('./features/host/host-bookings//host-booking-detail/host-booking-detail.component').then(m => m.HostBookingDetailComponent),
        data: { title: 'Reservation Details' }
      },

    ],

  },

  /*
 {
   path: 'messages',
   loadComponent: () => import('./features/messages/messages.component').then(m => m.MessagesComponent),
   canActivate: [authGuard],
   data: { title: 'Mes Messages' }
 },
 */

  // ========================================
  // ROUTES SEMI-PUBLIQUES (LISTINGS)
  // ========================================
  {
    path: 'notifications',
    loadComponent: () => import('./shared/components/notification-bell/notifications-page/notifications-page.component').then(m => m.NotificationsPageComponent),
    canActivate: [authGuard],
    data: { title: 'Notifications' }
  },
  {
    path: 'listings',
    loadComponent: () => import('./features/listing/listings.component').then(m => m.ListingsComponent),
    data: { title: 'Explore Properties' }
  },
  {
    path: 'property/:id',
    loadComponent: () => import('./features/property-detail/property-detail.component').then(m => m.PropertyDetailComponent),
    data: { title: 'Explore Properties' }
  },
  {
    path: 'trust-safety',
    loadComponent: () => import('./shared/components/trust-safety/trust-safety.component')
      .then(m => m.TrustSafetyComponent),
    title: 'Trust & Safety - RentalChain'
  },
  {
    path: 'faq',
    loadComponent: () => import('./shared/components/faq/faq.component')
      .then(m => m.FaqComponent),
    title: 'FAQ - RentalChain'
  },
  {
    path: 'about',
    loadComponent: () => import('./shared/components/about/about.component')
      .then(m => m.AboutComponent),
    title: 'About Us - RentalChain'
  },
  {
    path: 'How-it-works',
    loadComponent: () => import('./shared/components/how-it-works/how-it-works.component')
      .then(m => m.HowItWorksComponent),
    title: 'How it Works'
  },

  {
    path: 'contact',
    loadComponent: () => import('./shared/components/contact/contact.component')
      .then(m => m.ContactComponent),
    title: 'Contact Us - RentalChain'
  },
  {
    path: 'become-host',
    loadComponent: () => import('./shared/components/become-host/become-host.component')
      .then(m => m.BecomeHostComponent),
    title: 'Become a Host'
  },
  {
    path: 'messages',
    loadChildren: () => import('./features/messages/messages.routes')
      .then(m => m.MESSAGES_ROUTES),
    canActivate: [authGuard],
    data: { title: 'Messages' }
  }
];
