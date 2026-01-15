// src/app/features/host/host-property-detail/components/availability-calendar/availability-calendar.component.ts
import { Component, Input, OnInit, OnChanges, SimpleChanges, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';

import { PropertyService } from '../../../core/services/property.service';
import { PropertyAvailability } from '../../../core/models/property.model';

interface CalendarDay {
  date: Date;
  dayNumber: number;
  isCurrentMonth: boolean;
  isToday: boolean;
  isBlocked: boolean;
  isBooked: boolean;
  isSelected: boolean;
  isPast: boolean;
  blockReason?: string;
}

interface CalendarMonth {
  year: number;
  month: number;
  name: string;
  days: CalendarDay[];
}

@Component({
  selector: 'app-availability-calendar',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatNativeDateModule,
    MatSelectModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatDialogModule
  ],
  templateUrl: './availability-calendar.component.html',
  styleUrl: './availability-calendar.component.scss'
})
export class AvailabilityCalendarComponent implements OnInit, OnChanges {
  @Input() propertyId!: number;

  private propertyService = inject(PropertyService);
  private snackBar = inject(MatSnackBar);

  // Calendar state
  currentDate = new Date();
  months: CalendarMonth[] = [];
  weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  // Selection state
  selectionMode: 'none' | 'selecting' | 'selected' = 'none';
  selectionStart: Date | null = null;
  selectionEnd: Date | null = null;

  // Blocked dates from backend
  blockedPeriods: PropertyAvailability[] = [];

  // Loading state
  loading = false;
  saving = false;

  // Block reason
  blockReason = 'owner_block';
  blockReasons = [
    { value: 'owner_block', label: 'Personal use' },
    { value: 'maintenance', label: 'Maintenance' },
    { value: 'renovation', label: 'Renovation' },
    { value: 'other', label: 'Other' }
  ];

