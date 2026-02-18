package com.openvault.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.openvault.entity.Account;
import com.openvault.entity.Transaction;
import com.openvault.entity.TransactionStatus;
import com.openvault.entity.TransactionType;
import com.openvault.repository.AccountRepository;
import com.openvault.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    /**
     * Effectue un virement entre deux comptes
     * @Transactional avec SERIALIZABLE pour éviter les problèmes de concurrence
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction transfer(Long fromAccountId, String toIban, BigDecimal amount, String description) {
        log.info("Début du virement de {} EUR du compte {} vers {}", amount, fromAccountId, toIban);

        // Validations
        validateTransferAmount(amount);

        // Récupération des comptes avec verrouillage pessimiste
        Account fromAccount = accountRepository.findByIdWithLock(fromAccountId)
                .orElseThrow(() -> new RuntimeException("Compte source non trouvé"));

        Account toAccount = accountRepository.findByIban(toIban)
                .orElseThrow(() -> new RuntimeException("Compte destinataire non trouvé avec l'IBAN : " + toIban));

        // Vérifier que l'utilisateur possède le compte source
        accountService.getAccountById(fromAccountId);

        // Vérifier que les comptes sont différents
        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new RuntimeException("Impossible de faire un virement vers le même compte");
        }

        // Vérifier que les comptes sont actifs
        if (!fromAccount.getActive() || !toAccount.getActive()) {
            throw new RuntimeException("L'un des comptes est désactivé");
        }

        // Vérifier le solde suffisant
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            log.warn("Solde insuffisant. Solde: {}, Montant demandé: {}", fromAccount.getBalance(), amount);
            throw new RuntimeException("Solde insuffisant. Solde actuel: " + fromAccount.getBalance() + " EUR");
        }

        // Créer la transaction
        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setDescription(description);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setReference(generateTransactionReference());

        try {
            // Débiter le compte source
            fromAccount.debit(amount);
            
            // Créditer le compte destinataire
            toAccount.credit(amount);

            // Sauvegarder les comptes
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            // Marquer la transaction comme réussie
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction = transactionRepository.save(transaction);

            log.info("Virement réussi. Référence: {}", transaction.getReference());
            return transaction;

        } catch (Exception e) {
            log.error("Erreur lors du virement: {}", e.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new RuntimeException("Échec du virement: " + e.getMessage());
        }
    }

    /**
     * Effectue un dépôt sur un compte
     */
    @Transactional
    public Transaction deposit(Long accountId, BigDecimal amount, String description) {
        validateTransferAmount(amount);

        Account account = accountService.getAccountById(accountId);

        if (!account.getActive()) {
            throw new RuntimeException("Le compte est désactivé");
        }

        Transaction transaction = new Transaction();
        transaction.setToAccount(account);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setDescription(description);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setReference(generateTransactionReference());

        account.credit(amount);
        accountRepository.save(account);

        return transactionRepository.save(transaction);
    }

    /**
     * Effectue un retrait sur un compte
     */
    @Transactional
    public Transaction withdraw(Long accountId, BigDecimal amount, String description) {
        validateTransferAmount(amount);

        Account account = accountService.getAccountById(accountId);

        if (!account.getActive()) {
            throw new RuntimeException("Le compte est désactivé");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Solde insuffisant");
        }

        Transaction transaction = new Transaction();
        transaction.setFromAccount(account);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setDescription(description);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setReference(generateTransactionReference());

        account.debit(amount);
        accountRepository.save(account);

        return transactionRepository.save(transaction);
    }

    /**
     * Valide le montant d'un virement
     */
    private void validateTransferAmount(BigDecimal amount) {
        if (amount == null) {
            throw new RuntimeException("Le montant ne peut pas être null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Le montant doit être supérieur à 0");
        }
        if (amount.compareTo(new BigDecimal("100000")) > 0) {
            throw new RuntimeException("Le montant ne peut pas dépasser 100 000 EUR");
        }
        // Vérifier maximum 2 décimales
        if (amount.scale() > 2) {
            throw new RuntimeException("Le montant ne peut avoir plus de 2 décimales");
        }
    }

    /**
     * Génère une référence unique pour la transaction
     */
    private String generateTransactionReference() {
        return "TXN-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }
}