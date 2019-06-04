package com.aws.sample.cmr;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.xray.handlers.TracingHandler;

public class PaymentProcessing implements RequestHandler<Order, Order> {

    private AmazonDynamoDB dynamoDb = AmazonDynamoDBClientBuilder
        .standard()
        .withRegion(System.getenv("AWS_REGION"))
        .withRequestHandlers(new TracingHandler())
        .build();

    public Order handleRequest(Order order, Context context) {
        System.out.println(String.format("[%s] - process payment...", order.getOrderId()));

        Payment payment = new Payment();
        payment.setOrderId(order.getOrderId());
        payment.setMerchantId("merch1");
        payment.setPaymentAmount(order.total());
        payment.pay();

        try {
            savePayment(payment, context);
            order.setPayment(payment);
        } catch (Exception e) {
            System.out.println(String.format("[%s] - Unable to process payment: [%s]", order.getOrderId(), e.getMessage()));
            throw new PaymentProcessException(e.getMessage(), e);
        }

        if (order.getOrderId().startsWith("2")) {
            throw new PaymentProcessException(String.format("[%s] - Unable to process payment (test case 3)", order.getOrderId()));
        }

        System.out.println(String.format("[%s] - payment processed", order.getOrderId()));
        return order;
    }

    private void savePayment(Payment payment, Context context) throws Exception {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("merchant_id", new AttributeValue(payment.getMerchantId()));
        item.put("payment_amount", new AttributeValue(payment.getPaymentAmount().toPlainString()));
        item.put("transaction_id", new AttributeValue(payment.getTransactionId()));
        item.put("transaction_date", new AttributeValue(payment.getTransactionDate()));
        item.put("order_id", new AttributeValue(payment.getOrderId()));
        item.put("payment_type", new AttributeValue(payment.getPaymentType()));

        PutItemResult putItemResult = dynamoDb.putItem(
            new PutItemRequest()
                .withTableName(System.getenv("TABLE_NAME"))
                .withItem(item));
    }
}
