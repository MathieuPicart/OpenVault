export interface Account {
  id: number;
  iban: string;
  balance: number;
  type: AccountType;
  active: boolean;
  createdAt: Date;
}

export enum AccountType {
  CHECKING = 'CHECKING',
  SAVINGS = 'SAVINGS',
  BUSINESS = 'BUSINESS'
}

export function getAccountTypeLabel(type: AccountType): string {
  switch (type) {
    case AccountType.CHECKING:
      return 'Compte Courant';
    case AccountType.SAVINGS:
      return 'Compte Épargne';
    case AccountType.BUSINESS:
      return 'Compte Professionnel';
    default:
      return type;
  }
}