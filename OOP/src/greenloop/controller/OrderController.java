package greenloop.controller;

import greenloop.database.DBConnection;
import greenloop.model.Order;
import greenloop.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderController {

    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, c.business_name FROM orders o " +
                     "JOIN clients c ON o.client_id = c.client_id ORDER BY o.order_id DESC";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapOrder(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Order> getRecentOrders(int limit) {
        List<Order> list = new ArrayList<>();
        
        String sql = "SELECT o.*, c.business_name FROM orders o " +
                     "JOIN clients c ON o.client_id = c.client_id ORDER BY o.order_id DESC LIMIT ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapOrder(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }


    public int saveOrder(Order order, List<OrderItem> items) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return -1;
        try {
            conn.setAutoCommit(false);

            int orderId = -1;

            
            String sql = "INSERT INTO orders (client_id,order_date,total_amount,status,notes) VALUES (?,NOW(),?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, order.getClientId());
                ps.setDouble(2, order.getTotalAmount());
                ps.setString(3, "Pending");
                ps.setString(4, order.getNotes());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) orderId = keys.getInt(1);
                }
            }

            if (orderId < 0) { conn.rollback(); conn.setAutoCommit(true); return -1; }

            for (OrderItem item : items) {
                
                String iSql = "INSERT INTO order_items (order_id,product_id,quantity,unit_price) VALUES (?,?,?,?)";
                try (PreparedStatement ip = conn.prepareStatement(iSql)) {
                    ip.setInt(1, orderId);
                    ip.setInt(2, item.getProductId());
                    ip.setInt(3, item.getQuantity());
                    ip.setDouble(4, item.getUnitPrice());
                    ip.executeUpdate();
                }

                
                String checkSql = "SELECT quantity_on_hand FROM stock WHERE product_id=?";
                try (PreparedStatement cp = conn.prepareStatement(checkSql)) {
                    cp.setInt(1, item.getProductId());
                    try (ResultSet cr = cp.executeQuery()) {
                        if (cr.next() && cr.getInt(1) < item.getQuantity()) {
                            conn.rollback();
                            conn.setAutoCommit(true);
                            return -2; 
                        }
                    }
                }

                
                String stSql = "UPDATE stock SET quantity_on_hand = quantity_on_hand - ? WHERE product_id=?";
                try (PreparedStatement sp = conn.prepareStatement(stSql)) {
                    sp.setInt(1, item.getQuantity());
                    sp.setInt(2, item.getProductId());
                    sp.executeUpdate();
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
            return orderId;

        } catch (SQLException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        }
        return -1;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status=? WHERE order_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT oi.*, p.product_name FROM order_items oi " +
                     "JOIN products p ON oi.product_id=p.product_id WHERE oi.order_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, orderId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setOrderId(orderId);
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    list.add(item);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int getPendingOrderCount() {
        String sql = "SELECT COUNT(*) FROM orders WHERE status IN ('Pending','Processing')";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public double getMonthlyRevenue(int month, int year) {
        String sql = "SELECT COALESCE(SUM(total_amount),0) FROM orders " +
                     "WHERE MONTH(order_date)=? AND YEAR(order_date)=? AND status='Delivered'";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, month); ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getMonthlyOrderCount(int month, int year) {
        String sql = "SELECT COUNT(*) FROM orders WHERE MONTH(order_date)=? AND YEAR(order_date)=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, month); ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getMonthlyItemsSold(int month, int year) {
        String sql = "SELECT COALESCE(SUM(oi.quantity),0) FROM order_items oi " +
                     "JOIN orders o ON oi.order_id=o.order_id " +
                     "WHERE MONTH(o.order_date)=? AND YEAR(o.order_date)=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, month); ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setClientId(rs.getInt("client_id"));
        o.setClientName(rs.getString("business_name"));
        o.setOrderDate(rs.getString("order_date"));
        o.setTotalAmount(rs.getDouble("total_amount"));
        o.setStatus(rs.getString("status"));
        o.setNotes(rs.getString("notes"));
        return o;
    }
}
