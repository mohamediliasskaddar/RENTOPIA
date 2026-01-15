// src/app/shared/components/footer/footer.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {NavigationEnd, Router, RouterLink} from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import {filter} from "rxjs/operators";
import * as AuthActions from "../../../store/auth/auth.actions";
import {Store} from "@ngrx/store";

interface FooterLink {
  label: string;
  route: string;
}

interface FooterSection {
  title: string;
  links: FooterLink[];
}

interface SocialLink {
  icon: string;
  url: string;
  label: string;
}

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, RouterLink, MatIconModule, MatDividerModule],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss'
})
export class FooterComponent {
  showNavbar = true;
  constructor(private router: Router
  ) {}
  ngOnInit(): void {
    // Écouter les changements de route
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.updateFooterVisibility();
      });

    // Vérifier la route initiale
    this.updateFooterVisibility();
    // ✅ Initialiser l'auth au démarrage de l'app

  }


  private updateFooterVisibility(): void {
    const url = this.router.url;
    const hideNavbarRoutes = ['/login', '/register'];

    this.showNavbar = !hideNavbarRoutes.some(route => url.startsWith(route));
  }
  currentYear = new Date().getFullYear();

  footerSections: FooterSection[] = [
    {
      title: 'Company',
      links: [
        { label: 'About Us', route: '/about' },
        { label: 'How It Works', route: '/how-it-works' },
        { label: 'Careers', route: '/careers' },
        { label: 'Press', route: '/press' },
        { label: 'Blog', route: '/blog' }
      ]
    },
    {
      title: 'Support',
      links: [
        { label: 'Help Center', route: '/help' },
        { label: 'Trust & Safety', route: '/trust-safety' },

        { label: 'Contact Us', route: '/contact' },
        { label: 'FAQ', route: '/faq' },
        { label: 'Cancellation Policy', route: '/cancellation' }
      ]
    },
    {
      title: 'Hosting',
      links: [
        { label: 'Become a Host', route: '/become-host' },
        { label: 'Host Resources', route: '/host-resources' },
        { label: 'Community Guidelines', route: '/guidelines' },
        { label: 'Host Dashboard', route: '/host/dashboard' }
      ]
    },
    {
      title: 'Legal',
      links: [
        { label: 'Terms of Service', route: '/terms' },
        { label: 'Privacy Policy', route: '/privacy' },
        { label: 'Cookie Policy', route: '/cookies' },
        { label: 'Sitemap', route: '/sitemap' }
      ]
    }
  ];

  socialLinks: SocialLink[] = [
    { icon: 'facebook', url: 'https://facebook.com', label: 'Facebook' },
    { icon: 'twitter', url: 'https://twitter.com', label: 'Twitter' },
    { icon: 'instagram', url: 'https://instagram.com', label: 'Instagram' },
    { icon: 'linkedin', url: 'https://linkedin.com', label: 'LinkedIn' }
  ];

  // Note: Material Icons n'a pas toutes les icônes sociales
  // On utilise des icônes génériques ou vous pouvez ajouter Font Awesome
  getSocialIcon(icon: string): string {
    const iconMap: { [key: string]: string } = {
      'facebook': 'public',
      'twitter': 'tag',
      'instagram': 'photo_camera',
      'linkedin': 'business'
    };
    return iconMap[icon] || 'public';
  }

  openSocialLink(url: string): void {
    window.open(url, '_blank');
  }
}
