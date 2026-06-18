package com.example.flowpay.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private String message;

    @JsonProperty("key_message")
    private String keyMessage;
}
