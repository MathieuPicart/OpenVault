import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Transaction } from '../models/transaction.model';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  private readonly API_URL = 'http://localhost:8080/api/transactions';

  constructor(private http: HttpClient) {}

  getRecentTransactions(accountId: number, limit: number = 10): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.API_URL}/account/${accountId}/recent?limit=${limit}`);
  }

  getTransactionById(id: number): Observable<Transaction> {
    return this.http.get<Transaction>(`${this.API_URL}/${id}`);
  }

  getTransactionStats(accountId: number): Observable<any> {
    return this.http.get(`${this.API_URL}/account/${accountId}/stats`);
  }
}