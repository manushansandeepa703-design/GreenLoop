package greenloop.model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private int    orderId;
    private int    clientId;
    private String clientName;
    private String orderDate;
    private double totalAmount;
    private String status;
    private String notes;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public int    getOrderId()     { return orderId; }
    public int    getClientId()    { return clientId; }
    public String getClientName()  { return clientName; }
    public String getOrderDate()   { return orderDate; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus()      { return status; }
    public String getNotes()       { return notes; }
    public List<OrderItem> getItems() { return items; }

    public void setOrderId(int id)         { this.orderId     = id; }
    public void setClientId(int id)        { this.clientId    = id; }
    public void setClientName(String n)    { this.clientName  = n; }
    public void setOrderDate(String d)     { this.orderDate   = d; }
    public void setTotalAmount(double a)   { this.totalAmount = a; }
    public void setStatus(String s)        { this.status      = s; }
    public void setNotes(String n)         { this.notes       = n; }
    public void setItems(List<OrderItem> i){ this.items       = i; }

    @Override public String toString() { return "ORD-" + orderId; }
}
