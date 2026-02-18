import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { Router } from '@angular/router';

import { AccountService } from '../../../core/services/account.service';
import { TransactionService } from '../../../core/services/transaction.service';
import { Transaction, TransactionType } from '../../../core/models/transaction.model';

interface TransactionGroup {
  label: string;
  date: string;
  transactions: Transaction[];
}

@Component({
  selector: 'app-transaction-history',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  templateUrl: './transaction-history.html',
  styleUrl: './transaction-history.css',
})
export class TransactionHistory implements OnInit {
  allTransactions: Transaction[] = [];
  groupedTransactions: TransactionGroup[] = [];
  isLoading = true;
  error: string | null = null;
  activeFilter: string = 'ALL';
  accountId: number | null = null;

  readonly filters = [
    { key: 'ALL', label: 'Tous' },
    { key: 'TRANSFER', label: 'Virements' },
    { key: 'DEPOSIT', label: 'Dépôts' },
    { key: 'WITHDRAWAL', label: 'Retraits' },
  ];

  constructor(
    public router: Router,
    private accountService: AccountService,
    private transactionService: TransactionService
  ) { }

  ngOnInit(): void {
    this.accountService.getAllAccounts().subscribe({
      next: (accounts) => {
        if (accounts.length > 0) {
          this.accountId = accounts[0].id;
          this.loadTransactions();
        } else {
          this.isLoading = false;
        }
      },
      error: () => {
        this.error = 'Impossible de charger les comptes.';
        this.isLoading = false;
      }
    });
  }

  private loadTransactions(): void {
    if (!this.accountId) return;
    this.isLoading = true;
    this.error = null;

    this.transactionService.getRecentTransactions(this.accountId, 50).subscribe({
      next: (transactions) => {
        this.allTransactions = transactions;
        this.applyFilter(this.activeFilter);
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Impossible de charger les transactions.';
        this.isLoading = false;
      }
    });
  }

  applyFilter(filterKey: string): void {
    this.activeFilter = filterKey;
    const filtered = filterKey === 'ALL'
      ? this.allTransactions
      : this.allTransactions.filter(tx => tx.type === filterKey);

    this.groupedTransactions = this.groupByDate(filtered);
  }

  private groupByDate(transactions: Transaction[]): TransactionGroup[] {
    const groups = new Map<string, Transaction[]>();

    for (const tx of transactions) {
      const dateKey = this.getDateKey(tx.timestamp);
      if (!groups.has(dateKey)) {
        groups.set(dateKey, []);
      }
      groups.get(dateKey)!.push(tx);
    }

    return Array.from(groups.entries()).map(([dateKey, txs]) => ({
      label: this.formatDateLabel(dateKey),
      date: dateKey,
      transactions: txs
    }));
  }

  private getDateKey(date: any): string {
    return new Date(date).toISOString().split('T')[0];
  }

  private formatDateLabel(dateKey: string): string {
    const date = new Date(dateKey + 'T12:00:00');
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const txDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    const diffDays = Math.round((today.getTime() - txDate.getTime()) / (1000 * 60 * 60 * 24));

    if (diffDays === 0) return "Aujourd'hui";
    if (diffDays === 1) return 'Hier';
    return date.toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' });
  }

  isCredit(transaction: Transaction): boolean {
    return transaction.type === 'DEPOSIT' ||
      (transaction.type === 'TRANSFER' && transaction.fromIban === null);
  }

  formatTime(date: any): string {
    return new Date(date).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  getTypeLabel(type: string): string {
    switch (type) {
      case 'DEPOSIT': return 'Dépôt';
      case 'WITHDRAWAL': return 'Retrait';
      case 'TRANSFER': return 'Virement';
      case 'PAYMENT': return 'Paiement';
      default: return type;
    }
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
