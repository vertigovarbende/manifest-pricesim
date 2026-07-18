package com.manifest.concurrency.exception;

public class InvalidThreadModeException extends RuntimeException {

    public InvalidThreadModeException(String message, Throwable cause) {
        super(message, cause);
    }
}
