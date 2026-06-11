package greenloop.model;

public class Client {
    private int    clientId;
    private String businessName;
    private String salutation;      
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String status;

    public Client() {}

    public int    getClientId()      { return clientId; }
    public String getBusinessName()  { return businessName; }
    public String getSalutation()    { return salutation != null ? salutation : "Mr."; }
    public String getContactPerson() { return contactPerson; }
    public String getEmail()         { return email; }
    public String getPhone()         { return phone; }
    public String getAddress()       { return address; }
    public String getStatus()        { return status; }

    public void setClientId(int id)         { this.clientId      = id; }
    public void setBusinessName(String n)   { this.businessName  = n; }
    public void setSalutation(String s)     { this.salutation    = s; }
    public void setContactPerson(String cp) { this.contactPerson = cp; }
    public void setEmail(String e)          { this.email         = e; }
    public void setPhone(String p)          { this.phone         = p; }
    public void setAddress(String a)        { this.address       = a; }
    public void setStatus(String s)         { this.status        = s; }

    @Override public String toString() { return businessName; }
}
