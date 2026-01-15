// src/app/features/host/host-property-detail/components/discounts-manager/discounts-manager.component.ts
import { Component, Input, OnInit, inject, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';

import { PropertyService } from '../../../core/services/property.service';
import { Discount, DiscountType } from '../../../core/models/discount.model';
import { EthPricePipe } from '../../../core/pipes/eth-price.pipe';
interface DiscountTypeOption {
  value: string;
  label: string;
  description: string;
  icon: string;
  defaultMinNights?: number;
}

@Component({
  selector: 'app-discounts-manager',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    EthPricePipe
  ],
  templateUrl: './discounts-manager.component.html',
  styleUrl: './discounts-manager.component.scss'
})
export class DiscountsManagerComponent implements OnInit {
  @Input() propertyId!: number;
  @Input() currentDiscounts: Discount[] = [];
  @Output() discountsChanged = new EventEmitter<void>();

  private propertyService = inject(PropertyService);
  private snackBar = inject(MatSnackBar);
  private fb = inject(FormBuilder);

  // Form
  discountForm!: FormGroup;

  // States
  showAddForm = false;
  editingDiscount: Discount | null = null;
  saving = false;

  // Discount types - Utilise ton enum existant
  discountTypes: DiscountTypeOption[] = [
    {
      value: DiscountType.WEEKLY,
      label: 'Weekly Discount',
      description: 'Discount for stays of 7+ nights',
      icon: 'date_range',
      defaultMinNights: 7
    },
    {
      value: DiscountType.MONTHLY,
      label: 'Monthly Discount',
      description: 'Discount for stays of 28+ nights',
      icon: 'calendar_month',
      defaultMinNights: 28
    },
    {
      value: DiscountType.FIRST_BOOKING,
      label: 'First Booking',
      description: 'Special discount for first-time guests',
      icon: 'celebration',
      defaultMinNights: 1
    },
    {
      value: DiscountType.LAST_MINUTE,
      label: 'Last Minute',
      description: 'Discount for last minute bookings',
      icon: 'bolt',
      defaultMinNights: 1
    }
  ];

  ngOnInit(): void {
    this.initForm();
  }

  /**
   * Initialize form
   */
  initForm(): void {
    this.discountForm = this.fb.group({
      discountType: [DiscountType.WEEKLY, Validators.required],
      discountPercentage: [10, [Validators.required, Validators.min(1), Validators.max(90)]],
      minNights: [7, [Validators.min(1)]],
      description: ['']
    });

    // Update minNights when discount type changes
    this.discountForm.get('discountType')?.valueChanges.subscribe(type => {
      const discountType = this.discountTypes.find(t => t.value === type);
      if (discountType?.defaultMinNights) {
        this.discountForm.patchValue({ minNights: discountType.defaultMinNights });
      }
    });
  }

  /**
   * Get discount type info
   */
  getDiscountTypeInfo(type: string): DiscountTypeOption | undefined {
    return this.discountTypes.find(t => t.value === type);
  }

  /**
   * Show add form
   */
  showAdd(): void {
    this.showAddForm = true;
    this.editingDiscount = null;
    this.discountForm.reset({
      discountType: DiscountType.WEEKLY,
      discountPercentage: 10,
      minNights: 7,
      description: ''
    });
  }

  /**
   * Edit existing discount
   */
  editDiscount(discount: Discount): void {
    this.editingDiscount = discount;
    this.showAddForm = true;
    this.discountForm.patchValue({
      discountType: discount.discountType,
      discountPercentage: discount.discountPercentage,
      minNights: discount.minNights || 1,
      description: discount.description || ''
    });
  }

  /**
   * Cancel form
   */
  cancelForm(): void {
    this.showAddForm = false;
    this.editingDiscount = null;
  }

  /**
   * Save discount
   */
  saveDiscount(): void {
    if (this.discountForm.invalid) {
      this.snackBar.open('Please fill in all required fields', 'Close', { duration: 3000 });
      return;
    }

    this.saving = true;
    const formData = this.discountForm.value;

    if (this.editingDiscount) {
      // Update existing
      this.propertyService.updateDiscount(this.propertyId, this.editingDiscount.discountId, formData).subscribe({
        next: () => {
          this.saving = false;
          this.showAddForm = false;
          this.editingDiscount = null;
          this.snackBar.open('Discount updated successfully!', 'Close', { duration: 3000 });
          this.discountsChanged.emit();
        },
        error: (error) => {
          this.saving = false;
          this.snackBar.open(error.message || 'Error updating discount', 'Close', { duration: 3000 });
        }
      });
    } else {
      // Add new
      this.propertyService.addDiscountToProperty(this.propertyId, formData).subscribe({
        next: () => {
          this.saving = false;
          this.showAddForm = false;
          this.snackBar.open('Discount added successfully!', 'Close', { duration: 3000 });
          this.discountsChanged.emit();
        },
        error: (error) => {
          this.saving = false;
          this.snackBar.open(error.message || 'Error adding discount', 'Close', { duration: 3000 });
        }
      });
    }
  }

  /**
   * Delete discount
   */
  deleteDiscount(discount: Discount): void {
    const typeName = this.getDiscountTypeInfo(discount.discountType)?.label || 'discount';
    if (!confirm(`Are you sure you want to delete this ${typeName}?`)) {
      return;
    }

    this.saving = true;

    this.propertyService.removeDiscountFromProperty(this.propertyId, discount.discountId).subscribe({
      next: () => {
        this.saving = false;
        this.snackBar.open('Discount removed successfully!', 'Close', { duration: 3000 });
        this.discountsChanged.emit();
      },
      error: (error) => {
        this.saving = false;
        this.snackBar.open(error.message || 'Error removing discount', 'Close', { duration: 3000 });
      }
    });
  }

  /**
   * Check if discount type already exists
   */
  discountTypeExists(type: string): boolean {
    return this.currentDiscounts.some(d => d.discountType === type);
  }
}
