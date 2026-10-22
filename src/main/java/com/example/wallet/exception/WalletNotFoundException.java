package com.example.wallet.exception;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(Long id) { super("Wallet with ID " + id + " was not found"); }
}
