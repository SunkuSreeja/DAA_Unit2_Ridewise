package ui;

import algorithms.JobSequencingAlgorithm;
import algorithms.JobSequencingAlgorithm.JSResult;
import data.DataStore;
import models.RideRequest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static ui.UIConstants.*;

/**
 * RIDE SCHEDULING — Job Sequencing with Deadlines (Greedy)
 * Completely rewritten to be beginner-friendly and user-input-first.
 */
public class JobSequencingPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final transient DataStore ds = DataStore.getInstance();
    private final transient JobSequencingAlgorithm algo = new JobSequencingAlgorithm();

    // User input fields (PRIMARY — shown at top)
    private JTextField nameField, deadlineField, rewardField, destField;

    // Table showing all current rides
    private DefaultTableModel tableModel;
    private JTable rideTable;

    // Output areas
    private JTextArea stepsArea;
    private JTextArea summaryArea;
    private ScheduleVisualPanel scheduleVisual;

    @SuppressWarnings("this-escape")
    public JobSequencingPanel() {
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
        titleRow.add(makeLabel("📋  Ride Scheduling — Job Sequencing with Deadlines", FONT_TITLE, ACCENT_ORANGE));
        titleRow.add(makeLabel("Greedy algorithm: schedule rides to maximise total reward within deadlines  |  O(n²)",
                FONT_SMALL, TEXT_SECONDARY));
        content.add(titleRow);
        content.add(Box.createVerticalStrut(12));

        // 2. Concept boxes
        JPanel conceptRow = new JPanel(new GridLayout(1, 3, 12, 0));
        conceptRow.setBackground(BG_DARK);
        conceptRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        conceptRow.add(makeConceptBox("🎯  What is the Problem?",
                "We have many ride requests but limited time slots.\n" +
                "Each ride has a DEADLINE (must start by slot X)\n" +
                "and a REWARD (money we earn).\n" +
                "We can only do ONE ride per time slot.\n" +
                "Goal: Pick rides to EARN THE MOST MONEY.", ACCENT_ORANGE));
        conceptRow.add(makeConceptBox("🧠  The Greedy Idea",
                "GREEDY = Always grab the best available option.\n\n" +
                "Step 1: Sort all rides by reward (highest first).\n" +
                "Step 2: For each ride, find the LATEST empty slot\n" +
                "        before its deadline and assign it there.\n" +
                "Step 3: If no slot is free → skip that ride.\n\n" +
                "Why latest slot? To keep earlier slots free!", ACCENT_ORANGE));
        conceptRow.add(makeConceptBox("✅  Why This Works",
                "By always picking the highest-reward ride first,\n" +
                "we guarantee maximum total earnings.\n\n" +
                "Assigning to the LATEST possible slot is smart:\n" +
                "it leaves room for earlier-deadline rides.\n\n" +
                "Time Complexity: O(n²)\n" +
                "n = number of rides", ACCENT_GREEN));
        content.add(conceptRow);
        content.add(Box.createVerticalStrut(14));

        // 3. Input form
        JPanel inputCard = new JPanel(new BorderLayout(0, 8));
        inputCard.setBackground(BG_CARD);
        inputCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_ORANGE, 2),
                new EmptyBorder(10, 14, 10, 14)));
        inputCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        inputCard.add(makeLabel("➕  Add Your Own Ride Request", FONT_SUBTITLE, ACCENT_ORANGE), BorderLayout.NORTH);

        nameField = makeField(14); destField = makeField(14);
        deadlineField = makeField(5); rewardField = makeField(7);

        JPanel form = new JPanel(new GridLayout(2, 4, 10, 8));
        form.setBackground(BG_CARD);
        form.add(makeLabel("Passenger Name:",    FONT_BODY, TEXT_PRIMARY)); form.add(nameField);
        form.add(makeLabel("Destination:",       FONT_BODY, TEXT_PRIMARY)); form.add(destField);
        form.add(makeLabel("Deadline Slot (1–6):",FONT_BODY, TEXT_PRIMARY)); form.add(deadlineField);
        form.add(makeLabel("Reward (₹):",        FONT_BODY, TEXT_PRIMARY)); form.add(rewardField);
        inputCard.add(form, BorderLayout.CENTER);

        JPanel fBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        fBtnRow.setBackground(BG_CARD);
        JButton addBtn2     = makeButton("➕ Add Ride", ACCENT_ORANGE);
        JButton clearAllBtn = makeSmallButton("🗑 Clear All", ACCENT_PINK);
        JButton loadDemoBtn = makeSmallButton("📦 Load Demo Data", BORDER_COLOR);
        addBtn2.addActionListener(e -> addRide());
        clearAllBtn.addActionListener(e -> clearAll());
        loadDemoBtn.addActionListener(e -> loadDemo());
        fBtnRow.add(addBtn2); fBtnRow.add(clearAllBtn); fBtnRow.add(loadDemoBtn);
        fBtnRow.add(makeLabel("  💡 Slot = 30-min window  |  Deadline = must start by that slot",
                FONT_SMALL, TEXT_SECONDARY));
        inputCard.add(fBtnRow, BorderLayout.SOUTH);
        content.add(inputCard);
        content.add(Box.createVerticalStrut(14));

        // 4. Ride table
        String[] cols = {"#", "Passenger", "Destination", "Deadline Slot", "Reward (₹)", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        rideTable = new JTable(tableModel);
        styleTable(rideTable);
        rideTable.getColumnModel().getColumn(0).setMaxWidth(30);
        rideTable.getColumnModel().getColumn(3).setMaxWidth(110);
        rideTable.getColumnModel().getColumn(4).setMaxWidth(90);
        rideTable.getColumnModel().getColumn(5).setMaxWidth(90);

        JPanel tableCard = makeCard("📋  Current Ride Requests", ACCENT_ORANGE);
        JScrollPane tableScroll = new JScrollPane(rideTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        tableScroll.setPreferredSize(new Dimension(100, 200));
        tableCard.add(tableScroll, BorderLayout.CENTER);
        JPanel tblBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        tblBtnRow.setBackground(BG_CARD);
        JButton delBtn = makeSmallButton("🗑 Remove Selected", ACCENT_PINK);
        delBtn.addActionListener(e -> removeSelected());
        JButton runBtn = makeButton("▶  Run Scheduler Now", ACCENT_ORANGE);
        runBtn.addActionListener(e -> runAlgorithm());
        tblBtnRow.add(delBtn); tblBtnRow.add(runBtn);
        tableCard.add(tblBtnRow, BorderLayout.SOUTH);
        tableCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        content.add(tableCard);
        content.add(Box.createVerticalStrut(14));

        // 5. Algorithm steps
        JPanel stepsCard = makeCard("🧠  Algorithm Steps — Plain English Explanation", ACCENT_ORANGE);
        stepsArea = makeOutputArea(12, 60);
        stepsArea.setForeground(new Color(30, 64, 175));
        stepsArea.setText("← Add rides above, then click  ▶ Run Scheduler Now");
        stepsCard.add(makeScroll(stepsArea), BorderLayout.CENTER);
        stepsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        content.add(stepsCard);
        content.add(Box.createVerticalStrut(14));

        // 6. Visual timeline
        JPanel visCard = makeCard("🗓  Schedule Timeline (Which ride goes in which slot)", ACCENT_ORANGE);
        scheduleVisual = new ScheduleVisualPanel();
        scheduleVisual.setPreferredSize(new Dimension(100, 130));
        scheduleVisual.setMinimumSize(new Dimension(100, 130));
        scheduleVisual.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        visCard.add(scheduleVisual, BorderLayout.CENTER);
        visCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 185));
        content.add(visCard);
        content.add(Box.createVerticalStrut(14));

        // 7. Summary
        JPanel sumCard = makeCard("✅  Final Result & Explanation", ACCENT_GREEN);
        summaryArea = makeOutputArea(6, 60);
        summaryArea.setForeground(new Color(5, 120, 85));
        summaryArea.setText("Results will appear here after you run the scheduler.");
        sumCard.add(makeScroll(summaryArea), BorderLayout.CENTER);
        sumCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        content.add(sumCard);
        content.add(Box.createVerticalStrut(8));

        JScrollPane scroll = new JScrollPane(content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        add(scroll, BorderLayout.CENTER);

        loadDemo();
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

    private void addRide() {
        try {
            String name = nameField.getText().trim();
            String dest = destField.getText().trim();
            if (name.isEmpty()) throw new IllegalArgumentException(
                "Please enter the passenger name.");
            if (dest.isEmpty()) dest = "City Center";

            int dl = Integer.parseInt(deadlineField.getText().trim());
            int rw = Integer.parseInt(rewardField.getText().trim());

            if (dl < 1 || dl > 6)
                throw new IllegalArgumentException(
                    "Deadline slot must be between 1 and 6.\n" +
                    "(We support up to 6 time slots in this demo.)");
            if (rw <= 0)
                throw new IllegalArgumentException("Reward must be a positive number.");

            RideRequest r = new RideRequest(name, 0, 0, dest, dl, rw);
            ds.getRequests().add(r);
            refreshTable();

            nameField.setText(""); destField.setText("");
            deadlineField.setText(""); rewardField.setText("");
            nameField.requestFocus();

        } catch (NumberFormatException ex) {
            showError("Deadline Slot and Reward must be whole numbers.\n" +
                      "Example: Deadline = 2, Reward = 300");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void removeSelected() {
        int row = rideTable.getSelectedRow();
        if (row < 0) { showError("Please click on a ride in the table first."); return; }
        List<RideRequest> reqs = ds.getRequests();
        if (row < reqs.size()) { reqs.remove(row); refreshTable(); }
    }

    private void clearAll() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Remove ALL ride requests?", "Clear All", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ds.getRequests().clear();
            refreshTable();
            stepsArea.setText("All rides cleared. Add your own rides and run the scheduler.");
            summaryArea.setText("");
            scheduleVisual.setResult(null, null);
            scheduleVisual.repaint();
        }
    }

    private void loadDemo() {
        ds.loadDefaults();
        refreshTable();
        stepsArea.setText("Demo rides loaded! Click  ▶ Run Scheduler Now  to see the algorithm.");
        summaryArea.setText("");
        scheduleVisual.setResult(null, null);
        scheduleVisual.repaint();
    }

    private void runAlgorithm() {
        List<RideRequest> requests = ds.getRequests();
        if (requests.isEmpty()) {
            showError("No rides to schedule!\nPlease add at least one ride request first.");
            return;
        }

        JSResult result = algo.schedule(requests);

        // ── Plain-English Steps ────────────────────────────────────────
        StringBuilder steps = new StringBuilder();
        steps.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        steps.append("  HOW THE GREEDY ALGORITHM WORKS — STEP BY STEP\n");
        steps.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        steps.append("STEP 1 — List all rides and their rewards:\n");
        int idx = 1;
        for (RideRequest r : requests) {
            steps.append(String.format("   %d. %s → ₹%d reward, must start by Slot %d\n",
                    idx++, r.getPassengerName(), r.getReward(), r.getDeadline()));
        }

        steps.append("\nSTEP 2 — Sort by reward (highest first).\n");
        steps.append("   Reason: We always want to try the most profitable\n");
        steps.append("   ride first — this is the GREEDY CHOICE.\n\n");

        // Reconstruct sorted order from steps
        List<RideRequest> sorted = new ArrayList<>(requests);
        sorted.sort((a, b) -> Integer.compare(b.getReward(), a.getReward()));
        steps.append("   Sorted order:\n");
        int rank = 1;
        for (RideRequest r : sorted) {
            steps.append(String.format("   %d. %s  (₹%d, Deadline: Slot %d)\n",
                    rank++, r.getPassengerName(), r.getReward(), r.getDeadline()));
        }

        steps.append("\nSTEP 3 — Assign each ride to a time slot:\n");
        steps.append("   Rule: Try the LATEST empty slot at or before the deadline.\n");
        steps.append("   (This leaves earlier slots free for other rides!)\n\n");

        for (String s : result.steps) {
            // Re-express steps in friendlier language
            if (s.startsWith("  ✓")) {
                // Parse: "  ✓ Assigned [name] ... → Slot X"
                steps.append("   ✅ ").append(s.trim().substring(1).trim()).append("\n");
                // Add explanation
                steps.append("      Why? That was the latest empty slot before the deadline.\n\n");
            } else if (s.startsWith("  ✗")) {
                steps.append("   ❌ ").append(s.trim().substring(1).trim()).append("\n");
                steps.append("      Why skipped? All slots before its deadline were already\n");
                steps.append("      taken by higher-reward rides. This ride is MISSED.\n\n");
            }
        }

        steps.append("STEP 4 — Final assigned schedule:\n");
        if (result.slotAssignment != null) {
            for (int s = 1; s < result.slotAssignment.length; s++) {
                boolean filled = result.slotAssignment[s] != -1;
                if (filled) {
                    // find the ride
                    for (RideRequest r : requests) {
                        if (r.getId() == result.slotAssignment[s]) {
                            steps.append(String.format("   Slot %d → %-20s (₹%d earned)\n",
                                    s, r.getPassengerName(), r.getReward()));
                            break;
                        }
                    }
                } else {
                    steps.append(String.format("   Slot %d → [No ride assigned]\n", s));
                }
            }
        }

        stepsArea.setText(steps.toString());
        stepsArea.setCaretPosition(0);

        // ── Summary ────────────────────────────────────────────────────
        StringBuilder sb = new StringBuilder();
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("  RIDES ACCEPTED (").append(result.scheduledJobs.size()).append(")\n");
        for (RideRequest r : result.scheduledJobs) {
            sb.append(String.format("  ✅ %-20s %-20s  +₹%d\n",
                    r.getPassengerName(), r.getDestination(), r.getReward()));
        }
        if (!result.missedJobs.isEmpty()) {
            sb.append("\n  RIDES MISSED (").append(result.missedJobs.size()).append(")\n");
            for (RideRequest r : result.missedJobs) {
                sb.append(String.format("  ❌ %-20s (Deadline Slot %d, ₹%d lost)\n",
                        r.getPassengerName(), r.getDeadline(), r.getReward()));
            }
        }
        sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append(String.format("  TOTAL MONEY EARNED  =  ₹%d\n", result.totalReward));
        sb.append(String.format("  Rides done: %d / %d\n",
                result.scheduledJobs.size(), requests.size()));
        summaryArea.setText(sb.toString());

        // ── Visual timeline ────────────────────────────────────────────
        scheduleVisual.setResult(result, requests);
        scheduleVisual.repaint();

        // Update table with status column
        refreshTableWithStatus(result, requests);
    }

    // ── Table helpers ──────────────────────────────────────────────────

    private void refreshTable() {
        tableModel.setRowCount(0);
        int i = 1;
        for (RideRequest r : ds.getRequests()) {
            tableModel.addRow(new Object[]{
                i++, r.getPassengerName(), r.getDestination(),
                "Slot " + r.getDeadline(), "₹" + r.getReward(), "—"
            });
        }
    }

    private void refreshTableWithStatus(JSResult result, List<RideRequest> reqs) {
        tableModel.setRowCount(0);
        int i = 1;
        for (RideRequest r : reqs) {
            String status = r.isScheduled() ? "✅ Accepted" : "❌ Missed";
            tableModel.addRow(new Object[]{
                i++, r.getPassengerName(), r.getDestination(),
                "Slot " + r.getDeadline(), "₹" + r.getReward(), status
            });
        }
        // Colour-code rows
        rideTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBackground(Color.WHITE);
                setForeground(TEXT_PRIMARY);
                String status = (String) t.getValueAt(row, 5);
                if ("✅ Accepted".equals(status)) setForeground(ACCENT_GREEN);
                else if ("❌ Missed".equals(status)) setForeground(ACCENT_PINK);
                if (sel) setBackground(new Color(64, 100, 180, 60));
                return this;
            }
        });
    }

    private void styleTable(JTable t) {
        t.setBackground(Color.WHITE);
        t.setForeground(TEXT_PRIMARY);
        t.setFont(FONT_BODY);
        t.setRowHeight(28);
        t.setGridColor(BORDER_COLOR);
        t.setSelectionBackground(new Color(219, 234, 254));  // light blue highlight
        t.setSelectionForeground(TEXT_PRIMARY);
        t.getTableHeader().setBackground(new Color(243, 244, 246));
        t.getTableHeader().setForeground(TEXT_PRIMARY);
        t.getTableHeader().setFont(FONT_BADGE);
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 1));
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Input Error", JOptionPane.WARNING_MESSAGE);
    }

    // ── Schedule visual panel ──────────────────────────────────────────
    class ScheduleVisualPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private transient JSResult result;
        private transient List<RideRequest> allRequests;

        ScheduleVisualPanel() { setBackground(INPUT_BG); }

        void setResult(JSResult r, List<RideRequest> reqs) {
            this.result = r; this.allRequests = reqs;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(20, 25, 45));
            g2.fillRect(0, 0, w, h);

            if (result == null || result.slotAssignment == null) {
                g2.setFont(FONT_BODY);
                g2.setColor(TEXT_SECONDARY);
                g2.drawString("Run the scheduler to see the visual timeline here.", 20, h / 2);
                return;
            }

            int n = result.slotAssignment.length - 1;
            if (n <= 0) return;

            int margin = 16;
            int slotW = Math.max(60, (w - 2 * margin) / n);
            int slotH = h - 50;
            int startY = 12;

            for (int s = 1; s <= n; s++) {
                int x = margin + (s - 1) * slotW;
                boolean filled = result.slotAssignment[s] != -1;

                // Box
                Color fill = filled ? new Color(251, 146, 60, 200) : new Color(35, 45, 70, 180);
                Color border = filled ? ACCENT_ORANGE : BORDER_COLOR;
                g2.setColor(fill);
                g2.fillRoundRect(x + 2, startY, slotW - 6, slotH, 10, 10);
                g2.setColor(border);
                g2.setStroke(new BasicStroke(filled ? 2f : 1f));
                g2.drawRoundRect(x + 2, startY, slotW - 6, slotH, 10, 10);

                // Slot label below
                g2.setFont(FONT_BADGE);
                g2.setColor(TEXT_SECONDARY);
                String slotLabel = "Slot " + s;
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(slotLabel, x + (slotW - fm.stringWidth(slotLabel)) / 2,
                        startY + slotH + 16);

                if (filled && allRequests != null) {
                    for (RideRequest r : allRequests) {
                        if (r.getId() == result.slotAssignment[s]) {
                            // Name
                            String nameShort = r.getPassengerName().split(" ")[0];
                            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                            g2.setColor(Color.WHITE);
                            FontMetrics fmN = g2.getFontMetrics();
                            int tx = x + (slotW - fmN.stringWidth(nameShort)) / 2 - 2;
                            g2.drawString(nameShort, tx, startY + slotH / 2 - 5);

                            // Reward
                            String rw = "₹" + r.getReward();
                            g2.setFont(FONT_SMALL);
                            g2.setColor(ACCENT_GREEN);
                            FontMetrics fmR = g2.getFontMetrics();
                            int rwtx = x + (slotW - fmR.stringWidth(rw)) / 2 - 2;
                            g2.drawString(rw, rwtx, startY + slotH / 2 + 11);
                            break;
                        }
                    }
                } else if (!filled) {
                    g2.setFont(FONT_SMALL);
                    g2.setColor(BORDER_COLOR);
                    g2.drawString("empty", x + slotW / 2 - 14, startY + slotH / 2 + 4);
                }
            }

            // Total label
            g2.setFont(FONT_SUBTITLE);
            g2.setColor(ACCENT_GREEN);
            String totalStr = "Total Earned: ₹" + result.totalReward;
            g2.drawString(totalStr, w - g2.getFontMetrics().stringWidth(totalStr) - margin, startY + 16);
        }
    }
}
