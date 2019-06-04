package com.aws.sample.cmr;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.xray.handlers.TracingHandler;

public class OrderCancelation implements RequestHandler<Order, Order> {

    private AmazonDynamoDB dynamoDb = AmazonDynamoDBClientBuilder
        .standard()
        .withRegion(System.getenv("AWS_REGION"))
        .withRequestHandlers(new TracingHandler())
        .build();

    public Order handleRequest(Order order, Context context) {
        System.out.println(String.format("[%s] - process order cancelation...", order.getOrderId()));

        try {
            order = getOrder(order.getOrderId(), context);
            order.setOrderStatus("Canceled");

            saveOrder(order, context);
        } catch (Exception e) {
            System.out.println(String.format("[%s] - Unable to process order cancelation: [%s]", order.getOrderId(), e.getMessage()));
            throw new OrderCancelationException(e.getMessage(), e);
        }

        if (order.getOrderId().startsWith("11")) {
            throw new OrderCancelationException(String.format("[%s] - Unable to cancel order (test case 2)", order.getOrderId()));
        }

        System.out.println(String.format("[%s] - order cancelation processed", order.getOrderId()));
        return order;
    }

    private Order getOrder(String orderId, Context context) throws Exception {
        GetItemResult getItemResult = dynamoDb.getItem(
            new GetItemRequest()
                .withTableName(System.getenv("TABLE_NAME"))
                .addKeyEntry("order_id", new AttributeValue(orderId)));

        Map<String, AttributeValue> item = getItemResult.getItem();
        if (item == null || item.isEmpty()) {
            throw new OrderCancelationException(String.format("Couldn't find order with order_id='%s'", orderId));
        }

        Order order = new Order();
        order.setOrderId(item.get("order_id").getS());
        order.setOrderDate(item.get("order_date").getS());
        order.setCustomerId(item.get("customer_id").getS());
        order.setOrderStatus(item.get("order_status").getS());

        List<Item> orderItems = new ArrayList<>();
        AttributeValue itemsAttributeValue = item.get("items");
        if (itemsAttributeValue != null) {
            List<AttributeValue> itemAttributeValue = itemsAttributeValue.getL();
            if (itemAttributeValue != null && !itemAttributeValue.isEmpty()) {
                for (AttributeValue attributeValue : itemAttributeValue) {
                    Map<String, AttributeValue> m = attributeValue.getM();
                    Item i = new Item();
                    i.setItemId(m.get("item_id").getS());
                    i.setQuantity(Long.valueOf(m.get("quantity").getN()));
                    i.setDescription(m.get("description").getS());
                    i.setUnitPrice(new BigDecimal(m.get("unit_price").getN()));

                    orderItems.add(i);
                }

                order.setItems(orderItems);
            }
        }

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