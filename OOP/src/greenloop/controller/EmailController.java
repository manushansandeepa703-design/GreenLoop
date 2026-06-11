package greenloop.controller;

import greenloop.database.DBConnection;
import greenloop.model.EmailLog;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class EmailController {

    
    private final String smtpHost;
    private final String smtpPort;
    private final String smtpUser;
    private final String smtpPass;
    private final String fromAddress;
    private final String fromName;
    private final boolean useTLS;
    private final boolean useSSL;
    private final String connTimeout;
    private final String readTimeout;

    
    public EmailController() {
        Properties cfg = loadConfig();
        smtpHost    = cfg.getProperty("mail.smtp.host",                "localhost");
        smtpPort    = cfg.getProperty("mail.smtp.port",                "587");
        smtpUser    = cfg.getProperty("mail.smtp.username",            "");
        smtpPass    = cfg.getProperty("mail.smtp.password",            "");
        fromAddress = cfg.getProperty("mail.from.address",             smtpUser);
        fromName    = cfg.getProperty("mail.from.name",                "GreenLoop");
        useTLS      = Boolean.parseBoolean(cfg.getProperty("mail.smtp.starttls.enable", "true"));
        useSSL      = Boolean.parseBoolean(cfg.getProperty("mail.smtp.ssl.enable",      "false"));
        connTimeout = cfg.getProperty("mail.smtp.connectiontimeout",   "10000");
        readTimeout = cfg.getProperty("mail.smtp.timeout",             "10000");
    }

    
    private Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream in = getClass()
                .getResourceAsStream("/greenloop/resources/email.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                System.err.println("[EmailController] WARNING: email.properties not found in classpath.");
                System.err.println("  → Create: src/greenloop/resources/email.properties");
            }
        } catch (IOException e) {
            System.err.println("[EmailController] Failed to load email.properties: " + e.getMessage());
        }
        return props;
    }

    
    public boolean sendEmail(String toEmail, String subject, String body) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            logToDb("(unknown)", subject, "Failed");
            return false;
        }

        if (smtpHost.isEmpty() || smtpUser.isEmpty() || smtpPass.isEmpty()) {
            System.err.println("[EmailController] Mail server not configured. Edit email.properties.");
            logToDb(toEmail, subject, "Failed");
            return false;
        }

        String status = "Failed";

        try {
            Properties props = new Properties();
            props.put("mail.smtp.host",                smtpHost);
            props.put("mail.smtp.port",                smtpPort);
            props.put("mail.smtp.auth",                "true");
            props.put("mail.smtp.starttls.enable",     String.valueOf(useTLS));
            props.put("mail.smtp.ssl.enable",          String.valueOf(useSSL));
            props.put("mail.smtp.connectiontimeout",   connTimeout);
            props.put("mail.smtp.timeout",             readTimeout);

            
            if (useSSL) {
                props.put("mail.smtp.socketFactory.port",   smtpPort);
                props.put("mail.smtp.socketFactory.class",  "javax.net.ssl.SSLSocketFactory");
            }

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUser, smtpPass);
                }
            });

             
            session.setDebug(true);

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromAddress, fromName));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail.trim()));
            msg.setSubject(subject);

            
            msg.setContent(
                "<html><body style='font-family:Arial,sans-serif;font-size:14px;'>"
                + body.replace("\n", "<br>")
                + "<br><br><hr style='border:none;border-top:1px solid #ccc'>"
                + "<small style='color:#888'>This is an automated message from GreenLoop System.</small>"
                + "</body></html>",
                "text/html; charset=utf-8"
            );

            Transport.send(msg);
            status = "Sent";
            System.out.println("[EmailController] Email sent to: " + toEmail);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            System.err.println("[EmailController] Send failed: " + e.getMessage());
            status = "Failed";
        }

        logToDb(toEmail, subject, status);
        return "Sent".equals(status);
    }

    
    public List<EmailLog> getAllEmails() {
        List<EmailLog> list = new ArrayList<>();
        String sql = "SELECT * FROM email_log ORDER BY sent_at DESC";
        Connection conn = DBConnection.getConnection();
        if (conn == null) return list;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapLog(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<EmailLog> getEmailsByType(String type) {
        List<EmailLog> list = new ArrayList<>();
        String likeVal = "Client".equals(type) ? "Order%" : "New Delivery%";
        String sql = "SELECT * FROM email_log WHERE subject LIKE ? ORDER BY sent_at DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, likeVal);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapLog(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private void logToDb(String toEmail, String subject, String status) {
        String sql = "INSERT INTO email_log (recipient_email, subject, status) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, toEmail);
            ps.setString(2, subject);
            ps.setString(3, status);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private EmailLog mapLog(ResultSet rs) throws SQLException {
        EmailLog log = new EmailLog();
        log.setLogId(rs.getInt("log_id"));
        log.setRecipientEmail(rs.getString("recipient_email"));
        log.setSubject(rs.getString("subject"));
        log.setSentAt(rs.getString("sent_at"));
        log.setStatus(rs.getString("status"));
        String subj = rs.getString("subject");
        log.setType(subj != null && subj.startsWith("New Delivery") ? "Agent" : "Client");
        return log;
    }
}
