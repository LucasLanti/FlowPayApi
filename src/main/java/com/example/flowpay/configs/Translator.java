package com.example.flowpay.configs;

import java.util.Locale;
import java.util.Objects;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class Translator {
    private static final Locale DEFAULT_LOCALE = Locale.of("pt", "BR");

    private final MessageSource messageSource;

    public Translator(MessageSource messageSource) {
        this.messageSource = Objects.requireNonNull(messageSource);
    }

    public String translate(String keyMessage) {
        return translate(keyMessage, null, keyMessage);
    }

    public String translate(String keyMessage, Object[] args, String defaultMessage) {
        return messageSource.getMessage(keyMessage, args, defaultMessage, DEFAULT_LOCALE);
    }
}
