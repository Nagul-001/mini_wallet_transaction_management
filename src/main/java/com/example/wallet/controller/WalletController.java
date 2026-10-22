package com.example.wallet.controller;

import com.example.wallet.dto.*;
import com.example.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {
    private final WalletService walletService;
    public WalletController(WalletService walletService) { this.walletService = walletService; }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.createWallet(request));
    }

    @PostMapping("/{walletId}/deposit")
    public DepositResponse deposit(@PathVariable Long walletId, @Valid @RequestBody AmountRequest request) {
        return walletService.deposit(walletId, request.amount());
    }

    @PostMapping("/transfer")
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
        return walletService.transfer(request);
    }

    @GetMapping("/{walletId}")
    public WalletResponse getWallet(@PathVariable Long walletId) { return walletService.getWallet(walletId); }

    @GetMapping("/{walletId}/transactions")
    public List<TransactionResponse> getTransactions(@PathVariable Long walletId) {
        return walletService.getTransactions(walletId);
    }
}
