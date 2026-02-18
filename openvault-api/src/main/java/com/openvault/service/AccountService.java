package com.openvault.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.openvault.entity.Account;
import com.openvault.entity.AccountType;
import com.openvault.entity.User;
import com.openvault.repository.AccountRepository;
import com.openvault.repository.UserRepository;
import com.openvault.util.IbanGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final IbanGenerator ibanGenerator;

    /**
     * Récupère tous les comptes de l'utilisateur connecté
     */
    public List<Account> getUserAccounts() {
        User user = getCurrentUser();
        return accountRepository.findByUserIdAndActiveTrue(user.getId());
    }

    /**
     * Récupère un compte par son ID (vérifie qu'il appartient à l'utilisateur)
     */
    public Account getAccountById(Long accountId) {
        User user = getCurrentUser();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Accès non autorisé à ce compte");
        }

        return account;
    }

    /**
     * Récupère un compte par son IBAN
     */
    public Account getAccountByIban(String iban) {
        return accountRepository.findByIban(iban)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé avec cet IBAN"));
    }

    /**
     * Crée un nouveau compte pour l'utilisateur connecté
     */
    @Transactional
    public Account createAccount(AccountType type) {
        User user = getCurrentUser();

        // Vérifier le nombre de comptes existants
        List<Account> existingAccounts = accountRepository.findByUserId(user.getId());
        if (existingAccounts.size() >= 5) {
            throw new RuntimeException("Vous ne pouvez pas avoir plus de 5 comptes");
        }

        Account account = new Account();
        account.setIban(generateUniqueIban());
        account.setBalance(BigDecimal.ZERO);
        account.setType(type);
        account.setUser(user);
        account.setActive(true);

        return accountRepository.save(account);
    }

    /**
     * Désactive un compte (soft delete)
     */
    @Transactional
    public void deactivateAccount(Long accountId) {
        Account account = getAccountById(accountId);

        // Vérifier que le solde est à zéro
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new RuntimeException("Impossible de désactiver un compte avec un solde non nul");
        }

        account.setActive(false);
        accountRepository.save(account);
    }

    /**
     * Récupère le solde total de tous les comptes de l'utilisateur
     */
    public BigDecimal getTotalBalance() {
        User user = getCurrentUser();
        List<Account> accounts = accountRepository.findByUserIdAndActiveTrue(user.getId());
        
        return accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Génère un IBAN unique
     */
    private String generateUniqueIban() {
        String iban;
        do {
            iban = ibanGenerator.generateIban();
        } while (accountRepository.existsByIban(iban));
        return iban;
    }

    /**
     * Récupère l'utilisateur connecté
     */
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
}