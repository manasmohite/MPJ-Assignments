package dao;

import model.*;
import util.DBConnection;
import util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User entities.
 * All DB operations for users go through here.
 */
public class UserDAO {

    /**
     * Authenticate a user by username and plain-text password.
     * Returns the appropriate User subclass or null on failure.
     */
    public User authenticate(String username, String plainPassword) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (PasswordUtil.verify(plainPassword, storedHash)) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] authenticate error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Find a user by their ID.
     */
    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToUser(rs);

        } catch (SQLException e) {
            System.err.println("[UserDAO] findById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all users (Admin dashboard use).
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY role, full_name";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] findAll error: " + e.getMessage());
        }
        return users;
    }

    /**
     * Insert a new user into the database.
     * @return generated user_id, or -1 on failure
     */
    public int createUser(String username, String plainPassword, String email,
                           String fullName, String role, String extraId,
                           String department, String extra2) {
        String sql = "INSERT INTO users (username, password_hash, email, full_name, role, " +
                     "extra_id, department, extra_field, is_active) VALUES (?,?,?,?,?,?,?,?,1)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(plainPassword));
            ps.setString(3, email);
            ps.setString(4, fullName);
            ps.setString(5, role);
            ps.setString(6, extraId);
            ps.setString(7, department);
            ps.setString(8, extra2);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

        } catch (SQLException e) {
            System.err.println("[UserDAO] createUser error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Deactivate (soft-delete) a user.
     */
    public boolean deactivateUser(int userId) {
        String sql = "UPDATE users SET is_active = 0 WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] deactivateUser error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Maps a ResultSet row to the correct User subclass based on role.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int userId      = rs.getInt("user_id");
        String username = rs.getString("username");
        String pwdHash  = rs.getString("password_hash");
        String email    = rs.getString("email");
        String fullName = rs.getString("full_name");
        String role     = rs.getString("role");
        String extraId  = rs.getString("extra_id");
        String dept     = rs.getString("department");
        String extra2   = rs.getString("extra_field");

        switch (role) {
            case "ADMIN":
                return new Admin(userId, username, pwdHash, email, fullName, extraId);
            case "FACULTY":
                return new Faculty(userId, username, pwdHash, email, fullName,
                                   extraId, dept, extra2 != null ? extra2 : "Professor");
            case "STUDENT":
                int year = 1;
                try { year = Integer.parseInt(extra2); } catch (Exception ignored) {}
                return new Student(userId, username, pwdHash, email, fullName,
                                   extraId, dept, year);
            default:
                return new Student(userId, username, pwdHash, email, fullName,
                                   extraId, dept, 1);
        }
    }
}
