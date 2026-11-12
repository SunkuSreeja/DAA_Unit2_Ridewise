package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static ui.UIConstants.*;

/**
 * Main application window for RideWise.
 * Contains the tab bar that navigates between the five panels:
 *   Dashboard | Cab Matching | Ride Scheduling | Network Planning | Shortest Route
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("this-escape")
    public MainFrame() {
        super("RideWise — Smart Cab Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1340, 860);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        // Use cross-platform L&F as base so colours apply correctly
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Style scroll bars globally
        UIManager.put("ScrollBar.thumb",         new javax.swing.plaf.ColorUIResource(BORDER_COLOR));
        UIManager.put("ScrollBar.track",         new javax.swing.plaf.ColorUIResource(BG_DARK));
        UIManager.put("ScrollBar.thumbHighlight",new javax.swing.plaf.ColorUIResource(BORDER_COLOR));
        UIManager.put("ScrollBar.thumbShadow",   new javax.swing.plaf.ColorUIResource(BORDER_COLOR));

        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTabs(),      BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ── Top bar ───────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(10, 20, 10, 20)
        ));

        // Logo + subtitle
        JLabel logo = new JLabel("🚖  RideWise");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(ACCENT_BLUE);

        JLabel sub = makeLabel("Smart Cab Management System  |  DAA Unit II Case Study",
                               FONT_BODY, TEXT_SECONDARY);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setBackground(Color.WHITE);
        left.add(logo);
        left.add(sub);
        bar.add(left, BorderLayout.WEST);

        // Algorithm badges on the right
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(Color.WHITE);
        right.add(makePill("Divide & Conquer", ACCENT_BLUE));
        right.add(makePill("Greedy",           ACCENT_ORANGE));
        right.add(makePill("MST",              ACCENT_GREEN));
        right.add(makePill("Dijkstra",         ACCENT_PURPLE));
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    // ── Tabbed pane ───────────────────────────────────────────────────
    private JTabbedPane buildTabs() {
        // Theme the tab strip
        UIManager.put("TabbedPane.selected",              BG_DARK);
        UIManager.put("TabbedPane.background",            new Color(235, 237, 245));
        UIManager.put("TabbedPane.foreground",            TEXT_PRIMARY);
        UIManager.put("TabbedPane.selectedForeground",    ACCENT_BLUE);
        UIManager.put("TabbedPane.tabAreaBackground",     new Color(235, 237, 245));
        UIManager.put("TabbedPane.contentBorderInsets",   new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabInsets",             new Insets(10, 18, 10, 18));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(235, 237, 245));
        tabs.setForeground(TEXT_PRIMARY);
        tabs.setFont(FONT_SUBTITLE);
        tabs.setTabPlacement(JTabbedPane.TOP);

        tabs.addTab("🏠  Dashboard",         new DashboardPanel());
        tabs.addTab("🚕  Cab Matching",       new ClosestPairPanel());
        tabs.addTab("📋  Ride Scheduling",    new JobSequencingPanel());
        tabs.addTab("🌐  Network Planning",   new MSTPanel());
        tabs.addTab("🗺  Shortest Route",     new DijkstraPanel());

        return tabs;
    }

    // ── Status bar ────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
            new EmptyBorder(6, 20, 6, 20)
        ));

        JLabel status = makeLabel(
            "RideWise v1.0  ·  Sample Data: Hyderabad Metro  ·  " +
            "6 Cabs · 8 Passengers · 9 Locations · 14 Roads",
            FONT_SMALL, TEXT_SECONDARY);
        JLabel hint = makeLabel("Select a tab to explore each algorithm →",
                                FONT_SMALL, ACCENT_BLUE);

        bar.add(status, BorderLayout.WEST);
        bar.add(hint,   BorderLayout.EAST);
        return bar;
    }

    // ── Pill badge ────────────────────────────────────────────────────
    private JLabel makePill(String text, Color color) {
        // Tinted background from the accent colour
        Color bg = new Color(color.getRed(), color.getGreen(), color.getBlue(), 18);
        Color border = new Color(color.getRed(), color.getGreen(), color.getBlue(), 100);

        JLabel l = new JLabel("  " + text + "  ");
        l.setFont(FONT_BADGE);
        l.setForeground(color);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(BorderFactory.createLineBorder(border, 1));
        return l;
    }
}
