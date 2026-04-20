package service;

import dao.UserDAO;
import model.User;

/**
 * Handles user authentication and session management.
 * Singleton pattern — one auth session at a time in the desktop app.
 */
public class AuthService {

    private static AuthService instance;
    private final UserDAO userDAO;
    private User currentUser; // Logged-in user for this session

    private AuthService() {
        userDAO = new UserDAO();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) instance = new AuthService();
        return instance;
    }

    /**
     * Attempt login. Returns the authenticated User, or null on failure.
     */
    public User login(String username, String password) {
        User user = userDAO.authenticate(username, password);
        if (user != null) {
            currentUser = user;
            System.out.println("[Auth] Login: " + user);
        } else {
            System.out.println("[Auth] Failed login attempt for: " + username);
        }
        return user;
    }

    /**
     * Log out current user.
     */
    public void logout() {
        System.out.println("[Auth] Logout: " + (currentUser != null ? currentUser.getUsername() : "none"));
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return isLoggedIn() && "ADMIN".equals(currentUser.getRole());
    }

    public boolean isFaculty() {
        return isLoggedIn() && "FACULTY".equals(currentUser.getRole());
    }

    public boolean isStudent() {
        return isLoggedIn() && "STUDENT".equals(currentUser.getRole());
    }
}
