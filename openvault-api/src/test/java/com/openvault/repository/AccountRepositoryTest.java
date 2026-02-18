package com.openvault.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.openvault.entity.Account;
import com.openvault.entity.AccountType;
import com.openvault.entity.User;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests du AccountRepository")
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
        userRepository.deleteAll();

        // Créer un utilisateur
        testUser = new User();
        testUser.setFirstName("Jean");
        testUser.setLastName("Dupont");
        testUser.setEmail("jean.dupont@test.com");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

        // Créer un compte
        testAccount = new Account();
        testAccount.setIban("FR76 1027 8012 3456 7890 1234 567");
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setType(AccountType.CHECKING);
        testAccount.setUser(testUser);
        testAccount.setActive(true);
    }

    @Test
    @DisplayName("Doit sauvegarder un compte")
    void shouldSaveAccount() {
        // When
        Account savedAccount = accountRepository.save(testAccount);

        // Then
        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getId()).isNotNull();
        assertThat(savedAccount.getBalance()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Doit trouver un compte par IBAN")
    void shouldFindAccountByIban() {
        // Given
        accountRepository.save(testAccount);

        // When
        Optional<Account> foundAccount = accountRepository.findByIban("FR76 1027 8012 3456 7890 1234 567");

        // Then
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getType()).isEqualTo(AccountType.CHECKING);
    }

    @Test
    @DisplayName("Doit trouver tous les comptes d'un utilisateur")
    void shouldFindAllAccountsByUserId() {
        // Given
        accountRepository.save(testAccount);

        Account secondAccount = new Account();
        secondAccount.setIban("FR76 9999 8888 7777 6666 5555 444");
        secondAccount.setBalance(BigDecimal.ZERO);
        secondAccount.setType(AccountType.SAVINGS);
        secondAccount.setUser(testUser);
        secondAccount.setActive(true);
        accountRepository.save(secondAccount);

        // When
        List<Account> accounts = accountRepository.findByUserId(testUser.getId());

        // Then
        assertThat(accounts).hasSize(2);
    }

    @Test
    @DisplayName("Doit trouver uniquement les comptes actifs d'un utilisateur")
    void shouldFindOnlyActiveAccounts() {
        // Given
        testAccount.setActive(true);
        accountRepository.save(testAccount);

        Account inactiveAccount = new Account();
        inactiveAccount.setIban("FR76 9999 8888 7777 6666 5555 444");
        inactiveAccount.setBalance(BigDecimal.ZERO);
        inactiveAccount.setType(AccountType.SAVINGS);
        inactiveAccount.setUser(testUser);
        inactiveAccount.setActive(false);
        accountRepository.save(inactiveAccount);

        // When
        List<Account> activeAccounts = accountRepository.findByUserIdAndActiveTrue(testUser.getId());

        // Then
        assertThat(activeAccounts).hasSize(1);
        assertThat(activeAccounts.get(0).getActive()).isTrue();
    }

    @Test
    @DisplayName("Doit vérifier si un IBAN existe")
    void shouldCheckIfIbanExists() {
        // Given
        accountRepository.save(testAccount);

        // When
        boolean exists = accountRepository.existsByIban("FR76 1027 8012 3456 7890 1234 567");
        boolean notExists = accountRepository.existsByIban("FR76 0000 0000 0000 0000 0000 000");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}