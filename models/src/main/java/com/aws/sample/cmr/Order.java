package com.aws.sample.cmr;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private String orderId;
    private String orderDate;
    private String customerId;
    private String orderStatus;
    private List<Item> items;
    private Payment payment;
    private Inventory inventory;

    public BigDecimal total() {
        BigDecimal total = new BigDecimal(0).setScale(2);

        for (Item item : items) {
            total = total.add(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
        }

        return total;
    }

    public List<String> getItemIds() {
        List<String> itemIds = new ArrayList<>();

        if (items != null) {
            for (Item item : items) {
                itemIds.add(item.getItemId());
            }
        }

        return itemIds;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void addItem(Item item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}