// src/app/shared/components/search-bar/search-bar.component.ts
import {Component, OnInit, ViewChild, HostListener, Input, ElementRef} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, startWith } from 'rxjs/operators';

// Material Imports
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepicker, MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule, MatMenuTrigger } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatAutocompleteModule } from '@angular/material/autocomplete';

// Services
import { LocationService, Location } from '../../../core/services/location.service';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule,
    MatAutocompleteModule
  ],
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.scss'
})
export class SearchBarComponent implements OnInit {

  @Input() compact = false;
  @Input() theme: 'light' | 'dark' = 'dark';

  @ViewChild('dateRangePicker') dateRangePicker!: MatDatepicker<any>;
  @ViewChild('guestsTrigger') guestsTrigger!: MatMenuTrigger;

  searchForm!: FormGroup;

  // Autocomplete locations
  filteredLocations$!: Observable<Location[]>;
  selectedLocation: Location | null = null;

  // État des compteurs
  adults = 1;
  children = 0;
  babies = 0;
  pets = 0;

  // Date min (aujourd'hui)
  minDate = new Date();

  // État d'ouverture
  isDatePickerOpen = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private locationService: LocationService,
    private elementRef: ElementRef
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.setupLocationAutocomplete();
  }

  /**
   * ÉCOUTER LES CLICS EN DEHORS DU COMPOSANT
   */
  @HostListener('document:click', ['$event'])
  onClickOutside(event: MouseEvent): void {
    const target = event.target as HTMLElement;

    const searchBarElement = this.elementRef.nativeElement.querySelector('.search-bar-container');
    const isClickInsideSearchBar = searchBarElement?.contains(target);

    const overlayContainer = document.querySelector('.cdk-overlay-container');
    const isClickInOverlay = overlayContainer?.contains(target);

    if (!isClickInsideSearchBar && !isClickInOverlay) {
      this.closeAllOverlays();
    }
  }

  /**
   * INITIALISER LE FORMULAIRE
   */
  private initForm(): void {
    this.searchForm = this.fb.group({
      location: ['', Validators.required],
      checkIn: ['', Validators.required],
      checkOut: ['', Validators.required]
    }, { validators: this.dateRangeValidator });
  }

  /**
   * CONFIGURER L'AUTOCOMPLETE DES LOCATIONS
   */
  private setupLocationAutocomplete(): void {
    this.filteredLocations$ = this.searchForm.get('location')!.valueChanges.pipe(
      startWith(''),
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(value => {
        const searchTerm = typeof value === 'string' ? value : (value?.displayName || '');
        return this.locationService.searchLocations(searchTerm);
      })
    );
  }

  /**
   * VALIDATEUR PERSONNALISÉ : Check-out > Check-in
   */
  private dateRangeValidator(group: FormGroup): { [key: string]: boolean } | null {
    const checkIn = group.get('checkIn')?.value;
    const checkOut = group.get('checkOut')?.value;

    if (checkIn && checkOut && new Date(checkIn) >= new Date(checkOut)) {
      return { dateRangeInvalid: true };
    }
    return null;
  }

  /**
   * OUVRIR LE DATEPICKER
   */
  openDatePicker(): void {
    if (this.guestsTrigger?.menuOpen) {
      this.guestsTrigger.closeMenu();
    }
    this.dateRangePicker.open();
    this.isDatePickerOpen = true;
  }

  /**
   * FERMER LE DATEPICKER
   */
  closeDatePicker(): void {
    if (this.dateRangePicker) {
      this.dateRangePicker.close();
      this.isDatePickerOpen = false;
    }
  }

  /**
   * FERMER TOUS LES OVERLAYS
   */
  closeAllOverlays(): void {
    this.closeDatePicker();
    if (this.guestsTrigger?.menuOpen) {
      this.guestsTrigger.closeMenu();
    }
  }

  /**
   * ÉCOUTER LA FERMETURE DU GUESTS MENU
   */
  onGuestsMenuClosed(): void {
    // Menu fermé automatiquement
  }

  /**
   * ÉCOUTER LA FERMETURE DU DATEPICKER
   */
  onDatePickerClosed(): void {
    this.isDatePickerOpen = false;
  }

  /**
   * GESTION DES COMPTEURS (Guests)
   */
  incrementAdults(): void {
    if (this.adults < 16) this.adults++;
  }

  decrementAdults(): void {
    if (this.adults > 1) this.adults--;
  }

  incrementChildren(): void {
    if (this.children < 10) this.children++;
  }

  decrementChildren(): void {
    if (this.children > 0) this.children--;
  }

  incrementBabies(): void {
    if (this.babies < 5) this.babies++;
  }

  decrementBabies(): void {
    if (this.babies > 0) this.babies--;
  }

  incrementPets(): void {
    if (this.pets < 5) this.pets++;
  }

  decrementPets(): void {
    if (this.pets > 0) this.pets--;
  }

  /**
   * TOTAL DES GUESTS
   */
  getTotalGuests(): number {
    return this.adults + this.children + this.babies;
  }

  /**
   * TEXTE AFFICHÉ POUR LES GUESTS
   */
  getGuestsText(): string {
    const total = this.getTotalGuests();
    let text = `${total} guest${total > 1 ? 's' : ''}`;

    if (this.pets > 0) {
      text += `, ${this.pets} pet${this.pets > 1 ? 's' : ''}`;
    }

    return text;
  }

  /**
   * LOCATION SÉLECTIONNÉE DEPUIS L'AUTOCOMPLETE
   */
  onLocationSelected(location: Location): void {
    this.selectedLocation = location;
    this.searchForm.patchValue({ location: location.displayName });
  }

  /**
   * AFFICHER LE NOM DE LA LOCATION DANS L'AUTOCOMPLETE
   */
  displayLocation(location: Location | string): string {
    if (typeof location === 'string') {
      return location;
    }
    return location?.displayName || '';
  }

  /**
   * SOUMETTRE LA RECHERCHE
   */
  onSearch(): void {
    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      return;
    }

    const checkIn = this.formatDate(this.searchForm.value.checkIn);
    const checkOut = this.formatDate(this.searchForm.value.checkOut);

    const queryParams: any = {
      checkIn,
      checkOut,
      adults: this.adults
    };

    if (this.selectedLocation) {
      queryParams.city = this.selectedLocation.city;
      queryParams.country = this.selectedLocation.country;
    } else {
      const locationText = this.searchForm.value.location;
      const parsed = this.locationService.parseLocationString(locationText);
      if (parsed) {
        queryParams.city = parsed.city;
        queryParams.country = parsed.country;
      } else {
        queryParams.city = locationText;
      }
    }

    if (this.children > 0) queryParams.children = this.children;
    if (this.babies > 0) queryParams.babies = this.babies;
    if (this.pets > 0) queryParams.pets = this.pets;

    this.router.navigate(['/listings'], { queryParams });
  }

  /**
   * FORMATER UNE DATE EN ISO STRING
   */
  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }
}
