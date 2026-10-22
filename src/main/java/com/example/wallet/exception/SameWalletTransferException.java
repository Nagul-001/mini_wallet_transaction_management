package com.example.wallet.exception;

public class SameWalletTransferException extends RuntimeException {
    public SameWalletTransferException() { super("Sender and receiver wallets must be different"); }
}
