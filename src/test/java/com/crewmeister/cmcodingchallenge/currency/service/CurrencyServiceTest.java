package com.crewmeister.cmcodingchallenge.currency.service;

import com.crewmeister.cmcodingchallenge.currency.dto.ConversionResponse;
import com.crewmeister.cmcodingchallenge.currency.entity.ExchangeRate;
import com.crewmeister.cmcodingchallenge.currency.exception.RateNotFoundException;
import com.crewmeister.cmcodingchallenge.currency.repository.ExchangeRateRepository;
import com.crewmeister.cmcodingchallenge.currency.service.CurrencyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private ExchangeRateRepository repository;

    @InjectMocks
    private CurrencyServiceImpl service;

    private final LocalDate testDate = LocalDate.of(2024, 1, 15);

    @BeforeEach
    void setUp() {
        // nothing shared to set up — each test arranges its own mocks
    }

    @Test
    void getAvailableCurrencies_returnsDistinctList() {
        when(repository.findDistinctCurrencies()).thenReturn(List.of("GBP", "USD"));

        List<String> result = service.getAvailableCurrencies();

        assertThat(result).containsExactly("GBP", "USD");
    }

    @Test
    void getRatesByDate_throwsWhenNoneFound() {
        when(repository.findByDate(testDate)).thenReturn(List.of());

        assertThatThrownBy(() -> service.getRatesByDate(testDate))
                .isInstanceOf(RateNotFoundException.class)
                .hasMessageContaining(testDate.toString());
    }

    @Test
    void convertToEur_calculatesCorrectly() {
        // 1 EUR = 1.10 USD, so 110 USD = 100 EUR
        ExchangeRate rate = ExchangeRate.builder()
                .currency("USD")
                .date(testDate)
                .rate(new BigDecimal("1.10"))
                .build();
        when(repository.findByCurrencyAndDate("USD", testDate)).thenReturn(Optional.of(rate));

        ConversionResponse result = service.convertToEur("USD", new BigDecimal("110"), testDate);

        assertThat(result.amountInEur()).isEqualByComparingTo("100.000000");
    }

    @Test
    void convertToEur_throwsWhenRateMissing() {
        when(repository.findByCurrencyAndDate("USD", testDate)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.convertToEur("USD", BigDecimal.TEN, testDate))
                .isInstanceOf(RateNotFoundException.class)
                .hasMessageContaining("USD");
    }

    @Test
    void convertToEur_isCaseInsensitiveForCurrency() {
        ExchangeRate rate = ExchangeRate.builder()
                .currency("GBP")
                .date(testDate)
                .rate(new BigDecimal("0.86"))
                .build();
        when(repository.findByCurrencyAndDate("GBP", testDate)).thenReturn(Optional.of(rate));

        ConversionResponse result = service.convertToEur("gbp", new BigDecimal("86"), testDate);

        assertThat(result.currency()).isEqualTo("GBP");
    }
}
