package org.streamreasoning.polyflow.api.exceptions;

public class OutOfOrderElementException extends RuntimeException {
    public OutOfOrderElementException(String message) {
        super(message);
    }
}
