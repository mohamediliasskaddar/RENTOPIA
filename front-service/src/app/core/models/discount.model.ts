// src/app/core/models/discount.model.ts

/**
 * ============================
 * Interface Discount
 * Correspond EXACTEMENT à l'entité Discount backend
 * ============================
 */
export interface Discount {
  discountId: number;
  discountType: string;        // "first_booking", "last_minute", "weekly", "monthly"
  discountPercentage: number;
  minNights?: number;
  description?: string;
}

/**
 * ============================
 * Types de réductions disponibles
 * ============================
 */
export enum DiscountType {
  FIRST_BOOKING = 'first_booking',
  LAST_MINUTE = 'last_minute',
  WEEKLY = 'weekly',
  MONTHLY = 'monthly'
}
