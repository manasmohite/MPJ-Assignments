package ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Centralized theme constants for the Swing UI.
 * Modern light theme with white/soft-blue color scheme.
 */
public final class UITheme {

    private UITheme() {}

    // === Light Background Palette ===
    public static final Color BG_LIGHT       = new Color(248, 250, 252);   // Page background (almost white)
    public static final Color BG_WHITE       = new Color(255, 255, 255);   // Cards/panels
    public static final Color BG_SUBTLE      = new Color(241, 245, 249);   // Subtle section bg
    public static final Color SIDEBAR_BG     = new Color(255, 255, 255);   // Sidebar white
    public static final Color SIDEBAR_ACTIVE = new Color(238, 242, 255);   // Active nav item bg (soft indigo)

    // === Accent — Indigo/Blue ===
    public static final Color ACCENT         = new Color(99, 102, 241);    // Indigo-500
    public static final Color ACCENT_HOVER   = new Color(79, 70, 229);     // Indigo-600
    public static final Color ACCENT_LIGHT   = new Color(224, 231, 255);   // Indigo-100 (pill bg)

    // === Text ===
    public static final Color TEXT_PRIMARY   = new Color(15, 23, 42);      // Near-black
    public static final Color TEXT_SECONDARY = new Color(71, 85, 105);     // Slate-600
    public static final Color TEXT_MUTED     = new Color(148, 163, 184);   // Slate-400

    // === Status Colors ===
    public static final Color SUCCESS        = new Color(34, 197, 94);     // Green
    public static final Color SUCCESS_BG     = new Color(220, 252, 231);   // Green bg
    public static final Color WARNING        = new Color(234, 179, 8);     // Yellow
    public static final Color WARNING_BG     = new Color(254, 249, 195);   // Yellow bg
    public static final Color DANGER         = new Color(239, 68, 68);     // Red
    public static final Color DANGER_BG      = new Color(254, 226, 226);   // Red bg
    public static final Color INFO           = new Color(59, 130, 246);    // Blue
    public static final Color INFO_BG        = new Color(219, 234, 254);   // Blue bg

    // === Borders ===
    public static final Color BORDER_LIGHT   = new Color(226, 232, 240);   // Slate-200
    public static final Color BORDER_FOCUS   = new Color(99, 102, 241);    // Indigo on focus

    // === Fonts ===
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_HEADING  = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBHEAD  = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO     = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_BUTTON   = new Font("Segoe UI", Font.SEMI_BOLD, 13);

    // === Borders ===
    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT, 1),
            new EmptyBorder(16, 20, 16, 20)
        );
    }

    public static Border sectionBorder(String title) {
        Border lineBorder = BorderFactory.createLineBorder(BORDER_LIGHT, 1);
        return BorderFactory.createTitledBorder(
            lineBorder,
            title,
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            FONT_SUBHEAD,
            TEXT_SECONDARY
        );
    }

    public static Border paddingBorder(int v, int h) {
        return new EmptyBorder(v, h, v, h);
    }

    // === Component Factories ===

    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ACCENT);
        btn.setForeground(BG_WHITE);
        btn.setFont(FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        styleButtonHover(btn, ACCENT, ACCENT_HOVER, BG_WHITE, BG_WHITE);
        return btn;
    }

    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(BG_WHITE);
        btn.setForeground(TEXT_SECONDARY);
        btn.setFont(FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        styleButtonHover(btn, BG_WHITE, BG_SUBTLE, TEXT_SECONDARY, TEXT_PRIMARY);
        return btn;
    }

    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        styleButtonHover(btn, DANGER, new Color(220, 38, 38), Color.WHITE, Color.WHITE);
        return btn;
    }

    public static JButton dangerOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(BG_WHITE);
        btn.setForeground(DANGER);
        btn.setFont(FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createLineBorder(DANGER));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        styleButtonHover(btn, BG_WHITE, DANGER_BG, DANGER, DANGER);
        return btn;
    }

    public static JLabel titleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel headingLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_HEADING);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel bodyLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel mutedLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    public static JTextField styledField(int cols) {
        JTextField field = new JTextField(cols);
        field.setBackground(BG_WHITE);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT);
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT),
            new EmptyBorder(8, 12, 8, 12)
        ));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT, 2),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_LIGHT),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
        return field;
    }

    public static JPasswordField styledPassword(int cols) {
        JPasswordField field = new JPasswordField(cols);
        field.setBackground(BG_WHITE);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT);
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT),
            new EmptyBorder(8, 12, 8, 12)
        ));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT, 2),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_LIGHT),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
        return field;
    }

    public static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setBackground(BG_WHITE);
        combo.setForeground(TEXT_PRIMARY);
        combo.setFont(FONT_BODY);
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT),
            new EmptyBorder(4, 8, 4, 8)
        ));
        return combo;
    }

    public static JTextArea styledTextArea(int rows, int cols) {
        JTextArea area = new JTextArea(rows, cols);
        area.setBackground(BG_WHITE);
        area.setForeground(TEXT_PRIMARY);
        area.setCaretColor(ACCENT);
        area.setFont(FONT_BODY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(10, 12, 10, 12));
        return area;
    }

    public static JPanel lightPanel() {
        JPanel p = new JPanel();
        p.setBackground(BG_LIGHT);
        p.setOpaque(true);
        return p;
    }

    public static JPanel whitePanel() {
        JPanel p = new JPanel();
        p.setBackground(BG_WHITE);
        p.setOpaque(true);
        return p;
    }

    public static JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBackground(BG_WHITE);
        p.setOpaque(true);
        p.setBorder(cardBorder());
        return p;
    }

    public static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_LIGHT);
        sep.setBackground(BORDER_LIGHT);
        return sep;
    }

    public static void applyScrollPaneTheme(JScrollPane scroll) {
        scroll.setBackground(BG_LIGHT);
        scroll.getViewport().setBackground(BG_LIGHT);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
    }

    public static void applyTableTheme(javax.swing.JTable table) {
        table.setBackground(BG_WHITE);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(FONT_BODY);
        table.setGridColor(BORDER_LIGHT);
        table.setRowHeight(36);
        table.setSelectionBackground(ACCENT_LIGHT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.getTableHeader().setBackground(BG_SUBTLE);
        table.getTableHeader().setForeground(TEXT_SECONDARY);
        table.getTableHeader().setFont(FONT_SUBHEAD);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT));
        table.setFillsViewportHeight(true);
    }

    // Helper: hover effect
    private static void styleButtonHover(JButton btn,
                                          Color normalBg, Color hoverBg,
                                          Color normalFg, Color hoverFg) {
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(hoverBg);
                btn.setForeground(hoverFg);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(normalBg);
                btn.setForeground(normalFg);
            }
        });
    }
}
