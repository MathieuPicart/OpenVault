import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-transfer-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './transfer-form.html',
  styleUrl: './transfer-form.css',
})
export class TransferForm {
  transferForm: FormGroup;
  amountDisplay = '0';

  constructor(
    private fb: FormBuilder,
    public router: Router
  ) {
    this.transferForm = this.fb.group({
      sourceAccount: ['Compte Courant'], // Mocked for now
      recipientIban: ['', Validators.required],
      amount: [0, [Validators.required, Validators.min(0.01)]],
      description: ['']
    });
  }

  addDigit(digit: string) {
    if (this.amountDisplay === '0') {
      this.amountDisplay = digit;
    } else {
      this.amountDisplay += digit;
    }
    this.transferForm.patchValue({ amount: parseFloat(this.amountDisplay) });
  }

  removeDigit() {
    if (this.amountDisplay.length > 1) {
      this.amountDisplay = this.amountDisplay.slice(0, -1);
    } else {
      this.amountDisplay = '0';
    }
    this.transferForm.patchValue({ amount: parseFloat(this.amountDisplay) });
  }

  goBack() {
    this.router.navigate(['/dashboard']);
  }

  onSubmit() {
    if (this.transferForm.valid) {
      console.log('Transfer submitted:', this.transferForm.value);
      // Here call TransferService
      alert('Virement en cours de traitement...');
      this.router.navigate(['/dashboard']);
    }
  }
}
