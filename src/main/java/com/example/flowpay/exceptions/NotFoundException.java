package com.example.flowpay.exceptions;

public class NotFoundException extends RuntimeException {
    private final String keyMessage;

    public NotFoundException(String keyMessage) {
        super(keyMessage);
        this.keyMessage = keyMessage;
    }

    public NotFoundException(String message, String keyMessage) {
        super(message);
        this.keyMessage = keyMessage;
    }

    public String getKeyMessage() {
        return keyMessage;
    }
}
