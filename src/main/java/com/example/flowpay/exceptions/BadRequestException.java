package com.example.flowpay.exceptions;

public class BadRequestException extends RuntimeException {
    private final String keyMessage;

    public BadRequestException(String keyMessage) {
        super(keyMessage);
        this.keyMessage = keyMessage;
    }

    public BadRequestException(String message, String keyMessage) {
        super(message);
        this.keyMessage = keyMessage;
    }

    public String getKeyMessage() {
        return keyMessage;
    }
}
