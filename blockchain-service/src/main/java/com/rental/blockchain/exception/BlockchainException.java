package com.rental.blockchain.exception;

/**
 * Exception personnalisée pour les erreurs blockchain
 */
public class BlockchainException extends RuntimeException {

    public BlockchainException(String message) {
        super(message);
    }

    public BlockchainException(String message, Throwable cause) {
        super(message, cause);
    }
}