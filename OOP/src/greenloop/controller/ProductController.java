package greenloop.controller;

import greenloop.database.DBConnection;
import greenloop.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductController {

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, COALESCE(s.quantity_on_hand,0) AS stock " +
                     "FROM products p LEFT JOIN stock s ON p.product_id = s.product_id " +
                     "ORDER BY p.product_id";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Product pr = mapProduct(rs);
                pr.setStock(rs.getInt("stock"));
                list.add(pr);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Product> searchProducts(String keyword) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, COALESCE(s.quantity_on_hand,0) AS stock " +
                     "FROM products p LEFT JOIN stock s ON p.product_id = s.product_id " +
                     "WHERE p.product_name LIKE ? OR p.category LIKE ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw); ps.setString(2, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product pr = mapProduct(rs);
                    pr.setStock(rs.getInt("stock"));
                    list.add(pr);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean addProduct(Product p) {
        String sql = "INSERT INTO products (product_name,category,description,price,eco_rating) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getProductName());
            ps.setString(2, p.getCategory());
            ps.setString(3, p.getDescription());
            ps.setDouble(4, p.getPrice());
            ps.setInt(5, p.getEcoRating());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newId = keys.getInt(1);
                        String stockSql = "INSERT INTO stock (product_id,quantity_on_hand,reorder_level) VALUES (?,0,50)";
                        try (PreparedStatement sp = DBConnection.getConnection().prepareStatement(stockSql)) {
                            sp.setInt(1, newId);
                            sp.executeUpdate();
                        }
                    }
                }
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateProduct(Product p) {
        String sql = "UPDATE products SET product_name=?,category=?,description=?,price=?,eco_rating=?,status=? WHERE product_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, p.getProductName());
            ps.setString(2, p.getCategory());
            ps.setString(3, p.getDescription());
            ps.setDouble(4, p.getPrice());
            ps.setInt(5, p.getEcoRating());
            ps.setString(6, p.getStatus());
            ps.setInt(7, p.getProductId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE product_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    
    public int getNextProductId() {
        String sql = "SELECT AUTO_INCREMENT FROM information_schema.TABLES " +
                     "WHERE TABLE_SCHEMA='master' AND TABLE_NAME='products'";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 1;
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setProductName(rs.getString("product_name"));
        p.setCategory(rs.getString("category"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getDouble("price"));
        p.setEcoRating(rs.getInt("eco_rating"));
        p.setStatus(rs.getString("status"));
        return p;
    }
}
