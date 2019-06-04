package com.aws.sample.cmr;

public class PaymentProcessException extends RuntimeException {

    private static final long serialVersionUID = -8719086531265751262L;

    public PaymentProcessException() {
    }

    public PaymentProcessException(String message) {
        super(message);
    }

    public PaymentProcessException(String message, Exception exception) {
        super(message, exception);
    }
}
