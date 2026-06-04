package com.crewmeister.cmcodingchallenge.currency.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Stores a single EUR exchange rate for one currency on one date.
 * Rate meaning: 1 EUR = {@code rate} units of {@code currency}.
 */
@Entity
@Table(
    name = "exchange_rate",
    uniqueConstraints = @UniqueConstraint(columnNames = {"currency", "date"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private LocalDate date;

    // units of currency per 1 EUR — matches Bundesbank convention
    @Column(nullable = false, precision = 20, scale = 6)
    private BigDecimal rate;
}
