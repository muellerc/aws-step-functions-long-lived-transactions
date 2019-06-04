package com.aws.sample.cmr;

public class OrderCreationException extends RuntimeException {

    private static final long serialVersionUID = 5631060178442489103L;

    public OrderCreationException() {
    }

    public OrderCreationException(String message) {
        super(message);
    }

    public OrderCreationException(String message, Exception exception) {
        super(message, exception);
    }
}
