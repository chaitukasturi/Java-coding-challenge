package com.crewmeister.cmcodingchallenge.currency.service;

import com.crewmeister.cmcodingchallenge.currency.dto.ConversionResponse;
import com.crewmeister.cmcodingchallenge.currency.dto.ExchangeRateResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service contract for EUR foreign exchange rate operations.
 */
public interface CurrencyService {

    /** Returns all currency codes that have at least one stored exchange rate. */
    List<String> getAvailableCurrencies();

    /** Returns all stored EUR-FX rates across all dates and currencies. */
    List<ExchangeRateResponse> getAllRates();

    /**
     * Returns all EUR-FX rates for the given date.
     *
     * @throws RateNotFoundException if no rates are stored for that date
     */
    List<ExchangeRateResponse> getRatesByDate(LocalDate date);

    /**
     * Converts the given amount in a foreign currency to EUR using the rate on the specified date.
     * Formula: amountInEur = amount / rate  (since rate = 1 EUR = X units of currency)
     *
     * @throws RateNotFoundException if no rate exists for the currency/date combination
     */
    ConversionResponse convertToEur(String currency, BigDecimal amount, LocalDate date);
}
