package com.qrorder.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Without this, every validation failure the services throw (bad phone
 * number, empty cart, closed session, etc.) surfaces to the browser as a
 * bare 500 Internal Server Error with no message - technically "working"
 * but useless for the person filling in a form. This converts our own
 * IllegalArgumentException/IllegalStateException usages into proper HTTP
 * status codes with the actual message in the response body, which the
 * frontend's api() helper now reads and shows to the user directly.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler({IllegalStateException.class, NoSuchElementException.class})
    public ResponseEntity<Map<String, String>> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }
}
