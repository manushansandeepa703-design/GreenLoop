package greenloop.controller;

import greenloop.database.DBConnection;
import greenloop.model.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryController {

    
    public List<Order> getPendingOrders() {
        List<Order> list = new ArrayList<>();
        
        String sql = "SELECT o.*, c.business_name FROM orders o " +
                     "JOIN clients c ON o.client_id=c.client_id " +
                     "WHERE o.status IN ('Pending','Processing') " +
                     "ORDER BY o.order_id DESC";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Order o = new Order();
                o.setOrderId(rs.getInt("order_id"));
                o.setClientId(rs.getInt("client_id"));
                o.setClientName(rs.getString("business_name"));
                o.setOrderDate(rs.getString("order_date"));
                o.setTotalAmount(rs.getDouble("total_amount"));
                o.setStatus(rs.getString("status"));
                list.add(o);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    
    public boolean isOrderAssigned(int orderId) {
        String sql = "SELECT COUNT(*) FROM order_deliveries WHERE order_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    
    public boolean assignDelivery(int orderId, int agentId) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;
        try {
            conn.setAutoCommit(false);

            
            String dSql = "INSERT INTO order_deliveries (order_id, agent_id, delivery_status) " +
                          "VALUES (?, ?, 'Assigned') " +
                          "ON DUPLICATE KEY UPDATE agent_id=VALUES(agent_id), delivery_status='Assigned'";
            try (PreparedStatement dp = conn.prepareStatement(dSql)) {
                dp.setInt(1, orderId);
                dp.setInt(2, agentId);
                dp.executeUpdate();
            }

           
            try (PreparedStatement op = conn.prepareStatement(
                     "UPDATE orders SET status='Processing' WHERE order_id=?")) {
                op.setInt(1, orderId);
                op.executeUpdate();
            }

            
            try (PreparedStatement ap = conn.prepareStatement(
                     "UPDATE delivery_agents SET status='On Delivery' WHERE agent_id=?")) {
                ap.setInt(1, agentId);
                ap.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    
    public boolean updateDeliveryStatus(int orderId, String deliveryStatus) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;
        try {
            conn.setAutoCommit(false);

            
            String orderStatus = "Dispatched";
            if ("Delivered".equals(deliveryStatus)) orderStatus = "Delivered";

            try (PreparedStatement dp = conn.prepareStatement(
                     "UPDATE order_deliveries SET delivery_status=? WHERE order_id=?")) {
                dp.setString(1, deliveryStatus);
                dp.setInt(2, orderId);
                dp.executeUpdate();
            }

            try (PreparedStatement op = conn.prepareStatement(
                     "UPDATE orders SET status=? WHERE order_id=?")) {
                op.setString(1, orderStatus);
                op.setInt(2, orderId);
                op.executeUpdate();
            }

            
            if ("Delivered".equals(deliveryStatus)) {
                String freeSql = "UPDATE delivery_agents da " +
                                 "JOIN order_deliveries od ON da.agent_id=od.agent_id " +
                                 "SET da.status='Available' WHERE od.order_id=?";
                try (PreparedStatement ap = conn.prepareStatement(freeSql)) {
                    ap.setInt(1, orderId);
                    ap.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    
    public String getAssignedAgentName(int orderId) {
        String sql = "SELECT da.full_name FROM order_deliveries od " +
                     "JOIN delivery_agents da ON od.agent_id=da.agent_id WHERE od.order_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("full_name");
        } catch (SQLException e) { e.printStackTrace(); }
        return "Not Assigned";
    }

    
    public String getDeliveryStatus(int orderId) {
        String sql = "SELECT delivery_status FROM order_deliveries WHERE order_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("delivery_status");
        } catch (SQLException e) { e.printStackTrace(); }
        return "Not Assigned";
    }
}
