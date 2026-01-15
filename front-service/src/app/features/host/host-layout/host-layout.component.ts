// src/app/features/host/host-layout/host-layout.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';

@Component({
  selector: 'app-host-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    MatMenuModule
  ],
  templateUrl: './host-layout.component.html',
  styleUrl: './host-layout.component.scss'
})
export class HostLayoutComponent {
  navItems = [
    { path: '/host/properties', icon: 'home', label: 'My Properties' },
    { path: '/host/properties/new', icon: 'add_circle', label: 'Add Property' }
  ];
}