  ngOnInit(): void {
    this.generateCalendar();
    this.loadBlockedDates();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['propertyId'] && !changes['propertyId'].firstChange) {
      this.loadBlockedDates();
    }
  }

  /**
   * Generate 3 months calendar (current + 2 next)
   */
  generateCalendar(): void {
    this.months = [];
    const today = new Date();

    for (let i = 0; i < 6; i++) {
      const date = new Date(today.getFullYear(), today.getMonth() + i, 1);
      this.months.push(this.generateMonth(date.getFullYear(), date.getMonth()));
    }
  }

  /**
   * Generate a single month
   */
  generateMonth(year: number, month: number): CalendarMonth {
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const startDay = firstDay.getDay();
    const daysInMonth = lastDay.getDate();

    const days: CalendarDay[] = [];
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    // Previous month days
    const prevMonthLastDay = new Date(year, month, 0).getDate();
    for (let i = startDay - 1; i >= 0; i--) {
      const date = new Date(year, month - 1, prevMonthLastDay - i);
      days.push({
        date,
        dayNumber: prevMonthLastDay - i,
        isCurrentMonth: false,
        isToday: false,
        isBlocked: false,
        isBooked: false,
        isSelected: false,
        isPast: date < today
      });
    }

    // Current month days
    for (let i = 1; i <= daysInMonth; i++) {
      const date = new Date(year, month, i);
      days.push({
        date,
        dayNumber: i,
        isCurrentMonth: true,
        isToday: date.getTime() === today.getTime(),
        isBlocked: false,
        isBooked: false,
        isSelected: false,
        isPast: date < today
      });
    }

    // Next month days
    const remainingDays = 42 - days.length;
    for (let i = 1; i <= remainingDays; i++) {
      const date = new Date(year, month + 1, i);
      days.push({
        date,
        dayNumber: i,
        isCurrentMonth: false,
        isToday: false,
        isBlocked: false,
        isBooked: false,
        isSelected: false,
        isPast: date < today
      });
    }

    const monthNames = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];

    return {
      year,
      month,
      name: `${monthNames[month]} ${year}`,
      days
    };
  }

  /**
   * Load blocked dates from backend
   */
  loadBlockedDates(): void {
    if (!this.propertyId) return;

    this.loading = true;

    const start = new Date();
    const end = new Date();
    end.setMonth(end.getMonth() + 6);

    const startStr = this.formatDate(start);
    const endStr = this.formatDate(end);

    this.propertyService.getAvailabilities(this.propertyId, startStr, endStr).subscribe({
      next: (availabilities) => {
        this.blockedPeriods = availabilities;
        this.applyBlockedDates();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading blocked dates:', error);
        this.loading = false;
      }
    });
  }

  /**
   * Apply blocked dates to calendar
   */
  applyBlockedDates(): void {
    this.months.forEach(month => {
      month.days.forEach(day => {
        day.isBlocked = false;
        day.isBooked = false;
        day.blockReason = undefined;

        this.blockedPeriods.forEach(period => {
          const startDate = this.parseDate(period.dateDebut);
          const endDate = this.parseDate(period.dateFin);

          if (day.date >= startDate && day.date <= endDate) {
            if (period.because === 'booked') {
              day.isBooked = true;
            } else {
              day.isBlocked = true;
              day.blockReason = period.because;
            }
          }
        });
      });
    });
  }

  /**
   * Handle day click
   */
  onDayClick(day: CalendarDay): void {
    if (day.isPast || !day.isCurrentMonth) return;

    if (this.selectionMode === 'none' || this.selectionMode === 'selected') {
      // Start new selection
      this.clearSelection();
      this.selectionMode = 'selecting';
      this.selectionStart = day.date;
      this.selectionEnd = null;
      day.isSelected = true;
    } else if (this.selectionMode === 'selecting') {
      // Complete selection
      if (day.date >= this.selectionStart!) {
        this.selectionEnd = day.date;
      } else {
        this.selectionEnd = this.selectionStart;
        this.selectionStart = day.date;
      }
      this.selectionMode = 'selected';
      this.applySelection();
    }
  }

  /**
   * Apply selection highlight
   */
  applySelection(): void {
    if (!this.selectionStart || !this.selectionEnd) return;

    this.months.forEach(month => {
      month.days.forEach(day => {
        day.isSelected = day.date >= this.selectionStart! && day.date <= this.selectionEnd!;
      });
    });
  }

  /**
   * Clear selection
   */
  clearSelection(): void {
    this.selectionMode = 'none';
    this.selectionStart = null;
    this.selectionEnd = null;

    this.months.forEach(month => {
      month.days.forEach(day => {
        day.isSelected = false;
      });
    });
  }

  /**
   * Block selected dates
   */
  blockSelectedDates(): void {
    if (!this.selectionStart || !this.selectionEnd) {
      this.snackBar.open('Please select dates first', 'Close', { duration: 3000 });
      return;
    }

    this.saving = true;

    const startStr = this.formatDate(this.selectionStart);
    const endStr = this.formatDate(this.selectionEnd);

    this.propertyService.blockDates(this.propertyId, startStr, endStr, this.blockReason).subscribe({
      next: () => {
        this.saving = false;
        this.snackBar.open('Dates blocked successfully!', 'Close', { duration: 3000 });
        this.clearSelection();
        this.loadBlockedDates();
      },
      error: (error) => {
        this.saving = false;
        this.snackBar.open(error.message || 'Error blocking dates', 'Close', { duration: 3000 });
      }
    });
  }

  /**
   * Unblock selected dates
   */
  unblockSelectedDates(): void {
    if (!this.selectionStart || !this.selectionEnd) {
      this.snackBar.open('Please select dates first', 'Close', { duration: 3000 });
      return;
    }

    this.saving = true;

    const startStr = this.formatDate(this.selectionStart);
    const endStr = this.formatDate(this.selectionEnd);

    this.propertyService.unblockDates(this.propertyId, startStr, endStr).subscribe({
      next: () => {
        this.saving = false;
        this.snackBar.open('Dates unblocked successfully!', 'Close', { duration: 3000 });
        this.clearSelection();
        this.loadBlockedDates();
      },
      error: (error) => {
        this.saving = false;
        this.snackBar.open(error.message || 'Error unblocking dates', 'Close', { duration: 3000 });
      }
    });
  }

  /**
   * Check if selection has blocked dates
   */
  selectionHasBlockedDates(): boolean {
    if (!this.selectionStart || !this.selectionEnd) return false;

    for (const month of this.months) {
      for (const day of month.days) {
        if (day.isSelected && day.isBlocked) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Check if selection has unblocked dates
   */
  selectionHasUnblockedDates(): boolean {
    if (!this.selectionStart || !this.selectionEnd) return false;

    for (const month of this.months) {
      for (const day of month.days) {
        if (day.isSelected && !day.isBlocked && !day.isBooked && day.isCurrentMonth && !day.isPast) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Format date to string
   */
  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  /**
   * Parse date string to Date
   */
  private parseDate(dateStr: string | number[]): Date {
    if (Array.isArray(dateStr)) {
      return new Date(dateStr[0], dateStr[1] - 1, dateStr[2]);
    }
    return new Date(dateStr);
  }

  /**
   * Get selection info text
   */
  getSelectionInfo(): string {
    if (!this.selectionStart) return '';

    const formatOptions: Intl.DateTimeFormatOptions = {
      weekday: 'short',
      month: 'short',
      day: 'numeric'
    };

    if (!this.selectionEnd || this.selectionStart.getTime() === this.selectionEnd.getTime()) {
      return this.selectionStart.toLocaleDateString('en-US', formatOptions);
    }

    const nights = Math.ceil(
      (this.selectionEnd.getTime() - this.selectionStart.getTime()) / (1000 * 60 * 60 * 24)
    );

    return `${this.selectionStart.toLocaleDateString('en-US', formatOptions)} - ${this.selectionEnd.toLocaleDateString('en-US', formatOptions)} (${nights} nights)`;
  }

  /**
   * Get day tooltip
   */
  getDayTooltip(day: CalendarDay): string {
    if (day.isBooked) return 'Booked by guest';
    if (day.isBlocked) return `Blocked: ${day.blockReason || 'Owner block'}`;
    if (day.isPast) return 'Past date';
    return '';
  }
}
