package com.openvault.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openvault.dto.AuthResponse;
import com.openvault.dto.LoginRequest;
import com.openvault.dto.RegisterRequest;
import com.openvault.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Endpoints pour l'inscription et la connexion")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Inscription d'un nouvel utilisateur",
            description = "Crée un nouveau compte utilisateur et un compte bancaire courant par défaut avec un IBAN généré automatiquement"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Inscription réussie",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "token": "eyJhbGciOiJIUzI1NiJ9...",
                                      "type": "Bearer",
                                      "userId": 1,
                                      "email": "jean.dupont@example.com",
                                      "firstName": "Jean",
                                      "lastName": "Dupont"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données invalides ou email déjà utilisé",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-01-22T10:30:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Cet email est déjà utilisé"
                                    }
                                    """)
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Informations d'inscription",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "firstName": "Jean",
                                      "lastName": "Dupont",
                                      "email": "jean.dupont@example.com",
                                      "password": "Password123!",
                                      "phoneNumber": "0612345678"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(
            summary = "Connexion d'un utilisateur",
            description = "Authentifie un utilisateur et retourne un token JWT valable 24 heures"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Connexion réussie",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Email ou mot de passe incorrect",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-01-22T10:30:00",
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Email ou mot de passe incorrect"
                                    }
                                    """)
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Identifiants de connexion",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "email": "jean.dupont@example.com",
                                      "password": "Password123!"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }
}