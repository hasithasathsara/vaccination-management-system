package com.moh.vaxtrack.service;


public class InsufficientStockException extends RuntimeException {

    private final long available;

    public InsufficientStockException(long available) {
        super("Insufficient national stock: only " + available + " available");
        this.available = available;
    }

    public long getAvailable() {
        return available;
    }
}
