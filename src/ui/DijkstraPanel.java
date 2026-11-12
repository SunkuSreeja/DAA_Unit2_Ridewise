package ui;

import algorithms.DijkstraAlgorithm;
import algorithms.DijkstraAlgorithm.DijkstraResult;
import data.DataStore;
import models.Location;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

import static ui.UIConstants.*;

/**
 * Panel for Dijkstra's Shortest Path algorithm.
 * Lets the user pick source and destination city hubs,
 * runs Dijkstra's, draws the shortest path on the road graph,
 * and displays all algorithm steps.
 */
public class DijkstraPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final transient DataStore ds   = DataStore.getInstance();
    private final transient DijkstraAlgorithm algo = new DijkstraAlgorithm();

    private JComboBox<String> srcCombo, destCombo;
    private JTextArea stepsArea;
    private JTextArea resultArea;
    private RouteGraphPanel graphPanel;
    private transient DijkstraResult lastResult;

    @SuppressWarnings("this-escape")
    public DijkstraPanel() {
        setBackground(BG_DARK);
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setBackground(BG_DARK);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 20, 20, 20));

        // 1. Header
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 3));
        header.setBackground(BG_DARK);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        header.add(makeLabel("🗺  Shortest Route — Dijkstra's Algorithm", FONT_TITLE, ACCENT_PURPLE));
        header.add(makeLabel(
            "Finds the shortest road path between two city locations  |  O((V+E) log V)",
            FONT_SMALL, TEXT_SECONDARY));
        content.add(header);
        content.add(Box.createVerticalStrut(12));

        // 2. Controls bar
        content.add(buildControls());
        content.add(Box.createVerticalStrut(14));

        // 3. Graph — full width
        graphPanel = new RouteGraphPanel();
        JPanel graphCard = makeCard("🗺  City Road Network", ACCENT_PURPLE);
        graphPanel.setPreferredSize(new Dimension(100, 460));
        graphPanel.setMinimumSize(new Dimension(100, 460));
        graphPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 460));
        graphCard.add(graphPanel, BorderLayout.CENTER);
        graphCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 510));
        content.add(graphCard);
        content.add(Box.createVerticalStrut(14));

        // 4. Steps
        JPanel stepsCard = makeCard("🧠  Algorithm Steps (Dijkstra Trace)", ACCENT_PURPLE);
        stepsArea = makeOutputArea(14, 60);
        stepsArea.setForeground(new Color(76, 29, 149));
        stepsArea.setText("← Select source and destination above, then click ▶ Find Shortest Route");
        stepsCard.add(makeScroll(stepsArea), BorderLayout.CENTER);
        stepsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        content.add(stepsCard);
        content.add(Box.createVerticalStrut(14));

        // 5. Result
        JPanel resultCard = makeCard("✅  Shortest Route Result", ACCENT_GREEN);
        resultArea = makeOutputArea(6, 60);
        resultArea.setForeground(new Color(5, 120, 85));
        resultArea.setText("Result will appear here after you run the algorithm.");
        resultCard.add(makeScroll(resultArea), BorderLayout.CENTER);
        resultCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        content.add(resultCard);
        content.add(Box.createVerticalStrut(8));

        JScrollPane scroll = new JScrollPane(content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildControls() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(4, 8, 4, 8)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        List<Location> locs = ds.getLocations();
        String[] names = locs.stream().map(Location::getName).toArray(String[]::new);

        srcCombo  = makeCombo(names);
        destCombo = makeCombo(names);
        if (names.length > 1) destCombo.setSelectedIndex(names.length - 1);

        p.add(makeLabel("From:", FONT_BODY, TEXT_PRIMARY));
        p.add(srcCombo);
        p.add(makeLabel("To:", FONT_BODY, TEXT_PRIMARY));
        p.add(destCombo);

        JButton runBtn   = makeButton("▶ Find Shortest Route", ACCENT_PURPLE);
        JButton resetBtn = makeSmallButton("↺ Reset", BORDER_COLOR);

        runBtn.addActionListener(e   -> runAlgorithm());
        resetBtn.addActionListener(e -> reset());

        p.add(runBtn);
        p.add(resetBtn);
        return p;
    }

    private void runAlgorithm() {
        int src  = srcCombo.getSelectedIndex();
        int dest = destCombo.getSelectedIndex();

        if (src == dest) {
            JOptionPane.showMessageDialog(this,
                "Source and destination are the same location.\nPlease select different locations.",
                "Same Location", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Location> locs = ds.getLocations();
        if (locs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No city locations available.",
                "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        lastResult = algo.findShortestPath(locs, ds.getAdjMatrix(), src, dest);

        stepsArea.setText(String.join("\n", lastResult.steps));
        stepsArea.setCaretPosition(0);

        if (!lastResult.reachable) {
            resultArea.setText(String.format(
                "✗ NO ROUTE FOUND\n\n" +
                "  %s is unreachable from %s\n" +
                "  in the current road network.\n\n" +
                "  Hint: Add more roads using the MST panel\n" +
                "  or check graph connectivity.",
                locs.get(dest).getName(), locs.get(src).getName()));
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("✓ SHORTEST ROUTE FOUND!\n\n");
            sb.append("  Source      : ").append(locs.get(src).getName()).append("\n");
            sb.append("  Destination : ").append(locs.get(dest).getName()).append("\n\n");
            sb.append("  Route:\n  ");
            sb.append(String.join("  →  ", lastResult.pathNames));
            sb.append("\n\n");
            sb.append(String.format("  Total Distance: %.2f km\n\n", lastResult.totalDistance));

            // Show intermediate stops
            if (lastResult.path.size() > 2) {
                sb.append("  Via: ");
                for (int i = 1; i < lastResult.path.size() - 1; i++) {
                    sb.append(locs.get(lastResult.path.get(i)).getName());
                    if (i < lastResult.path.size() - 2) sb.append(", ");
                }
            }
            resultArea.setText(sb.toString());
        }

        graphPanel.setResult(lastResult, src, dest);
        graphPanel.repaint();
    }

    private void reset() {
        ds.loadDefaults();
        lastResult = null;
        stepsArea.setText("");
        resultArea.setText("");
        // Refresh combos
        List<Location> locs = ds.getLocations();
        String[] names = locs.stream().map(Location::getName).toArray(String[]::new);
        srcCombo.removeAllItems();
        destCombo.removeAllItems();
        for (String n : names) { srcCombo.addItem(n); destCombo.addItem(n); }
        if (names.length > 1) destCombo.setSelectedIndex(names.length - 1);
        graphPanel.setResult(null, -1, -1);
        graphPanel.repaint();
    }

    // ================================================================
    // Inner class: Road graph with highlighted shortest path
    // ================================================================
    class RouteGraphPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private transient DijkstraResult result;
        private int srcIdx = -1, destIdx = -1;

        RouteGraphPanel() { setBackground(BG_DARK); }

        void setResult(DijkstraResult r, int src, int dest) {
            this.result = r;
            this.srcIdx = src;
            this.destIdx = dest;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0,0, new Color(14,18,36), w,h, new Color(20,24,50));
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            List<Location> locs = ds.getLocations();
            if (locs.isEmpty()) return;

            // Scale locations to panel
            int[] sx = new int[locs.size()];
            int[] sy = new int[locs.size()];
            double minX=Double.MAX_VALUE, maxX=-Double.MAX_VALUE;
            double minY=Double.MAX_VALUE, maxY=-Double.MAX_VALUE;
            for (Location l : locs) {
                if (l.getX()<minX) minX=l.getX();
                if (l.getX()>maxX) maxX=l.getX();
                if (l.getY()<minY) minY=l.getY();
                if (l.getY()>maxY) maxY=l.getY();
            }
            double rangeX = maxX-minX==0?1:maxX-minX;
            double rangeY = maxY-minY==0?1:maxY-minY;
            int margin = 55;
            for (int i = 0; i < locs.size(); i++) {
                sx[i] = (int)(margin + (locs.get(i).getX()-minX)/rangeX * (w - 2*margin));
                sy[i] = (int)(h - margin - (locs.get(i).getY()-minY)/rangeY * (h - 2*margin));
            }

            double[][] adj = ds.getAdjMatrix();

            // Determine path edge set
            java.util.Set<String> pathEdges = new java.util.HashSet<>();
            if (result != null && result.reachable && result.path.size() >= 2) {
                List<Integer> path = result.path;
                for (int i = 0; i < path.size()-1; i++) {
                    int a = path.get(i), b = path.get(i+1);
                    pathEdges.add(Math.min(a,b)+"-"+Math.max(a,b));
                }
            }

            // Draw all edges
            g2.setFont(FONT_SMALL);
            for (int i = 0; i < locs.size(); i++) {
                for (int j = i+1; j < locs.size(); j++) {
                    if (adj[i][j] > 0) {
                        boolean onPath = pathEdges.contains(i+"-"+j);
                        if (onPath) {
                            g2.setColor(ACCENT_PURPLE);
                            g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND,
                                         BasicStroke.JOIN_ROUND));
                        } else {
                            g2.setColor(new Color(35, 48, 78));
                            g2.setStroke(new BasicStroke(1.0f));
                        }
                        g2.drawLine(sx[i], sy[i], sx[j], sy[j]);
                        // Weight label
                        int mx = (sx[i]+sx[j])/2, my = (sy[i]+sy[j])/2;
                        g2.setFont(FONT_SMALL);
                        g2.setColor(onPath ? ACCENT_PURPLE.brighter() : new Color(60,80,110));
                        g2.drawString(String.format("%.1f",adj[i][j]), mx-10, my-4);
                    }
                }
            }

            // Draw path direction arrows
            if (result != null && result.reachable && result.path.size() >= 2) {
                g2.setColor(ACCENT_PURPLE);
                g2.setStroke(new BasicStroke(2f));
                List<Integer> path = result.path;
                for (int i = 0; i < path.size()-1; i++) {
                    int a = path.get(i), b = path.get(i+1);
                    drawArrow(g2, sx[a], sy[a], sx[b], sy[b]);
                }
            }

            // Draw vertices
            for (int i = 0; i < locs.size(); i++) {
                boolean onPath = result!=null && result.path.contains(i);
                boolean isSrc  = i == srcIdx;
                boolean isDest = i == destIdx;

                Color vColor = isSrc  ? ACCENT_GREEN :
                               isDest ? ACCENT_PINK  :
                               onPath ? ACCENT_PURPLE : ACCENT_BLUE;

                int vr = (isSrc || isDest) ? 12 : onPath ? 10 : 8;

                // Glow
                g2.setColor(new Color(vColor.getRed(),vColor.getGreen(),vColor.getBlue(),40));
                g2.fillOval(sx[i]-vr-5, sy[i]-vr-5, (vr+5)*2, (vr+5)*2);

                g2.setColor(vColor);
                g2.fillOval(sx[i]-vr, sy[i]-vr, vr*2, vr*2);

                // Index label inside
                g2.setColor(Color.WHITE);
                g2.setFont(FONT_BADGE);
                g2.drawString(String.valueOf(i), sx[i]-4, sy[i]+4);

                // Name label — use bright colours on dark canvas
                g2.setColor(isSrc ? ACCENT_GREEN : isDest ? ACCENT_PINK :
                            onPath ? new Color(200, 160, 255) : new Color(160, 180, 220));
                g2.setFont(FONT_SMALL);
                g2.drawString(locs.get(i).getName(), sx[i]-12, sy[i]+vr+14);

                // Distance label from source
                if (result != null && result.allDistances != null && i < result.allDistances.length) {
                    double d = result.allDistances[i];
                    if (d < Double.MAX_VALUE) {
                        g2.setColor(new Color(120,140,180));
                        g2.drawString(String.format("%.1fkm",d), sx[i]-12, sy[i]-vr-4);
                    }
                }
            }

            // Legend
            drawLegend(g2, w);
        }

        private void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2) {
            int mx = (x1+x2)/2, my = (y1+y2)/2;
            double angle = Math.atan2(y2-y1, x2-x1);
            int as = 8;
            int ax = (int)(mx - as * Math.cos(angle - Math.PI/6));
            int ay = (int)(my - as * Math.sin(angle - Math.PI/6));
            int bx = (int)(mx - as * Math.cos(angle + Math.PI/6));
            int by = (int)(my - as * Math.sin(angle + Math.PI/6));
            g2.setColor(ACCENT_PURPLE);
            g2.fillPolygon(new int[]{mx, ax, bx}, new int[]{my, ay, by}, 3);
        }

        private void drawLegend(Graphics2D g2, int w) {
            // Legend on dark canvas — always use white text
            int lx = w - 178, ly = 12;
            g2.setColor(new Color(10, 14, 32, 210));
            g2.fillRoundRect(lx - 10, ly - 6, 175, 96, 10, 10);
            g2.setColor(new Color(60, 80, 130));
            g2.drawRoundRect(lx - 10, ly - 6, 175, 96, 10, 10);
            g2.setFont(FONT_SMALL);

            g2.setColor(ACCENT_GREEN);
            g2.fillOval(lx, ly+2, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString("Source",           lx+16, ly+11);

            g2.setColor(ACCENT_PINK);
            g2.fillOval(lx, ly+22, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString("Destination",      lx+16, ly+31);

            g2.setColor(ACCENT_PURPLE);
            g2.fillOval(lx, ly+42, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString("On Shortest Path", lx+16, ly+51);

            g2.setColor(ACCENT_BLUE);
            g2.fillOval(lx, ly+62, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString("Other Location",   lx+16, ly+71);
        }
    }
}
