package greenloop.model;

public class EmailLog {
    private int    logId;
    private String recipientEmail;
    private String subject;
    private String sentAt;
    private String status;
    private String type;

    public EmailLog() {}

    public int    getLogId()          { return logId; }
    public String getRecipientEmail() { return recipientEmail; }
    public String getSubject()        { return subject; }
    public String getSentAt()         { return sentAt; }
    public String getStatus()         { return status; }
    public String getType()           { return type; }

    public void setLogId(int id)            { this.logId         = id; }
    public void setRecipientEmail(String e) { this.recipientEmail= e; }
    public void setSubject(String s)        { this.subject       = s; }
    public void setSentAt(String t)         { this.sentAt        = t; }
    public void setStatus(String s)         { this.status        = s; }
    public void setType(String t)           { this.type          = t; }
}
