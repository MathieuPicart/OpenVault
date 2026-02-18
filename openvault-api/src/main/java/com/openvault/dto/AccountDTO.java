package com.openvault.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.openvault.entity.AccountType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private Long id;
    private String iban;
    private BigDecimal balance;
    private AccountType type;
    private Boolean active;
    private LocalDateTime createdAt;
}