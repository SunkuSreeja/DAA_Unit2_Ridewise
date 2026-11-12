package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static ui.UIConstants.*;

/**
 * Welcome dashboard shown when the application first opens.
 * Summarises all four modules and the DAA concepts they demonstrate.
 */
public class DashboardPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("this-escape")
    public DashboardPanel() {
        setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));
        buildUI();
    }

    private void buildUI() {
        // ---- Scrollable content ----
        JPanel content = new JPanel();
        content.setBackground(BG_DARK);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Hero title
        content.add(makeHero());
        content.add(Box.createVerticalStrut(30));

        // Module cards row 1
        JPanel row1 = makeRow(
            makeModuleCard("🚕", "Cab Matching",
                "Closest Pair of Points\n(Divide & Conquer)",
                "Assigns the nearest available cab to a passenger using the divide-and-conquer " +
                "closest-pair algorithm.\nTime: O(n log²n)",
                ACCENT_BLUE),
            makeModuleCard("📋", "Ride Scheduling",
                "Job Sequencing with Deadlines\n(Greedy)",
                "Schedules rides within their deadlines to maximise total reward using a " +
                "greedy algorithm.\nTime: O(n²)",
                ACCENT_ORANGE)
        );
        content.add(row1);
        content.add(Box.createVerticalStrut(18));

        // Module cards row 2
        JPanel row2 = makeRow(
            makeModuleCard("🌐", "Network Planning",
                "Minimum Spanning Tree\n(Prim's + Kruskal's)",
                "Finds the cheapest road network connecting all city hubs.\n" +
                "Compares Prim's and Kruskal's approaches.\nTime: O(E log E)",
                ACCENT_GREEN),
            makeModuleCard("🗺", "Shortest Route",
                "Dijkstra's Shortest Path",
                "Calculates the shortest road route between any two city locations.\n" +
                "Distinct from cab matching — uses road network graph.\nTime: O((V+E) log V)",
                ACCENT_PURPLE)
        );
        content.add(row2);
        content.add(Box.createVerticalStrut(24));

        // Info bar
        content.add(makeInfoBar());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.setBackground(BG_DARK);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel makeHero() {
        JPanel hero = new JPanel(new BorderLayout(0, 10));
        hero.setBackground(new Color(22, 28, 52));
        hero.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_BLUE.darker(), 1),
            new EmptyBorder(30, 36, 30, 36)
        ));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel bigTitle = new JLabel("RideWise");
        bigTitle.setFont(new Font("Segoe UI", Font.BOLD, 46));
        bigTitle.setForeground(ACCENT_BLUE);

        JLabel tagline = new JLabel("Smart Cab Management System");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        tagline.setForeground(TEXT_SECONDARY);

        JLabel sub = makeLabel(
            "A DAA Unit II Case Study demonstrating four fundamental algorithms through " +
            "a realistic ride-hailing application.",
            FONT_BODY, TEXT_SECONDARY);

        JPanel labels = new JPanel(new GridLayout(3, 1, 0, 6));
        labels.setBackground(new Color(22, 28, 52));
        labels.add(bigTitle);
        labels.add(tagline);
        labels.add(sub);
        hero.add(labels, BorderLayout.CENTER);

        // Badge panel
        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        badges.setBackground(new Color(22, 28, 52));
        badges.add(makeBadge("Java Swing", ACCENT_BLUE));
        badges.add(makeBadge("DAA Unit II", ACCENT_ORANGE));
        badges.add(makeBadge("4 Algorithms", ACCENT_GREEN));
        hero.add(badges, BorderLayout.EAST);

        return hero;
    }

    private JPanel makeRow(JPanel... cards) {
        JPanel row = new JPanel(new GridLayout(1, cards.length, 18, 0));
        row.setBackground(BG_DARK);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        for (JPanel c : cards) row.add(c);
        return row;
    }

    private JPanel makeModuleCard(String icon, String title, String algoLine,
                                   String description, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accent.darker(), 1),
            new EmptyBorder(20, 22, 20, 22)
        ));

        // Icon + title
        JLabel iconLbl = new JLabel(icon + "  " + title);
        iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        iconLbl.setForeground(accent);

        JLabel algoLbl = new JLabel("<html>" + algoLine.replace("\n", "<br>") + "</html>");
        algoLbl.setFont(FONT_BADGE);
        algoLbl.setForeground(accent.brighter());

        JLabel descLbl = new JLabel("<html><body style='width:280px;'>" +
                description.replace("\n","<br>") + "</body></html>");
        descLbl.setFont(FONT_BODY);
        descLbl.setForeground(TEXT_SECONDARY);

        JPanel top = new JPanel(new GridLayout(2, 1, 0, 4));
        top.setBackground(BG_CARD);
        top.add(iconLbl);
        top.add(algoLbl);

        card.add(top,     BorderLayout.NORTH);
        card.add(descLbl, BorderLayout.CENTER);

        // Usage hint
        JLabel hint = makeLabel("→ Click the tab above to open this module", FONT_SMALL, accent.darker());
        card.add(hint, BorderLayout.SOUTH);

        return card;
    }

    private JPanel makeInfoBar() {
        JPanel bar = new JPanel(new GridLayout(1, 4, 10, 0));
        bar.setBackground(new Color(22, 28, 50));
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(12, 16, 12, 16)
        ));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        bar.add(makeInfoItem("City",     "Hyderabad Metro",  ACCENT_BLUE));
        bar.add(makeInfoItem("Cabs",     "6 drivers",        ACCENT_GREEN));
        bar.add(makeInfoItem("Requests", "8 passengers",     ACCENT_ORANGE));
        bar.add(makeInfoItem("Locations","9 city hubs",      ACCENT_PURPLE));
        return bar;
    }

    private JPanel makeInfoItem(String label, String value, Color accent) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setBackground(new Color(22, 28, 50));
        p.add(makeLabel(value, new Font("Segoe UI", Font.BOLD, 16), accent));
        p.add(makeLabel(label, FONT_SMALL, TEXT_SECONDARY));
        return p;
    }

    private JLabel makeBadge(String text, Color color) {
        JLabel l = new JLabel("  " + text + "  ");
        l.setFont(FONT_BADGE);
        l.setForeground(color);
        l.setOpaque(true);
        l.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
        l.setBorder(BorderFactory.createLineBorder(color.darker(), 1));
        return l;
    }
}
