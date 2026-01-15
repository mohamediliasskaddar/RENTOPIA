// src/app/features/host/property-wizard/steps/step-type/step-type.component.ts
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';

interface TypeOption {
  value: string;
  label: string;
  icon: string;
  description: string;
}

@Component({
  selector: 'app-step-type',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule
  ],
  templateUrl: './step-type.component.html',
  styleUrl: './step-type.component.scss'
})
export class StepTypeComponent {
  @Input() form!: FormGroup;

  propertyTypes: TypeOption[] = [
    { value: 'APARTMENT', label: 'Apartment', icon: 'apartment', description: 'A unit in a building' },
    { value: 'HOUSE', label: 'House', icon: 'home', description: 'A standalone house' },
    { value: 'VILLA', label: 'Villa', icon: 'villa', description: 'A luxury villa' },
    { value: 'STUDIO', label: 'Studio', icon: 'weekend', description: 'A single room apartment' },
    { value: 'LOFT', label: 'Loft', icon: 'domain', description: 'An open-plan living space' }
  ];

  placeTypes: TypeOption[] = [
    { value: 'ENTIRE_PLACE', label: 'Entire place', icon: 'home', description: 'Guests have the whole place to themselves' },
    { value: 'PRIVATE_ROOM', label: 'Private room', icon: 'meeting_room', description: 'Guests have their own room with shared spaces' },
    { value: 'SHARED_ROOM', label: 'Shared room', icon: 'group', description: 'Guests share a room with others' }
  ];

  selectPropertyType(type: string): void {
    this.form.patchValue({ propertyType: type });
  }

  selectPlaceType(type: string): void {
    this.form.patchValue({ placeType: type });
  }
}
