package com.example.flowpay.exceptions;

import com.example.flowpay.configs.Translator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Translator translator;

    public GlobalExceptionHandler(Translator translator) {
        this.translator = Objects.requireNonNull(translator);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.builder()
                        .message(translator.translate(ex.getKeyMessage(), null, ex.getMessage()))
                        .keyMessage(ex.getKeyMessage())
                        .build());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .message(translator.translate(ex.getKeyMessage(), null, ex.getMessage()))
                        .keyMessage(ex.getKeyMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .orElse(null);
        String message = fieldError == null
                ? ex.getMessage()
                : fieldError.getDefaultMessage();
        String keyMessage = "request.validation.invalid";

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .message(message == null ? translator.translate(keyMessage, null, ex.getMessage()) : message)
                        .keyMessage(keyMessage)
                        .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBody(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        String keyMessage = "request.body.invalid";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .message(translator.translate(keyMessage, null, ex.getMessage()))
                        .keyMessage(keyMessage)
                        .build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameter(MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        String keyMessage = "request.parameter.invalid";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .message(translator.translate(keyMessage, null, ex.getMessage()))
                        .keyMessage(keyMessage)
                        .build());
    }

    @ExceptionHandler({ NoHandlerFoundException.class, NoResourceFoundException.class })
    public ResponseEntity<ErrorResponse> handleEndpointNotFound(Exception ex, HttpServletRequest request) {
        String keyMessage = "endpoint.not_found";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.builder()
                        .message(translator.translate(keyMessage, null, "Endpoint nao encontrado."))
                        .keyMessage(keyMessage)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        String keyMessage = "error.internal";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                        .message(translator.translate(keyMessage, null, ex.getMessage()))
                        .keyMessage(keyMessage)
                        .build());
    }
}
