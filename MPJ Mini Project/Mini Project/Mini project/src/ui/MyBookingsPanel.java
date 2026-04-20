package ui;

import model.Booking;
import model.User;
import service.AuthService;
import service.BookingService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel showing current user's bookings with tabbed interface and status cards.
 */
public class MyBookingsPanel extends JPanel {

    private final MainFrame mainFrame;
    private final BookingService bookingService;
    private final AuthService authService;

    private JTabbedPane tabbedPane;
    private JPanel upcomingPanel;
    private JPanel pastPanel;
    private JPanel cancelledPanel;
    private JLabel summaryLabel;

    private List<Booking> allBookings;

    public MyBookingsPanel(MainFrame mainFrame) {
        this.mainFrame      = mainFrame;
        this.bookingService = new BookingService();
        this.authService    = AuthService.getInstance();
        initUI();
    }

    public void refresh() {
        loadBookings();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabbedContent(), BorderLayout.CENTER);

        loadBookings();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_LIGHT);

        JLabel title = UITheme.headingLabel("📋  My Bookings");
        header.add(title, BorderLayout.WEST);

        summaryLabel = UITheme.mutedLabel(" ");
        header.add(summaryLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel buildTabbedContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(UITheme.BG_LIGHT);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(UITheme.BG_LIGHT);
        tabbedPane.setForeground(UITheme.TEXT_PRIMARY);
        tabbedPane.setFont(UITheme.FONT_SUBHEAD);

        // Style the tabbed panel
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) refreshTab(upcomingPanel);
            else if (selectedIndex == 1) refreshTab(pastPanel);
            else if (selectedIndex == 2) refreshTab(cancelledPanel);
        });

        upcomingPanel = new JPanel();
        upcomingPanel.setBackground(UITheme.BG_LIGHT);
        upcomingPanel.setLayout(new BoxLayout(upcomingPanel, BoxLayout.Y_AXIS));

        pastPanel = new JPanel();
        pastPanel.setBackground(UITheme.BG_LIGHT);
        pastPanel.setLayout(new BoxLayout(pastPanel, BoxLayout.Y_AXIS));

        cancelledPanel = new JPanel();
        cancelledPanel.setBackground(UITheme.BG_LIGHT);
        cancelledPanel.setLayout(new BoxLayout(cancelledPanel, BoxLayout.Y_AXIS));

        tabbedPane.addTab("Upcoming", upcomingPanel);
        tabbedPane.addTab("Past", pastPanel);
        tabbedPane.addTab("Cancelled", cancelledPanel);

        // Custom tab rendering for active state
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            final int index = i;
            JLabel tabLabel = new JLabel(tabbedPane.getTitleAt(i));
            tabLabel.setFont(UITheme.FONT_BODY);
            tabLabel.setForeground(UITheme.TEXT_SECONDARY);
            tabLabel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            tabbedPane.setTabComponentAt(i, tabLabel);
        }

        tabbedPane.addChangeListener(e -> {
            int selected = tabbedPane.getSelectedIndex();
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                JLabel tabLabel = (JLabel) tabbedPane.getTabComponentAt(i);
                if (i == selected) {
                    tabLabel.setForeground(UITheme.ACCENT);
                    tabLabel.setFont(UITheme.FONT_SUBHEAD);
                } else {
                    tabLabel.setForeground(UITheme.TEXT_SECONDARY);
                    tabLabel.setFont(UITheme.FONT_BODY);
                }
            }
        });

        // Set first tab as selected with active styling
        JLabel firstTab = (JLabel) tabbedPane.getTabComponentAt(0);
        if (firstTab != null) {
            firstTab.setForeground(UITheme.ACCENT);
            firstTab.setFont(UITheme.FONT_SUBHEAD);
        }

        content.add(tabbedPane, BorderLayout.CENTER);

        return content;
    }

    private void refreshTab(JPanel panel) {
        panel.removeAll();
        loadBookingsIntoPanel(panel, getFilterForPanel(panel));
        panel.revalidate();
        panel.repaint();
    }

    private String getFilterForPanel(JPanel panel) {
        if (panel == upcomingPanel) return "UPCOMING";
        if (panel == pastPanel) return "PAST";
        if (panel == cancelledPanel) return "CANCELLED";
        return "ALL";
    }

    private void loadBookings() {
        allBookings = bookingService.getMyBookings(authService.getCurrentUser().getUserId());

        // Update summary
        long upcoming = allBookings.stream()
            .filter(b -> b.getStatus() != Booking.Status.CANCELLED && !isPastBooking(b))
            .count();
        long past = allBookings.stream()
            .filter(b -> isPastBooking(b) && b.getStatus() != Booking.Status.CANCELLED)
            .count();
        long cancelled = allBookings.stream()
            .filter(b -> b.getStatus() == Booking.Status.CANCELLED)
            .count();

        summaryLabel.setText("Total: " + allBookings.size()
            + "  |  Upcoming: " + upcoming
            + "  |  Past: " + past
            + "  |  Cancelled: " + cancelled);

        // Load initial tab
        refreshTab(upcomingPanel);
    }

    private boolean isPastBooking(Booking booking) {
        try {
            LocalDate bookingDate = LocalDate.parse(booking.getBookingDate());
            return bookingDate.isBefore(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    private void loadBookingsIntoPanel(JPanel panel, String filter) {
        panel.removeAll();
        panel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        List<Booking> filteredBookings = allBookings.stream()
            .filter(b -> {
                if ("UPCOMING".equals(filter)) {
                    return b.getStatus() != Booking.Status.CANCELLED && !isPastBooking(b);
                } else if ("PAST".equals(filter)) {
                    return isPastBooking(b) && b.getStatus() != Booking.Status.CANCELLED;
                } else if ("CANCELLED".equals(filter)) {
                    return b.getStatus() == Booking.Status.CANCELLED;
                }
                return true;
            })
            .toList();

        if (filteredBookings.isEmpty()) {
            panel.add(buildEmptyState(filter));
            panel.add(Box.createVerticalGlue());
            return;
        }

        for (Booking booking : filteredBookings) {
            JPanel card = buildBookingCard(booking, filter);
            panel.add(card);
            panel.add(Box.createVerticalStrut(10));
        }

        panel.add(Box.createVerticalGlue());
    }

    private JPanel buildEmptyState(String filter) {
        JPanel emptyPanel = new JPanel();
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.setBackground(UITheme.BG_LIGHT);
        emptyPanel.setBorder(BorderFactory.createEmptyBorder(60, 20, 60, 20));
        emptyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String iconText = switch (filter) {
            case "UPCOMING" -> "📅";
            case "PAST" -> "📜";
            case "CANCELLED" -> "❌";
            default -> "📋";
        };

        String titleText = switch (filter) {
            case "UPCOMING" -> "No upcoming bookings";
            case "PAST" -> "No past bookings";
            case "CANCELLED" -> "No cancelled bookings";
            default -> "No bookings found";
        };

        JLabel icon = new JLabel(iconText);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel(titleText);
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        String subtitleText = switch (filter) {
            case "UPCOMING" -> "Book a facility to get started!";
            case "PAST" -> "Your past bookings will appear here.";
            case "CANCELLED" -> "Cancelled bookings will appear here.";
            default -> "";
        };

        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyPanel.add(icon);
        emptyPanel.add(Box.createVerticalStrut(16));
        emptyPanel.add(title);
        emptyPanel.add(Box.createVerticalStrut(8));
        emptyPanel.add(subtitle);

        if ("UPCOMING".equals(filter)) {
            emptyPanel.add(Box.createVerticalStrut(24));
            JButton bookBtn = UITheme.primaryButton("+ New Booking");
            bookBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            bookBtn.addActionListener(e -> mainFrame.showFacilities());
            emptyPanel.add(bookBtn);
        }

        return emptyPanel;
    }

    private JPanel buildBookingCard(Booking booking, String filter) {
        Color statusColor = getStatusColor(booking.getStatus());
        Color statusBgColor = getStatusBgColor(booking.getStatus());

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card shadow
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 10, 10);

                // Card background
                g2.setColor(UITheme.BG_WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 10, 10);

                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.setPreferredSize(new Dimension(0, 110));

        // Left status bar
        JPanel statusBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(statusColor);
                g2.fillRoundRect(0, 0, 6, getHeight(), 6, 6);
                g2.dispose();
            }
        };
        statusBar.setPreferredSize(new Dimension(6, 0));
        statusBar.setOpaque(false);

        card.add(statusBar, BorderLayout.WEST);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Top row: Facility name + status badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel facilityLabel = new JLabel(booking.getFacilityName());
        facilityLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        facilityLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel statusBadge = new JLabel(booking.getStatus().toString());
        statusBadge.setFont(UITheme.FONT_SMALL);
        statusBadge.setForeground(statusColor);
        statusBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(statusColor),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        statusBadge.setOpaque(true);
        statusBadge.setBackground(statusBgColor);

        topRow.add(facilityLabel, BorderLayout.WEST);
        topRow.add(statusBadge, BorderLayout.EAST);

        // Middle row: Date + Time
        JPanel middleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        middleRow.setOpaque(false);
        middleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel dateLabel = new JLabel("📅 " + booking.getBookingDate());
        dateLabel.setFont(UITheme.FONT_BODY);
        dateLabel.setForeground(UITheme.TEXT_SECONDARY);

        JLabel timeLabel = new JLabel("🕐 " + booking.getSlotLabel());
        timeLabel.setFont(UITheme.FONT_BODY);
        timeLabel.setForeground(UITheme.TEXT_SECONDARY);

        middleRow.add(dateLabel);
        middleRow.add(timeLabel);

        // Bottom row: Purpose pill + Notes preview
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel purposeBadge = new JLabel(booking.getPurpose().toString());
        purposeBadge.setFont(UITheme.FONT_SMALL);
        purposeBadge.setForeground(UITheme.ACCENT);
        purposeBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.ACCENT_LIGHT),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        purposeBadge.setOpaque(true);
        purposeBadge.setBackground(UITheme.ACCENT_LIGHT);

        String notesPreview = "";
        if (booking.getNotes() != null && !booking.getNotes().trim().isEmpty()) {
            String notes = booking.getNotes().trim();
            if (notes.length() > 40) {
                notes = notes.substring(0, 37) + "...";
            }
            notesPreview = "  •  " + notes;
        }
        final String notesFinal = notesPreview;
        JLabel notesLabel = new JLabel(notesFinal);
        notesLabel.setFont(UITheme.FONT_SMALL);
        notesLabel.setForeground(UITheme.TEXT_MUTED);

        bottomRow.add(purposeBadge, BorderLayout.WEST);
        bottomRow.add(notesLabel, BorderLayout.CENTER);

        content.add(topRow);
        content.add(Box.createVerticalStrut(8));
        content.add(middleRow);
        content.add(Box.createVerticalStrut(8));
        content.add(bottomRow);

        card.add(content, BorderLayout.CENTER);

        // Right side: Cancel button (only for upcoming confirmed/waitlisted bookings)
        if ("UPCOMING".equals(filter) &&
            (booking.getStatus() == Booking.Status.CONFIRMED || booking.getStatus() == Booking.Status.WAITLISTED)) {
            JButton cancelBtn = new JButton("Cancel");
            cancelBtn.setFont(UITheme.FONT_SMALL);
            cancelBtn.setForeground(UITheme.DANGER);
            cancelBtn.setBackground(UITheme.BG_WHITE);
            cancelBtn.setFocusPainted(false);
            cancelBtn.setBorderPainted(true);
            cancelBtn.setBorder(BorderFactory.createLineBorder(UITheme.DANGER));
            cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cancelBtn.setOpaque(true);
            cancelBtn.setPreferredSize(new Dimension(90, 32));
            cancelBtn.setMaximumSize(new Dimension(90, 32));
            cancelBtn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    cancelBtn.setBackground(UITheme.DANGER);
                    cancelBtn.setForeground(Color.WHITE);
                }
                public void mouseExited(MouseEvent e) {
                    cancelBtn.setBackground(UITheme.BG_WHITE);
                    cancelBtn.setForeground(UITheme.DANGER);
                }
            });
            cancelBtn.addActionListener(e -> cancelBooking(booking));

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
            buttonPanel.setOpaque(false);
            buttonPanel.add(cancelBtn);

            card.add(buttonPanel, BorderLayout.EAST);
        }

        return card;
    }

    private Color getStatusColor(Booking.Status status) {
        return switch (status) {
            case CONFIRMED -> UITheme.SUCCESS;
            case WAITLISTED -> UITheme.WARNING;
            case CANCELLED -> UITheme.TEXT_MUTED;
            case OVERRIDDEN -> UITheme.DANGER;
        };
    }

    private Color getStatusBgColor(Booking.Status status) {
        return switch (status) {
            case CONFIRMED -> UITheme.SUCCESS_BG;
            case WAITLISTED -> UITheme.WARNING_BG;
            case CANCELLED -> UITheme.BG_SUBTLE;
            case OVERRIDDEN -> UITheme.DANGER_BG;
        };
    }

    private void cancelBooking(Booking booking) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel booking #" + booking.getBookingId() + "?",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            User user = authService.getCurrentUser();
            boolean success = bookingService.cancelBooking(booking.getBookingId(), user);

            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Booking cancelled successfully.",
                    "Cancelled",
                    JOptionPane.INFORMATION_MESSAGE);
                refresh();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to cancel booking. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
