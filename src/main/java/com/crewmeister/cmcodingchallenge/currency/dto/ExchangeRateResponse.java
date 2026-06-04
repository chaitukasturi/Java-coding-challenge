package com.crewmeister.cmcodingchallenge.currency.dto;

import com.crewmeister.cmcodingchallenge.currency.entity.ExchangeRate;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRateResponse(String currency, LocalDate date, BigDecimal rate) {

    public static ExchangeRateResponse from(ExchangeRate entity) {
        return new ExchangeRateResponse(entity.getCurrency(), entity.getDate(), entity.getRate());
    }
}
