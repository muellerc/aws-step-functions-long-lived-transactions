package com.aws.sample.cmr;

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

public class InventoryRelease implements RequestHandler<Order, Order> {

    private AmazonDynamoDB dynamoDb = AmazonDynamoDBClientBuilder
        .standard()
        .withRegion(System.getenv("AWS_REGION"))
        .withRequestHandlers(new TracingHandler())
        .build();

    public Order handleRequest(Order order, Context context) {
        System.out.println(String.format("[%s] - processing inventory release...", order.getOrderId()));

        try {
            Inventory inventory = findInventory(order.getOrderId(), context);
            inventory.release();

            saveTransaction(inventory, context);
            order.setInventory(inventory);
        } catch (Exception e) {
            System.out.println(String.format("[%s] - Unable to process inventory release: [%s]", order.getOrderId(), e.getMessage()));
            throw new InventoryReleaseException(e.getMessage(), e);
        }

        if (order.getOrderId().startsWith("33")) {
            throw new InventoryReleaseException(String.format("[%s] - Unable to release inventory (test case 6)", order.getOrderId()));
        }

        System.out.println(String.format("[%s] - inventory release processed", order.getOrderId()));
        return order;
    }

    private Inventory findInventory(String orderId, Context context) {
        QueryResult queryResult = dynamoDb.query(
            new QueryRequest()
                .withTableName(System.getenv("TABLE_NAME"))
                .withIndexName("orderIDIndex")
                .withKeyConditionExpression("order_id = :v1 AND transaction_type = :v2")
                .addExpressionAttributeValuesEntry(":v1", new AttributeValue(orderId))
                .addExpressionAttributeValuesEntry(":v2", new AttributeValue("Reserve")));

        if (queryResult.getCount() < 1) {
            throw new InventoryReleaseException(String.format("Couldn't find inventory with order_id='%s' and transaction_type='%s'", orderId, "Reserve"));
        }

        if (queryResult.getCount() > 1) {
            throw new InventoryReleaseException(String.format("Found none unique inventory with order_id='%s' and transaction_type='%s'", orderId, "Reserve"));
        }

        Map<String, AttributeValue> item = queryResult.getItems().get(0);

        Inventory inventory = new Inventory();
        inventory.setTransactionId(item.get("transaction_id").getS());
        inventory.setTransactionDate(item.get("transaction_date").getS());
        inventory.setTransactionType(item.get("transaction_type").getS());
        inventory.setOrderId(item.get("order_id").getS());
        inventory.setOrderItems(item.get("order_items").getSS());
        return inventory;
    }

    private void saveTransaction(Inventory inventory, Context context) throws Exception {
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
