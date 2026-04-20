import util.DBConnection;
import util.PasswordUtil;

import java.sql.*;
import java.time.LocalDate;

/**
 * ╔══════════════════════════════════════════════════╗
 * ║  SeedDB — One-time database seeder               ║
 * ║  Run AFTER schema.sql to set proper passwords     ║
 * ║  and insert sample bookings.                      ║
 * ╚══════════════════════════════════════════════════╝
 *
 * Run:
 *   java -cp out:lib/mysql-connector-j-*.jar SeedDB
 */
public class SeedDB {

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("  Campus Navigator — Database Seeder");
        System.out.println("═══════════════════════════════════════════");

        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("✓ Connected to database.");

            updatePasswords(conn);
            insertSampleBookings(conn);

            System.out.println("\n✅ Seeding complete!");
            System.out.println("\nTest Accounts:");
            System.out.println("  admin    / admin123    (Admin)");
            System.out.println("  faculty1 / faculty123  (Faculty)");
            System.out.println("  faculty2 / faculty123  (Faculty)");
            System.out.println("  student1 / student123  (Student)");
            System.out.println("  student2 / student123  (Student)");
            System.out.println("  student3 / student123  (Student)");

        } catch (SQLException e) {
            System.err.println("❌ Seeding failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void updatePasswords(Connection conn) throws SQLException {
        System.out.println("\n[1/2] Setting hashed passwords...");

        String[][] accounts = {
            {"admin",    "admin123"},
            {"faculty1", "faculty123"},
            {"faculty2", "faculty123"},
            {"student1", "student123"},
            {"student2", "student123"},
            {"student3", "student123"}
        };

        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String[] acc : accounts) {
                String hash = PasswordUtil.hash(acc[1]);
                ps.setString(1, hash);
                ps.setString(2, acc[0]);
                int rows = ps.executeUpdate();
                System.out.println("  " + acc[0] + " → " + (rows > 0 ? "✓" : "⚠ Not found") + " (hash set)");
            }
        }
    }

    private static void insertSampleBookings(Connection conn) throws SQLException {
        System.out.println("\n[2/2] Inserting sample bookings...");

        // Get user IDs
        int adminId    = getUserId(conn, "admin");
        int faculty1Id = getUserId(conn, "faculty1");
        int faculty2Id = getUserId(conn, "faculty2");
        int student1Id = getUserId(conn, "student1");
        int student2Id = getUserId(conn, "student2");

        if (adminId < 0 || faculty1Id < 0 || student1Id < 0) {
            System.out.println("  ⚠ Could not find user IDs. Run schema.sql first.");
            return;
        }

        // Clear existing bookings for clean seed
        try (Statement st = conn.createStatement()) {
            st.execute("DELETE FROM bookings");
        }

        LocalDate tomorrow  = LocalDate.now().plusDays(1);
        LocalDate dayAfter  = LocalDate.now().plusDays(2);
        LocalDate nextWeek  = LocalDate.now().plusDays(7);

        String sql = "INSERT INTO bookings (user_id, facility_id, slot_id, booking_date, purpose, status, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Object[][] bookings = {
            // Exam booking in main auditorium (slot 2, facility 10)
            {adminId,    10, 2, Date.valueOf(tomorrow),  "EXAM",          "CONFIRMED",  "End Semester Examination — CS301"},
            // Faculty class booking (slot 1, classroom A-101 = facility 1)
            {faculty1Id, 1,  1, Date.valueOf(tomorrow),  "FACULTY_CLASS", "CONFIRMED",  "Data Structures Lecture"},
            // Faculty class (slot 3, facility 2)
            {faculty1Id, 2,  3, Date.valueOf(tomorrow),  "FACULTY_CLASS", "CONFIRMED",  "Algorithm Analysis"},
            // Faculty booking (slot 4, seminar room = facility 3)
            {faculty2Id, 3,  4, Date.valueOf(dayAfter),  "FACULTY_CLASS", "CONFIRMED",  "Physics Lab Orientation"},
            // Student booking — club event (facility 11, mini auditorium)
            {student1Id, 11, 5, Date.valueOf(dayAfter),  "CLUB_EVENT",    "CONFIRMED",  "Coding Club Workshop"},
            // Student study booking (facility 13, group study room)
            {student1Id, 13, 2, Date.valueOf(nextWeek),  "STUDENT_STUDY", "CONFIRMED",  "Project group study session"},
            // Student waitlisted (same slot as faculty1's booking)
            {student2Id, 1,  1, Date.valueOf(tomorrow),  "STUDENT_STUDY", "WAITLISTED", "Need room for assignment work"},
            // Research lab booking
            {faculty2Id, 17, 6, Date.valueOf(nextWeek),  "FACULTY_CLASS", "CONFIRMED",  "Quantum Physics Experiment"},
        };

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] b : bookings) {
                ps.setInt(1, (int) b[0]);
                ps.setInt(2, (int) b[1]);
                ps.setInt(3, (int) b[2]);
                ps.setDate(4, (Date) b[3]);
                ps.setString(5, (String) b[4]);
                ps.setString(6, (String) b[5]);
                ps.setString(7, (String) b[6]);
                ps.executeUpdate();
                System.out.println("  ✓ Booking: facility=" + b[1] + " slot=" + b[2] + " [" + b[4] + "/" + b[5] + "]");
            }
        }
    }

    private static int getUserId(Connection conn, String username) throws SQLException {
        String sql = "SELECT user_id FROM users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("user_id");
        }
        return -1;
    }
}
