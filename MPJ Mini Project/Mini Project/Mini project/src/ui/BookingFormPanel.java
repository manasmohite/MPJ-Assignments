package ui;

import dao.FacilityDAO;
import dao.TimeSlotDAO;
import model.*;
import service.AuthService;
import service.BookingService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Booking form panel — multi-step wizard with progress indicator.
 * Steps: 1. Select Facility → 2. Choose Date & Time → 3. Purpose & Notes → 4. Review & Confirm
 */
public class BookingFormPanel extends JPanel {

    private final MainFrame mainFrame;
    private final BookingService bookingService;
    private final FacilityDAO facilityDAO;
    private final TimeSlotDAO timeSlotDAO;
    private final AuthService authService;

    private CardLayout wizardCards;
    private JPanel wizardPanel;
    private JPanel progressContainer;

    private int currentStep = 0;
    private static final int TOTAL_STEPS = 4;

    // Step 1: Facility selection
    private JPanel facilityGrid;
    private Facility selectedFacility;
    private JPanel selectedFacilityCard;

    // Step 2: Date & Time
    private YearMonth currentMonth;
    private JPanel calendarGrid;
    private JPanel timeSlotContainer;
    private LocalDate selectedDate;
    private TimeSlot selectedTimeSlot;
    private List<TimeSlot> allSlots;

    // Step 3: Purpose & Notes
    private JComboBox<String> purposeCombo;
    private JTextArea notesArea;

    // Step 4: Review
    private JPanel reviewContainer;
    private JLabel bookingIdLabel;

    // Navigation buttons
    private JButton backButton;
    private JButton nextButton;

    public BookingFormPanel(MainFrame mainFrame) {
        this.mainFrame      = mainFrame;
        this.bookingService = new BookingService();
        this.facilityDAO    = new FacilityDAO();
        this.timeSlotDAO    = new TimeSlotDAO();
        this.authService    = AuthService.getInstance();
        this.currentMonth   = YearMonth.now();
        initUI();
    }

    public void setPreselectedFacility(int facilityId) {
        List<Facility> facilities = facilityDAO.findAll();
        for (Facility f : facilities) {
            if (f.getFacilityId() == facilityId) {
                selectedFacility = f;
                currentStep = 1; // Skip to step 2
                showStep(currentStep);
                break;
            }
        }
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Progress bar at top
        add(buildProgressBar(), BorderLayout.NORTH);

        // Wizard content area
        wizardCards = new CardLayout();
        wizardPanel = new JPanel(wizardCards);
        wizardPanel.setBackground(UITheme.BG_LIGHT);

        wizardPanel.add(buildStep1Panel(), "STEP_1");
        wizardPanel.add(buildStep2Panel(), "STEP_2");
        wizardPanel.add(buildStep3Panel(), "STEP_3");
        wizardPanel.add(buildStep4Panel(), "STEP_4");

        add(wizardPanel, BorderLayout.CENTER);

        // Navigation buttons at bottom
        add(buildNavigationPanel(), BorderLayout.SOUTH);

        showStep(0);
    }

    private JPanel buildProgressBar() {
        progressContainer = new JPanel();
        progressContainer.setLayout(new BoxLayout(progressContainer, BoxLayout.X_AXIS));
        progressContainer.setBackground(UITheme.BG_LIGHT);
        progressContainer.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        updateProgressBar();
        return progressContainer;
    }

    private void updateProgressBar() {
        progressContainer.removeAll();
        progressContainer.add(Box.createHorizontalGlue());

        String[] stepTitles = {"Select Facility", "Choose Date & Time", "Purpose & Notes", "Review & Confirm"};

        for (int i = 0; i < TOTAL_STEPS; i++) {
            JPanel stepPanel = new JPanel(new BorderLayout(8, 0));
            stepPanel.setOpaque(false);
            stepPanel.setMaximumSize(new Dimension(150, 60));

            // Circle
            JPanel circlePanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (i <= currentStep) {
                        g2.setColor(UITheme.ACCENT);
                    } else {
                        g2.setColor(UITheme.BORDER_LIGHT);
                    }
                    g2.fillOval(0, 0, 28, 28);
                    g2.dispose();
                }
            };
            circlePanel.setPreferredSize(new Dimension(28, 28));
            circlePanel.setLayout(new GridBagLayout());

