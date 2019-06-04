package com.aws.sample.cmr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;

public class OrderCreation implements RequestHandler<Order, Order> {

    private AmazonDynamoDB dynamoDb = AmazonDynamoDBClientBuilder
        .standard()
        .withRegion(System.getenv("AWS_REGION"))
        .withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder()))
        .build();

    public Order handleRequest(Order order, Context context) {
        System.out.println(String.format("[%s] - process order creation...", order.getOrderId()));

        order.setOrderStatus("New");

        try {
            saveOrder(order, context);
        } catch (Exception e) {
            System.out.println(String.format("[%s] - Unable to process order creation: [%s]", order.getOrderId(), e.getMessage()));
            throw new OrderCreationException(e.getMessage(), e);
        }

        if (order.getOrderId().startsWith("1")) {
            throw new OrderCreationException(String.format("[%s] - Unable to create order (test case 1)", order.getOrderId()));
        }

        System.out.println(String.format("[%s] - order creation processed", order.getOrderId()));
        return order;
    }

    private void saveOrder(Order order, Context context) throws Exception {
        List<AttributeValue> orderItems = new ArrayList<>();
        if (order.getItems() != null) {
            for (Item orderItem : order.getItems()) {
                AttributeValue i = new AttributeValue()
                    .addMEntry("item_id", new AttributeValue(orderItem.getItemId()))
                    .addMEntry("quantity", new AttributeValue().withN(orderItem.getQuantity().toString()))
                    .addMEntry("description", new AttributeValue(orderItem.getDescription()))
                    .addMEntry("unit_price", new AttributeValue().withN(orderItem.getUnitPrice().setScale(2).toPlainString()));

                orderItems.add(i);
            }
        }

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("order_id", new AttributeValue(order.getOrderId()));
        item.put("order_date", new AttributeValue(order.getOrderDate().toString()));
        item.put("customer_id", new AttributeValue(order.getCustomerId()));
        item.put("order_status", new AttributeValue(order.getOrderStatus()));
        item.put("items", new AttributeValue().withL(orderItems));

        PutItemResult putItemResult = dynamoDb.putItem(
            new PutItemRequest()
                .withTableName(System.getenv("TABLE_NAME"))
                .withItem(item));
    }
}
