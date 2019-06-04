package com.aws.sample.cmr;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.xray.handlers.TracingHandler;

public class PaymentRefund implements RequestHandler<Order, Order> {

    private AmazonDynamoDB dynamoDb = AmazonDynamoDBClientBuilder
        .standard()
        .withRegion(System.getenv("AWS_REGION"))
        .withRequestHandlers(new TracingHandler())
        .build();

    public Order handleRequest(Order order, Context context) {
        System.out.println(String.format("[%s] - process refund...", order.getOrderId()));

        try {
            Payment payment = findPayment(order.getOrderId(), context);
            payment.refund();
            savePayment(payment, context);
            order.setPayment(payment);
        } catch (Exception e) {
            System.out.println(String.format("[%s] - Unable to process refund: [%s]", order.getOrderId(), e.getMessage()));
            throw new PaymentRefundException(e.getMessage(), e);
        }

        if (order.getOrderId().startsWith("22")) {
            throw new PaymentRefundException(String.format("[%s] - Unable to process refund (test case 4)", order.getOrderId()));
        }

        System.out.println(String.format("[%s] - refund processed", order.getOrderId()));
        return order;
    }

    private Payment findPayment(String orderId, Context context) {
        QueryResult queryResult = dynamoDb.query(
            new QueryRequest()
                .withTableName(System.getenv("TABLE_NAME"))
                .withIndexName("orderIDIndex")
                .withKeyConditionExpression("order_id = :v1 AND payment_type = :v2")
                .addExpressionAttributeValuesEntry(":v1", new AttributeValue(orderId))
                .addExpressionAttributeValuesEntry(":v2", new AttributeValue("Debit")));

        if (queryResult.getCount() < 1) {
            throw new PaymentRefundException(String.format("Couldn't find payment with order_id='%s' and payment_type='%s'", orderId, "Debit"));
        }

        if (queryResult.getCount() > 1) {
            throw new PaymentRefundException(String.format("Found none unique payment with order_id='%s' and payment_type='%s'", orderId, "Debit"));
        }

        Map<String, AttributeValue> item = queryResult.getItems().get(0);

        Payment payment = new Payment();
        payment.setMerchantId(item.get("merchant_id").getS());
        payment.setPaymentAmount(new BigDecimal(item.get("payment_amount").getS()));
        payment.setTransactionId(item.get("transaction_id").getS());
        payment.setOrderId(item.get("order_id").getS());
        payment.setTransactionDate(item.get("transaction_date").getS());
        payment.setPaymentType(item.get("payment_type").getS());
        return payment;
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
