package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Manages JDBC connection to MySQL.
 * Uses a simple connection-per-call approach with lazy driver loading.
 */
public class DBConnection {

    private static final String CONFIG_FILE = "db.properties";

    private static String url;
    private static String username;
    private static String password;
    private static boolean loaded = false;

    // Static initializer — load config once
    static {
        loadConfig();
    }

    private static void loadConfig() {
        if (loaded) return;
        try {
            // Try loading from classpath (db.properties in src/)
            InputStream is = DBConnection.class.getClassLoader()
                                               .getResourceAsStream(CONFIG_FILE);
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                url      = props.getProperty("db.url",      "jdbc:mysql://localhost:3306/campus_nav");
                username = props.getProperty("db.username", "root");
                password = props.getProperty("db.password", "root");
            } else {
                // Fallback defaults
                url      = "jdbc:mysql://localhost:3306/campus_nav?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                username = "root";
                password = "root";
            }
            Class.forName("com.mysql.cj.jdbc.Driver");
            loaded = true;
            System.out.println("[DBConnection] Driver loaded. URL: " + url);
        } catch (IOException e) {
            System.err.println("[DBConnection] Could not load config: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL Driver not found: " + e.getMessage());
            System.err.println("Download mysql-connector-j-*.jar and add to classpath.");
        }
    }

    /**
     * Returns a new JDBC connection.
     * Caller is responsible for closing it (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Quick connectivity test — used on startup.
     */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            System.err.println("[DBConnection] Connection test failed: " + e.getMessage());
            return false;
        }
    }
}
