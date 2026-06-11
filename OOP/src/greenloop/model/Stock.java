package greenloop.model;

public class Stock {
    private int    stockId;
    private int    productId;
    private String productName;
    private String category;
    private int    quantityOnHand;
    private int    reorderLevel;
    private String supplierName;
    private String lastRestocked;
    private String status;

    public Stock() {}

    public int    getStockId()        { return stockId; }
    public int    getProductId()      { return productId; }
    public String getProductName()    { return productName; }
    public String getCategory()       { return category; }
    public int    getQuantityOnHand() { return quantityOnHand; }
    public int    getReorderLevel()   { return reorderLevel; }
    public String getSupplierName()   { return supplierName != null ? supplierName : ""; }
    public String getLastRestocked()  { return lastRestocked != null ? lastRestocked : ""; }
    public String getStatus()         { return status; }

    public void setStockId(int id)         { this.stockId        = id; }
    public void setProductId(int id)       { this.productId      = id; }
    public void setProductName(String n)   { this.productName    = n; }
    public void setCategory(String c)      { this.category       = c; }
    public void setQuantityOnHand(int q)   { this.quantityOnHand = q; }
    public void setReorderLevel(int r)     { this.reorderLevel   = r; }
    public void setSupplierName(String s)  { this.supplierName   = s; }
    public void setLastRestocked(String d) { this.lastRestocked  = d; }
    public void setStatus(String s)        { this.status         = s; }
}
