// src/app/features/host/property-wizard/steps/step-pricing/step-pricing.component.ts

import { Component, Input, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSliderModule } from '@angular/material/slider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { Subject, debounceTime, takeUntil, distinctUntilChanged } from 'rxjs';

import { PropertyService } from '../../../../../core/services/property.service';
import { PricePredictionResponse } from '../../../../../core/models/price-prediction.model';
import {EthPricePipe} from "../../../../../core/pipes/eth-price.pipe";
import {CurrencyService} from "../../../../../core/services/currency.service";
@Component({
  selector: 'app-step-pricing',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatSliderModule,
    MatProgressSpinnerModule,
    MatButtonModule,
    EthPricePipe  // ✅ Importer la pipe
  ],
  templateUrl: './step-pricing.component.html',
  styleUrl: './step-pricing.component.scss'
})
export class StepPricingComponent implements OnInit, OnDestroy {
  @Input() form!: FormGroup;

  // Données du wizard (passées depuis le parent)
  @Input() surfaceArea: number = 80;
  @Input() bedrooms: number = 2;
  @Input() amenitiesCount: number = 5;

  private propertyService = inject(PropertyService);
  private currencyService = inject(CurrencyService);
  private destroy$ = new Subject<void>();

  // État de la prédiction
  loadingPrediction = false;
  prediction: PricePredictionResponse | null = null;
  predictionError: string | null = null;

  // Taux de conversion ETH/EUR
  ethToEurRate: number = 3200;

  ngOnInit(): void {
    // Charger la prédiction au démarrage
    this.loadPricePrediction();

    // Observer le taux de conversion
    this.currencyService.getEthToEurRate()
      .pipe(takeUntil(this.destroy$))
      .subscribe(rate => {
        this.ethToEurRate = rate;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Charger la prédiction de prix depuis l'API
   */
  loadPricePrediction(): void {
    this.loadingPrediction = true;
    this.predictionError = null;

    this.propertyService.suggestPriceForNewProperty({
      surfaceArea: this.surfaceArea,
      bedrooms: this.bedrooms,
      amenitiesCount: this.amenitiesCount
    })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('✅ Price prediction received:', response);
          this.prediction = response;
          this.loadingPrediction = false;

          // Mettre à jour le taux de conversion si fourni par l'API
          if (response.eth_eur_rate) {
            this.ethToEurRate = response.eth_eur_rate;
          }

          // ✅ Pré-remplir avec le prix EN ETH (pas EUR)
          if (!this.form.get('pricePerNight')?.value) {
            this.form.patchValue({
              pricePerNight: this.roundEth(response.predicted_price_eth)
            });
          }
        },
        error: (error) => {
          console.error('❌ Failed to load price prediction:', error);
          this.predictionError = 'Unable to load price suggestion';
          this.loadingPrediction = false;
        }
      });
  }

  /**
   * Appliquer le prix suggéré au formulaire (EN ETH)
   */
  applyPredictedPrice(): void {
    if (this.prediction) {
      this.form.patchValue({
        pricePerNight: this.roundEth(this.prediction.predicted_price_eth),
        weekendPricePerNight: this.roundEth(this.prediction.predicted_price_eth * 1.2) // +20% weekend
      });
    }
  }

  /**
   * Calculer les gains mensuels estimés (EN ETH)
   */
  getEstimatedEarningsEth(): number {
    const pricePerNight = this.form.get('pricePerNight')?.value || 0;
    const occupancyRate = 0.7; // 70% occupancy
    return pricePerNight * 30 * occupancyRate;
  }

  /**
   * Calculer les gains mensuels estimés (EN EUR)
   */
  getEstimatedEarningsEur(): number {
    return this.getEstimatedEarningsEth() * this.ethToEurRate;
  }

  /**
   * Obtenir le prix actuel en EUR (conversion)
   */
  getCurrentPriceEur(): number {
    const priceEth = this.form.get('pricePerNight')?.value || 0;
    return priceEth * this.ethToEurRate;
  }

  /**
   * Vérifier si le prix actuel est dans la fourchette recommandée (EN ETH)
   */
  isPriceInRange(): boolean {
    if (!this.prediction || !this.prediction.confidence_range_eth) {
      return true;
    }

    const currentPrice = this.form.get('pricePerNight')?.value || 0;
    const { min, max } = this.prediction.confidence_range_eth;

    return currentPrice >= min && currentPrice <= max;
  }

  /**
   * Obtenir un message selon la position du prix
   */
  getPriceMessage(): string {
    if (!this.prediction || !this.prediction.confidence_range_eth) {
      return '';
    }

    const currentPrice = this.form.get('pricePerNight')?.value || 0;
    const { min, max } = this.prediction.confidence_range_eth;

    if (currentPrice < min) {
      return 'Your price is below the recommended range. You might be underpricing.';
    } else if (currentPrice > max) {
      return 'Your price is above the recommended range. This might reduce bookings.';
    } else {
      return 'Your price is within the optimal range! 🎯';
    }
  }

  /**
   * Arrondir ETH à 4 décimales
   */
  roundEth(value: number): number {
    return Math.round(value * 10000) / 10000;
  }
}