            JLabel circleLabel = new JLabel(String.valueOf(i + 1));
            circleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            circleLabel.setForeground(i <= currentStep ? Color.WHITE : UITheme.TEXT_MUTED);
            circlePanel.add(circleLabel);

            // Checkmark for completed steps
            if (i < currentStep) {
                circleLabel.setText("✓");
            }

            // Title
            JLabel titleLabel = new JLabel(stepTitles[i]);
            titleLabel.setFont(i == currentStep ? UITheme.FONT_SUBHEAD : UITheme.FONT_SMALL);
            titleLabel.setForeground(i <= currentStep ? UITheme.TEXT_PRIMARY : UITheme.TEXT_MUTED);

            stepPanel.add(circlePanel, BorderLayout.WEST);
            stepPanel.add(titleLabel, BorderLayout.CENTER);

            progressContainer.add(stepPanel);

            if (i < TOTAL_STEPS - 1) {
                // Connector line
                JPanel linePanel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(i < currentStep ? UITheme.ACCENT : UITheme.BORDER_LIGHT);
                        g2.fillRect(0, 13, 40, 2);
                        g2.dispose();
                    }
                };
                linePanel.setPreferredSize(new Dimension(40, 16));
                linePanel.setOpaque(false);
                progressContainer.add(linePanel);
            }
        }

        progressContainer.add(Box.createHorizontalGlue());
        progressContainer.revalidate();
        progressContainer.repaint();
    }

    private JPanel buildStep1Panel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(UITheme.BG_LIGHT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_LIGHT);
        header.add(UITheme.headingLabel("Step 1: Select a Facility"), BorderLayout.WEST);
        panel.add(header, BorderLayout.NORTH);

        // Facility grid
        facilityGrid = new JPanel();
        facilityGrid.setLayout(new BoxLayout(facilityGrid, BoxLayout.Y_AXIS));
        facilityGrid.setBackground(UITheme.BG_LIGHT);

        List<Facility> facilities = facilityDAO.findAll();
        for (Facility f : facilities) {
            JPanel card = buildFacilityCard(f);
            facilityGrid.add(card);
            facilityGrid.add(Box.createVerticalStrut(10));
        }

        JScrollPane scroll = new JScrollPane(facilityGrid);
        scroll.setBackground(UITheme.BG_LIGHT);
        scroll.getViewport().setBackground(UITheme.BG_LIGHT);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildFacilityCard(Facility facility) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card background
                g2.setColor(UITheme.BG_WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Selection border
                if (card.equals(selectedFacilityCard)) {
                    g2.setColor(UITheme.ACCENT);
                    g2.setStroke(new BasicStroke(3));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 9, 9);
                }

                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(12, 0));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Left colored bar
        Color typeColor = getHeaderColorForType(facility.getType());
        JPanel colorBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(typeColor);
                g2.fillRoundRect(0, 0, 4, getHeight(), 2, 2);
                g2.dispose();
            }
        };
        colorBar.setPreferredSize(new Dimension(4, 0));
        colorBar.setOpaque(false);
        card.add(colorBar, BorderLayout.WEST);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel nameLabel = new JLabel(facility.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel typeLabel = new JLabel(facility.getType() + " • " + facility.getBuilding() + ", Floor " + facility.getFloor());
        typeLabel.setFont(UITheme.FONT_SMALL);
        typeLabel.setForeground(UITheme.TEXT_SECONDARY);

        JLabel capacityLabel = new JLabel("👥 Capacity: " + facility.getCapacity() + " people");
        capacityLabel.setFont(UITheme.FONT_SMALL);
        capacityLabel.setForeground(UITheme.TEXT_MUTED);

        content.add(nameLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(typeLabel);
        content.add(Box.createVerticalStrut(2));
        content.add(capacityLabel);

        card.add(content, BorderLayout.CENTER);

        // Select button
        JButton selectBtn = new JButton("Select");
        selectBtn.setFont(UITheme.FONT_SMALL);
        selectBtn.setForeground(UITheme.ACCENT);
        selectBtn.setBackground(UITheme.ACCENT_LIGHT);
        selectBtn.setFocusPainted(false);
        selectBtn.setBorderPainted(false);
        selectBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        selectBtn.setOpaque(true);
        selectBtn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        selectBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                selectBtn.setBackground(UITheme.ACCENT);
                selectBtn.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent e) {
                selectBtn.setBackground(UITheme.ACCENT_LIGHT);
                selectBtn.setForeground(UITheme.ACCENT);
            }
        });
        selectBtn.addActionListener(e -> {
            selectedFacility = facility;
            selectedFacilityCard = card;
            facilityGrid.repaint();
        });

        card.add(selectBtn, BorderLayout.EAST);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectedFacility = facility;
                selectedFacilityCard = card;
                facilityGrid.repaint();
            }
        });

        return card;
    }

    private JPanel buildStep2Panel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(UITheme.BG_LIGHT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_LIGHT);
        header.add(UITheme.headingLabel("Step 2: Choose Date & Time"), BorderLayout.WEST);
        panel.add(header, BorderLayout.NORTH);

        // Split: Calendar | Time Slots
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.5);
        split.setBackground(UITheme.BG_LIGHT);
        split.setBorder(null);

        split.setLeftComponent(buildCalendarPanel());
        split.setRightComponent(buildTimeSlotsPanel());

        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildCalendarPanel() {
        JPanel calendarPanel = new JPanel(new BorderLayout());
        calendarPanel.setBackground(UITheme.BG_WHITE);
        calendarPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_LIGHT),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        // Month navigation
        JPanel monthNav = new JPanel(new BorderLayout());
        monthNav.setOpaque(false);

        JButton prevBtn = new JButton("◀");
        prevBtn.setFont(UITheme.FONT_SMALL);
        prevBtn.setForeground(UITheme.TEXT_SECONDARY);
        prevBtn.setBackground(UITheme.BG_SUBTLE);
        prevBtn.setFocusPainted(false);
        prevBtn.setBorderPainted(false);
        prevBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        prevBtn.setOpaque(true);
        prevBtn.setPreferredSize(new Dimension(36, 36));
        prevBtn.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            rebuildCalendar();
        });

        JButton nextBtn = new JButton("▶");
        nextBtn.setFont(UITheme.FONT_SMALL);
        nextBtn.setForeground(UITheme.TEXT_SECONDARY);
        nextBtn.setBackground(UITheme.BG_SUBTLE);
        nextBtn.setFocusPainted(false);
        nextBtn.setBorderPainted(false);
        nextBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nextBtn.setOpaque(true);
        nextBtn.setPreferredSize(new Dimension(36, 36));
        nextBtn.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            rebuildCalendar();
        });

        JLabel monthLabel = new JLabel(currentMonth.getMonth().name() + " " + currentMonth.getYear());
        monthLabel.setFont(UITheme.FONT_HEADING);
        monthLabel.setForeground(UITheme.TEXT_PRIMARY);
        monthLabel.setHorizontalAlignment(SwingConstants.CENTER);

        monthNav.add(prevBtn, BorderLayout.WEST);
        monthNav.add(monthLabel, BorderLayout.CENTER);
        monthNav.add(nextBtn, BorderLayout.EAST);

        calendarPanel.add(monthNav, BorderLayout.NORTH);
        calendarPanel.add(Box.createVerticalStrut(12), BorderLayout.SOUTH);

        // Calendar grid
        calendarGrid = new JPanel(new GridLayout(7, 7, 2, 2));
        calendarGrid.setBackground(UITheme.BG_WHITE);
        calendarGrid.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        rebuildCalendar();

        JScrollPane scroll = new JScrollPane(calendarGrid);
        scroll.setBackground(UITheme.BG_WHITE);
        scroll.getViewport().setBackground(UITheme.BG_WHITE);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(300, 280));

        calendarPanel.add(scroll, BorderLayout.CENTER);

        return calendarPanel;
    }

    private void rebuildCalendar() {
        calendarGrid.removeAll();

        // Day headers
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            JLabel header = new JLabel(day);
            header.setFont(UITheme.FONT_SMALL);
            header.setForeground(UITheme.TEXT_MUTED);
            header.setHorizontalAlignment(SwingConstants.CENTER);
            calendarGrid.add(header);
        }

        // Days
        int firstDay = currentMonth.atDay(1).getDayOfWeek().getValue() % 7;
        int daysInMonth = currentMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        // Empty cells before first day
        for (int i = 0; i < firstDay; i++) {
            calendarGrid.add(new JPanel());
        }

        // Day buttons
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            JButton dayBtn = createDayButton(day, date, today);
            calendarGrid.add(dayBtn);
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private JButton createDayButton(int day, LocalDate date, LocalDate today) {
        JButton btn = new JButton(String.valueOf(day));
        btn.setFont(UITheme.FONT_BODY);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        boolean isToday = date.equals(today);
        boolean isPast = date.isBefore(today);
        boolean isSelected = date.equals(selectedDate);

        if (isSelected) {
            btn.setBackground(UITheme.ACCENT);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT));
        } else if (isPast) {
            btn.setBackground(UITheme.BG_SUBTLE);
            btn.setForeground(UITheme.TEXT_MUTED);
            btn.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_LIGHT));
            btn.setEnabled(false);
        } else if (isToday) {
            btn.setBackground(UITheme.ACCENT_LIGHT);
            btn.setForeground(UITheme.ACCENT);
            btn.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT));
        } else {
            btn.setBackground(UITheme.BG_WHITE);
            btn.setForeground(UITheme.TEXT_PRIMARY);
            btn.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_LIGHT));
        }

        btn.setPreferredSize(new Dimension(36, 36));
        btn.addActionListener(e -> {
            selectedDate = date;
            rebuildCalendar();
            loadTimeSlots();
        });

        return btn;
    }

    private JPanel buildTimeSlotsPanel() {
        timeSlotContainer = new JPanel();
        timeSlotContainer.setLayout(new BoxLayout(timeSlotContainer, BoxLayout.Y_AXIS));
        timeSlotContainer.setBackground(UITheme.BG_WHITE);
        timeSlotContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_LIGHT),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("Available Time Slots");
        title.setFont(UITheme.FONT_SUBHEAD);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        timeSlotContainer.add(title);
        timeSlotContainer.add(Box.createVerticalStrut(12));

        if (selectedDate == null) {
            JLabel hint = new JLabel("Select a date first");
            hint.setFont(UITheme.FONT_SMALL);
            hint.setForeground(UITheme.TEXT_MUTED);
            hint.setAlignmentX(Component.CENTER_ALIGNMENT);
            timeSlotContainer.add(hint);
        } else {
            loadTimeSlots();
        }

        return timeSlotContainer;
    }

    private void loadTimeSlots() {
        timeSlotContainer.removeAll();

        JLabel title = new JLabel("Available Time Slots");
        title.setFont(UITheme.FONT_SUBHEAD);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        timeSlotContainer.add(title);
        timeSlotContainer.add(Box.createVerticalStrut(12));

        if (selectedDate == null || selectedFacility == null) {
            JLabel hint = new JLabel("Select a date and facility first");
            hint.setFont(UITheme.FONT_SMALL);
            hint.setForeground(UITheme.TEXT_MUTED);
            hint.setAlignmentX(Component.CENTER_ALIGNMENT);
            timeSlotContainer.add(hint);
            timeSlotContainer.revalidate();
            timeSlotContainer.repaint();
            return;
        }

        allSlots = timeSlotDAO.findAll();

        // Get booked slots for this facility and date
        List<Integer> bookedSlots = bookingService.getTakenSlots(selectedFacility.getFacilityId(), selectedDate);

        JPanel slotsPanel = new JPanel();
        slotsPanel.setLayout(new GridLayout(0, 2, 8, 8));
        slotsPanel.setOpaque(false);

        for (TimeSlot slot : allSlots) {
            boolean isBooked = bookedSlots.contains(slot.getSlotId());
            JButton slotBtn = createTimeSlotChip(slot, isBooked);
            slotsPanel.add(slotBtn);
        }

        timeSlotContainer.add(slotsPanel);
        timeSlotContainer.add(Box.createVerticalGlue());

        timeSlotContainer.revalidate();
        timeSlotContainer.repaint();
    }

    private JButton createTimeSlotChip(TimeSlot slot, boolean isBooked) {
        JButton btn = new JButton(slot.getLabel());
        btn.setFont(UITheme.FONT_SMALL);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        boolean isSelected = slot.equals(selectedTimeSlot);

        if (isBooked) {
            btn.setBackground(UITheme.DANGER_BG);
            btn.setForeground(UITheme.DANGER);
            btn.setBorder(BorderFactory.createLineBorder(UITheme.DANGER));
            btn.setEnabled(false);
            btn.setText(slot.getLabel() + " ✕");
        } else if (isSelected) {
            btn.setBackground(UITheme.ACCENT);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT));
        } else {
            btn.setBackground(UITheme.SUCCESS_BG);
            btn.setForeground(UITheme.SUCCESS);
            btn.setBorder(BorderFactory.createLineBorder(UITheme.SUCCESS));
        }

        if (!isBooked) {
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (!isSelected) {
                        btn.setBackground(UITheme.SUCCESS);
                        btn.setForeground(Color.WHITE);
                    }
                }
                public void mouseExited(MouseEvent e) {
                    if (!isSelected) {
                        btn.setBackground(UITheme.SUCCESS_BG);
                        btn.setForeground(UITheme.SUCCESS);
                    }
                }
            });
            btn.addActionListener(e -> {
                selectedTimeSlot = slot;
                loadTimeSlots();
            });
        }

        return btn;
    }

    private JPanel buildStep3Panel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(UITheme.BG_LIGHT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_LIGHT);
        header.add(UITheme.headingLabel("Step 3: Purpose & Notes"), BorderLayout.WEST);
        panel.add(header, BorderLayout.NORTH);

        // Form card
        JPanel formCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setOpaque(false);
        formCard.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        formCard.setMaximumSize(new Dimension(500, 400));
        formCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Purpose dropdown
        JPanel purposePanel = new JPanel(new BorderLayout(8, 0));
        purposePanel.setOpaque(false);
        purposePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        purposePanel.add(UITheme.bodyLabel("Purpose of Booking"), BorderLayout.WEST);

        String[] purposes = buildPurposeOptions();
        purposeCombo = new JComboBox<>(purposes);
        purposeCombo.setFont(UITheme.FONT_BODY);
        purposeCombo.setPreferredSize(new Dimension(200, 40));
        purposePanel.add(purposeCombo, BorderLayout.EAST);

        purposePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Notes text area
        JPanel notesPanel = new JPanel(new BorderLayout(0, 8));
        notesPanel.setOpaque(false);
        notesPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        notesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        notesPanel.add(UITheme.bodyLabel("Additional Notes (optional)"), BorderLayout.NORTH);

        notesArea = UITheme.styledTextArea(6, 40);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(0, 120));
        UITheme.applyScrollPaneTheme(notesScroll);
        notesPanel.add(notesScroll, BorderLayout.CENTER);

        formCard.add(purposePanel);
        formCard.add(Box.createVerticalStrut(20));
        formCard.add(notesPanel);
        formCard.add(Box.createVerticalGlue());

        panel.add(formCard, BorderLayout.CENTER);

        return panel;
    }

    private String[] buildPurposeOptions() {
        User user = authService.getCurrentUser();
        if ("ADMIN".equals(user.getRole())) {
            return new String[]{"EXAM", "FACULTY_CLASS", "CLUB_EVENT", "STUDENT_STUDY", "MEETING", "OTHER"};
        } else if ("FACULTY".equals(user.getRole())) {
            return new String[]{"FACULTY_CLASS", "CLUB_EVENT", "STUDENT_STUDY", "MEETING", "OTHER"};
        } else {
            return new String[]{"CLUB_EVENT", "STUDENT_STUDY", "MEETING", "OTHER"};
        }
    }

    private JPanel buildStep4Panel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(UITheme.BG_LIGHT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_LIGHT);
        header.add(UITheme.headingLabel("Step 4: Review & Confirm"), BorderLayout.WEST);
        panel.add(header, BorderLayout.NORTH);

        // Review card
        reviewContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        reviewContainer.setLayout(new BoxLayout(reviewContainer, BoxLayout.Y_AXIS));
        reviewContainer.setOpaque(false);
        reviewContainer.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        reviewContainer.setMaximumSize(new Dimension(500, 400));
        reviewContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        updateReviewCard();
        reviewContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(reviewContainer, BorderLayout.CENTER);

        return panel;
    }

    private void updateReviewCard() {
        reviewContainer.removeAll();

        User user = authService.getCurrentUser();

        // Summary items
        addReviewRow(reviewContainer, "Facility", selectedFacility.getName() + " (" + selectedFacility.getBuilding() + ")");
        addReviewRow(reviewContainer, "Date", selectedDate.toString());
        addReviewRow(reviewContainer, "Time Slot", selectedTimeSlot.getLabel());
        addReviewRow(reviewContainer, "Purpose", (String) purposeCombo.getSelectedItem());
        addReviewRow(reviewContainer, "Booked By", user.getFullName() + " (" + user.getRoleLabel() + ")");
        addReviewRow(reviewContainer, "Priority Level", String.valueOf(user.getBookingPriority()));

        if (notesArea != null && !notesArea.getText().trim().isEmpty()) {
            reviewContainer.add(Box.createVerticalStrut(16));
            reviewContainer.add(UITheme.bodyLabel("Notes:"));
            reviewContainer.add(Box.createVerticalStrut(6));
            JLabel notesLabel = new JLabel("<html><span style='color:#475569'>" + notesArea.getText().trim() + "</span></html>");
            notesLabel.setFont(UITheme.FONT_BODY);
            reviewContainer.add(notesLabel);
        }

        reviewContainer.revalidate();
        reviewContainer.repaint();
    }

    private void addReviewRow(JPanel container, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(UITheme.FONT_SMALL);
        labelLbl.setForeground(UITheme.TEXT_SECONDARY);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(UITheme.FONT_BODY);
        valueLbl.setForeground(UITheme.TEXT_PRIMARY);

        row.add(labelLbl, BorderLayout.WEST);
        row.add(valueLbl, BorderLayout.EAST);

        container.add(row);
        container.add(Box.createVerticalStrut(8));
    }

    private JPanel buildNavigationPanel() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        navPanel.setBackground(UITheme.BG_LIGHT);

        backButton = UITheme.secondaryButton("← Back");
        backButton.setPreferredSize(new Dimension(100, 40));
        backButton.addActionListener(e -> goToPreviousStep());

        nextButton = UITheme.primaryButton(currentStep == TOTAL_STEPS - 1 ? "✓ Confirm Booking" : "Next →");
        nextButton.setPreferredSize(new Dimension(140, 40));
        nextButton.addActionListener(e -> goToNextStep());

        navPanel.add(backButton);
        navPanel.add(nextButton);

        updateNavigationButtons();
        return navPanel;
    }

    private void updateNavigationButtons() {
        backButton.setEnabled(currentStep > 0);
        backButton.setVisible(currentStep > 0);

        if (currentStep == TOTAL_STEPS - 1) {
            nextButton.setText("✓ Confirm Booking");
            nextButton.setBackground(UITheme.SUCCESS);
        } else {
            nextButton.setText("Next →");
            nextButton.setBackground(UITheme.ACCENT);
        }
    }

    private void showStep(int step) {
        currentStep = step;
        updateProgressBar();
        updateNavigationButtons();

        String stepName = "STEP_" + (step + 1);
        wizardCards.show(wizardPanel, stepName);

        // Refresh step-specific data
        if (step == 1) {
            rebuildCalendar();
        } else if (step == 3) {
            updateReviewCard();
        } else if (step == 4) {
            // Show success banner
            showSuccessBanner();
        }
    }

    private void showSuccessBanner() {
        wizardPanel.removeAll();

        JPanel successPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SUCCESS_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        successPanel.setLayout(new BoxLayout(successPanel, BoxLayout.Y_AXIS));
        successPanel.setOpaque(false);
        successPanel.setBorder(BorderFactory.createEmptyBorder(60, 40, 60, 40));
        successPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        successPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Success icon
        JLabel icon = new JLabel("✓");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 48));
        icon.setForeground(UITheme.SUCCESS);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Success message
        JLabel title = new JLabel("Booking Confirmed!");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Booking ID
        bookingIdLabel = new JLabel("Booking ID: #");
        bookingIdLabel.setFont(UITheme.FONT_BODY);
        bookingIdLabel.setForeground(UITheme.TEXT_SECONDARY);
        bookingIdLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        successPanel.add(icon);
        successPanel.add(Box.createVerticalStrut(16));
        successPanel.add(title);
        successPanel.add(Box.createVerticalStrut(8));
        successPanel.add(bookingIdLabel);
        successPanel.add(Box.createVerticalStrut(24));

        JButton newBookingBtn = UITheme.primaryButton("Make Another Booking");
        newBookingBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        newBookingBtn.addActionListener(e -> resetWizard());
        successPanel.add(newBookingBtn);

        wizardPanel.add(successPanel);
        wizardPanel.revalidate();
        wizardPanel.repaint();
    }

    private void resetWizard() {
        selectedFacility = null;
        selectedFacilityCard = null;
        selectedDate = null;
        selectedTimeSlot = null;
        currentMonth = YearMonth.now();
        currentStep = 0;

        wizardPanel.removeAll();
        wizardPanel.add(buildStep1Panel(), "STEP_1");
        wizardPanel.add(buildStep2Panel(), "STEP_2");
        wizardPanel.add(buildStep3Panel(), "STEP_3");
        wizardPanel.add(buildStep4Panel(), "STEP_4");

        showStep(0);
    }

    private void goToNextStep() {
        // Validation
        if (currentStep == 0 && selectedFacility == null) {
            JOptionPane.showMessageDialog(this, "Please select a facility to continue.", "Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentStep == 1 && (selectedDate == null || selectedTimeSlot == null)) {
            JOptionPane.showMessageDialog(this, "Please select a date and time slot to continue.", "Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentStep == 2 && purposeCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a purpose to continue.", "Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentStep == TOTAL_STEPS - 1) {
            // Submit booking
            submitBooking();
        } else {
            showStep(currentStep + 1);
        }
    }

    private void goToPreviousStep() {
        if (currentStep > 0) {
            showStep(currentStep - 1);
        }
    }

    private void submitBooking() {
        nextButton.setEnabled(false);
        nextButton.setText("Processing...");

        User user = authService.getCurrentUser();
        Booking.Purpose purpose;
        try {
            purpose = Booking.Purpose.valueOf((String) purposeCombo.getSelectedItem());
        } catch (Exception e) {
            purpose = Booking.Purpose.OTHER;
        }

        String notes = notesArea != null ? notesArea.getText().trim() : "";

        SwingWorker<BookingRequest, Void> worker = new SwingWorker<>() {
            @Override
            protected BookingRequest doInBackground() {
                return bookingService.submitBooking(
                    user, selectedFacility.getFacilityId(), selectedTimeSlot.getSlotId(),
                    selectedDate, purpose, notes
                );
            }

            @Override
            protected void done() {
                try {
                    BookingRequest req = get();
                    if (bookingIdLabel != null) {
                        bookingIdLabel.setText("Booking ID: #" + req.getBookingId());
                    }
                    showStep(4); // Show success
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BookingFormPanel.this,
                        "Error: " + ex.getMessage(),
                        "Booking Failed",
                        JOptionPane.ERROR_MESSAGE);
                    nextButton.setEnabled(true);
                    nextButton.setText("✓ Confirm Booking");
                }
            }
        };
        worker.execute();
    }

    private Color getHeaderColorForType(String type) {
        return switch (type.toLowerCase()) {
            case "classroom" -> new Color(59, 130, 246);
            case "lab" -> new Color(34, 197, 94);
            case "auditorium" -> new Color(168, 85, 247);
            case "sports" -> new Color(234, 179, 8);
            default -> UITheme.ACCENT;
        };
    }
}
