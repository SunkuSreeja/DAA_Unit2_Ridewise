package ui;

import algorithms.ClosestPairAlgorithm;
import algorithms.ClosestPairAlgorithm.CPResult;
import algorithms.ClosestPairAlgorithm.Point;
import data.DataStore;
import models.Cab;
import models.RideRequest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static ui.UIConstants.*;

/**
 * Panel for the Closest Pair of Points (Divide & Conquer) feature.
 * Vertical, scrollable layout: controls → map → steps → result.
 * All algorithm logic unchanged.
 */
public class ClosestPairPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final transient DataStore ds = DataStore.getInstance();
    private final transient ClosestPairAlgorithm algo = new ClosestPairAlgorithm();

    private JComboBox<String> passengerCombo;
    private JTextArea stepsArea;
    private JTextArea resultArea;
    private MapPanel mapPanel;

    private transient CPResult lastResult;
    private transient RideRequest selectedPassenger;

    @SuppressWarnings("this-escape")
    public ClosestPairPanel() {
        setBackground(BG_DARK);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        // ── Vertical content panel (scrollable) ───────────────────────
        JPanel content = new JPanel();
        content.setBackground(BG_DARK);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 20, 20, 20));

        // 1. Header
        content.add(buildHeader());
        content.add(Box.createVerticalStrut(12));

        // 2. Controls bar (passenger selector + buttons)
        content.add(buildControls());
        content.add(Box.createVerticalStrut(14));

        // 3. Map — full width, fixed height
        mapPanel = new MapPanel();
        JPanel mapCard = makeCard("🗺  City Coordinate Map — Cabs (C) and Passenger (P)", ACCENT_BLUE);
        mapPanel.setPreferredSize(new Dimension(100, 460));
        mapPanel.setMinimumSize(new Dimension(100, 460));
        mapPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 460));
        mapCard.add(mapPanel, BorderLayout.CENTER);
        mapCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 510));
        content.add(mapCard);
        content.add(Box.createVerticalStrut(14));

        // 4. Algorithm steps
        JPanel stepsCard = makeCard("🧠  Algorithm Steps — Divide & Conquer Trace", ACCENT_BLUE);
        stepsArea = makeOutputArea(14, 60);
        stepsArea.setForeground(new Color(30, 64, 175));
        stepsArea.setText("← Select a passenger above and click  ▶ Find Nearest Cab");
        stepsCard.add(makeScroll(stepsArea), BorderLayout.CENTER);
        stepsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        content.add(stepsCard);
        content.add(Box.createVerticalStrut(14));

        // 5. Result
        JPanel resultCard = makeCard("✅  Result — Nearest Cab Found", ACCENT_GREEN);
        resultArea = makeOutputArea(6, 60);
        resultArea.setForeground(new Color(5, 120, 85));
        resultArea.setText("Result will appear here after you run the algorithm.");
        resultCard.add(makeScroll(resultArea), BorderLayout.CENTER);
        resultCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        content.add(resultCard);
        content.add(Box.createVerticalStrut(8));

        // Wrap in main scroll pane
        JScrollPane scroll = new JScrollPane(content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 3));
        p.setBackground(BG_DARK);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        p.add(makeLabel("🚕  Cab Matching — Closest Pair (Divide & Conquer)", FONT_TITLE, ACCENT_BLUE));
        p.add(makeLabel(
            "Finds the nearest available cab to a passenger using Divide-and-Conquer  |  O(n log²n)",
            FONT_SMALL, TEXT_SECONDARY));
        return p;
    }

    private JPanel buildControls() {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(4, 8, 4, 8)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        card.add(makeLabel("Select Passenger:", FONT_BODY, TEXT_PRIMARY));

        List<RideRequest> reqs = ds.getRequests();
        String[] names = new String[reqs.size()];
        for (int i = 0; i < reqs.size(); i++) {
            names[i] = reqs.get(i).getPassengerName() +
                       String.format("  (%.1f, %.1f)", reqs.get(i).getPickupX(), reqs.get(i).getPickupY());
        }
        passengerCombo = makeCombo(names);
        card.add(passengerCombo);

        JButton runBtn = makeButton("▶ Find Nearest Cab", ACCENT_BLUE);
        runBtn.addActionListener(e -> runAlgorithm());
        card.add(runBtn);

        JButton resetBtn = makeSmallButton("↺ Reset", BORDER_COLOR);
        resetBtn.addActionListener(e -> reset());
        card.add(resetBtn);

        return card;
    }

    // ── Algorithm logic (UNCHANGED) ────────────────────────────────────

    private void runAlgorithm() {
        List<RideRequest> reqs = ds.getRequests();
        if (reqs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No passengers in the system.", "No Data",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idx = passengerCombo.getSelectedIndex();
        if (idx < 0) return;
        selectedPassenger = reqs.get(idx);

        List<Cab> availableCabs = new ArrayList<>();
        for (Cab c : ds.getCabs()) {
            if (c.isAvailable()) availableCabs.add(c);
        }

        if (availableCabs.isEmpty()) {
            resultArea.setText("⚠ No available cabs in the system.\n"
                + "All cabs are currently busy. Please reset to reload data.");
            stepsArea.setText("No available cabs — algorithm cannot run.");
            return;
        }

        lastResult = algo.findClosestCab(selectedPassenger, availableCabs);

        stepsArea.setText("");
        if (lastResult != null && lastResult.steps != null) {
            stepsArea.setText(String.join("\n", lastResult.steps));
        }

        if (lastResult == null) {
            resultArea.setText("Could not find a nearby cab.");
        } else {
            Point cabPoint = lastResult.point1.cab != null
                            ? lastResult.point1 : lastResult.point2;
            Cab nearestCab = cabPoint.cab;

            resultArea.setText(String.format(
                "✓ NEAREST CAB FOUND!\n\n" +
                "  Passenger  : %s at (%.1f, %.1f)\n" +
                "  Cab        : %s\n" +
                "  Driver     : %s\n" +
                "  Vehicle    : %s\n" +
                "  Distance   : %.4f units\n\n" +
                "  → Cab #%d assigned to %s",
                selectedPassenger.getPassengerName(),
                selectedPassenger.getPickupX(), selectedPassenger.getPickupY(),
                "Cab #" + nearestCab.getId(),
                nearestCab.getDriverName(),
                nearestCab.getVehicleType(),
                lastResult.distance,
                nearestCab.getId(),
                selectedPassenger.getPassengerName()
            ));

            nearestCab.setAvailable(false);
        }

        mapPanel.setData(selectedPassenger, ds.getCabs(), lastResult);
        mapPanel.repaint();
    }

    private void reset() {
        ds.loadDefaults();
        lastResult = null;
        selectedPassenger = null;
        stepsArea.setText("");
        resultArea.setText("");
        mapPanel.setData(null, ds.getCabs(), null);
        mapPanel.repaint();
        List<RideRequest> reqs = ds.getRequests();
        passengerCombo.removeAllItems();
        for (RideRequest r : reqs) {
            passengerCombo.addItem(r.getPassengerName() +
                String.format("  (%.1f, %.1f)", r.getPickupX(), r.getPickupY()));
        }
    }

    // ================================================================
    // Inner class: Map canvas — painting logic UNCHANGED
    // ================================================================
    class MapPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private transient RideRequest passenger;
        private transient List<Cab> cabs = new ArrayList<>();
        private transient CPResult result;

        void setData(RideRequest p, List<Cab> cabs, CPResult result) {
            this.passenger = p;
            this.cabs = cabs != null ? cabs : new ArrayList<>();
            this.result = result;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            GradientPaint gp = new GradientPaint(0,0, new Color(15,20,40), w,h, new Color(20,28,55));
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            g2.setColor(new Color(40, 50, 80));
            g2.setStroke(new BasicStroke(0.5f));
            int gridSteps = 10;
            for (int i = 0; i <= gridSteps; i++) {
                int x = i * w / gridSteps;
                int y = i * h / gridSteps;
                g2.drawLine(x, 0, x, h);
                g2.drawLine(0, y, w, y);
            }

            g2.setFont(FONT_SMALL);
            g2.setColor(new Color(60, 80, 120));
            for (int i = 0; i <= gridSteps; i++) {
                int x = i * w / gridSteps;
                g2.drawString(String.valueOf(i), x + 2, h - 4);
                if (i > 0) {
                    int y = h - (i * h / gridSteps);
                    g2.drawString(String.valueOf(i), 2, y - 2);
                }
            }

            if (result != null && passenger != null) {
                Point cabPt = result.point1.cab != null ? result.point1 : result.point2;
                int px = toScreenX(passenger.getPickupX(), w);
                int py = toScreenY(passenger.getPickupY(), h);
                int cx = toScreenX(cabPt.x, w);
                int cy = toScreenY(cabPt.y, h);

                float[] dash = {8f, 5f};
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND,
                             BasicStroke.JOIN_ROUND, 0, dash, 0));
                g2.setColor(ACCENT_GREEN);
                g2.drawLine(px, py, cx, cy);

                String distLabel = String.format("%.2f", result.distance);
                int mx = (px + cx) / 2, my = (py + cy) / 2;
                g2.setFont(FONT_BADGE);
                g2.setColor(ACCENT_GREEN);
                g2.drawString(distLabel, mx + 4, my - 4);
            }

            g2.setStroke(new BasicStroke(1.5f));

            for (Cab cab : cabs) {
                int cx = toScreenX(cab.getX(), w);
                int cy = toScreenY(cab.getY(), h);

                boolean isNearest = result != null &&
                    ((result.point1.cab != null && result.point1.cab == cab) ||
                     (result.point2.cab != null && result.point2.cab == cab));

                Color cabColor = !cab.isAvailable()   ? TEXT_SECONDARY :
                                 isNearest             ? ACCENT_GREEN   : ACCENT_BLUE;

                g2.setColor(cabColor);
                int r = isNearest ? 10 : 7;
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);

                if (isNearest) {
                    g2.setColor(new Color(52, 211, 153, 80));
                    g2.fillOval(cx - 18, cy - 18, 36, 36);
                    g2.setColor(ACCENT_GREEN);
                    g2.drawOval(cx - 18, cy - 18, 36, 36);
                }

                g2.setFont(FONT_SMALL);
                g2.setColor(cabColor.brighter());
                g2.drawString("C" + cab.getId(), cx + r + 2, cy + 4);
                g2.setColor(new Color(150, 170, 210));
                g2.drawString(cab.getDriverName().split(" ")[0], cx - r, cy + r + 12);
            }

            if (passenger != null) {
                int px = toScreenX(passenger.getPickupX(), w);
                int py = toScreenY(passenger.getPickupY(), h);

                g2.setColor(new Color(251, 146, 60, 60));
                g2.fillOval(px - 16, py - 16, 32, 32);
                g2.setColor(ACCENT_ORANGE);
                g2.fillOval(px - 8, py - 8, 16, 16);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                g2.drawString("P", px - 3, py + 3);
                g2.setColor(ACCENT_ORANGE);
                g2.setFont(FONT_SMALL);
                g2.drawString(passenger.getPassengerName().split(" ")[0], px + 10, py - 10);
            }

            drawLegend(g2, w, h);
        }

        private void drawLegend(Graphics2D g2, int w, int h) {
            // Legend drawn on dark canvas — always use white text
            int lx = w - 168, ly = 12;
            // Semi-transparent dark background pill
            g2.setColor(new Color(10, 14, 32, 210));
            g2.fillRoundRect(lx - 10, ly - 6, 165, 96, 10, 10);
            g2.setColor(new Color(60, 80, 130));
            g2.drawRoundRect(lx - 10, ly - 6, 165, 96, 10, 10);

            g2.setFont(FONT_SMALL);

            // Row 1: Available Cab (blue dot)
            g2.setColor(ACCENT_BLUE);
            g2.fillOval(lx, ly + 2, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString("Available Cab",  lx + 16, ly + 11);

            // Row 2: Nearest Cab (green dot)
            g2.setColor(ACCENT_GREEN);
            g2.fillOval(lx, ly + 22, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString("Nearest Cab",    lx + 16, ly + 31);

            // Row 3: Busy Cab (grey dot)
            g2.setColor(new Color(120, 130, 150));
            g2.fillOval(lx, ly + 42, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString("Busy Cab",       lx + 16, ly + 51);

            // Row 4: Passenger (orange dot)
            g2.setColor(ACCENT_ORANGE);
            g2.fillOval(lx, ly + 62, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString("Passenger",      lx + 16, ly + 71);
        }

        private int toScreenX(double gx, int w) {
            return (int) (gx / 10.0 * (w - 30)) + 15;
        }

        private int toScreenY(double gy, int h) {
            return h - (int) (gy / 10.0 * (h - 30)) - 15;
        }
    }
}
