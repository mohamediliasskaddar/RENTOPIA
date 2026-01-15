// src/app/features/listings/components/property-card/property-card.component.ts

import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';

// Models
import { PropertyCard } from './../../core/models/property-card.model';
import { PropertySearchResultDTO } from './../../core/models/property.model';
import {EthPricePipe} from "../../core/pipes/eth-price.pipe";

/**
 * ============================
 * COMPOSANT PROPERTY CARD
 * Carte affichant une property (style Airbnb)
 *
 * Deux modes d'affichage :
 * - Mode browse (PropertyCard) : affiche pricePerNight + photo principale
 * - Mode search (PropertySearchResultDTO) : affiche totalPrice et discountPercentage
 * ============================
 */
@Component({
  selector: 'app-property-card',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatButtonModule,
    RouterLink,
    EthPricePipe // ✅ Import du pipe
  ],
  templateUrl: './property-card.component.html',
  styleUrl: './property-card.component.scss'
})
export class PropertyCardComponent {

  @Input() property!: PropertyCard | PropertySearchResultDTO;
  @Input() isSearchMode = false;

  /**
   * ============================
   * VÉRIFIER SI C'EST UN PropertySearchResultDTO
   * ============================
   */
  isSearchResult(property: any): property is PropertySearchResultDTO {
    return 'totalPrice' in property;
  }

  /**
   * ============================
   * OBTENIR L'IMAGE DE COUVERTURE
   * ============================
   */
  getCoverImage(): string {
    if ('mainPhotoUrl' in this.property && this.property.mainPhotoUrl) {
      return this.property.mainPhotoUrl;
    }
    return 'assets/images/hero.jpg';
  }

  /**
   * ============================
   * GESTION ERREUR IMAGE
   * ============================
   */
  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'assets/images/logo.png';
  }

  /**
   * ============================
   * OBTENIR LE PRIX À AFFICHER (toujours en ETH)
   * ============================
   */
  getDisplayPrice(): number {
    if (this.isSearchMode && this.isSearchResult(this.property)) {
      return this.property.totalPrice; // Total en ETH
    }
    return (this.property as PropertyCard).pricePerNight || 0; // Prix/nuit en ETH
  }

  /**
   * ============================
   * OBTENIR LE LABEL DU PRIX
   * ============================
   */
  getPriceLabel(): string {
    if (this.isSearchMode && this.isSearchResult(this.property)) {
      return 'total';
    }
    return 'night';
  }

  /**
   * ============================
   * OBTENIR LA RÉDUCTION SI DISPONIBLE
   * ============================
   */
  getDiscount(): number | null {
    if (this.isSearchMode && this.isSearchResult(this.property)) {
      return this.property.discountPercentage > 0 ? this.property.discountPercentage : null;
    }
    return null;
  }

  /**
   * ============================
   * OBTENIR LE NOMBRE DE NUITS (mode search)
   * ============================
   */
  getNights(): number | null {
    if (this.isSearchMode && this.isSearchResult(this.property)) {
      return this.property.nights;
    }
    return null;
  }

  /**
   * ============================
   * OBTENIR LA NOTE MOYENNE
   * ============================
   */
  getAverageRating(): number | null {
    if ('averageRating' in this.property) {
      return this.property.averageRating || null;
    }
    return null;
  }

  /**
   * ============================
   * OBTENIR LE NOMBRE D'AVIS
   * ============================
   */
  getReviewCount(): number | null {
    if ('reviewCount' in this.property) {
      return this.property.reviewCount || null;
    }
    return null;
  }
}
