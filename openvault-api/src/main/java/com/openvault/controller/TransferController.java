package com.openvault.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openvault.dto.DepositWithdrawRequest;
import com.openvault.dto.TransactionDTO;
import com.openvault.dto.TransferRequest;
import com.openvault.entity.Transaction;
import com.openvault.service.TransferService;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
@Tag(name = "Virements", description = "Opérations de transfert d'argent (virements, dépôts, retraits)")
@SecurityRequirement(name = "Bearer Authentication")
public class TransferController {

    private final TransferService transferService;
    private final EntityMapper entityMapper;

    @Operation(
            summary = "Effectue un virement",
            description = """
                    Effectue un virement sécurisé entre deux comptes.
                    - Le montant doit être positif et avoir maximum 2 décimales
                    - Le solde du compte source doit être suffisant
                    - Les deux comptes doivent être actifs
                    - L'opération est atomique (tout ou rien)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Virement effectué avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransactionDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 42,
                                      "fromIban": "FR76 1027 8012 3456 7890 1234 567",
                                      "toIban": "FR76 1027 8098 7654 3210 9876 543",
                                      "amount": 250.50,
                                      "type": "TRANSFER",
                                      "description": "Remboursement restaurant",
                                      "timestamp": "2026-01-22T14:30:00",
                                      "status": "COMPLETED",
                                      "reference": "TXN-1738123456-7832"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solde insuffisant, montant invalide ou compte inactif",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-01-22T14:30:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Solde insuffisant. Solde actuel: 100.00 EUR"
                                    }
                                    """)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<TransactionDTO> transfer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Détails du virement",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = TransferRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "fromAccountId": 1,
                                      "toIban": "FR76 1027 8098 7654 3210 9876 543",
                                      "amount": 250.50,
                                      "description": "Remboursement restaurant"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody TransferRequest request
    ) {
        Transaction transaction = transferService.transfer(
                request.getFromAccountId(),
                request.getToIban(),
                request.getAmount(),
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entityMapper.toTransactionDTO(transaction));
    }

    @Operation(
            summary = "Effectue un dépôt",
            description = "Ajoute de l'argent sur un compte (ex: dépôt de salaire, remboursement)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Dépôt effectué avec succès"),
            @ApiResponse(responseCode = "400", description = "Montant invalide ou compte inactif")
    })
    @PostMapping("/deposit/{accountId}")
    public ResponseEntity<TransactionDTO> deposit(
            @Parameter(description = "ID du compte à créditer", example = "1")
            @PathVariable Long accountId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Montant et description du dépôt",
                    content = @Content(
                            schema = @Schema(implementation = DepositWithdrawRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "amount": 1500.00,
                                      "description": "Salaire janvier 2026"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody DepositWithdrawRequest request
    ) {
        Transaction transaction = transferService.deposit(
                accountId,
                request.getAmount(),
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entityMapper.toTransactionDTO(transaction));
    }

    @Operation(
            summary = "Effectue un retrait",
            description = "Retire de l'argent d'un compte (ex: retrait DAB, paiement cash)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Retrait effectué avec succès"),
            @ApiResponse(responseCode = "400", description = "Solde insuffisant, montant invalide ou compte inactif")
    })
    @PostMapping("/withdraw/{accountId}")
    public ResponseEntity<TransactionDTO> withdraw(
            @Parameter(description = "ID du compte à débiter", example = "1")
            @PathVariable Long accountId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Montant et description du retrait",
                    content = @Content(
                            schema = @Schema(implementation = DepositWithdrawRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "amount": 50.00,
                                      "description": "Retrait DAB"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody DepositWithdrawRequest request
    ) {
        Transaction transaction = transferService.withdraw(
                accountId,
                request.getAmount(),
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entityMapper.toTransactionDTO(transaction));
    }
}