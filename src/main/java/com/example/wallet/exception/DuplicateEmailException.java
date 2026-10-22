package com.example.wallet.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) { super("A wallet already exists for email " + email); }
}
