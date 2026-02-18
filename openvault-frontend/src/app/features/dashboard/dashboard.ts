import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';
import { AccountService } from '../../core/services/account.service';
import { TransactionService } from '../../core/services/transaction.service';
import { AuthResponse } from '../../core/models/user.model';
import { Account, getAccountTypeLabel } from '../../core/models/account.model';
import { Transaction } from '../../core/models/transaction.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  user: AuthResponse | null = null;
  accounts: Account[] = [];
  recentTransactions: Transaction[] = [];
  totalBalance: number = 0;
  isLoading = true;
  error: string | null = null;

  constructor(
    private authService: AuthService,
    private accountService: AccountService,
    private transactionService: TransactionService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.user = user;
    });
    this.loadData();
  }

  private loadData(): void {
    this.isLoading = true;
    this.error = null;

    forkJoin({
      accounts: this.accountService.getAllAccounts(),
      totalBalance: this.accountService.getTotalBalance()
    }).subscribe({
      next: ({ accounts, totalBalance }) => {
        this.accounts = accounts;
        this.totalBalance = totalBalance.totalBalance;

        // Charger les transactions récentes du premier compte actif
        if (accounts.length > 0) {
          const firstAccount = accounts[0];
          this.transactionService.getRecentTransactions(firstAccount.id, 5).subscribe({
            next: (transactions) => {
              this.recentTransactions = transactions;
              this.isLoading = false;
            },
            error: () => {
              // Pas de transactions = pas une erreur bloquante
              this.isLoading = false;
            }
          });
        } else {
          this.isLoading = false;
        }
      },
      error: (err) => {
        this.error = 'Impossible de charger les données. Veuillez réessayer.';
        this.isLoading = false;
        console.error('Dashboard load error:', err);
      }
    });
  }

  getAccountTypeLabel(type: any): string {
    return getAccountTypeLabel(type);
  }

  isCredit(transaction: Transaction): boolean {
    return transaction.type === 'DEPOSIT' || transaction.type === 'TRANSFER' && transaction.fromIban === null;
  }

  getTransactionSign(transaction: Transaction): string {
    return this.isCredit(transaction) ? '+' : '-';
  }

  getTransactionColor(transaction: Transaction): string {
    return this.isCredit(transaction) ? 'text-green-500' : 'text-gray-900';
  }

  formatDate(date: any): string {
    const d = new Date(date);
    const now = new Date();
    const diff = now.getTime() - d.getTime();
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));

    if (days === 0) return "Aujourd'hui";
    if (days === 1) return 'Hier';
    return d.toLocaleDateString('fr-FR', { day: 'numeric', month: 'long' });
  }

  formatTime(date: any): string {
    return new Date(date).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  maskIban(iban: string): string {
    if (!iban) return '';
    const clean = iban.replace(/\s/g, '');
    return clean.substring(0, 4) + ' **** **** ' + clean.slice(-4);
  }

  navigateTo(path: string): void {
    this.router.navigate([path]);
  }

  logout(): void {
    this.authService.logout();
  }
}
