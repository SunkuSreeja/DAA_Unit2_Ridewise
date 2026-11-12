package ui;

import algorithms.MSTAlgorithm;
import algorithms.MSTAlgorithm.MSTResult;
import data.DataStore;
import models.Edge;
import models.Location;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

import static ui.UIConstants.*;

/**
 * NETWORK PLANNING — Minimum Spanning Tree (Prim's + Kruskal's)
 * Completely rewritten to be beginner-friendly and user-input-first.
 */
public class MSTPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final transient DataStore ds   = DataStore.getInstance();
    private final transient MSTAlgorithm algo = new MSTAlgorithm();

    private MSTGraphPanel graphPanel;
    private JTextArea stepsArea;
    private JTextArea comparisonArea;
    private transient MSTResult currentResult;


    // User input for new locations
    private JTextField newLocNameField, newLocXField, newLocYField;
    // User input for new roads
    private JComboBox<String> edgeSrcCombo, edgeDestCombo;
    private JTextField edgeWeightField;

    @SuppressWarnings("this-escape")
    public MSTPanel() {
        setBackground(BG_DARK);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setBackground(BG_DARK);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 20, 20, 20));

        // 1. Title
        JPanel titleRow = new JPanel(new GridLayout(2, 1, 0, 3));
        titleRow.setBackground(BG_DARK);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        titleRow.add(makeLabel("🌐  Network Planning — Minimum Spanning Tree (MST)", FONT_TITLE, ACCENT_GREEN));
        titleRow.add(makeLabel("Connect all city locations with minimum total road cost  |  Prim's O(E log V)  ·  Kruskal's O(E log E)",
                FONT_SMALL, TEXT_SECONDARY));
        content.add(titleRow);
        content.add(Box.createVerticalStrut(12));

        // 2. Concept boxes
        JPanel conceptRow = new JPanel(new GridLayout(1, 3, 12, 0));
        conceptRow.setBackground(BG_DARK);
        conceptRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        conceptRow.add(makeConceptBox("🏙  What is the Problem?",
                "Imagine you need to connect several city locations\n" +
                "with roads. Each road costs money (in km / cost).\n\n" +
                "Goal: Connect ALL locations using the MINIMUM\n" +
                "total road length — no unnecessary roads.\n\n" +
                "This is the Minimum Spanning Tree (MST) problem.", ACCENT_GREEN));
        conceptRow.add(makeConceptBox("🔵  Prim's Algorithm",
                "Think of growing a tree from one city.\n\n" +
                "1. Start from any city (e.g. Secunderabad).\n" +
                "2. Look at all roads FROM connected cities.\n" +
                "3. Pick the CHEAPEST road to a new city.\n" +
                "4. Add that city. Repeat until all are connected.\n\n" +
                "Like spreading paint — one city at a time.", ACCENT_GREEN));
        conceptRow.add(makeConceptBox("🟠  Kruskal's Algorithm",
                "Think of sorting all roads by cost first.\n\n" +
                "1. List ALL roads sorted cheapest to costliest.\n" +
                "2. Add the cheapest road — but ONLY if it doesn't\n" +
                "   create a loop (cycle) in the network.\n" +
                "3. Repeat until all cities are connected.\n\n" +
                "Uses Union-Find to detect loops quickly.", ACCENT_ORANGE));
        content.add(conceptRow);
        content.add(Box.createVerticalStrut(14));

        // 3. Add City input row
        JPanel locCard = new JPanel(new BorderLayout(0, 6));
        locCard.setBackground(BG_CARD);
        locCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_GREEN, 2),
                new EmptyBorder(10, 14, 10, 14)));
        locCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        locCard.add(makeLabel("➕  Add a New City Location", FONT_SUBTITLE, ACCENT_GREEN), BorderLayout.NORTH);

        JPanel locForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        locForm.setBackground(BG_CARD);
        newLocNameField = makeField(12); newLocNameField.setToolTipText("e.g. Madhapur");
        newLocXField    = makeField(5);  newLocXField.setToolTipText("X position 1-10");
        newLocYField    = makeField(5);  newLocYField.setToolTipText("Y position 1-10");
        locForm.add(makeLabel("Name:", FONT_BODY, TEXT_PRIMARY));  locForm.add(newLocNameField);
        locForm.add(makeLabel("X (1-10):", FONT_BODY, TEXT_PRIMARY)); locForm.add(newLocXField);
        locForm.add(makeLabel("Y (1-10):", FONT_BODY, TEXT_PRIMARY)); locForm.add(newLocYField);
        JButton addLocBtn   = makeSmallButton("+ Add City", ACCENT_GREEN);
        JButton loadDemoBtn = makeSmallButton("📦 Load Demo", BORDER_COLOR);
        addLocBtn.addActionListener(e -> addLocation());
        loadDemoBtn.addActionListener(e -> reset());
        locForm.add(addLocBtn); locForm.add(loadDemoBtn);
        locCard.add(locForm, BorderLayout.CENTER);
        content.add(locCard);
        content.add(Box.createVerticalStrut(10));

        // 4. Add Road input row
        JPanel roadCard = new JPanel(new BorderLayout(0, 6));
        roadCard.setBackground(BG_CARD);
        roadCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_ORANGE, 2),
                new EmptyBorder(10, 14, 10, 14)));
        roadCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        roadCard.add(makeLabel("🛣  Add a Road Between Two Cities", FONT_SUBTITLE, ACCENT_ORANGE), BorderLayout.NORTH);

        JPanel roadForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        roadForm.setBackground(BG_CARD);
        String[] locNames = ds.getLocations().stream().map(Location::getName).toArray(String[]::new);
        edgeSrcCombo  = makeCombo(locNames);
        edgeDestCombo = makeCombo(locNames);
        if (locNames.length > 1) edgeDestCombo.setSelectedIndex(1);
        edgeWeightField = makeField(6); edgeWeightField.setToolTipText("Road distance in km");
        roadForm.add(makeLabel("From:", FONT_BODY, TEXT_PRIMARY)); roadForm.add(edgeSrcCombo);
        roadForm.add(makeLabel("To:", FONT_BODY, TEXT_PRIMARY));   roadForm.add(edgeDestCombo);
        roadForm.add(makeLabel("Distance (km):", FONT_BODY, TEXT_PRIMARY)); roadForm.add(edgeWeightField);
        JButton addRoadBtn = makeSmallButton("+ Add Road", ACCENT_ORANGE);
        addRoadBtn.addActionListener(e -> addRoad());
        roadForm.add(addRoadBtn);
        roadCard.add(roadForm, BorderLayout.CENTER);

        JPanel runBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        runBtnRow.setBackground(BG_CARD);
        JButton primBtn    = makeButton("▶ Run Prim's", ACCENT_GREEN);
        JButton kruskalBtn = makeButton("▶ Run Kruskal's", new Color(200, 140, 60));
        JButton compareBtn = makeSmallButton("⇔ Compare Both", ACCENT_BLUE);
        primBtn.addActionListener(e    -> runPrims());
        kruskalBtn.addActionListener(e -> runKruskals());
        compareBtn.addActionListener(e -> compareBoth());
        runBtnRow.add(primBtn); runBtnRow.add(kruskalBtn); runBtnRow.add(compareBtn);
        roadCard.add(runBtnRow, BorderLayout.SOUTH);
        content.add(roadCard);
        content.add(Box.createVerticalStrut(14));

        // 5. Graph — full width
        graphPanel = new MSTGraphPanel();
        JPanel graphCard = makeCard("🗺  City Network Map  (green lines = MST result)", ACCENT_GREEN);
        graphPanel.setPreferredSize(new Dimension(100, 440));
        graphPanel.setMinimumSize(new Dimension(100, 440));
        graphPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 440));
        graphCard.add(graphPanel, BorderLayout.CENTER);
        graphCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 495));
        content.add(graphCard);
        content.add(Box.createVerticalStrut(14));

        // 6. Algorithm steps
        JPanel stepsCard = makeCard("🧠  Algorithm Steps — Plain English", ACCENT_GREEN);
        stepsArea = makeOutputArea(14, 60);
        stepsArea.setForeground(new Color(6, 95, 70));
        stepsArea.setText(
            "← Add cities and roads above, then click  ▶ Run Prim's  or  ▶ Run Kruskal's\n\n" +
            "Tip: Click  📦 Load Demo  to use the Hyderabad example.");
        stepsCard.add(makeScroll(stepsArea), BorderLayout.CENTER);
        stepsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));
        content.add(stepsCard);
        content.add(Box.createVerticalStrut(14));

        // 7. Comparison
        JPanel cmpCard = makeCard("📊  Prim's vs Kruskal's — Comparison", ACCENT_BLUE);
        comparisonArea = makeOutputArea(8, 60);
        comparisonArea.setForeground(new Color(30, 64, 175));
        comparisonArea.setText("Run both algorithms to see their comparison here.");
        cmpCard.add(makeScroll(comparisonArea), BorderLayout.CENTER);
        cmpCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        content.add(cmpCard);
        content.add(Box.createVerticalStrut(8));

        JScrollPane scroll = new JScrollPane(content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel makeConceptBox(String title, String body, Color accent) {
        JPanel box = new JPanel(new BorderLayout(0, 5));
        box.setBackground(BG_CARD);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent.darker(), 1),
                new EmptyBorder(8, 10, 8, 10)));
        box.add(makeLabel(title, FONT_BADGE, accent), BorderLayout.NORTH);
        JTextArea ta = new JTextArea(body);
        ta.setFont(FONT_SMALL);
        ta.setForeground(TEXT_SECONDARY);
        ta.setBackground(BG_CARD);
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        box.add(ta, BorderLayout.CENTER);
        return box;
    }

    // ── Actions ────────────────────────────────────────────────────────

    private void addLocation() {
        try {
            String name = newLocNameField.getText().trim();
            if (name.isEmpty()) throw new IllegalArgumentException("Please enter a city name.");
            double x = Double.parseDouble(newLocXField.getText().trim());
            double y = Double.parseDouble(newLocYField.getText().trim());
            if (x < 1 || x > 10 || y < 1 || y > 10)
                throw new IllegalArgumentException("X and Y must be between 1 and 10.");

            // Check duplicate name
            for (Location l : ds.getLocations()) {
                if (l.getName().equalsIgnoreCase(name))
                    throw new IllegalArgumentException("A city named '" + name + "' already exists.");
            }

            int n = ds.getLocations().size();
            ds.getLocations().add(new Location(name, x, y));

            // Expand adjacency matrix
            double[][] old = ds.getAdjMatrix();
            double[][] newM = new double[n + 1][n + 1];
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    newM[i][j] = old[i][j];
            ds.setAdjMatrix(newM);

            refreshCombos();
            currentResult = null;
            graphPanel.setResult(null, "");
            graphPanel.repaint();

            newLocNameField.setText(""); newLocXField.setText(""); newLocYField.setText("");
            stepsArea.setText("City '" + name + "' added! Now add roads connecting it to other cities.");

        } catch (NumberFormatException ex) {
            showError("X and Y must be numbers (e.g. 3.5)");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void addRoad() {
        try {
            int src  = edgeSrcCombo.getSelectedIndex();
            int dest = edgeDestCombo.getSelectedIndex();
            if (src == dest) throw new IllegalArgumentException(
                "A road must connect TWO DIFFERENT cities.\nPlease select different From and To cities.");
            double wt = Double.parseDouble(edgeWeightField.getText().trim());
            if (wt <= 0) throw new IllegalArgumentException("Distance must be a positive number (e.g. 3.5).");
            if (wt > 200) throw new IllegalArgumentException("Distance seems too large. Please check (in km).");

            ds.addRoad(src, dest, wt);
            currentResult = null;
            graphPanel.setResult(null, "");
            graphPanel.repaint();
            edgeWeightField.setText("");

            String srcName  = ds.getLocations().get(src).getName();
            String destName = ds.getLocations().get(dest).getName();
            stepsArea.setText("Road added: " + srcName + " ↔ " + destName + " (" + wt + " km)\n\n" +
                "Now run Prim's or Kruskal's to find the minimum spanning tree!");

        } catch (NumberFormatException ex) {
            showError("Please enter a valid distance number (e.g. 4.5)");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void runPrims() {
        if (ds.getLocations().size() < 2) { showError("Add at least 2 cities first."); return; }
        if (ds.getEdgeList().isEmpty())   { showError("Add at least 1 road first."); return; }

        currentResult = algo.runPrims(ds.getLocations(), ds.getAdjMatrix());
        graphPanel.setResult(currentResult, "Prim");
        graphPanel.repaint();
        displayPrimsSteps(currentResult);
        updateComparison(currentResult, null);

        if (!currentResult.connected) {
            showError("⚠  Some cities are not connected!\nAdd more roads so every city has at least one path to others.");
        }
    }

    private void runKruskals() {
        if (ds.getLocations().size() < 2) { showError("Add at least 2 cities first."); return; }
        if (ds.getEdgeList().isEmpty())   { showError("Add at least 1 road first."); return; }

        currentResult = algo.runKruskals(ds.getLocations(), ds.getEdgeList());
        graphPanel.setResult(currentResult, "Kruskal");
        graphPanel.repaint();
        displayKruskalsSteps(currentResult);
        updateComparison(null, currentResult);

        if (!currentResult.connected) {
            showError("⚠  Some cities are not connected!\nAdd more roads so every city has at least one path to others.");
        }
    }

    private void compareBoth() {
        if (ds.getLocations().size() < 2) { showError("Add at least 2 cities first."); return; }
        if (ds.getEdgeList().isEmpty())   { showError("Add at least 1 road first."); return; }

        MSTResult pr = algo.runPrims(ds.getLocations(), ds.getAdjMatrix());
        MSTResult kr = algo.runKruskals(ds.getLocations(), ds.getEdgeList());
        currentResult = pr;
        graphPanel.setResult(pr, "Prim");
        graphPanel.repaint();
        updateComparison(pr, kr);

        StringBuilder both = new StringBuilder();
        both.append("=== PRIM'S ALGORITHM ===\n");
        displayPrimsStepsTo(pr, both);
        both.append("\n\n=== KRUSKAL'S ALGORITHM ===\n");
        displayKruskalsStepsTo(kr, both);
        stepsArea.setText(both.toString());
        stepsArea.setCaretPosition(0);
    }

    // ── Plain-English step generation ─────────────────────────────────

    private void displayPrimsSteps(MSTResult r) {
        StringBuilder sb = new StringBuilder();
        displayPrimsStepsTo(r, sb);
        stepsArea.setText(sb.toString());
        stepsArea.setCaretPosition(0);
    }

    private void displayPrimsStepsTo(MSTResult r, StringBuilder sb) {
        List<Location> locs = ds.getLocations();
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("  PRIM'S ALGORITHM — Step-by-Step\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        sb.append("IDEA: Start at one city and keep adding the\n");
        sb.append("      cheapest road to a NEW (unvisited) city.\n\n");
        sb.append("START: We begin at → ").append(locs.isEmpty() ? "?" : locs.get(0).getName()).append("\n\n");

        int step = 1;
        for (Edge e : r.mstEdges) {
            String from = (e.getSrc() < locs.size()) ? locs.get(e.getSrc()).getName() : "?";
            String to   = (e.getDest() < locs.size()) ? locs.get(e.getDest()).getName() : "?";
            sb.append(String.format("STEP %d:\n", step++));
            sb.append(String.format("  From all connected cities, the CHEAPEST road\n"));
            sb.append(String.format("  to a NEW city is:\n"));
            sb.append(String.format("  ✅  %s  →  %s   (%.2f km)\n", from, to, e.getWeight()));
            sb.append(String.format("  We ADD this road. %s is now part of our network.\n\n", to));
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append(String.format("  RESULT: %d roads selected\n", r.mstEdges.size()));
        sb.append(String.format("  TOTAL road length = %.2f km\n", r.totalCost));
        sb.append("  All cities are now connected with minimum cost!\n");
        if (!r.connected) sb.append("\n  ⚠  WARNING: Graph is disconnected — not all cities reached!\n");
    }

    private void displayKruskalsSteps(MSTResult r) {
        StringBuilder sb = new StringBuilder();
        displayKruskalsStepsTo(r, sb);
        stepsArea.setText(sb.toString());
        stepsArea.setCaretPosition(0);
    }

    private void displayKruskalsStepsTo(MSTResult r, StringBuilder sb) {
        List<Location> locs = ds.getLocations();
        List<Edge> allEdges = ds.getEdgeList();
        java.util.Collections.sort(allEdges);

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("  KRUSKAL'S ALGORITHM — Step-by-Step\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        sb.append("IDEA: Sort ALL roads cheapest-first. Add each road\n");
        sb.append("      ONLY IF it doesn't create a loop.\n\n");

        sb.append("ALL ROADS SORTED BY COST:\n");
        int rank = 1;
        for (Edge e : allEdges) {
            String from = (e.getSrc()  < locs.size()) ? locs.get(e.getSrc()).getName()  : "?";
            String to   = (e.getDest() < locs.size()) ? locs.get(e.getDest()).getName() : "?";
            sb.append(String.format("   %2d. %-16s ↔ %-16s  %.2f km\n", rank++, from, to, e.getWeight()));
        }

        sb.append("\nNOW PROCESS EACH ROAD:\n");
        // Simulate Kruskal's decisions
        int[] parent = new int[locs.size()];
        for (int i = 0; i < locs.size(); i++) parent[i] = i;
        int added = 0;
        for (Edge e : allEdges) {
            String from = (e.getSrc()  < locs.size()) ? locs.get(e.getSrc()).getName()  : "?";
            String to   = (e.getDest() < locs.size()) ? locs.get(e.getDest()).getName() : "?";
            int rootA = findParent(parent, e.getSrc());
            int rootB = findParent(parent, e.getDest());
            if (rootA == rootB) {
                sb.append(String.format("  ❌ SKIP  %s ↔ %s (%.2f km)\n", from, to, e.getWeight()));
                sb.append(String.format("     Reason: These cities are already connected!\n"));
                sb.append(String.format("     Adding this road would create a LOOP (cycle).\n\n"));
            } else {
                sb.append(String.format("  ✅ ADD   %s ↔ %s (%.2f km)\n", from, to, e.getWeight()));
                sb.append(String.format("     Reason: Cheapest road that connects two\n"));
                sb.append(String.format("     separate groups — no loop created.\n\n"));
                parent[rootA] = rootB;
                added++;
            }
            if (added == locs.size() - 1) break;
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append(String.format("  RESULT: %d roads selected\n", r.mstEdges.size()));
        sb.append(String.format("  TOTAL road length = %.2f km\n", r.totalCost));
        sb.append("  All cities connected with minimum cost!\n");
        if (!r.connected) sb.append("\n  ⚠  WARNING: Graph disconnected — not all cities reached!\n");
    }

    private int findParent(int[] parent, int x) {
        if (parent[x] != x) parent[x] = findParent(parent, parent[x]);
        return parent[x];
    }

    private void updateComparison(MSTResult primR, MSTResult kruskalR) {
        StringBuilder sb = new StringBuilder();
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("  PRIM'S vs KRUSKAL'S — Key Differences\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        sb.append("  Feature          Prim's                  Kruskal's\n");
        sb.append("  ─────────────────────────────────────────────────────\n");
        sb.append("  Strategy         Grow from 1 city        Sort all roads first\n");
        sb.append("  Works on         Connected subgraph      All edges globally\n");
        sb.append("  Data structure   Priority Queue          Sorted list + Union-Find\n");
        sb.append("  Time complexity  O(E log V)              O(E log E)\n");
        sb.append("  Best for         Dense graphs            Sparse graphs\n\n");

        if (primR != null) {
            sb.append(String.format("  Prim's result   : %d roads, %.2f km total\n",
                    primR.mstEdges.size(), primR.totalCost));
        }
        if (kruskalR != null) {
            sb.append(String.format("  Kruskal's result: %d roads, %.2f km total\n",
                    kruskalR.mstEdges.size(), kruskalR.totalCost));
        }
        if (primR != null && kruskalR != null) {
            sb.append(String.format("\n  ✅ Both give the SAME optimal MST cost (%.2f km)\n",
                    primR.totalCost));
            sb.append("     for any connected graph — just different methods!\n");
        }
        comparisonArea.setText(sb.toString());
    }

    private void reset() {
        ds.loadDefaults();
        currentResult = null;
        stepsArea.setText("Demo data loaded! 9 cities, 14 roads.\nClick ▶ Run Prim's or ▶ Run Kruskal's.");
        comparisonArea.setText("Run both algorithms to see their comparison here.");
        refreshCombos();
        graphPanel.setResult(null, "");
        graphPanel.repaint();
    }

    private void refreshCombos() {
        List<Location> locs = ds.getLocations();
        String[] names = locs.stream().map(Location::getName).toArray(String[]::new);
        edgeSrcCombo.removeAllItems();
        edgeDestCombo.removeAllItems();
        for (String n : names) { edgeSrcCombo.addItem(n); edgeDestCombo.addItem(n); }
        if (names.length > 1) edgeDestCombo.setSelectedIndex(1);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.WARNING_MESSAGE);
    }

    // ── Graph drawing panel ────────────────────────────────────────────
    class MSTGraphPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private transient MSTResult result;
        private String algoName = "";

        MSTGraphPanel() { setBackground(BG_DARK); }

        void setResult(MSTResult r, String name) {
            this.result = r; this.algoName = name == null ? "" : name;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0,0,new Color(14,20,38),w,h,new Color(18,26,48));
            g2.setPaint(gp); g2.fillRect(0,0,w,h);

            List<Location> locs = ds.getLocations();
            if (locs.isEmpty()) {
                g2.setFont(FONT_BODY); g2.setColor(TEXT_SECONDARY);
                g2.drawString("Add cities to see the graph.", 20, h/2);
                return;
            }

            // Compute screen positions
            int[] sx = new int[locs.size()];
            int[] sy = new int[locs.size()];
            double minX=9999, maxX=-9999, minY=9999, maxY=-9999;
            for (Location l : locs) {
                if (l.getX()<minX) minX=l.getX(); if (l.getX()>maxX) maxX=l.getX();
                if (l.getY()<minY) minY=l.getY(); if (l.getY()>maxY) maxY=l.getY();
            }
            double rangeX = maxX-minX < 0.1 ? 1 : maxX-minX;
            double rangeY = maxY-minY < 0.1 ? 1 : maxY-minY;
            int mg = 58;
            for (int i = 0; i < locs.size(); i++) {
                sx[i] = (int)(mg + (locs.get(i).getX()-minX)/rangeX*(w-2*mg));
                sy[i] = (int)(h-mg - (locs.get(i).getY()-minY)/rangeY*(h-2*mg));
            }

            double[][] adj = ds.getAdjMatrix();

            // Build MST edge set
            java.util.Set<String> mstEdgeSet = new java.util.HashSet<>();
            if (result != null) {
                for (Edge e : result.mstEdges) {
                    mstEdgeSet.add(Math.min(e.getSrc(),e.getDest())+"-"+Math.max(e.getSrc(),e.getDest()));
                }
            }

            // Draw all edges (dim)
            g2.setFont(FONT_SMALL);
            for (int i = 0; i < locs.size(); i++) {
                for (int j = i+1; j < locs.size(); j++) {
                    if (adj[i][j] > 0) {
                        String key = i+"-"+j;
                        boolean isMST = mstEdgeSet.contains(key);
                        if (!isMST) {
                            g2.setColor(new Color(38, 52, 82));
                            g2.setStroke(new BasicStroke(1f));
                            g2.drawLine(sx[i],sy[i],sx[j],sy[j]);
                            int mx=(sx[i]+sx[j])/2, my=(sy[i]+sy[j])/2;
                            // Edge weight — use visible light colour on dark canvas
                            g2.setColor(new Color(90, 110, 155));
                            g2.drawString(String.format("%.1f",adj[i][j]), mx-8, my-3);
                        }
                    }
                }
            }

            // Draw MST edges (bright green)
            for (int i = 0; i < locs.size(); i++) {
                for (int j = i+1; j < locs.size(); j++) {
                    if (adj[i][j] > 0 && mstEdgeSet.contains(i+"-"+j)) {
                        g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.setColor(ACCENT_GREEN);
                        g2.drawLine(sx[i],sy[i],sx[j],sy[j]);
                        int mx=(sx[i]+sx[j])/2, my=(sy[i]+sy[j])/2;
                        // White bg for label readability
                        g2.setColor(new Color(20,30,50,180));
                        g2.fillRoundRect(mx-14, my-13, 28, 14, 4, 4);
                        g2.setFont(FONT_BADGE);
                        g2.setColor(ACCENT_GREEN);
                        g2.drawString(String.format("%.1f",adj[i][j]), mx-10, my-2);
                    }
                }
            }

            // Draw vertices
            for (int i = 0; i < locs.size(); i++) {
                boolean inMST = false;
                if (result != null) {
                    for (Edge e : result.mstEdges) {
                        if (e.getSrc()==i||e.getDest()==i) { inMST=true; break; }
                    }
                }
                Color vc = (result != null && inMST) ? ACCENT_GREEN : ACCENT_BLUE;
                int vr = 11;
                // Glow
                g2.setColor(new Color(vc.getRed(),vc.getGreen(),vc.getBlue(),35));
                g2.fillOval(sx[i]-vr-5, sy[i]-vr-5, (vr+5)*2, (vr+5)*2);
                g2.setColor(vc);
                g2.fillOval(sx[i]-vr, sy[i]-vr, vr*2, vr*2);
                // Index
                g2.setColor(Color.WHITE);
                g2.setFont(FONT_BADGE);
                g2.drawString(String.valueOf(i), sx[i]-4, sy[i]+4);
                // Name — bright colours on dark canvas
                g2.setColor(inMST && result!=null ? ACCENT_GREEN : new Color(140, 165, 210));
                g2.setFont(FONT_SMALL);
                g2.drawString(locs.get(i).getName(), sx[i]-10, sy[i]+vr+14);
            }

            // Title overlay
            if (result != null && !algoName.isEmpty()) {
                String label = algoName + "'s MST — " + String.format("%.2f km", result.totalCost);
                g2.setFont(FONT_SUBTITLE);
                g2.setColor(new Color(20,28,48,200));
                g2.fillRoundRect(10, 8, g2.getFontMetrics().stringWidth(label)+18, 22, 6, 6);
                g2.setColor(ACCENT_GREEN);
                g2.drawString(label, 18, 24);
            }

            // Legend — white text on dark canvas
            int lx = w - 185, ly = 10;
            g2.setColor(new Color(10, 14, 32, 215));
            g2.fillRoundRect(lx - 10, ly - 6, 185, 92, 10, 10);
            g2.setColor(new Color(60, 80, 130));
            g2.drawRoundRect(lx - 10, ly - 6, 185, 92, 10, 10);
            g2.setFont(FONT_SMALL);

            // Row 1 — MST road
            g2.setColor(ACCENT_GREEN); g2.fillRect(lx, ly+4, 16, 3);
            g2.setColor(Color.WHITE);  g2.drawString("MST road (selected)", lx+22, ly+11);

            // Row 2 — Other road
            g2.setColor(new Color(60, 80, 120)); g2.fillRect(lx, ly+22, 16, 2);
            g2.setColor(new Color(160, 175, 210)); g2.drawString("Other road",          lx+22, ly+30);

            // Row 3 — City in MST
            g2.setColor(ACCENT_GREEN); g2.fillOval(lx, ly+40, 10, 10);
            g2.setColor(Color.WHITE);  g2.drawString("City in MST",          lx+22, ly+49);

            // Row 4 — City not yet in MST
            g2.setColor(ACCENT_BLUE);  g2.fillOval(lx, ly+60, 10, 10);
            g2.setColor(new Color(160, 180, 220)); g2.drawString("City not in MST",     lx+22, ly+69);
        }
    }
}
