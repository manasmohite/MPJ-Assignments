package ui;

import model.User;
import service.AuthService;
import util.AppConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window.
 * Uses a CardLayout center with a modern light sidebar for navigation.
 * This is the "shell" — all panels are loaded into it.
 */
public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JPanel dashboardRoot;  // Store dashboard root

    // Sidebar nav buttons
    private JButton navFacilities;
    private JButton navBooking;
    private JButton navMyBookings;
    private JButton navAdmin;
    private JLabel userLabel;
    private JLabel roleLabel;

    // Panels (lazy-initialized to speed up startup)
    private FacilityListPanel facilityListPanel;
    private BookingFormPanel  bookingFormPanel;
    private MyBookingsPanel   myBookingsPanel;
    private AdminDashboardPanel adminPanel;

    private static final String CARD_LOGIN     = "LOGIN";
    private static final String CARD_DASHBOARD = "DASHBOARD";

    public MainFrame() {
        super(AppConstants.APP_NAME + " v" + AppConstants.APP_VERSION);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(AppConstants.WINDOW_WIDTH, AppConstants.WINDOW_HEIGHT);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setBackground(UITheme.BG_LIGHT);
        initUI();
    }

    private void initUI() {
        // Outer card: LOGIN vs DASHBOARD
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(UITheme.BG_LIGHT);

        // Login screen
        cardPanel.add(new LoginPanel(this), CARD_LOGIN);

        // Dashboard (built lazily after login)
        cardPanel.add(new JPanel(), CARD_DASHBOARD); // placeholder

        setContentPane(cardPanel);
        cardLayout.show(cardPanel, CARD_LOGIN);
    }

    /**
     * Called by LoginPanel after successful authentication.
     */
    public void showDashboard(User user) {
        // Build the full dashboard now
        JPanel dashboard = buildDashboard(user);
        cardPanel.remove(1);                      // remove placeholder
        cardPanel.add(dashboard, CARD_DASHBOARD); // add real dashboard
        cardLayout.show(cardPanel, CARD_DASHBOARD);
        setTitle(AppConstants.APP_NAME + "  —  " + user.getFullName());
        revalidate();
        repaint();
    }

    private JPanel buildDashboard(User user) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_LIGHT);

        // Sidebar
        root.add(buildSidebar(user), BorderLayout.WEST);

        // Main content area with header + inner panels
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(UITheme.BG_LIGHT);

        // Top header bar
        mainContent.add(buildHeaderBar(), BorderLayout.NORTH);

        // Inner card area for panels
        CardLayout innerCards = new CardLayout();
        JPanel innerPanel = new JPanel(innerCards);
        innerPanel.setBackground(UITheme.BG_LIGHT);

        // Build panels
        facilityListPanel = new FacilityListPanel(this);
        bookingFormPanel  = new BookingFormPanel(this);
        myBookingsPanel   = new MyBookingsPanel(this);

        innerPanel.add(facilityListPanel, "FACILITIES");
        innerPanel.add(bookingFormPanel,  "BOOKING");
        innerPanel.add(myBookingsPanel,   "MY_BOOKINGS");

        if ("ADMIN".equals(user.getRole())) {
            adminPanel = new AdminDashboardPanel(this);
            innerPanel.add(adminPanel, "ADMIN");
        }

        innerCards.show(innerPanel, "FACILITIES");

        // Wire up sidebar buttons
        navFacilities.addActionListener(e -> {
            innerCards.show(innerPanel, "FACILITIES");
            setActiveSidebarBtn(navFacilities);
            updateHeaderTitle("Facilities", "Browse and search campus facilities");
        });

        navBooking.addActionListener(e -> {
            innerCards.show(innerPanel, "BOOKING");
            setActiveSidebarBtn(navBooking);
            updateHeaderTitle("Book Facility", "Reserve a facility for your needs");
        });

        navMyBookings.addActionListener(e -> {
            myBookingsPanel.refresh();
            innerCards.show(innerPanel, "MY_BOOKINGS");
            setActiveSidebarBtn(navMyBookings);
            updateHeaderTitle("My Bookings", "View and manage your reservations");
        });

        if (navAdmin != null) {
            navAdmin.addActionListener(e -> {
                adminPanel.refresh();
                innerCards.show(innerPanel, "ADMIN");
                setActiveSidebarBtn(navAdmin);
                updateHeaderTitle("Admin Dashboard", "System administration and statistics");
            });
        }

        setActiveSidebarBtn(navFacilities);

        // Store inner cards reference for panel-to-panel navigation
        root.putClientProperty("innerCards", innerCards);
        root.putClientProperty("innerPanel", innerPanel);

        mainContent.add(innerPanel, BorderLayout.CENTER);
        root.add(mainContent, BorderLayout.CENTER);

        // Store for navigation use
        this.dashboardRoot = root;
        return root;
    }

    private JPanel buildHeaderBar() {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Subtle bottom shadow
                g2.setColor(new Color(0, 0, 0, 15));
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
            }
        };
        header.setLayout(new BorderLayout(12, 12));
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        header.setPreferredSize(new Dimension(0, 60));

        // Title and breadcrumb
        JPanel titleArea = new JPanel(new BorderLayout());
        titleArea.setOpaque(false);

        JLabel titleLabel = new JLabel("Facilities");
        titleLabel.setFont(UITheme.FONT_HEADING);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        titleArea.add(titleLabel, BorderLayout.WEST);

        JLabel breadcrumbLabel = new JLabel("Browse and search campus facilities");
        breadcrumbLabel.setFont(UITheme.FONT_SMALL);
        breadcrumbLabel.setForeground(UITheme.TEXT_MUTED);
        titleArea.add(breadcrumbLabel, BorderLayout.SOUTH);

        titleArea.putClientProperty("titleLabel", titleLabel);
        titleArea.putClientProperty("breadcrumbLabel", breadcrumbLabel);

        header.add(titleArea, BorderLayout.WEST);

        // User avatar and info on right
        JPanel userArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userArea.setOpaque(false);

        User currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser != null) {
            JLabel userLbl = new JLabel(currentUser.getFullName());
            userLbl.setFont(UITheme.FONT_BODY);
            userLbl.setForeground(UITheme.TEXT_SECONDARY);

            String roleText = currentUser.getRoleLabel();
            JLabel roleBadge = new JLabel(roleText);
            roleBadge.setFont(UITheme.FONT_SMALL);
            roleBadge.setForeground(UITheme.ACCENT);
            roleBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.ACCENT_LIGHT),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
            ));
            roleBadge.setOpaque(true);
            roleBadge.setBackground(UITheme.ACCENT_LIGHT);

            userArea.add(roleBadge);
            userArea.add(userLbl);
        }

        header.add(userArea, BorderLayout.EAST);

        return header;
    }

    private void updateHeaderTitle(String title, String breadcrumb) {
        if (dashboardRoot != null) {
            JPanel mainContent = (JPanel) ((JPanel) dashboardRoot.getComponent(1)).getComponent(0);
            if (mainContent != null && mainContent.getComponent(0) instanceof JPanel) {
                JPanel header = (JPanel) mainContent.getComponent(0);
                if (header.getComponent(0) instanceof JPanel) {
                    JPanel titleArea = (JPanel) header.getComponent(0);
                    JLabel titleLabel = (JLabel) titleArea.getClientProperty("titleLabel");
                    JLabel breadcrumbLabel = (JLabel) titleArea.getClientProperty("breadcrumbLabel");
                    if (titleLabel != null) titleLabel.setText(title);
                    if (breadcrumbLabel != null) breadcrumbLabel.setText(breadcrumb);
                }
            }
        }
    }

    // Panel-to-panel navigation helpers
    public void showFacilities() {
        navigateTo("FACILITIES", navFacilities);
        updateHeaderTitle("Facilities", "Browse and search campus facilities");
    }

    public void showBookingForm(int facilityId) {
        bookingFormPanel.setPreselectedFacility(facilityId);
        navigateTo("BOOKING", navBooking);
        updateHeaderTitle("Book Facility", "Reserve a facility for your needs");
    }

    public void showMyBookings() {
        myBookingsPanel.refresh();
        navigateTo("MY_BOOKINGS", navMyBookings);
        updateHeaderTitle("My Bookings", "View and manage your reservations");
    }

    private void navigateTo(String card, JButton activeBtn) {
        JPanel root = this.dashboardRoot;
        if (root != null) {
            JPanel innerPanel = (JPanel) root.getClientProperty("innerPanel");
            CardLayout cl     = (CardLayout) root.getClientProperty("innerCards");
            if (innerPanel != null && cl != null) {
                cl.show(innerPanel, card);
                setActiveSidebarBtn(activeBtn);
            }
        }
    }

    private JPanel buildSidebar(User user) {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SIDEBAR_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Right border
                g2.setColor(UITheme.BORDER_LIGHT);
                g2.fillRect(getWidth() - 1, 0, 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));

        // Logo area with colored badge
        JPanel logoArea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SIDEBAR_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        logoArea.setLayout(new BoxLayout(logoArea, BoxLayout.Y_AXIS));
        logoArea.setOpaque(false);
        logoArea.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));

        // CN Badge
        JPanel badgePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.ACCENT);
                g2.fillRoundRect(0, 0, 36, 36, 8, 8);
                g2.dispose();
            }
        };
        badgePanel.setPreferredSize(new Dimension(36, 36));
        badgePanel.setMaximumSize(new Dimension(36, 36));
        badgePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        badgePanel.setLayout(new GridBagLayout());

        JLabel badgeLabel = new JLabel("CN");
        badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        badgeLabel.setForeground(Color.WHITE);
        badgePanel.add(badgeLabel);

        JLabel appName = new JLabel("Campus Navigator");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        appName.setForeground(UITheme.TEXT_PRIMARY);
        appName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel version = new JLabel("v" + AppConstants.APP_VERSION);
        version.setFont(UITheme.FONT_SMALL);
        version.setForeground(UITheme.TEXT_MUTED);
        version.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoArea.add(badgePanel);
        logoArea.add(Box.createVerticalStrut(8));
        logoArea.add(appName);
        logoArea.add(version);
        sidebar.add(logoArea);

        // Separator with label
        sidebar.add(createSidebarSep("NAVIGATION"));

        // Nav buttons
        navFacilities = createNavButton("🏛  Facilities", false);
        navBooking    = createNavButton("📅  Book Facility", false);
        navMyBookings = createNavButton("📋  My Bookings", false);

        sidebar.add(navFacilities);
        sidebar.add(navBooking);
        sidebar.add(navMyBookings);

        if ("ADMIN".equals(user.getRole())) {
            sidebar.add(createSidebarSep("ADMIN"));
            navAdmin = createNavButton("⚙️  Admin Dashboard", false);
            sidebar.add(navAdmin);
        }

        sidebar.add(Box.createVerticalGlue());

        // User info at bottom with avatar
        JPanel userArea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SIDEBAR_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Top separator
                g2.setColor(UITheme.BORDER_LIGHT);
                g2.fillRect(0, 0, getWidth(), 1);
                g2.dispose();
            }
        };
        userArea.setLayout(new BoxLayout(userArea, BoxLayout.Y_AXIS));
        userArea.setOpaque(false);
        userArea.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Avatar with initials
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Avatar circle color based on role
                Color avatarColor = "ADMIN".equals(user.getRole()) ? UITheme.DANGER
                                    : "FACULTY".equals(user.getRole()) ? UITheme.INFO
                                    : UITheme.SUCCESS;
                g2.setColor(avatarColor);
                g2.fillOval(0, 0, 36, 36);
                g2.dispose();
            }
        };
        avatarPanel.setOpaque(false);
        avatarPanel.setPreferredSize(new Dimension(36, 36));
        avatarPanel.setMaximumSize(new Dimension(36, 36));

        String initials = getInitials(user.getFullName());
        JLabel avatarLabel = new JLabel(initials);
        avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        avatarLabel.setForeground(Color.WHITE);
        avatarPanel.add(avatarLabel);

        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setOpaque(false);

        userLabel = new JLabel(user.getFullName());
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(UITheme.TEXT_PRIMARY);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        roleLabel = new JLabel(user.getRoleLabel());
        roleLabel.setFont(UITheme.FONT_SMALL);
        roleLabel.setForeground(UITheme.TEXT_SECONDARY);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        userInfo.add(userLabel);
        userInfo.add(Box.createVerticalStrut(2));
        userInfo.add(roleLabel);

        userArea.add(avatarPanel);
        userArea.add(Box.createVerticalStrut(8));
        userArea.add(userInfo);
        userArea.add(Box.createVerticalStrut(10));

        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.setFont(UITheme.FONT_SMALL);
        logoutBtn.setForeground(UITheme.TEXT_SECONDARY);
        logoutBtn.setBackground(UITheme.BG_SUBTLE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setOpaque(true);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        logoutBtn.setHorizontalAlignment(SwingConstants.LEFT);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        logoutBtn.addActionListener(e -> handleLogout());
        logoutBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                logoutBtn.setBackground(UITheme.BORDER_LIGHT);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                logoutBtn.setBackground(UITheme.BG_SUBTLE);
            }
        });

        userArea.add(logoutBtn);
        userArea.add(Box.createVerticalStrut(8));

        sidebar.add(userArea);
        return sidebar;
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return fullName.substring(0, Math.min(2, fullName.length())).toUpperCase();
    }

    private JButton createNavButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BODY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBackground(UITheme.SIDEBAR_BG);
        btn.setForeground(UITheme.TEXT_SECONDARY);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(220, 44));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        btn.setOpaque(true);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!btn.getBackground().equals(UITheme.SIDEBAR_ACTIVE)) {
                    btn.setBackground(UITheme.BG_SUBTLE);
                    btn.setForeground(UITheme.TEXT_PRIMARY);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!btn.getBackground().equals(UITheme.SIDEBAR_ACTIVE)) {
                    btn.setBackground(UITheme.SIDEBAR_BG);
                    btn.setForeground(UITheme.TEXT_SECONDARY);
                }
            }
        });
        return btn;
    }

    private void setActiveSidebarBtn(JButton active) {
        JButton[] allBtns = {navFacilities, navBooking, navMyBookings, navAdmin};
        for (JButton btn : allBtns) {
            if (btn == null) continue;
            if (btn == active) {
                btn.setBackground(UITheme.SIDEBAR_ACTIVE);
                btn.setForeground(UITheme.ACCENT);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 3, UITheme.ACCENT),
                    BorderFactory.createEmptyBorder(0, 13, 0, 16)
                ));
            } else {
                btn.setBackground(UITheme.SIDEBAR_BG);
                btn.setForeground(UITheme.TEXT_SECONDARY);
                btn.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
            }
        }
    }

    private JPanel createSidebarSep(String label) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(UITheme.TEXT_MUTED);
        p.add(lbl, BorderLayout.WEST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return p;
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Sign out of Campus Navigator?",
            "Sign Out", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.getInstance().logout();
            // Rebuild login panel fresh
            cardPanel.removeAll();
            cardPanel.add(new LoginPanel(this), CARD_LOGIN);
            cardPanel.add(new JPanel(), CARD_DASHBOARD);
            cardLayout.show(cardPanel, CARD_LOGIN);
            setTitle(AppConstants.APP_NAME);
            revalidate();
            repaint();
        }
    }
}
