// src/app/features/host/property-wizard/steps/step-details/step-details.component.ts
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-step-details',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule
  ],
  templateUrl: './step-details.component.html',
  styleUrl: './step-details.component.scss'
})
export class StepDetailsComponent {
  @Input() form!: FormGroup;

  increment(field: string): void {
    const currentValue = this.form.get(field)?.value || 0;
    this.form.patchValue({ [field]: currentValue + 1 });
  }

  decrement(field: string, min: number = 0): void {
    const currentValue = this.form.get(field)?.value || 0;
    if (currentValue > min) {
      this.form.patchValue({ [field]: currentValue - 1 });
    }
  }
}
