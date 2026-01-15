
// src/app/features/home/home.component.ts

import { Component } from '@angular/core';
import { NavbarComponent } from "../../shared/components/navbar/navbar.component";
import { SearchBarComponent } from "../../shared/components/search-bar/search-bar.component";
import {MatCardModule} from "@angular/material/card";
import {MatIconModule} from "@angular/material/icon";
import {RouterLink} from "@angular/router";
import {HowItWorksComponent} from "../../shared/components/how-it-works/how-it-works.component";
import {BecomeHostComponent} from "../../shared/components/become-host/become-host.component";
import {StatsComponent} from "../../shared/components/stats/stats.component";

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    NavbarComponent,
    SearchBarComponent,
    MatCardModule,
    MatIconModule,
    RouterLink,
    HowItWorksComponent,
    BecomeHostComponent,
    StatsComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  ngAfterViewInit() {
    const section = document.querySelector('.fade-section');

    if (!section) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          section.classList.add('visible');
          observer.disconnect(); // animation une seule fois
        }
      },
      { threshold: 0.25 }
    );

    observer.observe(section);
  }

}
