// src/app/features/profile/components/add-language-dialog/add-language-dialog.component.ts
// ✅ VERSION AVEC ENDPOINT + DEBUG COMPLET

import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, tap, of } from 'rxjs';

import { ProficiencyLevel } from '../../core/models/user.model';
import { environment } from '../../../environments/environment';

/**
 * Interface pour les langues (correspond à LanguageDTO backend)
 */
interface LanguageDTO {
  id: number;
  code: string;
  name: string;
  nativeName?: string;
  isActive?: boolean;
}

@Component({
  selector: 'app-add-language-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './add-language-dialog.component.html',
  styleUrl: './add-language-dialog.component.scss'
})
export class AddLanguageDialogComponent implements OnInit {

  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<AddLanguageDialogComponent>);
  private http = inject(HttpClient);
  private snackBar = inject(MatSnackBar);

  languageForm!: FormGroup;
  languages$!: Observable<LanguageDTO[]>;
  loading = false;
  loadingLanguages = true;

  proficiencyLevels = [
    { value: ProficiencyLevel.BASIC, label: 'Basic' },
    { value: ProficiencyLevel.INTERMEDIATE, label: 'Intermediate' },
    { value: ProficiencyLevel.ADVANCED, label: 'Advanced' },
    { value: ProficiencyLevel.NATIVE, label: 'Native' }
  ];

  ngOnInit(): void {
    console.log('🎬 AddLanguageDialog - Init');
    this.initForm();
    this.loadLanguages();
  }

  private initForm(): void {
    this.languageForm = this.fb.group({
      languageId: ['', Validators.required],
      proficiencyLevel: [ProficiencyLevel.INTERMEDIATE, Validators.required]
    });

    console.log('📝 Form initialized:', this.languageForm.value);
  }

  private loadLanguages(): void {
    console.log('🌍 Loading languages from API...');
    console.log('📍 API URL:', `${environment.apiUrl}/users/languages`);

    this.loadingLanguages = true;

    this.languages$ = this.http.get<LanguageDTO[]>(`${environment.apiUrl}/users/languages/all`).pipe(
      tap(languages => {
        console.log('✅ Languages loaded:', languages);
        this.loadingLanguages = false;
      }),
      catchError(error => {
        console.error('❌ Error loading languages:', error);
        this.snackBar.open('Error loading languages', 'Close', { duration: 3000 });
        this.loadingLanguages = false;

        // Retourner liste vide en cas d'erreur
        return of([]);
      })
    );
  }

  onSubmit(): void {
    console.log('💾 Submit clicked');
    console.log('📋 Form valid:', this.languageForm.valid);
    console.log('📋 Form value:', this.languageForm.value);

    if (this.languageForm.invalid) {
      console.warn('⚠️ Form is invalid!');
      console.log('Errors:', this.languageForm.errors);
      this.snackBar.open('Please fill all fields', 'Close', { duration: 2000 });
      return;
    }

    const result = {
      languageId: this.languageForm.value.languageId,
      proficiencyLevel: this.languageForm.value.proficiencyLevel
    };

    console.log('✅ Closing dialog with result:', result);
    this.dialogRef.close(result);
  }

  onCancel(): void {
    console.log('❌ Cancel clicked');
    this.dialogRef.close();
  }
}
