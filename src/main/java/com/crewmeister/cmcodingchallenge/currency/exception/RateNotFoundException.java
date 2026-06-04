package com.crewmeister.cmcodingchallenge.currency.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when no exchange rate exists for the requested currency/date combination.
 * Maps to HTTP 404 via @ResponseStatus.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RateNotFoundException extends RuntimeException {

    public RateNotFoundException(String message) {
        super(message);
    }
}
