package greenloop.controller;

import greenloop.database.DBConnection;
import greenloop.model.GlUser;

import java.security.MessageDigest;
import java.sql.*;

public class AuthController {

    private static GlUser loggedInUser = null;

    public static GlUser login(String username, String password) {
        String hash = sha256(password);
        String sql  = "SELECT * FROM gl_users WHERE username=? AND password_hash=? AND active=1";
        Connection conn = DBConnection.getConnection();
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                GlUser user = new GlUser();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("active"));
                loggedInUser = user;
                return user;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static GlUser getLoggedInUser() { return loggedInUser; }

    public static void logout() { loggedInUser = null; }

    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { e.printStackTrace(); }
        return "";
    }
}
