package com.crewmeister.cmcodingchallenge.currency.service;

import com.crewmeister.cmcodingchallenge.currency.dto.ConversionResponse;
import com.crewmeister.cmcodingchallenge.currency.dto.ExchangeRateResponse;
import com.crewmeister.cmcodingchallenge.currency.entity.ExchangeRate;
import com.crewmeister.cmcodingchallenge.currency.exception.RateNotFoundException;
import com.crewmeister.cmcodingchallenge.currency.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private final ExchangeRateRepository repository;

    @Override
    public List<String> getAvailableCurrencies() {
        return repository.findDistinctCurrencies();
    }

    @Override
    public List<ExchangeRateResponse> getAllRates() {
        return repository.findAll().stream()
                .map(ExchangeRateResponse::from)
                .toList();
    }

    @Override
    public List<ExchangeRateResponse> getRatesByDate(LocalDate date) {
        List<ExchangeRate> rates = repository.findByDate(date);
        if (rates.isEmpty()) {
            throw new RateNotFoundException("No exchange rates found for date: " + date);
        }
        return rates.stream().map(ExchangeRateResponse::from).toList();
    }

    @Override
    public ConversionResponse convertToEur(String currency, BigDecimal amount, LocalDate date) {
        ExchangeRate rate = repository.findByCurrencyAndDate(currency.toUpperCase(), date)
                .orElseThrow(() -> new RateNotFoundException(
                        "No exchange rate found for " + currency.toUpperCase() + " on " + date
                ));

        // 1 EUR = rate units of currency, so: amountInEur = amount / rate
        BigDecimal amountInEur = amount.divide(rate.getRate(), 6, RoundingMode.HALF_UP);

        return new ConversionResponse(currency.toUpperCase(), amount, date, amountInEur);
    }
}
