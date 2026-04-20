package ui;

import service.AuthService;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Login screen — centered card design with modern light theme.
 */
public class LoginPanel extends JPanel {

    private final MainFrame mainFrame;
    private final AuthService authService;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JButton loginButton;
    private JPanel demoSection;
    private boolean demoSectionExpanded = false;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame   = mainFrame;
        this.authService = AuthService.getInstance();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_LIGHT);

        // Centered card layout
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(UITheme.BG_LIGHT);

        JPanel card = buildCard();
        card.setMaximumSize(new Dimension(420, 600));
        card.setPreferredSize(new Dimension(420, 560));

        centerPanel.add(card);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel buildCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card shadow
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(4, 4, getWidth(), getHeight(), 16, 16);

                // Card background
                g2.setColor(UITheme.BG_WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 16, 16);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(32, 40, 32, 40));

        // Header with campus icon
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Campus icon in colored circle
        JPanel iconContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.ACCENT_LIGHT);
                g2.fillOval(0, 0, 72, 72);
                g2.dispose();
            }
        };
        iconContainer.setPreferredSize(new Dimension(72, 72));
        iconContainer.setMaximumSize(new Dimension(72, 72));
        iconContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconContainer.setLayout(new GridBagLayout());

        JLabel campusIcon = new JLabel("🏫");
        campusIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        iconContainer.add(campusIcon);

        JLabel title = new JLabel("Campus Navigator");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Facility Booking System");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(iconContainer);
        headerPanel.add(Box.createVerticalStrut(16));
        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitle);
        headerPanel.add(Box.createVerticalStrut(8));

        // Separator line
        JSeparator separator = new JSeparator();
        separator.setForeground(UITheme.BORDER_LIGHT);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(headerPanel);
        card.add(separator);
        card.add(Box.createVerticalStrut(24));

        // Login form
        card.add(buildForm());
        card.add(Box.createVerticalStrut(16));

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.DANGER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(20));

        // Demo credentials section
        card.add(buildDemoSection());

        return card;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Username
        form.add(buildFieldLabel("Username"));
        form.add(Box.createVerticalStrut(6));
        usernameField = UITheme.styledField(20);
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        form.add(usernameField);

        form.add(Box.createVerticalStrut(16));

        // Password
        form.add(buildFieldLabel("Password"));
        form.add(Box.createVerticalStrut(6));
        passwordField = UITheme.styledPassword(20);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        form.add(passwordField);

        form.add(Box.createVerticalStrut(24));

        // Login button
        loginButton = new JButton("Sign In");
        loginButton.setFont(UITheme.FONT_BUTTON);
        loginButton.setBackground(UITheme.ACCENT);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setOpaque(true);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginButton.setPreferredSize(new Dimension(300, 44));
        loginButton.addActionListener(e -> handleLogin());
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                loginButton.setBackground(UITheme.ACCENT_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                loginButton.setBackground(UITheme.ACCENT);
            }
        });
        form.add(loginButton);

        // Enter key triggers login
        KeyAdapter enterListener = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleLogin();
            }
        };
        usernameField.addKeyListener(enterListener);
        passwordField.addKeyListener(enterListener);

        return form;
    }

    private JLabel buildFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_SUBHEAD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel buildDemoSection() {
        JPanel demoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_SUBTLE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
        };
        demoPanel.setLayout(new BoxLayout(demoPanel, BoxLayout.Y_AXIS));
        demoPanel.setOpaque(false);
        demoPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        demoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        demoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Collapsible header
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                demoSectionExpanded = !demoSectionExpanded;
                updateDemoSection(demoPanel);
            }
        });

        JLabel demoLabel = new JLabel("Demo accounts");
        demoLabel.setFont(UITheme.FONT_SMALL);
        demoLabel.setForeground(UITheme.TEXT_SECONDARY);
        demoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel toggleIcon = new JLabel(demoSectionExpanded ? "▼" : "▶");
        toggleIcon.setFont(UITheme.FONT_SMALL);
        toggleIcon.setForeground(UITheme.TEXT_MUTED);
        toggleIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        header.add(demoLabel, BorderLayout.WEST);
        header.add(toggleIcon, BorderLayout.EAST);
        header.putClientProperty("toggleIcon", toggleIcon);

        demoPanel.add(header);

        // Demo credentials (collapsible)
        demoSection = new JPanel();
        demoSection.setLayout(new BoxLayout(demoSection, BoxLayout.Y_AXIS));
        demoSection.setOpaque(false);

        if (demoSectionExpanded) {
            demoSection.add(Box.createVerticalStrut(8));
            demoSection.add(createDemoRow("admin", "admin123", "Administrator"));
            demoSection.add(Box.createVerticalStrut(4));
            demoSection.add(createDemoRow("faculty1", "faculty123", "Faculty Member"));
            demoSection.add(Box.createVerticalStrut(4));
            demoSection.add(createDemoRow("student1", "student123", "Student"));
        }

        demoPanel.add(demoSection);
        demoPanel.putClientProperty("header", header);
        demoPanel.putClientProperty("demoSection", demoSection);

        return demoPanel;
    }

    private void updateDemoSection(JPanel demoPanel) {
        JPanel header = (JPanel) demoPanel.getClientProperty("header");
        JLabel toggleIcon = (JLabel) header.getClientProperty("toggleIcon");
        toggleIcon.setText(demoSectionExpanded ? "▼" : "▶");

        demoSection.removeAll();
        if (demoSectionExpanded) {
            demoSection.add(Box.createVerticalStrut(8));
            demoSection.add(createDemoRow("admin", "admin123", "Administrator"));
            demoSection.add(Box.createVerticalStrut(4));
            demoSection.add(createDemoRow("faculty1", "faculty123", "Faculty Member"));
            demoSection.add(Box.createVerticalStrut(4));
            demoSection.add(createDemoRow("student1", "student123", "Student"));
        }
        demoPanel.revalidate();
        demoPanel.repaint();
    }

    private JPanel createDemoRow(String username, String password, String role) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel creds = new JLabel("<html><b>" + username + "</b> / " + password + "</html>");
        creds.setFont(UITheme.FONT_SMALL);
        creds.setForeground(UITheme.TEXT_PRIMARY);

        JLabel roleLabel = new JLabel(role);
        roleLabel.setFont(UITheme.FONT_SMALL);
        roleLabel.setForeground(UITheme.TEXT_MUTED);

        row.add(creds, BorderLayout.WEST);
        row.add(roleLabel, BorderLayout.EAST);

        return row;
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("⚠ Please enter username and password.");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");
        statusLabel.setText(" ");

        // Run auth in background thread to avoid freezing UI
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        statusLabel.setForeground(UITheme.SUCCESS);
                        statusLabel.setText("✓ Login successful! Loading...");
                        mainFrame.showDashboard(user);
                    } else {
                        statusLabel.setForeground(UITheme.DANGER);
                        statusLabel.setText("✗ Invalid username or password.");
                        passwordField.setText("");
                        loginButton.setEnabled(true);
                        loginButton.setText("Sign In");
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(UITheme.DANGER);
                    statusLabel.setText("✗ Connection error. Check database.");
                    loginButton.setEnabled(true);
                    loginButton.setText("Sign In");
                }
            }
        };
        worker.execute();
    }
}
