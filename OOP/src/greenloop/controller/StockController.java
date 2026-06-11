package greenloop.controller;

import greenloop.database.DBConnection;
import greenloop.model.Stock;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockController {

    public List<Stock> getAllStock() {
        List<Stock> list = new ArrayList<>();
        String sql = "SELECT s.*, p.product_name, p.category FROM stock s JOIN products p ON s.product_id=p.product_id ORDER BY p.product_id";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapStock(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Stock> getLowStockItems() {
        List<Stock> list = new ArrayList<>();
        String sql = "SELECT s.*, p.product_name, p.category FROM stock s JOIN products p ON s.product_id=p.product_id " +
                     "WHERE s.quantity_on_hand <= s.reorder_level ORDER BY s.quantity_on_hand";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapStock(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Stock> searchStock(String keyword) {
        List<Stock> list = new ArrayList<>();
        String sql = "SELECT s.*, p.product_name, p.category FROM stock s JOIN products p ON s.product_id=p.product_id " +
                     "WHERE p.product_name LIKE ? OR p.category LIKE ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw); ps.setString(2, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapStock(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean stockIn(int productId, int quantity, String supplierName) {
        String sql = "UPDATE stock SET quantity_on_hand = quantity_on_hand + ?, supplier_name=?, last_restocked=CURDATE() WHERE product_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setString(2, supplierName);
            ps.setInt(3, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean adjustStock(int stockId, int newQty, int reorderLevel) {
        String sql = "UPDATE stock SET quantity_on_hand=?, reorder_level=? WHERE stock_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, newQty);
            ps.setInt(2, reorderLevel);
            ps.setInt(3, stockId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int getTotalProducts() {
        String sql = "SELECT COUNT(*) FROM stock";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getLowStockCount() {
        String sql = "SELECT COUNT(*) FROM stock WHERE quantity_on_hand <= reorder_level AND quantity_on_hand > 0";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getOutOfStockCount() {
        String sql = "SELECT COUNT(*) FROM stock WHERE quantity_on_hand = 0";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public double getTotalStockValue() {
        String sql = "SELECT COALESCE(SUM(s.quantity_on_hand * p.price),0) FROM stock s JOIN products p ON s.product_id=p.product_id";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private Stock mapStock(ResultSet rs) throws SQLException {
        Stock s = new Stock();
        s.setStockId(rs.getInt("stock_id"));
        s.setProductId(rs.getInt("product_id"));
        s.setProductName(rs.getString("product_name"));
        s.setCategory(rs.getString("category"));
        s.setQuantityOnHand(rs.getInt("quantity_on_hand"));
        s.setReorderLevel(rs.getInt("reorder_level"));
        s.setSupplierName(rs.getString("supplier_name"));
        s.setLastRestocked(rs.getString("last_restocked"));
        int qty = rs.getInt("quantity_on_hand");
        int reorder = rs.getInt("reorder_level");
        s.setStatus(qty == 0 ? "Out of Stock" : qty <= reorder ? "Low Stock" : "In Stock");
        return s;
    }
}
