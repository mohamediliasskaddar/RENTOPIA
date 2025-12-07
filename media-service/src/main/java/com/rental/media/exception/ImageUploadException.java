package com.rental.media.exception;

/**
 * Exception levée lors d'une erreur d'upload vers S3
 */
public class ImageUploadException extends RuntimeException {

    public ImageUploadException(String message) {
        super(message);
    }

    public ImageUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}