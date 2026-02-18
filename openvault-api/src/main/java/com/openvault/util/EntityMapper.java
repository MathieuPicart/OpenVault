package com.openvault.util;

import org.springframework.stereotype.Component;

import com.openvault.dto.AccountDTO;
import com.openvault.dto.TransactionDTO;
import com.openvault.entity.Account;
import com.openvault.entity.Transaction;

@Component
public class EntityMapper {

    public AccountDTO toAccountDTO(Account account) {
        return AccountDTO.builder()
                .id(account.getId())
                .iban(account.getIban())
                .balance(account.getBalance())
                .type(account.getType())
                .active(account.getActive())
                .createdAt(account.getCreatedAt())
                .build();
    }

    public TransactionDTO toTransactionDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .fromIban(transaction.getFromAccount() != null ? transaction.getFromAccount().getIban() : null)
                .toIban(transaction.getToAccount() != null ? transaction.getToAccount().getIban() : null)
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .description(transaction.getDescription())
                .timestamp(transaction.getTimestamp())
                .status(transaction.getStatus())
                .reference(transaction.getReference())
                .build();
    }
}