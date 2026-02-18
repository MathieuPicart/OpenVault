package com.openvault.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.openvault.entity.TransactionStatus;
import com.openvault.entity.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private String fromIban;
    private String toIban;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private LocalDateTime timestamp;
    private TransactionStatus status;
    private String reference;
}