package com.crewmeister.cmcodingchallenge.currency.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConversionResponse(
        String currency,
        BigDecimal amount,
        LocalDate date,
        BigDecimal amountInEur
) {}
