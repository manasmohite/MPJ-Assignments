import ui.MainFrame;
import ui.UITheme;
import util.DBConnection;

import javax.swing.*;

/**
 * ╔════════════════════════════════════════════════════════════╗
 * ║   Intelligent Campus Navigation & Facility Booking System  ║
 * ║   Entry Point                                              ║
 * ╚════════════════════════════════════════════════════════════╝
 *
 * Run: javac -cp .:lib/mysql-connector-j-*.jar $(find src -name "*.java") -d out
 *      java  -cp out:lib/mysql-connector-j-*.jar Main
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Campus Navigator — Starting up...");
        System.out.println("═══════════════════════════════════════════════");

        // 1. Test DB connection before launching UI
        System.out.print("[Init] Testing database connection... ");
        boolean dbOk = DBConnection.testConnection();
        if (dbOk) {
            System.out.println("✓ Connected.");
        } else {
            System.out.println("✗ FAILED.");
            System.out.println("[Init] Cannot connect to MySQL. Check:");
            System.out.println("       - MySQL is running (brew services start mysql)");
            System.out.println("       - Database 'campus_nav' exists");
            System.out.println("       - Credentials in src/db.properties are correct");
            System.out.println("       - mysql-connector-j-*.jar is in lib/");
            System.out.println("[Init] Continuing anyway — UI will show connection errors.");
        }

        // 2. Set system look & feel (platform-native decorations)
        try {
            // Use cross-platform look to respect our custom dark theme
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("[Init] L&F warning: " + e.getMessage());
        }

        // 3. Launch Swing UI on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // Global UI defaults for our dark theme
                UIManager.put("OptionPane.background",       UITheme.BG_PANEL);
                UIManager.put("Panel.background",            UITheme.BG_PANEL);
                UIManager.put("OptionPane.messageForeground",UITheme.TEXT_PRIMARY);
                UIManager.put("Button.background",           UITheme.BG_CARD);
                UIManager.put("Button.foreground",           UITheme.TEXT_PRIMARY);
                UIManager.put("ComboBox.background",         UITheme.BG_INPUT);
                UIManager.put("ComboBox.foreground",         UITheme.TEXT_PRIMARY);
                UIManager.put("TextField.background",        UITheme.BG_INPUT);
                UIManager.put("TextField.foreground",        UITheme.TEXT_PRIMARY);
                UIManager.put("TextArea.background",         UITheme.BG_INPUT);
                UIManager.put("TextArea.foreground",         UITheme.TEXT_PRIMARY);
                UIManager.put("ScrollPane.background",       UITheme.BG_PANEL);
                UIManager.put("TabbedPane.background",       UITheme.BG_PANEL);
                UIManager.put("TabbedPane.foreground",       UITheme.TEXT_PRIMARY);
                UIManager.put("TabbedPane.selected",         UITheme.BG_CARD);
                UIManager.put("Table.background",            UITheme.BG_CARD);
                UIManager.put("Table.foreground",            UITheme.TEXT_PRIMARY);
                UIManager.put("TableHeader.background",      UITheme.BG_PANEL);
                UIManager.put("TableHeader.foreground",      UITheme.TEXT_SECONDARY);

                MainFrame frame = new MainFrame();
                frame.setVisible(true);
                System.out.println("[Init] UI launched successfully.");

            } catch (Exception e) {
                System.err.println("[Init] Fatal UI error: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Failed to launch application:\n" + e.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
