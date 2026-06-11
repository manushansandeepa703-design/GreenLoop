package greenloop.model;

public class GlUser {
    private int     userId;
    private String  username;
    private String  fullName;
    private String  email;
    private String  role;
    private boolean active;

    public GlUser() {}

    public int     getUserId()   { return userId; }
    public String  getUsername() { return username; }
    public String  getFullName() { return fullName; }
    public String  getEmail()    { return email; }
    public String  getRole()     { return role; }
    public boolean isActive()    { return active; }

    public void setUserId(int id)         { this.userId   = id; }
    public void setUsername(String u)     { this.username = u; }
    public void setFullName(String n)     { this.fullName = n; }
    public void setEmail(String e)        { this.email    = e; }
    public void setRole(String r)         { this.role     = r; }
    public void setActive(boolean a)      { this.active   = a; }
}
