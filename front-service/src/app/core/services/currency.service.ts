// src/app/core/services/currency.service.ts

import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CurrencyService {
  // Taux de conversion ETH/EUR (à mettre à jour régulièrement)
  // Tu peux aussi le récupérer depuis une API comme CoinGecko
  private ethToEurRate$ = new BehaviorSubject<number>(3200); // 1 ETH ≈ 3200 EUR

  // Préférence d'affichage de l'utilisateur
  private showEurConversion$ = new BehaviorSubject<boolean>(true);

  constructor() {
    this.loadUserPreference();
  }

  /**
   * Obtenir le taux de conversion actuel
   */
  getEthToEurRate(): Observable<number> {
    return this.ethToEurRate$.asObservable();
  }

  /**
   * Obtenir le taux actuel (sync)
   */
  getCurrentRate(): number {
    return this.ethToEurRate$.value;
  }

  /**
   * Convertir ETH en EUR
   */
  convertEthToEur(ethAmount: number): number {
    return ethAmount * this.getCurrentRate();
  }

  /**
   * Formater le prix en ETH avec conversion EUR optionnelle
   */
  formatPrice(ethAmount: number, showConversion: boolean = true): string {
    const ethFormatted = `${ethAmount.toFixed(4)} ETH`;

    if (showConversion && this.showEurConversion$.value) {
      const eurAmount = this.convertEthToEur(ethAmount);
      return `${ethFormatted} (≈ €${eurAmount.toFixed(2)})`;
    }

    return ethFormatted;
  }

  /**
   * Obtenir la préférence d'affichage EUR
   */
  shouldShowEurConversion(): Observable<boolean> {
    return this.showEurConversion$.asObservable();
  }

  /**
   * Définir la préférence d'affichage EUR
   */
  setShowEurConversion(show: boolean): void {
    this.showEurConversion$.next(show);
    localStorage.setItem('showEurConversion', JSON.stringify(show));
  }

  /**
   * Charger la préférence depuis localStorage
   */
  private loadUserPreference(): void {
    const saved = localStorage.getItem('showEurConversion');
    if (saved !== null) {
      this.showEurConversion$.next(JSON.parse(saved));
    }
  }

  /**
   * Mettre à jour le taux de conversion (à appeler périodiquement)
   * Tu peux utiliser l'API CoinGecko ou une autre source
   */
  async updateEthRate(): Promise<void> {
    try {
      // Exemple avec CoinGecko API (gratuit)
      const response = await fetch(
        'https://api.coingecko.com/api/v3/simple/price?ids=ethereum&vs_currencies=eur'
      );
      const data = await response.json();

      if (data.ethereum?.eur) {
        this.ethToEurRate$.next(data.ethereum.eur);
      }
    } catch (error) {
      console.warn('Failed to update ETH rate:', error);
    }
  }
}
