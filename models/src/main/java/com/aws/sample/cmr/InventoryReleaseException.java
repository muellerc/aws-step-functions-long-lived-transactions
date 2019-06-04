package com.aws.sample.cmr;

public class InventoryReleaseException extends RuntimeException {

	private static final long serialVersionUID = 7695197590980362632L;

    public InventoryReleaseException() {
    }

    public InventoryReleaseException(String message) {
        super(message);
    }

    public InventoryReleaseException(String message, Exception exception) {
        super(message, exception);
    }
}
