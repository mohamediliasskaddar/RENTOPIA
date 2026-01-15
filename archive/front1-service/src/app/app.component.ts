import {Component, inject} from '@angular/core';
import {NavigationEnd, Router, RouterOutlet} from '@angular/router';
import {HomeComponent} from "./features/home/home.component";
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { Store } from '@ngrx/store';
import * as AuthActions from './store/auth/auth.actions';
import {filter} from "rxjs/operators";
import { CommonModule } from '@angular/common';
import {FooterComponent} from "./shared/components/footer/footer.component";
import {AppInitService} from "./core/services/app-init.service";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, CommonModule, FooterComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  private appInitService = inject(AppInitService);
  showNavbar = true;
  title = 'real-estate-rent-frontend';
  constructor(private store: Store ,private router: Router
  ) {}

  ngOnInit(): void {
    // Initialiser le WebSocket
    //this.appInitService.initializeWebSocket();
    // Écouter les changements de route
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.updateNavbarVisibility();
      });

    // Vérifier la route initiale
    this.updateNavbarVisibility();
    // ✅ Initialiser l'auth au démarrage de l'app
    console.log('🚀 App starting - Initializing auth...');
    this.store.dispatch(AuthActions.initAuth());
  }

  /**
   * Masquer la navbar sur /login et /register
   */
  private updateNavbarVisibility(): void {
    const url = this.router.url;
    const hideNavbarRoutes = ['/login', '/register'  ,'/messages/'];

    this.showNavbar = !hideNavbarRoutes.some(route => url.startsWith(route));
  }




  ngOnDestroy(): void {
    // Déconnecter le WebSocket
    //this.appInitService.disconnectWebSocket();
  }
}
