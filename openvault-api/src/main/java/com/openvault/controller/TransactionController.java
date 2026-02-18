package com.openvault.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openvault.dto.TransactionDTO;
import com.openvault.entity.Transaction;
import com.openvault.entity.TransactionType;
import com.openvault.service.TransactionHistoryService;
import com.openvault.util.EntityMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Consultation de l'historique et des statistiques des transactions")
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionController {

    private final TransactionHistoryService transactionHistoryService;
    private final EntityMapper entityMapper;

    @Operation(
            summary = "Historique paginé des transactions",
            description = "Retourne l'historique des transactions d'un compte avec pagination"
    )
    @GetMapping("/account/{accountId}")
    public ResponseEntity<Page<TransactionDTO>> getAccountTransactions(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<Transaction> transactions = transactionHistoryService.getAccountTransactions(accountId, page, size);
        Page<TransactionDTO> transactionDTOs = transactions.map(entityMapper::toTransactionDTO);
        return ResponseEntity.ok(transactionDTOs);
    }

    @Operation(
            summary = "Dernières transactions",
            description = "Retourne les dernières transactions d'un compte"
    )
    @GetMapping("/account/{accountId}/recent")
    public ResponseEntity<List<TransactionDTO>> getRecentTransactions(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<Transaction> transactions = transactionHistoryService.getRecentTransactions(accountId, limit);
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(entityMapper::toTransactionDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDTOs);
    }

    @Operation(
            summary = "Transactions par type",
            description = "Retourne les transactions d'un compte filtrées par type"
    )
    @GetMapping("/account/{accountId}/type/{type}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByType(
            @PathVariable Long accountId,
            @PathVariable TransactionType type
    ) {
        List<Transaction> transactions = transactionHistoryService.getTransactionsByType(accountId, type);
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(entityMapper::toTransactionDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDTOs);
    }

    @Operation(
            summary = "Transactions par période",
            description = "Retourne les transactions d'un compte filtrées par période"
    )
    @GetMapping("/account/{accountId}/date-range")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByDateRange(
            @PathVariable Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        List<Transaction> transactions = transactionHistoryService.getTransactionsByDateRange(accountId, start, end);
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(entityMapper::toTransactionDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDTOs);
    }

    @Operation(
            summary = "Transaction par ID",
            description = "Retourne une transaction par son ID"
    )
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long id) {
        Transaction transaction = transactionHistoryService.getTransactionById(id);
        return ResponseEntity.ok(entityMapper.toTransactionDTO(transaction));
    }

    @Operation(
            summary = "Statistiques des transactions",
            description = "Retourne les statistiques des transactions d'un compte"
    )
    @GetMapping("/account/{accountId}/stats")
    public ResponseEntity<TransactionHistoryService.TransactionStats> getTransactionStats(
            @PathVariable Long accountId
    ) {
        TransactionHistoryService.TransactionStats stats = transactionHistoryService.getTransactionStats(accountId);
        return ResponseEntity.ok(stats);
    }
}