package dao;

import model.TimeSlot;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for TimeSlot entities.
 */
public class TimeSlotDAO {

    public List<TimeSlot> findAll() {
        List<TimeSlot> list = new ArrayList<>();
        String sql = "SELECT * FROM time_slots ORDER BY slot_id";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new TimeSlot(
                    rs.getInt("slot_id"),
                    rs.getString("label"),
                    rs.getString("start_time"),
                    rs.getString("end_time")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[TimeSlotDAO] findAll error: " + e.getMessage());
        }
        return list;
    }

    public TimeSlot findById(int slotId) {
        String sql = "SELECT * FROM time_slots WHERE slot_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, slotId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new TimeSlot(
                    rs.getInt("slot_id"),
                    rs.getString("label"),
                    rs.getString("start_time"),
                    rs.getString("end_time")
                );
            }
        } catch (SQLException e) {
            System.err.println("[TimeSlotDAO] findById error: " + e.getMessage());
        }
        return null;
    }
}
