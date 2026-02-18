package com.openvault.controller;

import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openvault.dto.LoginRequest;
import com.openvault.dto.RegisterRequest;
import com.openvault.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests du AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Doit inscrire un nouvel utilisateur")
    void shouldRegisterNewUser() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Jean");
        request.setLastName("Dupont");
        request.setEmail("jean.dupont@test.com");
        request.setPassword("Password123!");
        request.setPhoneNumber("0612345678");

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.email").value("jean.dupont@test.com"))
                .andExpect(jsonPath("$.firstName").value("Jean"))
                .andExpect(jsonPath("$.lastName").value("Dupont"));
    }

    @Test
    @DisplayName("Doit échouer l'inscription avec un email déjà utilisé")
    void shouldFailRegisterWithDuplicateEmail() throws Exception {
        // Given - Créer un premier utilisateur
        RegisterRequest firstRequest = new RegisterRequest();
        firstRequest.setFirstName("Jean");
        firstRequest.setLastName("Dupont");
        firstRequest.setEmail("jean.dupont@test.com");
        firstRequest.setPassword("Password123!");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)));

        // Essayer de créer un second utilisateur avec le même email
        RegisterRequest secondRequest = new RegisterRequest();
        secondRequest.setFirstName("Marie");
        secondRequest.setLastName("Martin");
        secondRequest.setEmail("jean.dupont@test.com"); // Même email
        secondRequest.setPassword("Password456!");

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cet email est déjà utilisé"));
    }

    @Test
    @DisplayName("Doit se connecter avec succès")
    void shouldLoginSuccessfully() throws Exception {
        // Given - Créer un utilisateur d'abord
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Jean");
        registerRequest.setLastName("Dupont");
        registerRequest.setEmail("jean.dupont@test.com");
        registerRequest.setPassword("Password123!");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Se connecter
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("jean.dupont@test.com");
        loginRequest.setPassword("Password123!");

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email").value("jean.dupont@test.com"));
    }

    @Test
    @DisplayName("Doit échouer la connexion avec un mauvais mot de passe")
    void shouldFailLoginWithWrongPassword() throws Exception {
        // Given - Créer un utilisateur
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Jean");
        registerRequest.setLastName("Dupont");
        registerRequest.setEmail("jean.dupont@test.com");
        registerRequest.setPassword("Password123!");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Essayer de se connecter avec un mauvais mot de passe
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("jean.dupont@test.com");
        loginRequest.setPassword("WrongPassword!");

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email ou mot de passe incorrect"));
    }

    @Test
    @DisplayName("Doit échouer l'inscription avec des données invalides")
    void shouldFailRegisterWithInvalidData() throws Exception {
        // Given - Email invalide
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Jean");
        request.setLastName("Dupont");
        request.setEmail("email-invalide"); // Pas un email valide
        request.setPassword("123"); // Mot de passe trop court

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }
}