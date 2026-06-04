package com.crewmeister.cmcodingchallenge.currency.controller;

import com.crewmeister.cmcodingchallenge.currency.dto.ConversionResponse;
import com.crewmeister.cmcodingchallenge.currency.dto.ExchangeRateResponse;
import com.crewmeister.cmcodingchallenge.currency.exception.RateNotFoundException;
import com.crewmeister.cmcodingchallenge.currency.service.CurrencyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CurrencyController.class)
class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyService currencyService;

    @Test
    void getCurrencies_returns200WithList() throws Exception {
        when(currencyService.getAvailableCurrencies()).thenReturn(List.of("USD", "GBP"));

        mockMvc.perform(get("/api/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("USD"))
                .andExpect(jsonPath("$[1]").value("GBP"));
    }

    @Test
    void getRatesByDate_returns200() throws Exception {
        LocalDate date = LocalDate.of(2024, 1, 15);
        when(currencyService.getRatesByDate(date)).thenReturn(List.of(
                new ExchangeRateResponse("USD", date, new BigDecimal("1.0930"))
        ));

        mockMvc.perform(get("/api/exchange-rates/2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].currency").value("USD"))
                .andExpect(jsonPath("$[0].rate").value(1.0930));
    }

    @Test
    void getRatesByDate_returns404WhenNoRates() throws Exception {
        LocalDate date = LocalDate.of(2024, 1, 13);
        when(currencyService.getRatesByDate(date))
                .thenThrow(new RateNotFoundException("No exchange rates found for date: " + date));

        mockMvc.perform(get("/api/exchange-rates/2024-01-13"))
                .andExpect(status().isNotFound());
    }

    @Test
    void convert_returns200WithResult() throws Exception {
        LocalDate date = LocalDate.of(2024, 1, 15);
        when(currencyService.convertToEur("USD", new BigDecimal("110"), date))
                .thenReturn(new ConversionResponse("USD", new BigDecimal("110"), date, new BigDecimal("100.000000")));

        mockMvc.perform(get("/api/exchange-rates/2024-01-15/convert")
                        .param("currency", "USD")
                        .param("amount", "110"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.amountInEur").value(100.0));
    }

}
