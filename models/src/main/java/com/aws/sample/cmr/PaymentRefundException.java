package com.aws.sample.cmr;

public class PaymentRefundException extends RuntimeException {

    private static final long serialVersionUID = 782058119377938670L;

    public PaymentRefundException() {
    }

    public PaymentRefundException(String message) {
        super(message);
    }

    public PaymentRefundException(String message, Exception exception) {
        super(message, exception);
    }
}
