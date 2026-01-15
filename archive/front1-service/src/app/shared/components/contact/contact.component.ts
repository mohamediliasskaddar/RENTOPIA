// src/app/features/contact/contact.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

interface ContactMethod {
  icon: string;
  title: string;
  value: string;
  link?: string;
}

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule
  ],
  templateUrl: './contact.component.html',
  styleUrl: './contact.component.scss'
})
export class ContactComponent {

  contactForm: FormGroup;
  isSubmitting = false;
  isSubmitted = false;

  contactMethods: ContactMethod[] = [
    {
      icon: 'email',
      title: 'Email',
      value: 'support@rentalchain.com',
      link: 'mailto:support@rentopia.com'
    },
    {
      icon: 'phone',
      title: 'Phone',
      value: '+212 5XX-XXXXXX',
      link: 'tel:+212XXXXXXXXX'
    },
    {
      icon: 'location_on',
      title: 'Office',
      value: 'Tangier, Morocco',
      link: 'https://maps.google.com'
    },
    {
      icon: 'schedule',
      title: 'Hours',
      value: '24/7 Support',
      link: undefined
    }
  ];

  topicOptions = [
    { value: 'general', label: 'General Inquiry' },
    { value: 'booking', label: 'Booking Issue' },
    { value: 'payment', label: 'Payment Problem' },
    { value: 'hosting', label: 'Host Question' },
    { value: 'technical', label: 'Technical Support' },
    { value: 'partnership', label: 'Partnership' },
    { value: 'other', label: 'Other' }
  ];

  constructor(
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.contactForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      topic: ['general', Validators.required],
      subject: ['', [Validators.required, Validators.minLength(5)]],
      message: ['', [Validators.required, Validators.minLength(20)]]
    });
  }

  onSubmit(): void {
    if (this.contactForm.valid) {
      this.isSubmitting = true;

      // Simuler un appel API
      setTimeout(() => {
        console.log('Contact form submitted:', this.contactForm.value);

        this.isSubmitting = false;
        this.isSubmitted = true;

        this.snackBar.open('Message sent successfully! We\'ll get back to you soon.', 'Close', {
          duration: 5000,
          horizontalPosition: 'center',
          verticalPosition: 'top',
          panelClass: ['success-snackbar']
        });

        // Reset form après 3 secondes
        setTimeout(() => {
          this.contactForm.reset({ topic: 'general' });
          this.isSubmitted = false;
        }, 3000);

      }, 1500);
    } else {
      // Marquer tous les champs comme touchés pour afficher les erreurs
      Object.keys(this.contactForm.controls).forEach(key => {
        this.contactForm.get(key)?.markAsTouched();
      });

      this.snackBar.open('Please fill in all required fields correctly', 'Close', {
        duration: 3000,
        panelClass: ['error-snackbar']
      });
    }
  }

  getErrorMessage(fieldName: string): string {
    const field = this.contactForm.get(fieldName);

    if (field?.hasError('required')) {
      return 'This field is required';
    }

    if (field?.hasError('email')) {
      return 'Please enter a valid email';
    }

    if (field?.hasError('minlength')) {
      const minLength = field.errors?.['minlength'].requiredLength;
      return `Minimum ${minLength} characters required`;
    }

    return '';
  }

  openLink(link?: string): void {
    if (link) {
      window.open(link, '_blank');
    }
  }
}
