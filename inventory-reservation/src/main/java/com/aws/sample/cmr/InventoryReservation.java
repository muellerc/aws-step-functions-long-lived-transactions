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

public class InventoryReservation implements RequestHandler<Order, Order> {

    private AmazonDynamoDB dynamoDb = AmazonDynamoDBClientBuilder
        .standard()
        .withRegion(System.getenv("AWS_REGION"))
        .withRequestHandlers(new TracingHandler())
        .build();

    public Order handleRequest(Order order, Context context) {
        System.out.println(String.format("[%s] - processing inventory reservation...", order.getOrderId()));

        Inventory inventory = new Inventory();
        inventory.setOrderId(order.getOrderId());
        inventory.setOrderItems(order.getItemIds());
        inventory.reserve(); // reserve the items in the inventory
        order.setInventory(inventory); // annotate saga with inventory transaction id

        try {
            saveInventory(inventory, context);
        } catch (Exception e) {
            System.out.println(String.format("[%s] - Unable to process inventory reservation: [%s]", order.getOrderId(), e.getMessage()));
            throw new InventoryReservationException(e.getMessage(), e);
        }

        if (order.getOrderId().startsWith("3")) {
            throw new InventoryReservationException(String.format("[%s] - Unable to reserve inventory (test case 5)", order.getOrderId()));
        }

        System.out.println(String.format("[%s] - inventory reservation processed", order.getOrderId()));
        return order;
    }

    private void saveInventory(Inventory inventory, Context context) throws Exception {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("transaction_id", new AttributeValue(inventory.getTransactionId()));
        item.put("order_id", new AttributeValue(inventory.getOrderId()));
        item.put("transaction_type", new AttributeValue(inventory.getTransactionType()));
        item.put("order_items", new AttributeValue(inventory.getOrderItems()));
        item.put("transaction_date", new AttributeValue(inventory.getTransactionDate()));

        PutItemResult putItemResult = dynamoDb.putItem(
            new PutItemRequest()
                .withTableName(System.getenv("TABLE_NAME"))
                .withItem(item));
    }
}
