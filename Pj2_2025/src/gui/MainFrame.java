// src/gui/MainFrame.java
package gui;

import graph.Edge;
import graph.Graph;
import graph.Graph.OptimizationCriteria;
import model.Station;
import model.TransportData;
import util.SimpleJsonParser;
import util.TransportDataMapper;
import util.TransportDataGenerator;
import manager.ReceiptManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Main application window for finding optimal transport routes.
 * Dark-themed, modern UI with interactive map and route search panel.
 */
public class MainFrame extends JFrame {

    public static final long serialVersionUID = 1L;

    // === Dark Color Palette ===
    public static final Color BG_DARK       = new Color(18, 18, 30);
    public static final Color BG_PANEL      = new Color(28, 28, 46);
    public static final Color BG_CARD       = new Color(38, 38, 60);
    public static final Color ACCENT        = new Color(99, 102, 241);
    public static final Color ACCENT_HOVER  = new Color(129, 140, 255);
    public static final Color SUCCESS       = new Color(52, 211, 153);
    public static final Color WARNING       = new Color(251, 191, 36);
    public static final Color DANGER        = new Color(248, 113, 113);
    public static final Color TEXT_PRIMARY  = new Color(236, 236, 255);
    public static final Color TEXT_MUTED    = new Color(148, 148, 180);
    public static final Color BORDER_COLOR  = new Color(60, 60, 90);

    public JTextField txtNRows, txtMCols;
    public JComboBox<String> cmbStartStation, cmbEndStation;
    public ButtonGroup criteriaGroup;
    public JRadioButton rbTime, rbPrice, rbTransfers;
    public JTable routeTable;
    public DefaultTableModel routeTableModel;
    public JLabel lblTotalTickets, lblTotalRevenue, lblStatus;
    public MapPanel mapPanel;
    public TransportData transportData;
    public Graph transportGraph;
    public Graph.Path currentOptimalPath;

