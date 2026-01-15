// src/app/features/host/property-wizard/property-wizard.component.ts
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatStepperModule } from '@angular/material/stepper';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { STEPPER_GLOBAL_OPTIONS } from '@angular/cdk/stepper';

import { Store } from '@ngrx/store';
import { selectCurrentUser } from '../../../store/auth/auth.selectors';

import { PropertyService } from '../../../core/services/property.service';
import { MediaService } from '../../../core/services/media.service';

// Step Components
import { StepTypeComponent } from './steps/step-type/step-type.component';
import { StepLocationComponent } from './steps/step-location/step-location.component';
import { StepPhotosComponent } from './steps/step-photos/step-photos.component';
import { StepDetailsComponent } from './steps/step-details/step-details.component';
import { StepPricingComponent } from './steps/step-pricing/step-pricing.component';
import { StepRulesComponent } from './steps/step-rules/step-rules.component';
import { StepSummaryComponent } from './steps/step-summary/step-summary.component';

export interface PhotoFile {
  file: File;
  preview: string;
  isCover: boolean;
  displayOrder: number;
}

@Component({
  selector: 'app-property-wizard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatStepperModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    StepTypeComponent,
    StepLocationComponent,
    StepPhotosComponent,
    StepDetailsComponent,
    StepPricingComponent,
    StepRulesComponent,
    StepSummaryComponent
  ],
  providers: [
    {
      provide: STEPPER_GLOBAL_OPTIONS,
      useValue: { showError: true }
    }
  ],
  templateUrl: './property-wizard.component.html',
  styleUrl: './property-wizard.component.scss'
})
export class PropertyWizardComponent implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private store = inject(Store);
  private propertyService = inject(PropertyService);
  private mediaService = inject(MediaService);
  private snackBar = inject(MatSnackBar);

  // User
  currentUser$ = this.store.select(selectCurrentUser);
  userId: number | null = null;

  // Forms for each step
  typeForm!: FormGroup;
  locationForm!: FormGroup;
  detailsForm!: FormGroup;
  pricingForm!: FormGroup;
  rulesForm!: FormGroup;

  // Photos (stored locally until creation)
  photos: PhotoFile[] = [];

  // Loading state
  creating = false;
  uploadingPhotos = false;
  currentUploadIndex = 0;

  ngOnInit(): void {
    this.initForms();

    // Get user ID
    this.currentUser$.subscribe(user => {
      if (user?.id) {
        this.userId = user.id;
      }
    });
  }

  /**
   * Initialize all forms
   */
  private initForms(): void {
    this.typeForm = this.fb.group({
      propertyType: ['APARTMENT', Validators.required],
      placeType: ['ENTIRE_PLACE', Validators.required],
      title: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(100)]]
    });

    this.locationForm = this.fb.group({
      country: ['', Validators.required],
      city: ['', Validators.required],
      adresseLine: ['', Validators.required],
      postalCode: [''],
      latitude: [null],
      longitude: [null],
      neighborhoodDescription: ['']
    });

    this.detailsForm = this.fb.group({
      description: ['', [Validators.required, Validators.minLength(50)]],
      maxGuests: [1, [Validators.required, Validators.min(1)]],
      bedrooms: [1, [Validators.required, Validators.min(0)]],
      beds: [1, [Validators.required, Validators.min(1)]],
      bathrooms: [1, [Validators.required, Validators.min(1)]],
      surfaceArea: [null],
      floorNumber: [null]
    });

    this.pricingForm = this.fb.group({
      pricePerNight: [50, [Validators.required, Validators.min(1)]],
      weekendPricePerNight: [60, [Validators.required, Validators.min(1)]],
      cleaningFee: [0, Validators.min(0)],
      petFee: [0, Validators.min(0)],
      minStayNights: [1, [Validators.required, Validators.min(1)]],
      maxStayNights: [30, [Validators.required, Validators.min(1)]],
      bookingAdvanceDays: [1, Validators.min(0)]
    });

    this.rulesForm = this.fb.group({
      // Check-in/out times
      checkInTimeStart: ['14:00', Validators.required],
      checkInTimeEnd: ['22:00', Validators.required],
      checkOutTime: ['11:00', Validators.required],
      instantBooking: [true],
      cancellationPolicy: ['Flexible', Validators.required],

      // Property Rules (PropertyRule entity)
      childrenAllowed: [true],
      babiesAllowed: [true],
      petsAllowed: [false],
      smokingAllowed: [false],
      eventsAllowed: [false],
      additionalRules: [''],

      // Host Preferences (HostInteractionPreference entity)
      communicationStyle: ['Friendly and available'],
      responseTime: ['Within an hour'],
      checkInProcess: ['Self check-in with lockbox']
    });
  }

  /**
   * Handle photos change from step-photos
   */
  onPhotosChange(photos: PhotoFile[]): void {
    this.photos = photos;
  }

  /**
   * Create property
   */
  // src/app/features/host/property-wizard/property-wizard.component.ts

  /**
   * Create property
   */
  async createProperty(): Promise<void> {
    if (!this.userId) {
      this.snackBar.open('You must be logged in to create a property', 'Close', { duration: 3000 });
      return;
    }

    // Validate all forms
    if (this.typeForm.invalid || this.locationForm.invalid ||
      this.detailsForm.invalid || this.pricingForm.invalid || this.rulesForm.invalid) {
      this.snackBar.open('Please complete all required fields', 'Close', { duration: 3000 });
      return;
    }

    // Check photos
    if (this.photos.length === 0) {
      this.snackBar.open('Please add at least one photo', 'Close', { duration: 3000 });
      return;
    }

    this.creating = true;

    try {
      // 1. Build property data with proper structure
      const propertyData = this.buildPropertyData();

      console.log('Creating property with data:', propertyData);

      // 2. Create property (status will be DRAFT)
      const createdProperty = await this.propertyService.createProperty(propertyData).toPromise();

      if (!createdProperty?.propertyId) {
        throw new Error('Failed to create property');
      }

      const propertyId = createdProperty.propertyId;

      // 3. Upload photos
      this.uploadingPhotos = true;
      this.currentUploadIndex = 0;

      for (let i = 0; i < this.photos.length; i++) {
        this.currentUploadIndex = i + 1;
        const photo = this.photos[i];

        await this.mediaService.uploadPropertyImage(
          photo.file,
          propertyId,
          photo.isCover,
          photo.displayOrder
        ).toPromise();
      }

      this.uploadingPhotos = false;
      this.creating = false;

      // 4. Success - redirect
      this.snackBar.open('Property created successfully!', 'Close', { duration: 3000 });
      this.router.navigate(['/host/properties', propertyId]);

    } catch (error: any) {
      this.creating = false;
      this.uploadingPhotos = false;
      console.error('Error creating property:', error);
      this.snackBar.open(error.message || 'Error creating property', 'Close', { duration: 5000 });
    }
  }

  /**
   * Build property data with proper structure for backend
   */
  private buildPropertyData(): any {
    const typeData = this.typeForm.value;
    const locationData = this.locationForm.value;
    const detailsData = this.detailsForm.value;
    const pricingData = this.pricingForm.value;
    const rulesData = this.rulesForm.value;

    return {
      // User
      userId: this.userId,

      // Type (Step 1)
      title: typeData.title,
      propertyType: typeData.propertyType,
      placeType: typeData.placeType,

      // Location (Step 2)
      country: locationData.country,
      city: locationData.city,
      adresseLine: locationData.adresseLine,
      postalCode: locationData.postalCode,
      latitude: locationData.latitude,
      longitude: locationData.longitude,
      neighborhoodDescription: locationData.neighborhoodDescription,

      // Details (Step 4)
      description: detailsData.description,
      maxGuests: detailsData.maxGuests,
      bedrooms: detailsData.bedrooms,
      beds: detailsData.beds,
      bathrooms: detailsData.bathrooms,
      surfaceArea: detailsData.surfaceArea,
      floorNumber: detailsData.floorNumber,

      // Pricing (Step 5)
      pricePerNight: pricingData.pricePerNight,
      weekendPricePerNight: pricingData.weekendPricePerNight,
      cleaningFee: pricingData.cleaningFee,
      petFee: pricingData.petFee,
      minStayNights: pricingData.minStayNights,
      maxStayNights: pricingData.maxStayNights,
      bookingAdvanceDays: pricingData.bookingAdvanceDays,

      // Rules (Step 6) - Direct fields on Property
      checkInTimeStart: rulesData.checkInTimeStart,
      checkInTimeEnd: rulesData.checkInTimeEnd,
      checkOutTime: rulesData.checkOutTime,
      instantBooking: rulesData.instantBooking,
      cancellationPolicy: rulesData.cancellationPolicy,

      // PropertyRule entity
      rules: {
        childrenAllowed: rulesData.childrenAllowed,
        babiesAllowed: rulesData.babiesAllowed,
        petsAllowed: rulesData.petsAllowed,
        smokingAllowed: rulesData.smokingAllowed,
        eventsAllowed: rulesData.eventsAllowed,
        additionalRules: rulesData.additionalRules
      },

      // HostInteractionPreference entity
      hostPreferences: {
        communicationStyle: rulesData.communicationStyle,
        responseTime: rulesData.responseTime,
        checkInProcess: rulesData.checkInProcess
      }
    };
  }
  /**
   * Get all form data for summary
   */
  getPropertyData(): any {
    return {
      ...this.typeForm.value,
      ...this.locationForm.value,
      ...this.detailsForm.value,
      ...this.pricingForm.value,
      ...this.rulesForm.value,
      photosCount: this.photos.length
    };
  }
}
