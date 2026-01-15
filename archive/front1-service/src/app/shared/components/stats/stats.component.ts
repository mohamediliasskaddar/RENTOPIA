// src/app/shared/components/stats/stats.component.ts
import { Component, OnInit, ElementRef, ViewChildren, QueryList } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

interface Stat {
  icon: string;
  value: number;
  suffix: string;
  label: string;
  prefix?: string;
}

@Component({
  selector: 'app-stats',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './stats.component.html',
  styleUrl: './stats.component.scss'
})
export class StatsComponent implements OnInit {

  @ViewChildren('statValue') statElements!: QueryList<ElementRef>;

  stats: Stat[] = [
    {
      icon: 'home_work',
      value: 10000,
      suffix: '+',
      label: 'Properties Listed',
      prefix: ''
    },
    {
      icon: 'people',
      value: 50000,
      suffix: '+',
      label: 'Happy Guests',
      prefix: ''
    },
    {
      icon: 'star',
      value: 4.8,
      suffix: '',
      label: 'Average Rating',
      prefix: ''
    },
    {
      icon: 'location_city',
      value: 100,
      suffix: '+',
      label: 'Cities Covered',
      prefix: ''
    }
  ];

  displayValues: number[] = [];
  hasAnimated = false;

  ngOnInit(): void {
    // Initialiser les valeurs d'affichage à 0
    this.displayValues = this.stats.map(() => 0);

    // Observer l'intersection pour déclencher l'animation au scroll
    this.setupIntersectionObserver();
  }

  private setupIntersectionObserver(): void {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting && !this.hasAnimated) {
            this.hasAnimated = true;
            this.animateStats();
          }
        });
      },
      { threshold: 0.5 }
    );

    // Observer le premier élément stat
    setTimeout(() => {
      const firstElement = this.statElements.first?.nativeElement;
      if (firstElement) {
        observer.observe(firstElement.closest('.stats-section'));
      }
    }, 100);
  }

  private animateStats(): void {
    this.stats.forEach((stat, index) => {
      this.animateValue(index, stat.value);
    });
  }

  private animateValue(index: number, targetValue: number): void {
    const duration = 2000; // 2 secondes
    const startTime = Date.now();
    const startValue = 0;

    const animate = () => {
      const currentTime = Date.now();
      const elapsed = currentTime - startTime;
      const progress = Math.min(elapsed / duration, 1);

      // Easing function (ease-out)
      const easeOut = 1 - Math.pow(1 - progress, 3);

      const currentValue = startValue + (targetValue - startValue) * easeOut;

      // Pour les décimales (comme 4.8)
      if (targetValue < 10) {
        this.displayValues[index] = Math.round(currentValue * 10) / 10;
      } else {
        this.displayValues[index] = Math.floor(currentValue);
      }

      if (progress < 1) {
        requestAnimationFrame(animate);
      } else {
        this.displayValues[index] = targetValue;
      }
    };

    // Délai progressif pour chaque stat
    setTimeout(() => {
      requestAnimationFrame(animate);
    }, index * 150);
  }

  formatValue(index: number): string {
    const value = this.displayValues[index];
    const stat = this.stats[index];

    // Pour les décimales
    if (stat.value < 10) {
      return value.toFixed(1);
    }

    // Pour les grands nombres, ajouter des séparateurs de milliers
    return value.toLocaleString('en-US');
  }
}
