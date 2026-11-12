package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Shared UI constants and utility methods used across all panels.
 * Light / white professional theme — clean, spacious, modern.
 * All algorithm logic is unchanged; only colours, fonts, and helpers are here.
 */
public class UIConstants {

    // ── Colour Palette (Light Theme) ──────────────────────────────────
    /** Main window background — near-white */
    public static final Color BG_DARK        = new Color(245, 246, 250);
    /** Card / section background — white */
    public static final Color BG_CARD        = Color.WHITE;
    /** Slightly tinted card variant */
    public static final Color BG_CARD2       = new Color(237, 240, 248);
    /** Input field background */
    public static final Color INPUT_BG       = new Color(250, 251, 253);

    // Accent colours — kept vivid so charts/graphs stay readable
    public static final Color ACCENT_BLUE    = new Color( 37, 99, 235);   // #2563EB
    public static final Color ACCENT_GREEN   = new Color( 16, 185, 129);  // #10B981
    public static final Color ACCENT_ORANGE  = new Color(234, 88,  12);   // #EA580C
    public static final Color ACCENT_PURPLE  = new Color(124, 58, 237);   // #7C3AED
    public static final Color ACCENT_PINK    = new Color(220, 38,  38);   // #DC2626
    public static final Color ACCENT_TEAL    = new Color(  6,182,212);    // #06B6D4

    // Text
    public static final Color TEXT_PRIMARY   = new Color( 17,  24,  39);  // near-black
    public static final Color TEXT_SECONDARY = new Color(107, 114, 128);  // muted grey

    // Borders
    public static final Color BORDER_COLOR   = new Color(209, 213, 219);  // light grey
    public static final Color BORDER_FOCUS   = new Color(147, 197, 253);  // blue focus ring

    // ── Fonts ─────────────────────────────────────────────────────────
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,  20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_MONO     = new Font("Consolas",  Font.PLAIN, 12);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BADGE    = new Font("Segoe UI", Font.BOLD,  11);

    // ── Button Builders ───────────────────────────────────────────────

    /** Primary action button */
    public static JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_SUBTITLE);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 20, 9, 20));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(bg.darker());
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    /** Smaller secondary button */
    public static JButton makeSmallButton(String text, Color bg) {
        JButton btn = makeButton(text, bg);
        btn.setFont(FONT_BODY);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        return btn;
    }

    // ── Panel / Card Builder ──────────────────────────────────────────

    /**
     * White card with a coloured left accent bar and a bold title header.
     * Keeps BorderLayout so callers can add CENTER/SOUTH content.
     */
    public static JPanel makeCard(String title, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(10, 12, 10, 12)
        ));

        if (title != null && !title.isEmpty()) {
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(BG_CARD);
            header.setBorder(new EmptyBorder(0, 0, 8, 0));

            // Coloured left stripe
            JPanel stripe = new JPanel();
            stripe.setPreferredSize(new Dimension(4, 0));
            stripe.setBackground(accentColor);
            header.add(stripe, BorderLayout.WEST);

            JLabel lbl = new JLabel("  " + title);
            lbl.setFont(FONT_SUBTITLE);
            lbl.setForeground(TEXT_PRIMARY);
            lbl.setOpaque(false);
            header.add(lbl, BorderLayout.CENTER);

            card.add(header, BorderLayout.NORTH);
        }
        return card;
    }

    // ── Form Components ───────────────────────────────────────────────

    /** Clean white text field with a subtle border */
    public static JTextField makeField(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(FONT_BODY);
        f.setBackground(INPUT_BG);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT_BLUE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(5, 8, 5, 8)
        ));
        // Focus highlight
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_FOCUS, 2),
                    new EmptyBorder(4, 7, 4, 7)));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR),
                    new EmptyBorder(5, 8, 5, 8)));
            }
        });
        return f;
    }

    /** Plain label with font + colour */
    public static JLabel makeLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        lbl.setOpaque(false);
        return lbl;
    }

    /** Clean combo box */
    public static <T> JComboBox<T> makeCombo(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setBackground(INPUT_BG);
        cb.setForeground(TEXT_PRIMARY);
        cb.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        return cb;
    }

    /**
     * Read-only monospaced output / log area.
     * Light background, dark text — easy to read in a white UI.
     */
    public static JTextArea makeOutputArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setFont(FONT_MONO);
        ta.setBackground(new Color(248, 250, 252));   // very light slate
        ta.setForeground(new Color(30, 41, 59));       // near-black text
        ta.setCaretColor(ACCENT_BLUE);
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(new EmptyBorder(10, 12, 10, 12));
        return ta;
    }

    /** Scroll pane styled to match the light theme */
    public static JScrollPane makeScroll(java.awt.Component comp) {
        JScrollPane sp = new JScrollPane(comp,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBackground(Color.WHITE);
        // Style scrollbar
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setBackground(BG_DARK);
        return sp;
    }

    /** Section divider label */
    public static JLabel makeSectionLabel(String text, Color color) {
        JLabel lbl = new JLabel("▶  " + text);
        lbl.setFont(FONT_SUBTITLE);
        lbl.setForeground(color);
        lbl.setBorder(new EmptyBorder(6, 0, 4, 0));
        return lbl;
    }
}
