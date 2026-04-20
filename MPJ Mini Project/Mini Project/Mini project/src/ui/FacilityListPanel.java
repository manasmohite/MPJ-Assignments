package ui;

import dao.FacilityDAO;
import dao.TimeSlotDAO;
import model.Facility;
import service.NavigationService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Panel showing all campus facilities with a modern card grid layout.
 */
public class FacilityListPanel extends JPanel {

    private final MainFrame mainFrame;
    private final NavigationService navService;
    private final FacilityDAO facilityDAO;
    private final TimeSlotDAO timeSlotDAO;

    private JTextField searchField;
    private JPanel facilitiesGrid;
    private JScrollPane scrollPane;
    private JLabel resultCountLabel;
    private JLabel statsTotalLabel;
    private JLabel statsAvailableLabel;
    private JLabel statsMyBookingsLabel;

    private JPanel selectedFacilityCard;
    private Facility selectedFacility;

    // Filter pills
    private JButton filterAll;
    private JButton filterClassroom;
    private JButton filterLab;
    private JButton filterAuditorium;
    private JButton filterSports;

    private String currentFilter = "ALL";

    public FacilityListPanel(MainFrame mainFrame) {
        this.mainFrame  = mainFrame;
        this.navService = new NavigationService();
        this.facilityDAO = new FacilityDAO();
        this.timeSlotDAO = new TimeSlotDAO();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(UITheme.BG_LIGHT);

        JLabel title = UITheme.headingLabel("🏛  Campus Facilities");
        header.add(title, BorderLayout.WEST);

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBar.setBackground(UITheme.BG_LIGHT);

        searchField = UITheme.styledField(20);
        searchField.setPreferredSize(new Dimension(280, 40));
        searchField.putClientProperty("placeholder", "Search by name, building, type...");
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    loadFacilities(searchField.getText());
                }
            }
        });

        JButton searchBtn = UITheme.primaryButton("🔍 Search");
        searchBtn.setPreferredSize(new Dimension(100, 40));
        searchBtn.addActionListener(e -> loadFacilities(searchField.getText()));

        JButton clearBtn = UITheme.secondaryButton("Clear");
        clearBtn.setPreferredSize(new Dimension(80, 40));
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            loadFacilities(null);
        });

        searchBar.add(searchField);
        searchBar.add(searchBtn);
        searchBar.add(clearBtn);

        header.add(searchBar, BorderLayout.EAST);
        return header;
    }

    private JPanel buildMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(UITheme.BG_LIGHT);

        // Stats row
        content.add(buildStatsRow(), BorderLayout.NORTH);

        // Filter pills
        content.add(buildFilterBar(), BorderLayout.CENTER);

        // Facilities grid
        content.add(buildGridPanel(), BorderLayout.SOUTH);

        return content;
    }

    private JPanel buildStatsRow() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        statsPanel.setBackground(UITheme.BG_LIGHT);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        // Total Facilities card
        statsPanel.add(buildStatCard("Total Facilities", "Loading...", UITheme.INFO, UITheme.INFO_BG));

        // Available Today card
        statsPanel.add(buildStatCard("Available Today", "Loading...", UITheme.SUCCESS, UITheme.SUCCESS_BG));

        // My Bookings card
        statsPanel.add(buildStatCard("My Bookings", "Loading...", UITheme.WARNING, UITheme.WARNING_BG));

        return statsPanel;
    }

    private JPanel buildStatCard(String label, String value, Color accentColor, Color bgColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        card.setOpaque(false);

        // Icon circle
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fillOval(0, 0, 40, 40);
                g2.dispose();
            }
        };
        iconPanel.setPreferredSize(new Dimension(40, 40));
        iconPanel.setLayout(new GridBagLayout());

        JLabel iconLabel = new JLabel();
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        iconPanel.add(iconLabel);

        if (label.contains("Total")) {
            iconLabel.setText("🏛");
            statsTotalLabel = new JLabel(value);
        } else if (label.contains("Available")) {
            iconLabel.setText("✅");
            statsAvailableLabel = new JLabel(value);
        } else {
            iconLabel.setText("📋");
            statsMyBookingsLabel = new JLabel(value);
        }

        JLabel valueLabel = label.contains("Total") ? statsTotalLabel
                          : label.contains("Available") ? statsAvailableLabel
                          : statsMyBookingsLabel;
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(UITheme.FONT_SMALL);
        nameLabel.setForeground(UITheme.TEXT_SECONDARY);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(valueLabel, BorderLayout.NORTH);
        textPanel.add(nameLabel, BorderLayout.SOUTH);

        card.add(iconPanel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildFilterBar() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setBackground(UITheme.BG_LIGHT);
        filterPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        filterAll = createFilterPill("All", true);
        filterClassroom = createFilterPill("Classroom", false);
        filterLab = createFilterPill("Lab", false);
        filterAuditorium = createFilterPill("Auditorium", false);
        filterSports = createFilterPill("Sports", false);

        filterPanel.add(filterAll);
        filterPanel.add(filterClassroom);
        filterPanel.add(filterLab);
        filterPanel.add(filterAuditorium);
        filterPanel.add(filterSports);

        return filterPanel;
    }

    private JButton createFilterPill(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BODY);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        if (active) {
            btn.setBackground(UITheme.ACCENT);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.ACCENT),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
            ));
        } else {
            btn.setBackground(UITheme.BG_WHITE);
            btn.setForeground(UITheme.TEXT_SECONDARY);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
            ));
        }

        final String filterType = text;
        btn.addActionListener(e -> {
            currentFilter = filterType;
            updateFilterPills();
            loadFacilities(searchField.getText());
        });

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!btn.equals(getActivePill())) {
                    btn.setBackground(UITheme.BG_SUBTLE);
                }
            }
            public void mouseExited(MouseEvent e) {
                if (!btn.equals(getActivePill())) {
                    btn.setBackground(UITheme.BG_WHITE);
                }
            }
        });

        return btn;
    }

    private JButton getActivePill() {
        return switch (currentFilter) {
            case "Classroom" -> filterClassroom;
            case "Lab" -> filterLab;
            case "Auditorium" -> filterAuditorium;
            case "Sports" -> filterSports;
            default -> filterAll;
        };
    }

    private void updateFilterPills() {
        JButton[] pills = {filterAll, filterClassroom, filterLab, filterAuditorium, filterSports};
        for (JButton btn : pills) {
            if (btn.equals(getActivePill())) {
                btn.setBackground(UITheme.ACCENT);
                btn.setForeground(Color.WHITE);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.ACCENT),
                    BorderFactory.createEmptyBorder(8, 20, 8, 20)
                ));
            } else {
                btn.setBackground(UITheme.BG_WHITE);
                btn.setForeground(UITheme.TEXT_SECONDARY);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.BORDER_LIGHT),
                    BorderFactory.createEmptyBorder(8, 20, 8, 20)
                ));
            }
        }
    }

    private JPanel buildGridPanel() {
        JPanel gridPanel = new JPanel(new BorderLayout());
        gridPanel.setBackground(UITheme.BG_LIGHT);

        facilitiesGrid = new JPanel();
        facilitiesGrid.setLayout(new BoxLayout(facilitiesGrid, BoxLayout.Y_AXIS));
        facilitiesGrid.setBackground(UITheme.BG_LIGHT);

        scrollPane = new JScrollPane(facilitiesGrid);
        scrollPane.setBackground(UITheme.BG_LIGHT);
        scrollPane.getViewport().setBackground(UITheme.BG_LIGHT);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        gridPanel.add(scrollPane, BorderLayout.CENTER);

        return gridPanel;
    }

    private void loadFacilities(String keyword) {
        facilitiesGrid.removeAll();

        List<Facility> facilities = facilityDAO.findAll();

        // Apply filter
        if (!"ALL".equals(currentFilter)) {
            facilities = facilities.stream()
                .filter(f -> currentFilter.equalsIgnoreCase(f.getType()))
                .toList();
        }

        // Apply search
        if (keyword != null && !keyword.isBlank()) {
            String searchLower = keyword.toLowerCase();
            facilities = facilities.stream()
                .filter(f -> f.getName().toLowerCase().contains(searchLower)
                          || f.getBuilding().toLowerCase().contains(searchLower)
                          || f.getType().toLowerCase().contains(searchLower))
                .toList();
        }

        // Update stats
        int totalFacilities = facilityDAO.findAll().size();
        int availableToday = (int) facilities.stream()
            .filter(f -> isFacilityAvailableToday(f.getFacilityId()))
            .count();
        int myBookings = getMyBookingsCount();

        statsTotalLabel.setText(String.valueOf(totalFacilities));
        statsAvailableLabel.setText(String.valueOf(availableToday));
        statsMyBookingsLabel.setText(String.valueOf(myBookings));

        resultCountLabel = new JLabel(facilities.size() + " facilities found");
        resultCountLabel.setFont(UITheme.FONT_SMALL);
        resultCountLabel.setForeground(UITheme.TEXT_MUTED);

        if (facilities.isEmpty()) {
            facilitiesGrid.add(buildEmptyState());
        } else {
            // Create cards in 2-column grid
            JPanel rowPanel = null;
            int colIndex = 0;

            for (Facility f : facilities) {
                if (colIndex == 0) {
                    rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
                    rowPanel.setBackground(UITheme.BG_LIGHT);
                    rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
                    facilitiesGrid.add(rowPanel);
                    facilitiesGrid.add(Box.createVerticalStrut(12));
                }

                JPanel card = buildFacilityCard(f);
                rowPanel.add(card);
                colIndex++;

                if (colIndex == 2) {
                    colIndex = 0;
                }
            }

            // Add filler if last row has only one card
            if (colIndex == 1) {
                JPanel filler = new JPanel();
                filler.setBackground(UITheme.BG_LIGHT);
                filler.setPreferredSize(new Dimension(300, 180));
                filler.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
                rowPanel.add(filler);
            }
        }

        facilitiesGrid.add(Box.createVerticalGlue());
        facilitiesGrid.revalidate();
        facilitiesGrid.repaint();
    }

    private boolean isFacilityAvailableToday(int facilityId) {
        // Simplified - check if any slots are available today
        try {
            List<Integer> bookedSlots = new java.util.ArrayList<>();
            // Would need BookingDAO to check properly
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    private int getMyBookingsCount() {
        try {
            model.User user = service.AuthService.getInstance().getCurrentUser();
            if (user == null) return 0;
            service.BookingService bookingService = new service.BookingService();
            return bookingService.getMyBookings(user.getUserId()).size();
        } catch (Exception e) {
            return 0;
        }
    }

    private JPanel buildEmptyState() {
        JPanel emptyPanel = new JPanel();
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.setBackground(UITheme.BG_LIGHT);
        emptyPanel.setBorder(BorderFactory.createEmptyBorder(60, 20, 60, 20));
        emptyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel icon = new JLabel("🔍");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("No facilities found");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Try adjusting your search or filter criteria");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyPanel.add(icon);
        emptyPanel.add(Box.createVerticalStrut(16));
        emptyPanel.add(title);
        emptyPanel.add(Box.createVerticalStrut(8));
        emptyPanel.add(subtitle);

        return emptyPanel;
    }

    private JPanel buildFacilityCard(Facility facility) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 12, 12);

                // Card background
                g2.setColor(UITheme.BG_WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);

                // Selection border
                if (card.equals(selectedFacilityCard)) {
                    g2.setColor(UITheme.ACCENT);
                    g2.setStroke(new BasicStroke(3));
                    g2.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 12, 12);
                }

                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(300, 180));
        card.setMaximumSize(new Dimension(300, 180));
        card.setOpaque(false);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Colored header bar based on facility type
        Color headerColor = getHeaderColorForType(facility.getType());
        JPanel headerBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(headerColor);
                g2.fillRoundRect(0, 0, getWidth(), 8, 8, 8);
                g2.dispose();
            }
        };
        headerBar.setPreferredSize(new Dimension(0, 8));
        headerBar.setOpaque(false);

        card.add(headerBar, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Facility name
        JLabel nameLabel = new JLabel(facility.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLabel.setForeground(UITheme.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Type badge
        JLabel typeBadge = new JLabel(facility.getType());
        typeBadge.setFont(UITheme.FONT_SMALL);
        typeBadge.setForeground(headerColor);
        typeBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(headerColor),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        typeBadge.setOpaque(true);
        typeBadge.setBackground(getLightVersion(headerColor));

        // Capacity with icon
        JLabel capacityLabel = new JLabel("👥 " + facility.getCapacity() + " people");
        capacityLabel.setFont(UITheme.FONT_SMALL);
        capacityLabel.setForeground(UITheme.TEXT_SECONDARY);
        capacityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Location with icon
        JLabel locationLabel = new JLabel("📍 " + facility.getBuilding() + ", Floor " + facility.getFloor());
        locationLabel.setFont(UITheme.FONT_SMALL);
        locationLabel.setForeground(UITheme.TEXT_SECONDARY);
        locationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        locationLabel.setMaximumSize(new Dimension(260, 30));

        // Description (truncated)
        String description = facility.getDescription() != null ? facility.getDescription() : "No description available";
        if (description.length() > 60) {
            description = description.substring(0, 57) + "...";
        }
        JLabel descLabel = new JLabel("<html><span style='color:#94a3b8;font-size:11px'>" + description + "</span></html>");
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(nameLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(typeBadge);
        content.add(Box.createVerticalStrut(8));
        content.add(capacityLabel);
        content.add(locationLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(descLabel);
        content.add(Box.createVerticalGlue());

        // Book Now button
        JButton bookBtn = new JButton("Book Now");
        bookBtn.setFont(UITheme.FONT_SMALL);
        bookBtn.setForeground(UITheme.ACCENT);
        bookBtn.setBackground(UITheme.ACCENT_LIGHT);
        bookBtn.setFocusPainted(false);
        bookBtn.setBorderPainted(false);
        bookBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bookBtn.setOpaque(true);
        bookBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        bookBtn.setMaximumSize(new Dimension(100, 28));
        bookBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        bookBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                bookBtn.setBackground(UITheme.ACCENT);
                bookBtn.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent e) {
                bookBtn.setBackground(UITheme.ACCENT_LIGHT);
                bookBtn.setForeground(UITheme.ACCENT);
            }
        });
        bookBtn.addActionListener(e -> {
            selectedFacility = facility;
            selectedFacilityCard = card;
            facilitiesGrid.repaint();
            mainFrame.showBookingForm(facility.getFacilityId());
        });

        content.add(Box.createVerticalStrut(10));
        content.add(bookBtn);

        card.add(content, BorderLayout.CENTER);

        // Click to select and book
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectedFacility = facility;
                selectedFacilityCard = card;
                facilitiesGrid.repaint();
                mainFrame.showBookingForm(facility.getFacilityId());
            }
        });

        return card;
    }

    private Color getHeaderColorForType(String type) {
        return switch (type.toLowerCase()) {
            case "classroom" -> new Color(59, 130, 246);    // Blue
            case "lab" -> new Color(34, 197, 94);           // Green
            case "auditorium" -> new Color(168, 85, 247);   // Purple
            case "sports" -> new Color(234, 179, 8);        // Yellow
            default -> UITheme.ACCENT;
        };
    }

    private Color getLightVersion(Color base) {
        return new Color(
            Math.min(255, base.getRed() + 100),
            Math.min(255, base.getGreen() + 100),
            Math.min(255, base.getBlue() + 100)
        );
    }
}
