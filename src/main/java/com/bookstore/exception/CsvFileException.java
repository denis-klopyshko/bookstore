package com.bookstore.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CsvFileException extends RuntimeException {
    public CsvFileException(String message) {
        super(message);
    }

    public CsvFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
