package com.openvault.repository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.openvault.entity.User;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests du UserRepository")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Nettoyer la base avant chaque test
        userRepository.deleteAll();

        // Créer un utilisateur de test
        testUser = new User();
        testUser.setFirstName("Jean");
        testUser.setLastName("Dupont");
        testUser.setEmail("jean.dupont@test.com");
        testUser.setPassword("hashedPassword123");
        testUser.setPhoneNumber("0612345678");
    }

    @Test
    @DisplayName("Doit sauvegarder un utilisateur")
    void shouldSaveUser() {
        // When
        User savedUser = userRepository.save(testUser);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("jean.dupont@test.com");
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Doit trouver un utilisateur par email")
    void shouldFindUserByEmail() {
        // Given
        userRepository.save(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmail("jean.dupont@test.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getFirstName()).isEqualTo("Jean");
    }

    @Test
    @DisplayName("Ne doit pas trouver un utilisateur avec un email inexistant")
    void shouldNotFindUserWithNonExistentEmail() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("inexistant@test.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Doit vérifier si un email existe")
    void shouldCheckIfEmailExists() {
        // Given
        userRepository.save(testUser);

        // When
        boolean exists = userRepository.existsByEmail("jean.dupont@test.com");
        boolean notExists = userRepository.existsByEmail("autre@test.com");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Doit trouver un utilisateur par numéro de téléphone")
    void shouldFindUserByPhoneNumber() {
        // Given
        userRepository.save(testUser);

        // When
        Optional<User> foundUser = userRepository.findByPhoneNumber("0612345678");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("jean.dupont@test.com");
    }
}   