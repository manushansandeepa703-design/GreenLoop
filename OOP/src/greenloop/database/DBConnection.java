package greenloop.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {

    private static final String DB_URL  = "#Your Database Url";
    private static final String USER     = "#Username";
    private static final String PASSWORD = "#Password"; 

    private static Connection connection = null;

    private DBConnection() {}

    public static synchronized Connection getConnection() {
        try {
            boolean needsReconnect = false;
            try {
                needsReconnect = (connection == null || connection.isClosed() || !connection.isValid(2));
            } catch (SQLException ignored) {
                needsReconnect = true;
            }
            if (needsReconnect) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("DB Connection error: " + e.getMessage());
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
