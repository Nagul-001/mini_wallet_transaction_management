package com.example.wallet.dto;

import com.example.wallet.entity.Wallet;
import java.math.BigDecimal;

public record WalletResponse(Long walletId, String name, String email, BigDecimal balance) {
    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(wallet.getId(), wallet.getName(), wallet.getEmail(), wallet.getBalance());
    }
}
