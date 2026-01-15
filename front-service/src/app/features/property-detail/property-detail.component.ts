// src/app/features/property-detail/property-detail.component.ts
// ✅ VERSION FINALE avec Reviews intégrés

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, takeUntil, forkJoin, of, Observable } from 'rxjs';
import { switchMap, catchError } from 'rxjs/operators';
import { Store } from '@ngrx/store';

// Material
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';

// Leaflet
import { LeafletModule } from '@asymmetrik/ngx-leaflet';
import * as L from 'leaflet';

// Models & Services
import { PropertyDetail } from '../../core/models/property-detail.model';
import { UserResponseDTO } from '../../core/models/user.model';
import { PropertyService } from '../../core/services/property.service';
import { UserService } from '../../core/services/user.service';

// Reviews
import { Review } from '../../core/models/review.model';
import { PropertyReviewStats } from '../../core/models/review-stats.model';
import * as ReviewActions from '../../store/review/review.actions';
import {
  selectAllReviews,
  selectReviewStats,
  selectReviewsLoading
} from '../../store/review/review.selectors';

// Components
import { PhotoGalleryDialogComponent } from './photo-gallery-dialog/photo-gallery-dialog.component';
import { ReviewListComponent } from '../reviews/components/review-list/review-list.component';
import { BookingCardComponent } from './booking-card/booking-card.component';
@Component({
  selector: 'app-property-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule,
    MatDialogModule,
    LeafletModule,
    ReviewListComponent,
    BookingCardComponent
  ],
  templateUrl: './property-detail.component.html',
  styleUrl: './property-detail.component.scss'
})
export class PropertyDetailComponent implements OnInit, OnDestroy {

  property: PropertyDetail | null = null;
  host: UserResponseDTO | null = null;
  loading = true;
  error: string | null = null;

  // Photos
  mainPhoto: string = '';
  visiblePhotos: string[] = [];
  allPhotos: string[] = [];

  // Map
  map!: L.Map;
  mapOptions: any;
  mapLayers: L.Layer[] = [];

  // ✅ Reviews
  reviews$!: Observable<Review[]>;
  reviewStats$!: Observable<PropertyReviewStats | null>;
  reviewsLoading$!: Observable<boolean>;

  propertyId: number = 0; // ✅ AJOUTER
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private propertyService: PropertyService,
    private userService: UserService,
    private dialog: MatDialog,
    private store: Store
  ) {}

  ngOnInit(): void {
    // Initialiser les observables reviews
    this.reviews$ = this.store.select(selectAllReviews);
    this.reviewStats$ = this.store.select(selectReviewStats);
    this.reviewsLoading$ = this.store.select(selectReviewsLoading);

    const propertyId = this.route.snapshot.paramMap.get('id');
    if (propertyId) {
      this.propertyId = +propertyId;
      this.loadProperty(+propertyId);
    } else {
      this.error = 'Invalid property ID';
      this.loading = false;
    }

  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadProperty(id: number): void {
    this.loading = true;

    this.propertyService.getPropertyDetails(id)
      .pipe(
        switchMap(property =>
          forkJoin({
            property: of(property),
            host: this.userService.getUserById(property.userId).pipe(
              catchError(error => {
                console.warn('Failed to load host info:', error);
                return of(null);
              })
            )
          })
        ),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: ({ property, host }) => {
          this.property = property;
          this.host = host;
          this.preparePhotos();
          this.initMap();
          this.loading = false;

          // ✅ Charger les reviews
          this.loadReviews(id);
        },
        error: (error) => {
          console.error('Error loading property:', error);
          this.error = 'Failed to load property';
          this.loading = false;
        }
      });
  }

  // ✅ Charger les reviews
  private loadReviews(propertyId: number): void {
    console.log('✅ Loading reviews for property:', propertyId);
    this.store.dispatch(ReviewActions.loadPropertyReviews({ propertyId }));
    this.store.dispatch(ReviewActions.loadPropertyStats({ propertyId }));
  }

  private preparePhotos(): void {
    if (!this.property?.photos || this.property.photos.length === 0) {
      return;
    }

    const sortedPhotos = [...this.property.photos].sort((a, b) =>
      (a.displayOrder || 0) - (b.displayOrder || 0)
    );

    this.allPhotos = sortedPhotos.map(p => p.photoUrl);
    this.mainPhoto = this.allPhotos[0] || '';
    this.visiblePhotos = this.allPhotos.slice(0, 5);
  }

  private initMap(): void {
    if (!this.property?.latitude || !this.property?.longitude) {
      return;
    }

    const lat = this.property.latitude;
    const lng = this.property.longitude;

    this.mapOptions = {
      layers: [
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          maxZoom: 18,
          attribution: '© OpenStreetMap'
        })
      ],
      zoom: 15,
      center: L.latLng(lat, lng)
    };

    const marker = L.marker([lat, lng], {
      icon: L.icon({
        iconSize: [25, 41],
        iconAnchor: [13, 41],
        iconUrl: 'assets/marker-icon.png',
        shadowUrl: 'assets/marker-shadow.png'
      })
    });

    this.mapLayers = [marker];
  }


// src/app/features/property-detail/property-detail.component.ts
// ✅ Méthode openPhotoGallery() CORRIGÉE

  openPhotoGallery(): void {
    this.dialog.open(PhotoGalleryDialogComponent, {
      data: {
        photos: this.allPhotos,
        startIndex: 0,
        propertyTitle: this.property?.title || 'Property Photos'
      },
      width: '90vw',
      maxWidth: '1200px',
      height: '85vh',
      maxHeight: '800px',
      panelClass: 'photo-gallery-dialog-container', // ✅ Cette classe permet le centrage via CSS
      hasBackdrop: true,
      backdropClass: 'photo-gallery-backdrop',
      disableClose: false,
      autoFocus: false
    });
  }
  // ✅ Getters pour les reviews
  get averageRating(): number {
    let rating = 0;
    this.reviewStats$.pipe(takeUntil(this.destroy$)).subscribe(stats => {
      rating = stats?.averageRating || 0;
    });
    return rating;
  }

  get totalReviews(): number {
    let total = 0;
    this.reviewStats$.pipe(takeUntil(this.destroy$)).subscribe(stats => {
      total = stats?.totalReviews || 0;
    });
    return total;
  }

  // Autres getters
  get hasMorePhotos(): boolean {
    return this.allPhotos.length > 5;
  }

  get remainingPhotosCount(): number {
    return this.allPhotos.length - 5;
  }

  get pricePerNight(): number {
    return this.property?.pricePerNight || 0;
  }

  get weekendPrice(): number | null {
    return this.property?.weekendPricePerNight || null;
  }

  get hasWeekendPrice(): boolean {
    return !!this.weekendPrice && this.weekendPrice !== this.pricePerNight;
  }

  get hostFullName(): string {
    if (!this.host) return 'Host';
    return `${this.host.prenom} ${this.host.nom}`;
  }

  get hostPhotoUrl(): string {
    return this.host?.photoUrl || 'assets/default-avatar.png';
  }

  get hostLanguages(): string {
    if (!this.host?.languages || this.host.languages.length === 0) {
      return 'Not specified';
    }
    return this.host.languages.map(l => l.languageName).join(', ');
  }

  get hostJoinedYear(): string {
    if (!this.host?.createdAt) return '';
    return new Date(this.host.createdAt).getFullYear().toString();
  }

  goBack(): void {
    this.router.navigate(['/listings']);
  }


}
