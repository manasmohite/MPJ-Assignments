package model;

/**
 * Abstract base class for all user types.
 * Demonstrates: Inheritance, Encapsulation, Abstraction
 */
public abstract class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String email;
    private String fullName;
    private String role; // "STUDENT", "FACULTY", "ADMIN"

    public User(int userId, String username, String passwordHash,
                String email, String fullName, String role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    // Polymorphic method — each subclass defines booking priority
    public abstract int getBookingPriority();

    // Polymorphic method — display role label
    public abstract String getRoleLabel();

    // Getters & Setters (Encapsulation)
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "[" + getRoleLabel() + "] " + fullName + " (" + username + ")";
    }
}
