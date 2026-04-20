package dao;

import model.Booking;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Booking entities.
 * Thread-safe reads; writes are synchronized at the service layer.
 */
public class BookingDAO {

    /**
     * Insert a new booking. Returns generated booking_id or -1.
     */
    public int insert(Booking b) {
        String sql = "INSERT INTO bookings (user_id, facility_id, slot_id, booking_date, " +
                     "purpose, status, notes) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, b.getUserId());
            ps.setInt(2, b.getFacilityId());
            ps.setInt(3, b.getSlotId());
            ps.setDate(4, Date.valueOf(b.getBookingDate()));
            ps.setString(5, b.getPurpose().name());
            ps.setString(6, b.getStatus().name());
            ps.setString(7, b.getNotes());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

        } catch (SQLException e) {
            System.err.println("[BookingDAO] insert error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Update booking status.
     */
    public boolean updateStatus(int bookingId, Booking.Status status) {
        String sql = "UPDATE bookings SET status = ? WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[BookingDAO] updateStatus error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check if a slot is already CONFIRMED for a given facility and date.
     */
    public boolean isSlotTaken(int facilityId, int slotId, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE facility_id = ? AND slot_id = ? " +
                     "AND booking_date = ? AND status = 'CONFIRMED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityId);
            ps.setInt(2, slotId);
            ps.setDate(3, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("[BookingDAO] isSlotTaken error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get the current CONFIRMED booking for a slot (for priority override check).
     */
    public Booking getConfirmedBooking(int facilityId, int slotId, LocalDate date) {
        String sql = "SELECT b.*, u.full_name as user_name, f.name as facility_name, " +
                     "ts.label as slot_label " +
                     "FROM bookings b " +
                     "JOIN users u ON b.user_id = u.user_id " +
                     "JOIN facilities f ON b.facility_id = f.facility_id " +
                     "JOIN time_slots ts ON b.slot_id = ts.slot_id " +
                     "WHERE b.facility_id = ? AND b.slot_id = ? AND b.booking_date = ? " +
                     "AND b.status = 'CONFIRMED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityId);
            ps.setInt(2, slotId);
            ps.setDate(3, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapFull(rs);

        } catch (SQLException e) {
            System.err.println("[BookingDAO] getConfirmedBooking error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all bookings for a specific user.
     */
    public List<Booking> findByUser(int userId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, u.full_name as user_name, f.name as facility_name, " +
                     "ts.label as slot_label " +
                     "FROM bookings b " +
                     "JOIN users u ON b.user_id = u.user_id " +
                     "JOIN facilities f ON b.facility_id = f.facility_id " +
                     "JOIN time_slots ts ON b.slot_id = ts.slot_id " +
                     "WHERE b.user_id = ? ORDER BY b.booking_date DESC, ts.slot_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapFull(rs));

        } catch (SQLException e) {
            System.err.println("[BookingDAO] findByUser error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get all bookings (Admin view).
     */
    public List<Booking> findAll() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, u.full_name as user_name, f.name as facility_name, " +
                     "ts.label as slot_label " +
                     "FROM bookings b " +
                     "JOIN users u ON b.user_id = u.user_id " +
                     "JOIN facilities f ON b.facility_id = f.facility_id " +
                     "JOIN time_slots ts ON b.slot_id = ts.slot_id " +
                     "ORDER BY b.booking_date DESC, ts.slot_id";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapFull(rs));

        } catch (SQLException e) {
            System.err.println("[BookingDAO] findAll error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get available (free) slot IDs for a facility on a date.
     */
    public List<Integer> getTakenSlotIds(int facilityId, LocalDate date) {
        List<Integer> taken = new ArrayList<>();
        String sql = "SELECT slot_id FROM bookings WHERE facility_id = ? AND booking_date = ? AND status = 'CONFIRMED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityId);
            ps.setDate(2, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) taken.add(rs.getInt("slot_id"));

        } catch (SQLException e) {
            System.err.println("[BookingDAO] getTakenSlotIds error: " + e.getMessage());
        }
        return taken;
    }

    /**
     * Cancel a booking.
     */
    public boolean cancel(int bookingId) {
        return updateStatus(bookingId, Booking.Status.CANCELLED);
    }

    private Booking mapFull(ResultSet rs) throws SQLException {
        Booking b = new Booking(
            rs.getInt("booking_id"),
            rs.getInt("user_id"),
            rs.getInt("facility_id"),
            rs.getInt("slot_id"),
            rs.getDate("booking_date").toLocalDate(),
            Booking.Purpose.valueOf(rs.getString("purpose")),
            Booking.Status.valueOf(rs.getString("status")),
            rs.getString("notes")
        );
        b.setUserName(rs.getString("user_name"));
        b.setFacilityName(rs.getString("facility_name"));
        b.setSlotLabel(rs.getString("slot_label"));
        try { b.setCreatedAt(rs.getString("created_at")); } catch (Exception ignored) {}
        return b;
    }
}
