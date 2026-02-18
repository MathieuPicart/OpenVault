package com.openvault.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.openvault.entity.Account;
import com.openvault.entity.Transaction;
import com.openvault.entity.TransactionType;
import com.openvault.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionHistoryService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    /**
     * Récupère l'historique paginé des transactions d'un compte
     */
    public Page<Transaction> getAccountTransactions(Long accountId, int page, int size) {
        // Vérifier que l'utilisateur possède le compte
        accountService.getAccountById(accountId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return transactionRepository.findByAccountId(accountId, pageable);
    }

    /**
     * Récupère les dernières transactions d'un compte
     */
    public List<Transaction> getRecentTransactions(Long accountId, int limit) {
        accountService.getAccountById(accountId);
        
        Pageable pageable = PageRequest.of(0, limit);
        return transactionRepository.findTop10ByAccountId(accountId, pageable);
    }

    /**
     * Récupère les transactions d'un compte par type
     */
    public List<Transaction> getTransactionsByType(Long accountId, TransactionType type) {
        accountService.getAccountById(accountId);
        return transactionRepository.findByTypeAndFromAccountIdOrToAccountId(type, accountId, accountId);
    }

    /**
     * Récupère les transactions d'un compte sur une période
     */
    public List<Transaction> getTransactionsByDateRange(
            Long accountId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        accountService.getAccountById(accountId);
        return transactionRepository.findByAccountIdAndDateRange(accountId, startDate, endDate);
    }

    /**
     * Récupère une transaction par son ID
     */
    public Transaction getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction non trouvée"));

        // Vérifier que l'utilisateur a accès à cette transaction
        if (transaction.getFromAccount() != null) {
            accountService.getAccountById(transaction.getFromAccount().getId());
        } else if (transaction.getToAccount() != null) {
            accountService.getAccountById(transaction.getToAccount().getId());
        }

        return transaction;
    }

    /**
     * Récupère les statistiques des transactions d'un compte
     */
    public TransactionStats getTransactionStats(Long accountId) {
        Account account = accountService.getAccountById(accountId);
        
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();
        
        List<Transaction> monthTransactions = transactionRepository.findByAccountIdAndDateRange(
                accountId, startOfMonth, now
        );

        return TransactionStats.builder()
                .totalTransactions(monthTransactions.size())
                .totalIncoming(calculateIncoming(monthTransactions, accountId))
                .totalOutgoing(calculateOutgoing(monthTransactions, accountId))
                .currentBalance(account.getBalance())
                .build();
    }

    private java.math.BigDecimal calculateIncoming(List<Transaction> transactions, Long accountId) {
        return transactions.stream()
                .filter(t -> t.getToAccount() != null && t.getToAccount().getId().equals(accountId))
                .map(Transaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    private java.math.BigDecimal calculateOutgoing(List<Transaction> transactions, Long accountId) {
        return transactions.stream()
                .filter(t -> t.getFromAccount() != null && t.getFromAccount().getId().equals(accountId))
                .map(Transaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    // Classe interne pour les statistiques
    @lombok.Data
    @lombok.Builder
    public static class TransactionStats {
        private int totalTransactions;
        private java.math.BigDecimal totalIncoming;
        private java.math.BigDecimal totalOutgoing;
        private java.math.BigDecimal currentBalance;
    }
}