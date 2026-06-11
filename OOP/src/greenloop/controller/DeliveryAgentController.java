package greenloop.controller;

import greenloop.database.DBConnection;
import greenloop.model.DeliveryAgent;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DeliveryAgentController {

   

    public List<DeliveryAgent> getAllAgents() {
        return query("SELECT * FROM delivery_agents ORDER BY agent_id");
    }

    public List<DeliveryAgent> getAvailableAgents() {
        return query("SELECT * FROM delivery_agents WHERE status='Available' ORDER BY full_name");
    }

    public List<DeliveryAgent> searchAgents(String keyword) {
        List<DeliveryAgent> list = new ArrayList<>();
        String sql = "SELECT * FROM delivery_agents " +
                     "WHERE full_name LIKE ? OR email LIKE ? OR phone LIKE ? OR nic_number LIKE ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw); ps.setString(2, kw);
            ps.setString(3, kw); ps.setString(4, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    

    public boolean addAgent(DeliveryAgent a) {
        String sql = "INSERT INTO delivery_agents " +
                     "(salutation, full_name, nic_number, date_of_birth, email, phone, address, " +
                     " license_number, date_of_joining, vehicle_number, vehicle_type, " +
                     " vehicle_make, vehicle_year, vehicle_color, vehicle_model, status, remarks, photo_path) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1,  a.getSalutation());
            ps.setString(2,  a.getFullName());
            ps.setString(3,  a.getNicNumber());
            ps.setString(4,  a.getDateOfBirth());
            ps.setString(5,  a.getEmail());
            ps.setString(6,  a.getPhone());
            ps.setString(7,  a.getAddress());
            ps.setString(8,  a.getLicenseNumber());
            ps.setString(9,  a.getDateOfJoining());
            ps.setString(10, a.getVehicleNumber());
            ps.setString(11, a.getVehicleType());
            ps.setString(12, a.getVehicleMake());
            ps.setString(13, a.getVehicleYear());
            ps.setString(14, a.getVehicleColor());
            ps.setString(15, a.getVehicleModel());
            ps.setString(16, a.getStatus() != null ? a.getStatus() : "Available");
            ps.setString(17, a.getRemarks());
            ps.setString(18, a.getPhotoPath());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    

    public boolean updateAgent(DeliveryAgent a) {
        String sql = "UPDATE delivery_agents SET " +
                     "salutation=?, full_name=?, nic_number=?, date_of_birth=?, email=?, phone=?, address=?, " +
                     "license_number=?, date_of_joining=?, vehicle_number=?, vehicle_type=?, " +
                     "vehicle_make=?, vehicle_year=?, vehicle_color=?, vehicle_model=?, " +
                     "status=?, remarks=?, photo_path=? " +
                     "WHERE agent_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1,  a.getSalutation());
            ps.setString(2,  a.getFullName());
            ps.setString(3,  a.getNicNumber());
            ps.setString(4,  a.getDateOfBirth());
            ps.setString(5,  a.getEmail());
            ps.setString(6,  a.getPhone());
            ps.setString(7,  a.getAddress());
            ps.setString(8,  a.getLicenseNumber());
            ps.setString(9,  a.getDateOfJoining());
            ps.setString(10, a.getVehicleNumber());
            ps.setString(11, a.getVehicleType());
            ps.setString(12, a.getVehicleMake());
            ps.setString(13, a.getVehicleYear());
            ps.setString(14, a.getVehicleColor());
            ps.setString(15, a.getVehicleModel());
            ps.setString(16, a.getStatus());
            ps.setString(17, a.getRemarks());
            ps.setString(18, a.getPhotoPath());
            ps.setInt(19, a.getAgentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    

    public boolean deleteAgent(int agentId) {
        String sql = "DELETE FROM delivery_agents WHERE agent_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, agentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    

    private List<DeliveryAgent> query(String sql) {
        List<DeliveryAgent> list = new ArrayList<>();
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private DeliveryAgent map(ResultSet rs) throws SQLException {
        DeliveryAgent a = new DeliveryAgent();
        a.setAgentId(rs.getInt("agent_id"));
        String sal = safeGet(rs, "salutation");
        a.setSalutation(!sal.isEmpty() ? sal : "Mr.");
        a.setFullName(rs.getString("full_name"));
        a.setNicNumber(safeGet(rs, "nic_number"));
        a.setDateOfBirth(safeGet(rs, "date_of_birth"));
        a.setEmail(rs.getString("email"));
        a.setPhone(rs.getString("phone"));
        a.setAddress(safeGet(rs, "address"));
        a.setLicenseNumber(safeGet(rs, "license_number"));
        a.setDateOfJoining(safeGet(rs, "date_of_joining"));
        a.setVehicleNumber(rs.getString("vehicle_number"));
        a.setVehicleType(rs.getString("vehicle_type"));
        a.setVehicleMake(safeGet(rs, "vehicle_make"));
        a.setVehicleYear(safeGet(rs, "vehicle_year"));
        a.setVehicleColor(safeGet(rs, "vehicle_color"));
        a.setVehicleModel(safeGet(rs, "vehicle_model"));
        a.setStatus(rs.getString("status"));
        a.setRemarks(safeGet(rs, "remarks"));
        a.setPhotoPath(safeGet(rs, "photo_path"));
        return a;
    }

    
    private String safeGet(ResultSet rs, String col) {
        try {
            String val = rs.getString(col);
            return val != null ? val : "";
        } catch (SQLException e) { return ""; }
    }
}
