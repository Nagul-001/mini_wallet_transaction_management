package com.example.wallet.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(LocalDateTime timestamp, int status, String errorCode,
                       String message, Map<String, String> validationErrors) {
    public static ApiError of(int status, String errorCode, String message) {
        return new ApiError(LocalDateTime.now(), status, errorCode, message, Map.of());
    }
}
