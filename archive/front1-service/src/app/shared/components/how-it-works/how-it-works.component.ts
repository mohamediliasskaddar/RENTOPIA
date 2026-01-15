// src/app/shared/components/how-it-works/how-it-works.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';

interface Step {
  icon: string;
  title: string;
  description: string;
}

@Component({
  selector: 'app-how-it-works',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule],
  templateUrl: './how-it-works.component.html',
  styleUrl: './how-it-works.component.scss'
})
export class HowItWorksComponent {

  steps: Step[] = [
    {
      icon: 'search',
      title: 'Search & Discover',
      description: 'Browse verified properties with smart filters and transparent blockchain details.'
    },
    {
      icon: 'account_balance_wallet',
      title: 'Book with Ethereum',
      description: 'Secure your reservation instantly with cryptocurrency payments.'
    },
    {
      icon: 'vpn_key',
      title: 'Check-in & Enjoy',
      description: 'Receive details from your host and enjoy your stay.'
    }
  ];

  constructor(private router: Router) {}

  navigateToListings(): void {
    this.router.navigate(['/listings']);
  }
}
