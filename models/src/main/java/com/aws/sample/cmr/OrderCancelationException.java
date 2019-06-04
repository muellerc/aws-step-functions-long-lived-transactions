package com.aws.sample.cmr;

public class OrderCancelationException extends RuntimeException {

    private static final long serialVersionUID = 6307541123249930399L;

    public OrderCancelationException() {
    }

    public OrderCancelationException(String message) {
        super(message);
    }

    public OrderCancelationException(String message, Exception exception) {
        super(message, exception);
    }
}
