// src/app/features/host/property-wizard/steps/step-rules/step-rules.component.ts
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-step-rules',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
    MatSlideToggleModule,
    MatDividerModule
  ],
  templateUrl: './step-rules.component.html',
  styleUrl: './step-rules.component.scss'
})
export class StepRulesComponent {
  @Input() form!: FormGroup;

  cancellationPolicies = [
    {
      value: 'Flexible',
      label: 'Flexible',
      description: 'Full refund up to 24 hours before check-in'
    },
    {
      value: 'Moderate',
      label: 'Moderate',
      description: 'Full refund up to 5 days before check-in'
    },
    {
      value: 'Strict',
      label: 'Strict',
      description: '50% refund up to 7 days before check-in'
    }
  ];

  communicationStyles = [
    'Friendly and available',
    'Professional',
    'Minimal contact',
    'Available for questions only'
  ];

  responseTimes = [
    'Within an hour',
    'Within a few hours',
    'Within a day',
    'Within a few days'
  ];

  checkInProcesses = [
    'Self check-in with lockbox',
    'Self check-in with smart lock',
    'Self check-in with building staff',
    'Meet and greet in person',
    'Key pickup at location'
  ];
}
