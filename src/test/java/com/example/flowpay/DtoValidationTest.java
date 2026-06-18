package com.example.flowpay;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.flowpay.dtos.AttendantDto;
import com.example.flowpay.dtos.TicketDto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;

class DtoValidationTest {

    @Test
    void ticketDtoValidatesRequiredFieldsAndContentLength() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();

            Set<ConstraintViolation<TicketDto>> blankViolations = validator
                    .validate(new TicketDto("", null));
            Set<ConstraintViolation<TicketDto>> longContentViolations = validator
                    .validate(new TicketDto("x".repeat(101), UUID.randomUUID()));

            assertTrue(hasMessageTemplate(blankViolations, "{ticket.content.required}"));
            assertTrue(hasMessageTemplate(blankViolations, "{ticket.teamId.required}"));
            assertTrue(hasMessageTemplate(longContentViolations, "{ticket.content.maxLength}"));
        }
    }

    @Test
    void attendantDtoValidatesRequiredFieldsAndNameLength() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();

            Set<ConstraintViolation<AttendantDto>> blankViolations = validator
                    .validate(new AttendantDto("", null));
            Set<ConstraintViolation<AttendantDto>> longNameViolations = validator
                    .validate(new AttendantDto("x".repeat(51), UUID.randomUUID()));

            assertTrue(hasMessageTemplate(blankViolations, "{attendant.name.required}"));
            assertTrue(hasMessageTemplate(blankViolations, "{attendant.teamId.required}"));
            assertTrue(hasMessageTemplate(longNameViolations, "{attendant.name.maxLength}"));
        }
    }

    private <T> boolean hasMessageTemplate(Set<ConstraintViolation<T>> violations, String messageTemplate) {
        return violations.stream()
                .anyMatch(violation -> messageTemplate.equals(violation.getMessageTemplate()));
    }
}
