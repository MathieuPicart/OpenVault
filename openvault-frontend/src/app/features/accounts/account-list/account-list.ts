import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { Router } from '@angular/router';

import { AccountService } from '../../../core/services/account.service';
import { Account, AccountType, getAccountTypeLabel } from '../../../core/models/account.model';

@Component({
  selector: 'app-account-list',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  templateUrl: './account-list.html',
  styleUrl: './account-list.css',
})
export class AccountList implements OnInit {
  accounts: Account[] = [];
  isLoading = true;
  error: string | null = null;

  constructor(
    public router: Router,
    private accountService: AccountService
  ) { }

  ngOnInit(): void {
    this.loadAccounts();
  }

  private loadAccounts(): void {
    this.isLoading = true;
    this.error = null;

    this.accountService.getAllAccounts().subscribe({
      next: (accounts) => {
        this.accounts = accounts;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Impossible de charger les comptes.';
        this.isLoading = false;
        console.error('AccountList load error:', err);
      }
    });
  }

  getAccountTypeLabel(type: AccountType): string {
    return getAccountTypeLabel(type);
  }

  maskIban(iban: string): string {
    if (!iban) return '';
    const clean = iban.replace(/\s/g, '');
    return clean.substring(0, 4) + ' •••• ' + clean.slice(-4);
  }

  isChecking(type: AccountType): boolean {
    return type === AccountType.CHECKING;
  }

  isSavings(type: AccountType): boolean {
    return type === AccountType.SAVINGS;
  }

  isBusiness(type: AccountType): boolean {
    return type === AccountType.BUSINESS;
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
