package model;

/**
 * Admin user — manages system, highest operational access.
 * Priority 1 reserved for Exam; Admin has system-level override.
 */
public class Admin extends User {
    private String adminCode;

    public Admin(int userId, String username, String passwordHash,
                 String email, String fullName, String adminCode) {
        super(userId, username, passwordHash, email, fullName, "ADMIN");
        this.adminCode = adminCode;
    }

    @Override
    public int getBookingPriority() {
        return 1; // Highest — Admin can always book
    }

    @Override
    public String getRoleLabel() {
        return "Admin";
    }

    public String getAdminCode() { return adminCode; }
    public void setAdminCode(String adminCode) { this.adminCode = adminCode; }
}
