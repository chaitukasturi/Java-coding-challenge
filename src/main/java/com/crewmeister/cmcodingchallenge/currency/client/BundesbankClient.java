package com.crewmeister.cmcodingchallenge.currency.client;

import com.crewmeister.cmcodingchallenge.currency.entity.ExchangeRate;
import com.crewmeister.cmcodingchallenge.currency.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetches daily EUR exchange rates from the Bundesbank SDMX REST API and persists them in H2.
 *
 * On startup, loads the last 3 years of historical rates for all supported currencies.
 * A scheduled job then refreshes today's rates every midnight to keep the data current.
 *
 * Rate convention: 1 EUR = X units of the foreign currency (as published by the ECB/Bundesbank).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BundesbankClient {

    // Bundesbank publishes daily EUR rates for these currencies
    private static final List<String> SUPPORTED_CURRENCIES = List.of(
            "USD", "GBP", "JPY", "CHF", "AUD", "CAD", "CNY", "HKD",
            "INR", "KRW", "MXN", "NOK", "NZD", "PLN", "SEK", "SGD",
            "TRY", "ZAR", "DKK", "HUF", "CZK", "RON", "BGN", "HRK",
            "RUB", "BRL", "IDR", "MYR", "PHP", "THB"
    );

    private final RestTemplate restTemplate;
    private final ExchangeRateRepository repository;

    @Value("${bundesbank.api.base-url}")
    private String baseUrl;

    @PostConstruct
    public void loadHistoricalRates() {
        log.info("Loading exchange rates from Bundesbank...");
        LocalDate startDate = LocalDate.now().minusYears(3);
        LocalDate endDate = LocalDate.now();

        for (String currency : SUPPORTED_CURRENCIES) {
            try {
                fetchAndStore(currency, startDate, endDate);
            } catch (Exception e) {
                log.warn("Could not fetch rates for {}: {}", currency, e.getMessage(), e);
            }
        }
        log.info("Exchange rate loading complete.");
    }

    // refreshes today's rates every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    public void refreshTodaysRates() {
        LocalDate today = LocalDate.now();
        for (String currency : SUPPORTED_CURRENCIES) {
            try {
                fetchAndStore(currency, today, today);
            } catch (Exception e) {
                log.warn("Daily refresh failed for {}: {}", currency, e.getMessage());
            }
        }
    }

    private void fetchAndStore(String currency, LocalDate from, LocalDate to) {
        // Bundesbank SDMX key: BBEX3.D.{CURRENCY}.EUR.BB.AC.000
        String url = String.format(
                "%s/D.%s.EUR.BB.AC.000?format=csv&lang=en&startPeriod=%s&endPeriod=%s",
                baseUrl, currency, from, to
        );

        String csv;
        try {
            csv = restTemplate.getForObject(url, String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Bundesbank API request failed for " + currency, e);
        }

        if (csv == null || csv.isBlank()) {
            return;
        }

        List<ExchangeRate> rates = parseCsv(csv, currency);
        if (!rates.isEmpty()) {
            // upsert: skip rows that already exist (e.g. during daily refresh)
            for (ExchangeRate rate : rates) {
                repository.findByCurrencyAndDate(rate.getCurrency(), rate.getDate())
                        .ifPresentOrElse(
                                existing -> { /* already stored, skip */ },
                                () -> repository.save(rate)
                        );
            }
        }
    }

    private List<ExchangeRate> parseCsv(String csv, String currency) {
        List<ExchangeRate> rates = new ArrayList<>();

        for (String line : csv.split("\n")) {
            line = line.trim();
            // skip header lines and empty lines
            if (line.isEmpty() || !line.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
                continue;
            }

            String[] parts = line.split(",");
            if (parts.length < 2) continue;

            String dateStr = parts[0].trim();
            String valueStr = parts[1].trim();

            // Bundesbank uses "." for missing data (e.g. weekends, bank holidays)
            if (valueStr.equals(".") || valueStr.isEmpty() || valueStr.startsWith("No value")) {
                continue;
            }

            try {
                LocalDate date = LocalDate.parse(dateStr);
                BigDecimal rate = new BigDecimal(valueStr);
                rates.add(ExchangeRate.builder()
                        .currency(currency)
                        .date(date)
                        .rate(rate)
                        .build());
            } catch (DateTimeParseException | NumberFormatException e) {
                log.debug("Skipping unparseable line for {}: {}", currency, line);
            }
        }

        return rates;
    }
}
