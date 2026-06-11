package greenloop.model;

public class DeliveryAgent {
    private int    agentId;
    private String salutation;      
    private String fullName;
    private String nicNumber;
    private String dateOfBirth;
    private String email;
    private String phone;
    private String address;
    private String licenseNumber;
    private String dateOfJoining;
    private String vehicleNumber;
    private String vehicleType;
    private String vehicleMake;
    private String vehicleYear;
    private String vehicleColor;
    private String vehicleModel;
    private String status;
    private String remarks;
    private String photoPath;

    public DeliveryAgent() {}

    public int    getAgentId()       { return agentId; }
    public String getSalutation()    { return salutation != null ? salutation : "Mr."; }
    public String getFullName()      { return fullName; }
    public String getNicNumber()     { return nicNumber; }
    public String getDateOfBirth()   { return dateOfBirth; }
    public String getEmail()         { return email; }
    public String getPhone()         { return phone; }
    public String getAddress()       { return address; }
    public String getLicenseNumber() { return licenseNumber; }
    public String getDateOfJoining() { return dateOfJoining; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getVehicleType()   { return vehicleType; }
    public String getVehicleMake()   { return vehicleMake; }
    public String getVehicleYear()   { return vehicleYear; }
    public String getVehicleColor()  { return vehicleColor; }
    public String getVehicleModel()  { return vehicleModel; }
    public String getStatus()        { return status; }
    public String getRemarks()       { return remarks; }
    public String getPhotoPath()     { return photoPath; }

    public void setAgentId(int id)          { this.agentId       = id; }
    public void setSalutation(String s)     { this.salutation    = s; }
    public void setFullName(String n)       { this.fullName      = n; }
    public void setNicNumber(String n)      { this.nicNumber     = n; }
    public void setDateOfBirth(String d)    { this.dateOfBirth   = d; }
    public void setEmail(String e)          { this.email         = e; }
    public void setPhone(String p)          { this.phone         = p; }
    public void setAddress(String a)        { this.address       = a; }
    public void setLicenseNumber(String l)  { this.licenseNumber = l; }
    public void setDateOfJoining(String d)  { this.dateOfJoining = d; }
    public void setVehicleNumber(String v)  { this.vehicleNumber = v; }
    public void setVehicleType(String t)    { this.vehicleType   = t; }
    public void setVehicleMake(String m)    { this.vehicleMake   = m; }
    public void setVehicleYear(String y)    { this.vehicleYear   = y; }
    public void setVehicleColor(String c)   { this.vehicleColor  = c; }
    public void setVehicleModel(String m)   { this.vehicleModel  = m; }
    public void setStatus(String s)         { this.status        = s; }
    public void setRemarks(String r)        { this.remarks       = r; }
    public void setPhotoPath(String p)      { this.photoPath     = p; }

    @Override
    public String toString() {
        String sal = salutation != null ? salutation + " " : "";
        String vt  = vehicleType != null ? vehicleType : "–";
        return sal + fullName + " [" + vt + "]";
    }
}
