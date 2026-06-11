package greenloop.model;

public class Product {
    private int    productId;
    private String productName;
    private String category;
    private String description;
    private double price;
    private int    ecoRating;
    private int    stock;
    private String status;

    public Product() {}

    public int    getProductId()   { return productId; }
    public String getProductName() { return productName; }
    public String getCategory()    { return category; }
    public String getDescription() { return description; }
    public double getPrice()       { return price; }
    public int    getEcoRating()   { return ecoRating; }
    public int    getStock()       { return stock; }
    public String getStatus()      { return status != null ? status : "Active"; }

    public void setProductId(int id)       { this.productId   = id; }
    public void setProductName(String n)   { this.productName = n; }
    public void setCategory(String c)      { this.category    = c; }
    public void setDescription(String d)   { this.description = d; }
    public void setPrice(double p)         { this.price       = p; }
    public void setEcoRating(int r)        { this.ecoRating   = r; }
    public void setStock(int s)            { this.stock       = s; }
    public void setStatus(String s)        { this.status      = s; }

    public double getUnitPrice()           { return price; }
    public void   setUnitPrice(double p)   { this.price = p; }

    @Override public String toString() { return productName; }
}
