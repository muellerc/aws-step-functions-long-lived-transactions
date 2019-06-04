package com.aws.sample.cmr;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

public class Inventory {

    private String transactionId;
    private String transactionDate;
    private String orderId;
    private List<String> orderItems;
    private String transactionType;

    public void reserve() {
        setTransactionId(UUID.randomUUID().toString());
        setTransactionDate(new DateTime().toString()); // ISO 8601 / RFC 3339 string format
        setTransactionType("Reserve");
    }

    public void release() {
        setTransactionId(UUID.randomUUID().toString());
        setTransactionDate(new DateTime().toString()); // ISO 8601 / RFC 3339 string format
        setTransactionType("Release");
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<String> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<String> orderItems) {
        this.orderItems = orderItems;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
}
