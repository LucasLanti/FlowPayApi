package com.example.flowpay.dtos.response;

import java.util.List;

import org.springframework.data.domain.Page;

public record PaginatedResponseDto<T>(
        T data,
        int page,
        long total,
        int size) {
    public static <T> PaginatedResponseDto<List<T>> from(Page<T> page) {
        return new PaginatedResponseDto<>(
                page.getContent(),
                page.getNumber(),
                page.getTotalElements(),
                page.getSize());
    }

    public static <T> PaginatedResponseDto<T> from(Page<?> page, T data) {
        return new PaginatedResponseDto<>(
                data,
                page.getNumber(),
                page.getTotalElements(),
                page.getSize());
    }
}
