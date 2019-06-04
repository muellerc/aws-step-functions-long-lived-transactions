package com.aws.sample.cmr;

import java.math.BigDecimal;
import java.util.UUID;

import org.joda.time.DateTime;

public class Payment {

    private String merchantId;
    private BigDecimal paymentAmount;
    private String transactionId;
    private String transactionDate;
    private String orderId;
    private String paymentType;

    public void pay() {
        setTransactionId(UUID.randomUUID().toString());
        setTransactionDate(new DateTime().toString()); // ISO 8601 / RFC 3339 string format
        setPaymentType("Debit");
    }

    public void refund() {
        setTransactionId(UUID.randomUUID().toString());
        setTransactionDate(new DateTime().toString()); // ISO 8601 / RFC 3339 string format
        setPaymentAmount(new BigDecimal(-1).multiply(getPaymentAmount()));
        setPaymentType("Credit");
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
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

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
}