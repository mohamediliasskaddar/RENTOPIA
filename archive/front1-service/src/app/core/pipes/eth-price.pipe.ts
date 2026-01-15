// src/app/core/pipes/eth-price.pipe.ts

import { Pipe, PipeTransform, inject } from '@angular/core';
import { CurrencyService } from '../services/currency.service';

@Pipe({
  name: 'ethPrice',
  standalone: true,
  pure: false // Pour réagir aux changements du service
})
export class EthPricePipe implements PipeTransform {
  private currencyService = inject(CurrencyService);

  /**
   * Transformer un montant ETH en affichage formaté
   *
   * @param value - Montant en ETH
   * @param showConversion - Afficher la conversion EUR (défaut: true)
   * @param compact - Mode compact (défaut: false)
   *
   * Exemples:
   * {{ 0.5 | ethPrice }} => "0.5000 ETH (≈ €1,600.00)"
   * {{ 0.5 | ethPrice:false }} => "0.5000 ETH"
   * {{ 0.5 | ethPrice:true:true }} => "0.5 ETH (≈ €1,600)"
   */
  transform(
    value: number | null | undefined,
    showConversion: boolean = true,
    compact: boolean = false
  ): string {
    if (value === null || value === undefined || isNaN(value)) {
      return '0 ETH';
    }

    const ethDecimals = compact ? 2 : 4;
    const eurDecimals = compact ? 0 : 2;

    const ethFormatted = value.toFixed(ethDecimals);

    if (showConversion && this.currencyService.shouldShowEurConversion()) {
      const eurAmount = this.currencyService.convertEthToEur(value);
      const eurFormatted = eurAmount.toLocaleString('fr-FR', {
        minimumFractionDigits: eurDecimals,
        maximumFractionDigits: eurDecimals
      });

      return `${ethFormatted} ETH (≈ €${eurFormatted})`;
    }

    return `${ethFormatted} ETH`;
  }
}
