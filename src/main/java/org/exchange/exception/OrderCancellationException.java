package org.exchange.exception;

public class OrderCancellationException extends Exception {
    public OrderCancellationException() {
        super();
    }

    public OrderCancellationException(String message) {
        super(message);
    }
}
