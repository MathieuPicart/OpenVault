package com.openvault.service;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.openvault.entity.Account;
import com.openvault.entity.AccountType;
import com.openvault.entity.Transaction;
import com.openvault.entity.User;
import com.openvault.repository.AccountRepository;
import com.openvault.repository.TransactionRepository;
import com.openvault.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests du TransferService")
class TransferServiceTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private User testUser;
    private Account sourceAccount;
    private Account destinationAccount;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        // Créer un utilisateur
        testUser = new User();
        testUser.setFirstName("Jean");
        testUser.setLastName("Dupont");
        testUser.setEmail("jean.dupont@test.com");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

        // Authentifier l'utilisateur pour les tests
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        testUser.getEmail(),
                        null,
                        new ArrayList<>()
                )
        );

        // Créer un compte source avec 1000€
        sourceAccount = new Account();
        sourceAccount.setIban("FR76 1111 1111 1111 1111 1111 111");
        sourceAccount.setBalance(new BigDecimal("1000.00"));
        sourceAccount.setType(AccountType.CHECKING);
        sourceAccount.setUser(testUser);
        sourceAccount.setActive(true);
        sourceAccount = accountRepository.save(sourceAccount);

        // Créer un compte destination avec 500€
        destinationAccount = new Account();
        destinationAccount.setIban("FR76 2222 2222 2222 2222 2222 222");
        destinationAccount.setBalance(new BigDecimal("500.00"));
        destinationAccount.setType(AccountType.SAVINGS);
        destinationAccount.setUser(testUser);
        destinationAccount.setActive(true);
        destinationAccount = accountRepository.save(destinationAccount);
    }

    @Test
    @DisplayName("Doit effectuer un virement avec succès")
    void shouldTransferSuccessfully() {
        // Given
        BigDecimal transferAmount = new BigDecimal("250.00");

        // When
        Transaction transaction = transferService.transfer(
                sourceAccount.getId(),
                destinationAccount.getIban(),
                transferAmount,
                "Test de virement"
        );

        // Then
        assertThat(transaction).isNotNull();
        assertThat(transaction.getAmount()).isEqualByComparingTo(transferAmount);

        // Vérifier les soldes
        Account updatedSource = accountRepository.findById(sourceAccount.getId()).get();
        Account updatedDestination = accountRepository.findById(destinationAccount.getId()).get();

        assertThat(updatedSource.getBalance()).isEqualByComparingTo("750.00");
        assertThat(updatedDestination.getBalance()).isEqualByComparingTo("750.00");
    }

    @Test
    @DisplayName("Doit échouer si le solde est insuffisant")
    void shouldFailWhenInsufficientBalance() {
        // Given
        BigDecimal transferAmount = new BigDecimal("1500.00"); // Plus que le solde

        // When & Then
        assertThatThrownBy(() ->
                transferService.transfer(
                        sourceAccount.getId(),
                        destinationAccount.getIban(),
                        transferAmount,
                        "Test solde insuffisant"
                )
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Solde insuffisant");

        // Vérifier que les soldes n'ont pas changé
        Account unchangedSource = accountRepository.findById(sourceAccount.getId()).get();
        assertThat(unchangedSource.getBalance()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Doit échouer si le montant est négatif")
    void shouldFailWhenAmountIsNegative() {
        // Given
        BigDecimal negativeAmount = new BigDecimal("-100.00");

        // When & Then
        assertThatThrownBy(() ->
                transferService.transfer(
                        sourceAccount.getId(),
                        destinationAccount.getIban(),
                        negativeAmount,
                        "Test montant négatif"
                )
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("doit être supérieur à 0");
    }

    @Test
    @DisplayName("Doit effectuer un dépôt avec succès")
    void shouldDepositSuccessfully() {
        // Given
        BigDecimal depositAmount = new BigDecimal("500.00");

        // When
        Transaction transaction = transferService.deposit(
                sourceAccount.getId(),
                depositAmount,
                "Dépôt de salaire"
        );

        // Then
        assertThat(transaction).isNotNull();
        assertThat(transaction.getAmount()).isEqualByComparingTo(depositAmount);

        Account updatedAccount = accountRepository.findById(sourceAccount.getId()).get();
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("Doit effectuer un retrait avec succès")
    void shouldWithdrawSuccessfully() {
        // Given
        BigDecimal withdrawAmount = new BigDecimal("200.00");

        // When
        Transaction transaction = transferService.withdraw(
                sourceAccount.getId(),
                withdrawAmount,
                "Retrait DAB"
        );

        // Then
        assertThat(transaction).isNotNull();
        assertThat(transaction.getAmount()).isEqualByComparingTo(withdrawAmount);

        Account updatedAccount = accountRepository.findById(sourceAccount.getId()).get();
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo("800.00");
    }

    @Test
    @DisplayName("Doit échouer le retrait si solde insuffisant")
    void shouldFailWithdrawWhenInsufficientBalance() {
        // Given
        BigDecimal withdrawAmount = new BigDecimal("1500.00");

        // When & Then
        assertThatThrownBy(() ->
                transferService.withdraw(
                        sourceAccount.getId(),
                        withdrawAmount,
                        "Retrait impossible"
                )
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Solde insuffisant");
    }

    @Test
    @DisplayName("Doit échouer si virement vers le même compte")
    void shouldFailWhenTransferToSameAccount() {
        // When & Then
        assertThatThrownBy(() ->
                transferService.transfer(
                        sourceAccount.getId(),
                        sourceAccount.getIban(),
                        new BigDecimal("100.00"),
                        "Virement impossible"
                )
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("même compte");
    }
}