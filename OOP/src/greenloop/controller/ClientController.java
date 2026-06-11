package greenloop.controller;

import greenloop.database.DBConnection;
import greenloop.model.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientController {

    public List<Client> getAllClients() {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM clients ORDER BY client_id";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapClient(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Client> searchClients(String keyword) {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM clients WHERE business_name LIKE ? OR email LIKE ? OR phone LIKE ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapClient(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean addClient(Client c) {
        String sql = "INSERT INTO clients (business_name,salutation,contact_person,email,phone,address) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getBusinessName());
            ps.setString(2, c.getSalutation());
            ps.setString(3, c.getContactPerson());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getPhone());
            ps.setString(6, c.getAddress());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateClient(Client c) {
        String sql = "UPDATE clients SET business_name=?,salutation=?,contact_person=?,email=?,phone=?,address=? WHERE client_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getBusinessName());
            ps.setString(2, c.getSalutation());
            ps.setString(3, c.getContactPerson());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getPhone());
            ps.setString(6, c.getAddress());
            ps.setInt(7, c.getClientId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean deleteClient(int clientId) {
        String sql = "DELETE FROM clients WHERE client_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, clientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int getTotalClientCount() {
        String sql = "SELECT COUNT(*) FROM clients";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private Client mapClient(ResultSet rs) throws SQLException {
        Client c = new Client();
        c.setClientId(rs.getInt("client_id"));
        c.setBusinessName(rs.getString("business_name"));
        String sal = rs.getString("salutation");
        c.setSalutation(sal != null && !sal.isEmpty() ? sal : "Mr.");
        c.setContactPerson(rs.getString("contact_person"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        c.setStatus(rs.getString("status") != null ? rs.getString("status") : "Active");
        return c;
    }
}
