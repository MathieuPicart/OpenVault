package com.openvault.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openvault.dto.AccountDTO;
import com.openvault.entity.Account;
import com.openvault.entity.AccountType;
import com.openvault.service.AccountService;
import com.openvault.util.EntityMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Comptes", description = "Gestion des comptes bancaires")
@SecurityRequirement(name = "Bearer Authentication")
public class AccountController {

    private final AccountService accountService;
    private final EntityMapper entityMapper;

    @Operation(
            summary = "Liste tous les comptes de l'utilisateur",
            description = "Retourne la liste de tous les comptes actifs appartenant à l'utilisateur connecté"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des comptes récupérée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountDTO.class),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "id": 1,
                                        "iban": "FR76 1027 8012 3456 7890 1234 567",
                                        "balance": 1250.50,
                                        "type": "CHECKING",
                                        "active": true,
                                        "createdAt": "2026-01-15T10:30:00"
                                      },
                                      {
                                        "id": 2,
                                        "iban": "FR76 1027 8098 7654 3210 9876 543",
                                        "balance": 5000.00,
                                        "type": "SAVINGS",
                                        "active": true,
                                        "createdAt": "2026-01-20T14:20:00"
                                      }
                                    ]
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        List<Account> accounts = accountService.getUserAccounts();
        List<AccountDTO> accountDTOs = accounts.stream()
                .map(entityMapper::toAccountDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accountDTOs);
    }

    @Operation(
            summary = "Récupère un compte par son ID",
            description = "Retourne les détails d'un compte spécifique appartenant à l'utilisateur"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compte trouvé"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compte non trouvé ou accès non autorisé",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-01-22T10:30:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Compte non trouvé"
                                    }
                                    """)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO> getAccountById(
            @Parameter(description = "ID du compte", example = "1")
            @PathVariable Long id
    ) {
        Account account = accountService.getAccountById(id);
        return ResponseEntity.ok(entityMapper.toAccountDTO(account));
    }

    @Operation(
            summary = "Crée un nouveau compte",
            description = "Crée un nouveau compte bancaire avec un IBAN généré automatiquement. Types disponibles : CHECKING (courant), SAVINGS (épargne), BUSINESS (professionnel)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Compte créé avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Limite de comptes atteinte (maximum 5)"
            )
    })
    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(
            @Parameter(description = "Type de compte", example = "SAVINGS")
            @RequestParam AccountType type
    ) {
        Account account = accountService.createAccount(type);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entityMapper.toAccountDTO(account));
    }

    @Operation(
            summary = "Désactive un compte",
            description = "Désactive (ferme) un compte. Le solde doit être à zéro."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Compte désactivé avec succès"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Impossible de désactiver un compte avec un solde non nul"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deactivateAccount(
            @Parameter(description = "ID du compte à désactiver", example = "1")
            @PathVariable Long id
    ) {
        accountService.deactivateAccount(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Compte désactivé avec succès");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Solde total de tous les comptes",
            description = "Calcule et retourne la somme des soldes de tous les comptes actifs de l'utilisateur"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Solde total calculé",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "totalBalance": 6250.50
                            }
                            """)
            )
    )
    @GetMapping("/total-balance")
    public ResponseEntity<Map<String, BigDecimal>> getTotalBalance() {
        BigDecimal totalBalance = accountService.getTotalBalance();
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("totalBalance", totalBalance);
        return ResponseEntity.ok(response);
    }
}