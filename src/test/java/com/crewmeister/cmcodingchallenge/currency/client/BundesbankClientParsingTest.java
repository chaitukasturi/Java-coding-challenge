package com.crewmeister.cmcodingchallenge.currency.client;

import com.crewmeister.cmcodingchallenge.currency.entity.ExchangeRate;
import com.crewmeister.cmcodingchallenge.currency.repository.ExchangeRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BundesbankClientParsingTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ExchangeRateRepository repository;

    @InjectMocks
    private BundesbankClient client;

    @Test
    void parsesValidCsvAndSavesRates() {
        ReflectionTestUtils.setField(client, "baseUrl", "https://api.bundesbank.de/service/data/BBK");

        String csv = """
                BBK_BBEX3_D_USD_EUR_BB_AC_000
                2024-01-15;1.0930
                2024-01-16;1.0945
                """;

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(csv);
        when(repository.findByCurrencyAndDate(any(), any())).thenReturn(Optional.empty());

        ArgumentCaptor<ExchangeRate> captor = ArgumentCaptor.forClass(ExchangeRate.class);

        // only fetch USD for this test
        ReflectionTestUtils.invokeMethod(client, "fetchAndStore", "USD",
                LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 16));

        verify(repository, times(2)).save(captor.capture());
        List<ExchangeRate> saved = captor.getAllValues();

        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getCurrency()).isEqualTo("USD");
        assertThat(saved.get(0).getRate()).isEqualByComparingTo("1.0930");
        assertThat(saved.get(1).getDate()).isEqualTo(LocalDate.of(2024, 1, 16));
    }

    @Test
    void skipsMissingDataRows() {
        ReflectionTestUtils.setField(client, "baseUrl", "https://api.bundesbank.de/service/data/BBK");

        // Bundesbank uses "." for bank holidays / missing data
        String csv = """
                BBK_BBEX3_D_USD_EUR_BB_AC_000
                2024-01-13;.
                2024-01-14;.
                2024-01-15;1.0930
                """;

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(csv);
        when(repository.findByCurrencyAndDate(any(), any())).thenReturn(Optional.empty());

        ReflectionTestUtils.invokeMethod(client, "fetchAndStore", "USD",
                LocalDate.of(2024, 1, 13), LocalDate.of(2024, 1, 15));

        // only the one valid row should be saved
        verify(repository, times(1)).save(any());
    }
}
