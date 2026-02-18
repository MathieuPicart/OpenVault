import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },

  // Routes publiques
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register').then(m => m.Register)
  },

  // Routes protégées
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard').then(m => m.Dashboard),
    canActivate: [authGuard]
  },
  {
    path: 'accounts',
    loadComponent: () => import('./features/accounts/account-list/account-list').then(m => m.AccountList),
    canActivate: [authGuard]
  },
  {
    path: 'transfer',
    loadComponent: () => import('./features/transfers/transfer-form/transfer-form').then(m => m.TransferForm),
    canActivate: [authGuard]
  },
  {
    path: 'transactions',
    loadComponent: () => import('./features/transactions/transaction-history/transaction-history').then(m => m.TransactionHistory),
    canActivate: [authGuard]
  },
  {
    path: 'analysis',
    loadComponent: () => import('./features/analysis/analysis').then(m => m.Analysis),
    canActivate: [authGuard]
  },

  // 404
  { path: '**', redirectTo: '/dashboard' }
];