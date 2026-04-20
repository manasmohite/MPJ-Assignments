package dao;

import model.Facility;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Facility entities.
 */
public class FacilityDAO {

    /**
     * Get all active facilities.
     */
    public List<Facility> findAll() {
        List<Facility> list = new ArrayList<>();
        String sql = "SELECT * FROM facilities WHERE is_active = 1 ORDER BY building, floor, room_number";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) list.add(map(rs));

        } catch (SQLException e) {
            System.err.println("[FacilityDAO] findAll error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Search facilities by keyword (name, building, type).
     */
    public List<Facility> search(String keyword) {
        List<Facility> list = new ArrayList<>();
        String sql = "SELECT * FROM facilities WHERE is_active = 1 AND " +
                     "(name LIKE ? OR building LIKE ? OR type LIKE ? OR room_number LIKE ?) " +
                     "ORDER BY building, floor";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));

        } catch (SQLException e) {
            System.err.println("[FacilityDAO] search error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Find facility by ID.
     */
    public Facility findById(int id) {
        String sql = "SELECT * FROM facilities WHERE facility_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);

        } catch (SQLException e) {
            System.err.println("[FacilityDAO] findById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Find facilities by building.
     */
    public List<Facility> findByBuilding(String building) {
        List<Facility> list = new ArrayList<>();
        String sql = "SELECT * FROM facilities WHERE building = ? AND is_active = 1 ORDER BY floor, room_number";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, building);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));

        } catch (SQLException e) {
            System.err.println("[FacilityDAO] findByBuilding error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get distinct building names for navigation.
     */
    public List<String> getBuildings() {
        List<String> buildings = new ArrayList<>();
        String sql = "SELECT DISTINCT building FROM facilities WHERE is_active = 1 ORDER BY building";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) buildings.add(rs.getString("building"));

        } catch (SQLException e) {
            System.err.println("[FacilityDAO] getBuildings error: " + e.getMessage());
        }
        return buildings;
    }

    private Facility map(ResultSet rs) throws SQLException {
        return new Facility(
            rs.getInt("facility_id"),
            rs.getString("name"),
            rs.getString("building"),
            rs.getInt("floor"),
            rs.getString("room_number"),
            rs.getString("type"),
            rs.getInt("capacity"),
            rs.getString("description"),
            rs.getBoolean("is_active")
        );
    }
}
