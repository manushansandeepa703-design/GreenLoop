package greenloop.model;

public class OrderItem {
    private int    itemId;
    private int    orderId;
    private int    productId;
    private String productName;
    private int    quantity;
    private double unitPrice;

    public OrderItem() {}

    public OrderItem(int productId, String productName, int quantity, double unitPrice) {
        this.productId   = productId;
        this.productName = productName;
        this.quantity    = quantity;
        this.unitPrice   = unitPrice;
    }

    public int    getItemId()      { return itemId; }
    public int    getOrderId()     { return orderId; }
    public int    getProductId()   { return productId; }
    public String getProductName() { return productName; }
    public int    getQuantity()    { return quantity; }
    public double getUnitPrice()   { return unitPrice; }
    public double getTotal()       { return quantity * unitPrice; }

    public void setItemId(int id)        { this.itemId      = id; }
    public void setOrderId(int id)       { this.orderId     = id; }
    public void setProductId(int id)     { this.productId   = id; }
    public void setProductName(String n) { this.productName = n; }
    public void setQuantity(int q)       { this.quantity    = q; }
    public void setUnitPrice(double p)   { this.unitPrice   = p; }
}
