package com.crewmeister.cmcodingchallenge.currency.repository;

import com.crewmeister.cmcodingchallenge.currency.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for exchange rate data. Spring Data JPA generates all implementations at runtime.
 */
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    /** Returns all rates stored for the given date (one per currency). */
    List<ExchangeRate> findByDate(LocalDate date);

    /** Looks up the rate for a specific currency on a specific date. */
    Optional<ExchangeRate> findByCurrencyAndDate(String currency, LocalDate date);

    /** Returns all distinct currency codes present in the database, sorted alphabetically. */
    @Query("SELECT DISTINCT e.currency FROM ExchangeRate e ORDER BY e.currency")
    List<String> findDistinctCurrencies();
}