    public MainFrame() {
        applyDarkDefaults();
        setTitle("TransitFinder — Optimal Route Navigator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DARK);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(760);
        split.setDividerSize(3);
        split.setBorder(null);
        split.setBackground(BG_DARK);

        mapPanel = new MapPanel();
        mapPanel.setBackground(BG_DARK);
        split.setLeftComponent(mapPanel);
        split.setRightComponent(buildControlPanel());
        root.add(split, BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        updateReceiptStats();
    }

    private void applyDarkDefaults() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) {}
        UIManager.put("OptionPane.background", BG_PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("Table.background", BG_CARD);
        UIManager.put("Table.foreground", TEXT_PRIMARY);
        UIManager.put("Table.gridColor", BORDER_COLOR);
        UIManager.put("TableHeader.background", BG_PANEL);
        UIManager.put("TableHeader.foreground", ACCENT_HOVER);
        UIManager.put("ScrollPane.background", BG_CARD);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout(15, 0));
        h.setBackground(BG_PANEL);
        h.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(12, 20, 12, 20)));

        JLabel title = new JLabel("  TransitFinder");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(ACCENT_HOVER);
        h.add(title, BorderLayout.WEST);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        row.setOpaque(false);
        row.add(muted("Rows (n):"));
        txtNRows = darkField("5", 4);
        row.add(txtNRows);
        row.add(muted("Cols (m):"));
        txtMCols = darkField("5", 4);
        row.add(txtMCols);
        JButton gen = gradientButton("Generate / Load Data", ACCENT);
        gen.addActionListener(e -> loadTransportData());
        row.add(gen);
        h.add(row, BorderLayout.CENTER);
        return h;
    }

    private JPanel buildControlPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG_PANEL);
        p.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Search card
        JPanel card = darkCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 6, 5, 6);

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0; card.add(muted("From:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        cmbStartStation = darkCombo(); card.add(cmbStartStation, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0; card.add(muted("To:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        cmbEndStation = darkCombo(); card.add(cmbEndStation, gc);

        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0; card.add(muted("Optimize by:"), gc);
        gc.gridx = 1;
        JPanel crit = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        crit.setOpaque(false);
        criteriaGroup = new ButtonGroup();
        rbTime      = darkRadio("Time");
        rbPrice     = darkRadio("Price");
        rbTransfers = darkRadio("Transfers");
        rbTime.setSelected(true);
        criteriaGroup.add(rbTime); criteriaGroup.add(rbPrice); criteriaGroup.add(rbTransfers);
        crit.add(rbTime); crit.add(rbPrice); crit.add(rbTransfers);
        card.add(crit, gc);

        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btns.setOpaque(false);
        JButton bFind = gradientButton("Find Route", SUCCESS);
        bFind.addActionListener(e -> findAndDisplayRoute());
        JButton bAlt  = gradientButton("Alternative Routes", WARNING);
        bAlt.addActionListener(e -> showAdditionalRoutes());
        btns.add(bFind); btns.add(bAlt);
        card.add(btns, gc);

        p.add(card, BorderLayout.NORTH);

        // Route table
        routeTableModel = new DefaultTableModel(
            new Object[]{"From", "To", "Type", "Dep. Time", "Duration (min)", "Price (€)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        routeTable = new JTable(routeTableModel);
        styleTable(routeTable);
        JScrollPane scroll = new JScrollPane(routeTable);
        scroll.getViewport().setBackground(BG_CARD);
        scroll.setBackground(BG_CARD);
        scroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        p.add(scroll, BorderLayout.CENTER);

        // Bottom
        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.setOpaque(false);
        JButton buy = gradientButton("Buy Ticket", ACCENT);
        buy.addActionListener(e -> buyTicket());
        JPanel buyRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buyRow.setOpaque(false); buyRow.add(buy);
        bottom.add(buyRow, BorderLayout.NORTH);

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        stats.setOpaque(false);
        lblTotalTickets = muted("Tickets sold: 0");
        lblTotalRevenue = muted("Total revenue: 0.00 E");
        stats.add(lblTotalTickets); stats.add(muted("|")); stats.add(lblTotalRevenue);
        bottom.add(stats, BorderLayout.SOUTH);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        bar.setBackground(new Color(12, 12, 22));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        lblStatus = new JLabel("Ready. Generate or load map data to begin.");
        lblStatus.setForeground(TEXT_MUTED);
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bar.add(lblStatus);
        return bar;
    }

    // ── Widget helpers ──

    private JLabel muted(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(TEXT_MUTED);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return l;
    }

    private JTextField darkField(String def, int cols) {
        JTextField tf = new JTextField(def, cols);
        tf.setBackground(BG_CARD);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_HOVER);
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_COLOR), new EmptyBorder(3,6,3,6)));
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return tf;
    }

    private JComboBox<String> darkCombo() {
        JComboBox<String> cb = new JComboBox<>();
        cb.setBackground(BG_CARD);
        cb.setForeground(TEXT_PRIMARY);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return cb;
    }

    private JRadioButton darkRadio(String t) {
        JRadioButton rb = new JRadioButton(t);
        rb.setForeground(TEXT_PRIMARY);
        rb.setOpaque(false);
        rb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return rb;
    }

    private JPanel darkCard() {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(10,12,10,12)));
        return p;
    }

    private JButton gradientButton(String text, Color base) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? base.darker() :
                          getModel().isRollover() ? base.brighter() : base;
                g2.setPaint(new GradientPaint(0, 0, c.brighter(), 0, getHeight(), c.darker()));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    private void styleTable(JTable t) {
        t.setBackground(BG_CARD); t.setForeground(TEXT_PRIMARY);
        t.setGridColor(BORDER_COLOR); t.setRowHeight(26);
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));
        t.setSelectionBackground(ACCENT); t.setSelectionForeground(Color.WHITE);
        t.setShowHorizontalLines(true); t.setShowVerticalLines(false);
        JTableHeader hdr = t.getTableHeader();
        hdr.setBackground(BG_PANEL); hdr.setForeground(ACCENT_HOVER);
        hdr.setFont(new Font("SansSerif", Font.BOLD, 13));
        hdr.setBorder(new LineBorder(BORDER_COLOR));
    }

    private void setStatus(String msg) { if (lblStatus != null) lblStatus.setText(msg); }

    // ── Application logic ──

    public void loadTransportData() {
        try {
            int n = Integer.parseInt(txtNRows.getText().trim());
            int m = Integer.parseInt(txtMCols.getText().trim());
            if (n <= 0 || m <= 0) {
                JOptionPane.showMessageDialog(this, "Map dimensions must be positive integers.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            setStatus("Generating data for a " + n + " x " + m + " grid...");
            TransportDataGenerator gen = new TransportDataGenerator(n, m);
            TransportData generated = gen.generateData();
            gen.saveToJson(generated, "transport_data.json");

            String json = new String(Files.readAllBytes(Paths.get("transport_data.json")));
            transportData = TransportDataMapper.mapToTransportData(new SimpleJsonParser(json).parse());

            transportGraph = new Graph();
            transportGraph.buildGraph(transportData);

            populateStationComboBoxes();
            mapPanel.setCountryMap(transportData.getCountryMap());
            mapPanel.setStations(transportData.getStations());
            mapPanel.setTransportGraph(transportGraph);
            mapPanel.repaint();

            setStatus("Graph built — " + transportGraph.getAllNodes().size() + " nodes across " + (n * m) + " cities.");
            JOptionPane.showMessageDialog(this, "Data generated and loaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid integers for map dimensions.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "File I/O error: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unexpected error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void populateStationComboBoxes() {
        if (transportData == null || transportData.getStations().isEmpty()) {
            cmbStartStation.removeAllItems(); cmbEndStation.removeAllItems(); return;
        }
        Vector<String> ids = new Vector<>();
        for (Station s : transportData.getStations()) { ids.add(s.getBusStation()); ids.add(s.getTrainStation()); }
        ids = ids.stream().distinct().sorted().collect(Collectors.toCollection(Vector::new));
        cmbStartStation.setModel(new DefaultComboBoxModel<>(ids));
        cmbEndStation.setModel(new DefaultComboBoxModel<>(ids));
        if (ids.size() > 1) cmbEndStation.setSelectedIndex(1);
    }

    public void findAndDisplayRoute() {
        if (transportGraph == null || cmbStartStation.getSelectedItem() == null || cmbEndStation.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please generate/load map data and select stations first.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String start = (String) cmbStartStation.getSelectedItem();
        String end   = (String) cmbEndStation.getSelectedItem();
        if (start.equals(end)) {
            JOptionPane.showMessageDialog(this, "Start and destination stations must be different.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        OptimizationCriteria criteria = rbTime.isSelected() ? OptimizationCriteria.TIME
                : rbPrice.isSelected() ? OptimizationCriteria.PRICE : OptimizationCriteria.TRANSFERS;

        currentOptimalPath = transportGraph.findOptimalRoute(start, end, criteria);
        routeTableModel.setRowCount(0);
        mapPanel.clearHighlightedPath();

        if (currentOptimalPath != null) {
            for (Edge edge : currentOptimalPath.getEdges()) {
                String dep = "", dur = "", price = "", type = edge.getType();
                if ("departure".equals(type) && edge.getDepartureDetails() != null) {
                    dep   = edge.getDepartureDetails().getDepartureTime();
                    dur   = String.valueOf(edge.getDepartureDetails().getDuration());
                    price = String.valueOf(edge.getDepartureDetails().getPrice());
                } else if ("transfer".equals(type)) {
                    dep = "—"; dur = String.valueOf((int)edge.getWeight()); price = "0";
                }
                routeTableModel.addRow(new Object[]{
                    edge.getSource().getStationName() + " (" + edge.getSource().getCity() + ")",
                    edge.getDestination().getStationName() + " (" + edge.getDestination().getCity() + ")",
                    type, dep, dur, price
                });
            }
            mapPanel.setHighlightedPath(currentOptimalPath);
            mapPanel.repaint();
            long d = Math.abs(currentOptimalPath.getTotalDurationMinutes());
            int  c = currentOptimalPath.getTotalPrice(), x = currentOptimalPath.getNumberOfTransfers();
            setStatus("Route found — " + d + " min | " + c + " E | " + x + " transfer(s)");
            JOptionPane.showMessageDialog(this,
                String.format("Route found (optimized by %s):\n  Duration  : %d min\n  Cost      : %d E\n  Transfers : %d",
                    criteria.toString().toLowerCase(), d, c, x),
                "Route Found", JOptionPane.INFORMATION_MESSAGE);
        } else {
            setStatus("No route found for the selected stations/criteria.");
            JOptionPane.showMessageDialog(this, "No route found for the selected criteria.", "Not Found", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void buyTicket() {
        if (currentOptimalPath == null) {
            JOptionPane.showMessageDialog(this, "Find a route before purchasing a ticket.", "No Route", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String from = currentOptimalPath.getNodes().get(0).getStationName() + " (" + currentOptimalPath.getNodes().get(0).getCity() + ")";
        String to   = currentOptimalPath.getNodes().get(currentOptimalPath.getNodes().size()-1).getStationName()
                    + " (" + currentOptimalPath.getNodes().get(currentOptimalPath.getNodes().size()-1).getCity() + ")";
        StringBuilder segs = new StringBuilder();
        for (Edge e : currentOptimalPath.getEdges()) segs.append("  ").append(e).append("\n");
        String receipt = String.format(
            "TRANSIT TICKET RECEIPT\n===============================\nRoute     : %s -> %s\nIssued    : %s\nDuration  : %d min\nTotal cost: %d E\nTransfers : %d\n-------------------------------\nSegments:\n%s",
            from, to,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
            Math.abs(currentOptimalPath.getTotalDurationMinutes()),
            currentOptimalPath.getTotalPrice(),
            currentOptimalPath.getNumberOfTransfers(),
            segs
        );
        try {
            ReceiptManager.saveReceipt(receipt);
            updateReceiptStats();
            setStatus("Ticket purchased! Receipt saved.");
            JOptionPane.showMessageDialog(this, "Ticket purchased successfully! Receipt saved.", "Purchase Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving receipt: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showAdditionalRoutes() {
        if (transportGraph == null || cmbStartStation.getSelectedItem() == null || cmbEndStation.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please generate/load data and select stations first.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String start = (String) cmbStartStation.getSelectedItem();
        String end   = (String) cmbEndStation.getSelectedItem();
        if (start.equals(end)) {
            JOptionPane.showMessageDialog(this, "Start and destination stations must be different.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        OptimizationCriteria criteria = rbTime.isSelected() ? OptimizationCriteria.TIME
                : rbPrice.isSelected() ? OptimizationCriteria.PRICE : OptimizationCriteria.TRANSFERS;
        List<Graph.Path> routes = transportGraph.findMultiplePaths(start, end, criteria, 5);
        if (routes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No alternative routes found.", "No Routes", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        new AdditionalRoutesWindow(this, routes, criteria).setVisible(true);
    }

    public void updateReceiptStats() {
        try {
            lblTotalTickets.setText("Tickets sold: " + ReceiptManager.getTotalTicketsSold());
            lblTotalRevenue.setText(String.format("Total revenue: %.2f E", ReceiptManager.getTotalRevenue()));
        } catch (IOException ex) {
            lblTotalTickets.setText("Tickets sold: N/A");
            lblTotalRevenue.setText("Total revenue: N/A");
        }
    }
}
