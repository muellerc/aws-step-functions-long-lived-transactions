package com.aws.sample.cmr;

public class InventoryReservationException extends RuntimeException {

    private static final long serialVersionUID = -860560491439691311L;

    public InventoryReservationException() {
    }

    public InventoryReservationException(String message) {
        super(message);
    }

    public InventoryReservationException(String message, Exception exception) {
        super(message, exception);
    }
}
