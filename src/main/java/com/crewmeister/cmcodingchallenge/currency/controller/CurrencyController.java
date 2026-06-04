package com.crewmeister.cmcodingchallenge.currency.controller;

import com.crewmeister.cmcodingchallenge.currency.dto.ConversionResponse;
import com.crewmeister.cmcodingchallenge.currency.dto.ExchangeRateResponse;
import com.crewmeister.cmcodingchallenge.currency.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller exposing EUR foreign exchange rate endpoints.
 *
 * All rates follow the Bundesbank convention: 1 EUR = X units of the foreign currency.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    /**
     * Returns all currency codes for which exchange rates are available.
     * GET /api/currencies
     */
    @GetMapping("/currencies")
    public ResponseEntity<List<String>> getCurrencies() {
        return ResponseEntity.ok(currencyService.getAvailableCurrencies());
    }

    /**
     * Returns all EUR-FX exchange rates across all stored dates and currencies.
     * GET /api/exchange-rates
     */
    @GetMapping("/exchange-rates")
    public ResponseEntity<List<ExchangeRateResponse>> getAllRates() {
        return ResponseEntity.ok(currencyService.getAllRates());
    }

    /**
     * Returns all EUR-FX exchange rates for a specific date.
     * GET /api/exchange-rates/{date}
     *
     * @param date date in YYYY-MM-DD format
     * @throws com.crewmeister.cmcodingchallenge.currency.exception.RateNotFoundException if no rates exist for that date (e.g. weekend)
     */
    @GetMapping("/exchange-rates/{date}")
    public ResponseEntity<List<ExchangeRateResponse>> getRatesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(currencyService.getRatesByDate(date));
    }

    /**
     * Converts a foreign currency amount to EUR using the exchange rate on the given date.
     * GET /api/exchange-rates/{date}/convert?currency=USD&amount=100
     *
     * @param date     date in YYYY-MM-DD format
     * @param currency ISO 4217 currency code (e.g. USD, GBP)
     * @param amount   amount in the foreign currency to convert
     * @throws com.crewmeister.cmcodingchallenge.currency.exception.RateNotFoundException if no rate exists for the currency/date combination
     */
    @GetMapping("/exchange-rates/{date}/convert")
    public ResponseEntity<ConversionResponse> convert(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String currency,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(currencyService.convertToEur(currency, amount, date));
    }
}
