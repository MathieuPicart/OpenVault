import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Account, AccountType } from '../models/account.model';

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  private readonly API_URL = 'http://localhost:8080/api/accounts';

  constructor(private http: HttpClient) {}

  getAllAccounts(): Observable<Account[]> {
    return this.http.get<Account[]>(this.API_URL);
  }

  getAccountById(id: number): Observable<Account> {
    return this.http.get<Account>(`${this.API_URL}/${id}`);
  }

  createAccount(type: AccountType): Observable<Account> {
    return this.http.post<Account>(`${this.API_URL}?type=${type}`, {});
  }

  deactivateAccount(id: number): Observable<any> {
    return this.http.delete(`${this.API_URL}/${id}`);
  }

  getTotalBalance(): Observable<{ totalBalance: number }> {
    return this.http.get<{ totalBalance: number }>(`${this.API_URL}/total-balance`);
  }
}