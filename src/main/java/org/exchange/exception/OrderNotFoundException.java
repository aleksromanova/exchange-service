package org.exchange.exception;

public class OrderNotFoundException extends Exception {
    public OrderNotFoundException() {
        super();
    }

    public OrderNotFoundException(String message) {
        super(message);
    }
}
