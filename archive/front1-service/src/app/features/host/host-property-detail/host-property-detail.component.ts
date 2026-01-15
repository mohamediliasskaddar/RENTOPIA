// src/app/features/host/host-property-detail/host-property-detail.component.ts
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { AvailabilityCalendarComponent } from '../../host/availability-calendar/availability-calendar.component';
import { AmenitiesManagerComponent } from '../../host/amenities-manager/amenities-manager.component';
import { PropertyService } from '../../../core/services/property.service';
import { PropertyDetail, PropertyPhoto, PropertyAmenity } from '../../../core/models/property-detail.model';
import { PricePredictionResponse } from '../../../core/models/price-prediction.model';
import { Observable, BehaviorSubject } from 'rxjs';
import { switchMap, tap, catchError, finalize } from 'rxjs/operators';
import { of } from 'rxjs';
import { DiscountsManagerComponent } from "../discounts-manager/discounts-manager.component";
import { PhotosManagerComponent } from '../../host/photos-manager/photos-manager.component';
import { EthPricePipe } from "../../../core/pipes/eth-price.pipe";

interface SelectOption {
  value: string;
  label: string;
}

@Component({
  selector: 'app-host-property-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatChipsModule,
    MatDividerModule,
    MatListModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatSlideToggleModule,
    AvailabilityCalendarComponent,
    AmenitiesManagerComponent,
    DiscountsManagerComponent,
    PhotosManagerComponent,
    EthPricePipe
  ],
  templateUrl: './host-property-detail.component.html',
  styleUrl: './host-property-detail.component.scss'
})
export class HostPropertyDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private propertyService = inject(PropertyService);
  private snackBar = inject(MatSnackBar);
  private fb = inject(FormBuilder);

  property$!: Observable<PropertyDetail | null>;
  property: PropertyDetail | null = null;
  propertyId!: number;
  loading$ = new BehaviorSubject<boolean>(true);
  error$ = new BehaviorSubject<string | null>(null);
  saving$ = new BehaviorSubject<boolean>(false);

  // AI Price Prediction
  loadingPrediction$ = new BehaviorSubject<boolean>(false);
  pricePrediction: PricePredictionResponse | null = null;
  showPriceSuggestion = false;

  // Edit modes for each section
  editMode: Record<string, boolean> = {
    basicInfo: false,
    capacity: false,
    description: false,
    checkInOut: false,
    stayRules: false,
    location: false,
    pricing: false,
    fees: false,
    rules: false,
    hostPreferences: false
  };

  // Forms for each section
  basicInfoForm!: FormGroup;
  capacityForm!: FormGroup;
  descriptionForm!: FormGroup;
  checkInOutForm!: FormGroup;
  stayRulesForm!: FormGroup;
  locationForm!: FormGroup;
  pricingForm!: FormGroup;
  feesForm!: FormGroup;
  rulesForm!: FormGroup;
  hostPreferencesForm!: FormGroup;

  selectedPhotoIndex = 0;

  propertyTypes: SelectOption[] = [
    { value: 'APARTMENT', label: 'Apartment' },
    { value: 'HOUSE', label: 'House' },
    { value: 'VILLA', label: 'Villa' },
    { value: 'STUDIO', label: 'Studio' },
    { value: 'LOFT', label: 'Loft' }
  ];

  placeTypes: SelectOption[] = [
    { value: 'ENTIRE_PLACE', label: 'Entire place' },
    { value: 'PRIVATE_ROOM', label: 'Private room' },
    { value: 'SHARED_ROOM', label: 'Shared room' }
  ];

  cancellationPolicies: SelectOption[] = [
    { value: 'Flexible', label: 'Flexible' },
    { value: 'Moderate', label: 'Moderate' },
    { value: 'Strict', label: 'Strict' }
  ];

  ngOnInit(): void {
    this.initForms();

    this.route.paramMap.subscribe(params => {
      this.propertyId = Number(params.get('id'));
      this.loadProperty();
    });

    this.property$ = this.route.paramMap.pipe(
      switchMap(() => {
        if (this.property) {
          return of(this.property);
        }
        return this.propertyService.getPropertyDetails(this.propertyId);
      })
    );
  }

  loadProperty(): void {
    this.loading$.next(true);
    this.error$.next(null);

    this.propertyService.getPropertyDetails(this.propertyId).pipe(
      tap(property => {
        this.property = property;
        this.populateForms(property);
        this.loading$.next(false);
      }),
      catchError(error => {
        console.error('Error loading property:', error);
        this.loading$.next(false);
        this.error$.next(error.message || 'Error loading property details');
        return of(null);
      })
    ).subscribe();
  }

  private initForms(): void {
    this.basicInfoForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(10)]],
      propertyType: ['', Validators.required],
      placeType: ['', Validators.required],
      surfaceArea: [null],
      floorNumber: [null]
    });

    this.capacityForm = this.fb.group({
      maxGuests: [1, [Validators.required, Validators.min(1)]],
      bedrooms: [1, [Validators.required, Validators.min(0)]],
      beds: [1, [Validators.required, Validators.min(1)]],
      bathrooms: [1, [Validators.required, Validators.min(1)]]
    });

    this.descriptionForm = this.fb.group({
      description: ['', [Validators.required, Validators.minLength(50)]]
    });

    this.checkInOutForm = this.fb.group({
      checkInTimeStart: ['', Validators.required],
      checkInTimeEnd: ['', Validators.required],
      checkOutTime: ['', Validators.required],
      instantBooking: [false]
    });

    this.stayRulesForm = this.fb.group({
      minStayNights: [1, [Validators.required, Validators.min(1)]],
      maxStayNights: [30, [Validators.required, Validators.min(1)]],
      bookingAdvanceDays: [1, [Validators.required, Validators.min(0)]],
      cancellationPolicy: ['Flexible', Validators.required]
    });

    this.locationForm = this.fb.group({
      adresseLine: ['', Validators.required],
      city: ['', Validators.required],
      country: ['', Validators.required],
      postalCode: ['', Validators.required],
      latitude: [null],
      longitude: [null],
      neighborhoodDescription: ['']
    });

    this.pricingForm = this.fb.group({
      pricePerNight: [0, [Validators.required, Validators.min(0.0001)]],
      weekendPricePerNight: [0, [Validators.required, Validators.min(0.0001)]]
    });

    this.feesForm = this.fb.group({
      cleaningFee: [0, Validators.min(0)],
      petFee: [0, Validators.min(0)],
      platformFeePercentage: [10, [Validators.min(0), Validators.max(100)]]
    });

    this.rulesForm = this.fb.group({
      childrenAllowed: [true],
      babiesAllowed: [true],
      petsAllowed: [false],
      smokingAllowed: [false],
      eventsAllowed: [false],
      additionalRules: ['']
    });

    this.hostPreferencesForm = this.fb.group({
      communicationStyle: [''],
      responseTime: [''],
      checkInProcess: ['']
    });
  }

  /**
   * ✅ FIX: Reload property after child component changes
   * Cette méthode est appelée par les composants enfants via (photosChanged), (discountsChanged), etc.
   */
  reloadProperty(): void {
    this.loading$.next(true);

    this.propertyService.getPropertyDetails(this.propertyId).subscribe({
      next: (property) => {
        this.property = property;
        this.populateForms(property);
        this.loading$.next(false);

        // Force change detection pour mettre à jour la vue
        this.property$ = of(property);
      },
      error: (error) => {
        console.error('Error reloading property:', error);
        this.loading$.next(false);
        this.snackBar.open('Error reloading property', 'Close', { duration: 3000 });
      }
    });
  }

  private populateForms(property: PropertyDetail): void {
    if (!property) return;

    this.basicInfoForm.patchValue({
      title: property.title,
      propertyType: property.propertyType,
      placeType: property.placeType,
      surfaceArea: property.surfaceArea,
      floorNumber: property.floorNumber
    });

    this.capacityForm.patchValue({
      maxGuests: property.maxGuests,
      bedrooms: property.bedrooms,
      beds: property.beds,
      bathrooms: property.bathrooms
    });

    this.descriptionForm.patchValue({
      description: property.description
    });

    this.checkInOutForm.patchValue({
      checkInTimeStart: property.checkInTimeStart,
      checkInTimeEnd: property.checkInTimeEnd,
      checkOutTime: property.checkOutTime,
      instantBooking: property.instantBooking
    });

    this.stayRulesForm.patchValue({
      minStayNights: property.minStayNights,
      maxStayNights: property.maxStayNights,
      bookingAdvanceDays: property.bookingAdvanceDays,
      cancellationPolicy: property.cancellationPolicy
    });

    this.locationForm.patchValue({
      adresseLine: property.adresseLine,
      city: property.city,
      country: property.country,
      postalCode: property.postalCode,
      latitude: property.latitude,
      longitude: property.longitude,
      neighborhoodDescription: property.neighborhoodDescription
    });

    this.pricingForm.patchValue({
      pricePerNight: property.pricePerNight,
      weekendPricePerNight: property.weekendPricePerNight
    });

    this.feesForm.patchValue({
      cleaningFee: property.cleaningFee,
      petFee: property.petFee,
      platformFeePercentage: property.platformFeePercentage
    });

    if (property.rules) {
      this.rulesForm.patchValue({
        childrenAllowed: property.rules.childrenAllowed,
        babiesAllowed: property.rules.babiesAllowed,
        petsAllowed: property.rules.petsAllowed,
        smokingAllowed: property.rules.smokingAllowed,
        eventsAllowed: property.rules.eventsAllowed,
        additionalRules: property.rules.additionalRules
      });
    }

    if (property.hostPreferences) {
      this.hostPreferencesForm.patchValue({
        communicationStyle: property.hostPreferences.communicationStyle,
        responseTime: property.hostPreferences.responseTime,
        checkInProcess: property.hostPreferences.checkInProcess
      });
    }
  }

  getPricePrediction(): void {
    if (!this.propertyId) {
      this.snackBar.open('Property ID not available', 'Close', { duration: 3000 });
      return;
    }

    this.loadingPrediction$.next(true);
    this.showPriceSuggestion = false;

    this.propertyService.suggestPriceForExistingProperty(this.propertyId).pipe(
      tap((prediction) => {
        this.pricePrediction = prediction;
        this.showPriceSuggestion = true;

        this.snackBar.open('✅ AI price suggestion generated!', 'Close', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
      }),
      catchError((error) => {
        console.error('Error getting price prediction:', error);
        this.snackBar.open('❌ Failed to get price suggestion', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
        return of(null);
      }),
      finalize(() => {
        this.loadingPrediction$.next(false);
      })
    ).subscribe();
  }

  applySuggestedPrice(): void {
    if (!this.pricePrediction) return;

    const suggestedPrice = parseFloat(this.pricePrediction.predicted_price_eth.toFixed(8));
    const weekendPrice = parseFloat((suggestedPrice * 1.2).toFixed(8));

    this.pricingForm.patchValue({
      pricePerNight: suggestedPrice,
      weekendPricePerNight: weekendPrice
    });

    this.pricingForm.get('pricePerNight')?.markAsTouched();
    this.pricingForm.get('weekendPricePerNight')?.markAsTouched();

    this.snackBar.open('💡 AI suggested price applied! Remember to save.', 'Close', {
      duration: 4000
    });
  }

  dismissSuggestion(): void {
    this.showPriceSuggestion = false;
    this.pricePrediction = null;
  }

  getPriceDifferencePercentage(): number | null {
    if (!this.pricePrediction || !this.property) return null;

    const currentPrice = this.property.pricePerNight;
    const suggestedPrice = this.pricePrediction.predicted_price_eth;

    if (currentPrice === 0) return null;

    return ((suggestedPrice - currentPrice) / currentPrice) * 100;
  }

  getPriceDifferenceColor(): string {
    const diff = this.getPriceDifferencePercentage();
    if (!diff) return '#666';

    if (diff > 0) return '#4CAF50';
    if (diff < 0) return '#f44336';
    return '#666';
  }

  toggleEditMode(section: string): void {
    if (this.editMode[section] && this.property) {
      this.populateForms(this.property);
    }
    this.editMode[section] = !this.editMode[section];

    if (section === 'pricing' && this.editMode[section]) {
      this.showPriceSuggestion = false;
    }
  }

  cancelEdit(section: string): void {
    if (this.property) {
      this.populateForms(this.property);
    }
    this.editMode[section] = false;

    if (section === 'pricing') {
      this.showPriceSuggestion = false;
    }
  }

  saveSection(section: string, form: FormGroup): void {
    if (form.invalid) {
      this.snackBar.open('Please fix the errors before saving', 'Close', {
        duration: 3000
      });
      return;
    }

    if (!this.property) {
      this.snackBar.open('Property data not loaded', 'Close', {
        duration: 3000
      });
      return;
    }

    this.saving$.next(true);

    const updateData = this.buildUpdateData(section, form.value);

    this.propertyService.patchProperty(this.propertyId, updateData).subscribe({
      next: (updatedProperty) => {
        this.saving$.next(false);
        this.editMode[section] = false;

        // ✅ Update local property immediately
        this.updateLocalProperty(section, form.value);

        this.snackBar.open('Changes saved successfully!', 'Close', {
          duration: 3000
        });

        if (section === 'pricing') {
          this.showPriceSuggestion = false;
          this.pricePrediction = null;
        }

        // ✅ Reload to ensure full sync
        this.reloadProperty();
      },
      error: (error) => {
        this.saving$.next(false);
        this.snackBar.open(error.message || 'Error saving changes', 'Close', {
          duration: 3000
        });
      }
    });
  }

  private buildUpdateData(section: string, formData: any): any {
    return formData;
  }

  private updateLocalProperty(section: string, formData: any): void {
    if (!this.property) return;

    if (section === 'rules') {
      this.property.rules = {
        ...this.property.rules,
        childrenAllowed: formData.childrenAllowed,
        babiesAllowed: formData.babiesAllowed,
        petsAllowed: formData.petsAllowed,
        smokingAllowed: formData.smokingAllowed,
        eventsAllowed: formData.eventsAllowed,
        additionalRules: formData.additionalRules
      };
    } else if (section === 'hostPreferences') {
      this.property.hostPreferences = {
        ...this.property.hostPreferences,
        communicationStyle: formData.communicationStyle,
        responseTime: formData.responseTime,
        checkInProcess: formData.checkInProcess
      };
    } else {
      Object.assign(this.property, formData);
    }
  }

  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      'ACTIVE': '#4CAF50',
      'DRAFT': '#FF9800',
      'ARCHIVED': '#9E9E9E',
      'DELETED': '#f44336'
    };
    return colors[status] || '#9E9E9E';
  }

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      'ACTIVE': 'Active',
      'DRAFT': 'Draft',
      'ARCHIVED': 'Archived',
      'DELETED': 'Deleted'
    };
    return labels[status] || status;
  }

  getPropertyTypeLabel(type: string): string {
    const found = this.propertyTypes.find(t => t.value === type);
    return found?.label || type;
  }

  getPlaceTypeLabel(type: string): string {
    const found = this.placeTypes.find(t => t.value === type);
    return found?.label || type;
  }

  getSortedPhotos(photos: PropertyPhoto[]): PropertyPhoto[] {
    if (!photos) return [];
    return [...photos].sort((a, b) => a.displayOrder - b.displayOrder);
  }

  selectPhoto(index: number): void {
    this.selectedPhotoIndex = index;
  }

  getAmenitiesByCategory(amenities: PropertyAmenity[]): Map<string, PropertyAmenity[]> {
    const map = new Map<string, PropertyAmenity[]>();
    if (!amenities) return map;

    amenities.forEach(amenity => {
      const category = amenity.category || 'Other';
      if (!map.has(category)) {
        map.set(category, []);
      }
      map.get(category)!.push(amenity);
    });

    return map;
  }

  getRuleIcon(allowed: boolean): string {
    return allowed ? 'check_circle' : 'cancel';
  }

  getRuleColor(allowed: boolean): string {
    return allowed ? '#4CAF50' : '#f44336';
  }

  copyToClipboard(text: string, label: string): void {
    navigator.clipboard.writeText(text).then(() => {
      this.snackBar.open(`${label} copied to clipboard`, 'Close', {
        duration: 2000
      });
    });
  }

  openGoogleMaps(lat: number, lng: number): void {
    window.open(`https://www.google.com/maps?q=${lat},${lng}`, '_blank');
  }
}
