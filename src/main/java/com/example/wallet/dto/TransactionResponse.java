package com.example.wallet.dto;

import com.example.wallet.entity.WalletTransaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(Long transactionId, String type, BigDecimal amount,
                                  String status, String referenceId, LocalDateTime createdAt) {
    public static TransactionResponse from(WalletTransaction tx) {
        return new TransactionResponse(tx.getId(), tx.getType().name(), tx.getAmount(),
                tx.getStatus().name(), tx.getReferenceId(), tx.getCreatedAt());
    }
}
