import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Transaction, TransferRequest, DepositWithdrawRequest } from '../models/transaction.model';

@Injectable({
  providedIn: 'root'
})
export class TransferService {
  private readonly API_URL = 'http://localhost:8080/api/transfers';

  constructor(private http: HttpClient) {}

  transfer(request: TransferRequest): Observable<Transaction> {
    return this.http.post<Transaction>(this.API_URL, request);
  }

  deposit(accountId: number, request: DepositWithdrawRequest): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.API_URL}/deposit/${accountId}`, request);
  }

  withdraw(accountId: number, request: DepositWithdrawRequest): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.API_URL}/withdraw/${accountId}`, request);
  }
}