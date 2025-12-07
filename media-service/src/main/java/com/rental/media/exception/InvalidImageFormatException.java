package com.rental.media.exception;

/**
 * Exception levée quand le format d'image est invalide
 */
public class InvalidImageFormatException extends RuntimeException {

    public InvalidImageFormatException(String message) {
        super(message);
    }

    public InvalidImageFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}