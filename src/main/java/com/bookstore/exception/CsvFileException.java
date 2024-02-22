package com.bookstore.exception;

public class CsvFileException extends RuntimeException {
    public CsvFileException(String message) {
        super(message);
    }

    public CsvFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
