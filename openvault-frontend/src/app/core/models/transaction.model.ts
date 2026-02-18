export interface Transaction {
  id: number;
  fromIban: string | null;
  toIban: string | null;
  amount: number;
  type: TransactionType;
  description: string;
  timestamp: Date;
  status: TransactionStatus;
  reference: string;
}

export enum TransactionType {
  TRANSFER = 'TRANSFER',
  DEPOSIT = 'DEPOSIT',
  WITHDRAWAL = 'WITHDRAWAL',
  PAYMENT = 'PAYMENT'
}

export enum TransactionStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
}

export interface TransferRequest {
  fromAccountId: number;
  toIban: string;
  amount: number;
  description?: string;
}

export interface DepositWithdrawRequest {
  amount: number;
  description?: string;
}