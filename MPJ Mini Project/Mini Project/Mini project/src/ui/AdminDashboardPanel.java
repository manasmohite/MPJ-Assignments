package ui;

import dao.UserDAO;
import model.Booking;
import model.User;
import service.BookingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Admin-only dashboard: view all bookings, all users, manage cancellations.
 * Features stats cards, styled tables, and a bookings bar chart.
 */
public class AdminDashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final BookingService bookingService;
    private final UserDAO userDAO;

    // Bookings tab
    private JTable bookingsTable;
    private DefaultTableModel bookingsModel;

    // Users tab
    private JTable usersTable;
    private DefaultTableModel usersModel;

    private JLabel statsLabel;

    public AdminDashboardPanel(MainFrame mainFrame) {
        this.mainFrame      = mainFrame;
        this.bookingService = new BookingService();
        this.userDAO        = new UserDAO();
        initUI();
    }

    public void refresh() {
        loadAllBookings();
        loadAllUsers();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);

        loadAllBookings();
        loadAllUsers();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_LIGHT);

        JLabel title = UITheme.headingLabel("⚙️  Admin Dashboard");
        header.add(title, BorderLayout.WEST);

        statsLabel = UITheme.mutedLabel(" ");
        header.add(statsLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel buildMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(UITheme.BG_LIGHT);

        // Stats row at top
        content.add(buildStatsRow(), BorderLayout.NORTH);

        // Tabbed panel below
        content.add(buildTabbedPanel(), BorderLayout.CENTER);

        return content;
    }

    private JPanel buildStatsRow() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        statsPanel.setBackground(UITheme.BG_LIGHT);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        List<Booking> allBookings = bookingService.getAllBookings();
        List<User> allUsers = userDAO.findAll();

        int totalBookings = allBookings.size();
        long todayBookings = allBookings.stream()
            .filter(b -> b.getBookingDate().equals(LocalDate.now().toString()))
            .count();
        int totalUsers = allUsers.size();
        long pendingRequests = allBookings.stream()
            .filter(b -> b.getStatus() == Booking.Status.WAITLISTED)
            .count();

        statsPanel.add(buildStatCard("Total Bookings", String.valueOf(totalBookings), "📅", UITheme.INFO, UITheme.INFO_BG));
        statsPanel.add(buildStatCard("Today's Bookings", String.valueOf(todayBookings), "📆", UITheme.SUCCESS, UITheme.SUCCESS_BG));
        statsPanel.add(buildStatCard("Total Users", String.valueOf(totalUsers), "👥", UITheme.ACCENT, UITheme.ACCENT_LIGHT));
        statsPanel.add(buildStatCard("Pending Requests", String.valueOf(pendingRequests), "⏳", UITheme.WARNING, UITheme.WARNING_BG));

        return statsPanel;
    }

    private JPanel buildStatCard(String title, String value, String icon, Color accentColor, Color bgColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(12, 0));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Icon panel
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fillOval(0, 0, 44, 44);
                g2.dispose();
            }
        };
        iconPanel.setPreferredSize(new Dimension(44, 44));
        iconPanel.setLayout(new GridBagLayout());

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        iconPanel.add(iconLabel);

        // Value and title
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_SMALL);
        titleLabel.setForeground(UITheme.TEXT_SECONDARY);

        textPanel.add(valueLabel, BorderLayout.NORTH);
        textPanel.add(titleLabel, BorderLayout.SOUTH);

        card.add(iconPanel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildTabbedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BG_LIGHT);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(UITheme.BG_LIGHT);
        tabs.setForeground(UITheme.TEXT_PRIMARY);
        tabs.setFont(UITheme.FONT_SUBHEAD);

        tabs.addTab("📅  All Bookings", buildBookingsPanel());
        tabs.addTab("👥  All Users", buildUsersPanel());
        tabs.addTab("📊  Statistics", buildStatsPanel());

        panel.add(tabs, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BG_LIGHT);

        String[] cols = {"ID", "User", "Facility", "Date", "Time Slot", "Purpose", "Status", "Notes"};
        bookingsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        bookingsTable = new JTable(bookingsModel);
        UITheme.applyTableTheme(bookingsTable);

        // Alternating row colors
        bookingsTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focused, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focused, row, col);

                if (selected) {
                    setBackground(UITheme.ACCENT_LIGHT);
                    setForeground(UITheme.TEXT_PRIMARY);
                } else {
                    setBackground(row % 2 == 0 ? UITheme.BG_WHITE : UITheme.BG_SUBTLE);
                    setForeground(UITheme.TEXT_PRIMARY);
                }

                // Status color
                if (!selected && col == 6 && value != null) {
                    String status = value.toString();
                    switch (status) {
                        case "CONFIRMED"  -> setForeground(UITheme.SUCCESS);
                        case "WAITLISTED" -> setForeground(UITheme.WARNING);
                        case "CANCELLED"  -> setForeground(UITheme.TEXT_MUTED);
                        case "OVERRIDDEN" -> setForeground(UITheme.DANGER);
                    }
                }

                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        int[] widths = {40, 130, 140, 100, 120, 120, 100, 180};
        for (int i = 0; i < widths.length; i++) {
            bookingsTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(bookingsTable);
        UITheme.applyScrollPaneTheme(scroll);
        panel.add(scroll, BorderLayout.CENTER);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(UITheme.BG_LIGHT);

        JButton refreshBtn = UITheme.secondaryButton("↺  Refresh");
        refreshBtn.addActionListener(e -> loadAllBookings());

        JButton cancelBtn = UITheme.dangerButton("✕  Cancel Selected");
        cancelBtn.addActionListener(e -> adminCancelBooking());

        btnRow.add(refreshBtn);
        btnRow.add(cancelBtn);
        panel.add(btnRow, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BG_LIGHT);

        String[] cols = {"ID", "Username", "Full Name", "Role", "Email", "Department", "Extra ID"};
        usersModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        usersTable = new JTable(usersModel);
        UITheme.applyTableTheme(usersTable);

        usersTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focused, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focused, row, col);

                if (selected) {
                    setBackground(UITheme.ACCENT_LIGHT);
                    setForeground(UITheme.TEXT_PRIMARY);
                } else {
                    setBackground(row % 2 == 0 ? UITheme.BG_WHITE : UITheme.BG_SUBTLE);
                    setForeground(UITheme.TEXT_PRIMARY);
                }

                // Role color
                if (!selected && col == 3 && value != null) {
                    String role = value.toString();
                    switch (role) {
                        case "ADMIN"   -> setForeground(UITheme.DANGER);
                        case "FACULTY" -> setForeground(UITheme.INFO);
                        case "STUDENT" -> setForeground(UITheme.SUCCESS);
                    }
                }

                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        int[] widths = {40, 110, 160, 80, 170, 120, 90};
        for (int i = 0; i < widths.length; i++) {
            usersTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(usersTable);
        UITheme.applyScrollPaneTheme(scroll);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(UITheme.BG_LIGHT);
        JButton refreshBtn = UITheme.secondaryButton("↺  Refresh");
        refreshBtn.addActionListener(e -> loadAllUsers());
        btnRow.add(refreshBtn);
        panel.add(btnRow, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Bar chart
        JPanel chartPanel = buildBarChartPanel();
        chartPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        chartPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(chartPanel);
        panel.add(Box.createVerticalStrut(20));

        // Stats list
        JLabel title = UITheme.headingLabel("Detailed Statistics");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(16));

        List<Booking> allBookings = bookingService.getAllBookings();
        List<User> allUsers = userDAO.findAll();

        long confirmed  = allBookings.stream().filter(b -> b.getStatus() == Booking.Status.CONFIRMED).count();
        long waitlisted = allBookings.stream().filter(b -> b.getStatus() == Booking.Status.WAITLISTED).count();
        long cancelled  = allBookings.stream().filter(b -> b.getStatus() == Booking.Status.CANCELLED).count();
        long overridden = allBookings.stream().filter(b -> b.getStatus() == Booking.Status.OVERRIDDEN).count();
        long students   = allUsers.stream().filter(u -> "STUDENT".equals(u.getRole())).count();
        long faculty    = allUsers.stream().filter(u -> "FACULTY".equals(u.getRole())).count();
        long admins     = allUsers.stream().filter(u -> "ADMIN".equals(u.getRole())).count();

        String[][] stats = {
            {"Total Bookings",        String.valueOf(allBookings.size())},
            {"Confirmed",             String.valueOf(confirmed)},
            {"Waitlisted",            String.valueOf(waitlisted)},
            {"Cancelled",             String.valueOf(cancelled)},
            {"Overridden (Priority)", String.valueOf(overridden)},
            {"", ""},
            {"Total Users",           String.valueOf(allUsers.size())},
            {"Students",              String.valueOf(students)},
            {"Faculty",               String.valueOf(faculty)},
            {"Admins",                String.valueOf(admins)},
        };

        for (String[] row : stats) {
            if (row[0].isEmpty()) {
                panel.add(Box.createVerticalStrut(12));
                panel.add(UITheme.separator());
                panel.add(Box.createVerticalStrut(12));
                continue;
            }
            JPanel rowPanel = new JPanel(new BorderLayout());
            rowPanel.setBackground(UITheme.BG_WHITE);
            rowPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 4, UITheme.ACCENT),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
            ));
            rowPanel.setMaximumSize(new Dimension(500, 52));
            rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel keyLbl = UITheme.bodyLabel(row[0]);
            JLabel valLbl = UITheme.headingLabel(row[1]);
            valLbl.setForeground(UITheme.ACCENT);

            rowPanel.add(keyLbl, BorderLayout.WEST);
            rowPanel.add(valLbl, BorderLayout.EAST);
            panel.add(rowPanel);
            panel.add(Box.createVerticalStrut(6));
        }

        panel.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(panel);
        UITheme.applyScrollPaneTheme(scroll);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.BG_LIGHT);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildBarChartPanel() {
        JPanel chartContainer = new JPanel();
        chartContainer.setLayout(new BoxLayout(chartContainer, BoxLayout.Y_AXIS));
        chartContainer.setBackground(UITheme.BG_LIGHT);

        JLabel chartTitle = new JLabel("Bookings per Day (Last 7 Days)");
        chartTitle.setFont(UITheme.FONT_SUBHEAD);
        chartTitle.setForeground(UITheme.TEXT_PRIMARY);
        chartTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        chartContainer.add(chartTitle);
        chartContainer.add(Box.createVerticalStrut(12));

        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int padding = 40;
                int chartWidth = width - 2 * padding;
                int chartHeight = height - 2 * padding;

                // Get data for last 7 days
                LocalDate today = LocalDate.now();
                int[] bookingsPerDay = new int[7];
                List<Booking> allBookings = bookingService.getAllBookings();

                for (int i = 0; i < 7; i++) {
                    LocalDate day = today.minusDays(6 - i);
                    String dayStr = day.toString();
                    final int idx = i;
                    bookingsPerDay[i] = (int) allBookings.stream()
                        .filter(b -> b.getBookingDate().equals(dayStr))
                        .count();
                }

                int maxBookings = 1;
                for (int count : bookingsPerDay) {
                    if (count > maxBookings) maxBookings = count;
                }

                // Draw bars
                int barWidth = chartWidth / 7 - 10;
                String[] dayLabels = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

                for (int i = 0; i < 7; i++) {
                    int x = padding + i * (chartWidth / 7) + 5;
                    int barHeight = (bookingsPerDay[i] * chartHeight) / maxBookings;
                    int y = padding + chartHeight - barHeight;

                    // Bar gradient
                    GradientPaint gp = new GradientPaint(
                        x, y, UITheme.ACCENT_LIGHT,
                        x, y + barHeight, UITheme.ACCENT
                    );
                    g2.setPaint(gp);
                    g2.fillRoundRect(x, y, barWidth, barHeight, 4, 4);

                    // Border
                    g2.setColor(UITheme.ACCENT);
                    g2.drawRoundRect(x, y, barWidth, barHeight, 4, 4);

                    // Value on top
                    g2.setColor(UITheme.TEXT_PRIMARY);
                    g2.setFont(UITheme.FONT_SMALL);
                    FontMetrics fm = g2.getFontMetrics();
                    String valueStr = String.valueOf(bookingsPerDay[i]);
                    int valueX = x + (barWidth - fm.stringWidth(valueStr)) / 2;
                    g2.drawString(valueStr, valueX, y - 4);

                    // Day label at bottom
                    g2.setColor(UITheme.TEXT_SECONDARY);
                    g2.setFont(UITheme.FONT_SMALL);
                    fm = g2.getFontMetrics();
                    String dayStr = dayLabels[today.minusDays(6 - i).getDayOfWeek().getValue() % 7];
                    int labelX = x + (barWidth - fm.stringWidth(dayStr)) / 2;
                    g2.drawString(dayStr, labelX, padding + chartHeight + 16);
                }

                // Y-axis line
                g2.setColor(UITheme.BORDER_LIGHT);
                g2.drawLine(padding, padding, padding, padding + chartHeight);

                // X-axis line
                g2.drawLine(padding, padding + chartHeight, width - padding, padding + chartHeight);

                g2.dispose();
            }
        };
        chartPanel.setPreferredSize(new Dimension(0, 200));
        chartPanel.setBackground(UITheme.BG_WHITE);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_LIGHT),
            BorderFactory.createEmptyBorder(10, 10, 20, 10)
        ));

        chartContainer.add(chartPanel);

        return chartContainer;
    }

    private void loadAllBookings() {
        bookingsModel.setRowCount(0);
        List<Booking> bookings = bookingService.getAllBookings();
        for (Booking b : bookings) {
            bookingsModel.addRow(new Object[]{
                b.getBookingId(),
                b.getUserName(),
                b.getFacilityName(),
                b.getBookingDate(),
                b.getSlotLabel(),
                b.getPurpose(),
                b.getStatus(),
                b.getNotes()
            });
        }
        statsLabel.setText("Total bookings: " + bookings.size());
    }

    private void loadAllUsers() {
        usersModel.setRowCount(0);
        List<User> users = userDAO.findAll();
        for (User u : users) {
            usersModel.addRow(new Object[]{
                u.getUserId(),
                u.getUsername(),
                u.getFullName(),
                u.getRole(),
                u.getEmail(),
                "", // department
                ""  // extra id
            });
        }
    }

    private void adminCancelBooking() {
        int row = bookingsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a booking first.");
            return;
        }
        int bookingId = (int) bookingsModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Admin cancel booking #" + bookingId + "?",
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            model.User admin = new model.Admin(0, "admin", "", "", "", "");
            admin.setRole("ADMIN");
            boolean ok = bookingService.cancelBooking(bookingId, admin);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Cancelled.");
                loadAllBookings();
            } else {
                JOptionPane.showMessageDialog(this, "Failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
