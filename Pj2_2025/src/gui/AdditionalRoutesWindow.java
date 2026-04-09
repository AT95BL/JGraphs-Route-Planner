// src/gui/AdditionalRoutesWindow.java
package gui;

import graph.Edge;
import graph.Graph;
import manager.ReceiptManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dialog displaying the top alternative routes sorted by the chosen optimization criteria.
 */
public class AdditionalRoutesWindow extends JDialog {

    private static final long serialVersionUID = 1L;
    private final MainFrame parentWindow;
    private final List<Graph.Path> topRoutes;
    private final Graph.OptimizationCriteria criteria;

    public AdditionalRoutesWindow(MainFrame parent, List<Graph.Path> topRoutes, Graph.OptimizationCriteria criteria) {
        super(parent, "Alternative Routes", true);
        this.parentWindow = parent;
        this.topRoutes = topRoutes;
        this.criteria = criteria;

        setSize(860, 620);
        setLocationRelativeTo(parent);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(MainFrame.BG_DARK);
        content.setBorder(new EmptyBorder(14, 14, 14, 14));
        setContentPane(content);

        JLabel title = new JLabel("Alternative Routes — optimized by " + criteria.toString().toLowerCase(), SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(MainFrame.ACCENT_HOVER);
        content.add(title, BorderLayout.NORTH);

        JPanel routesPanel = new JPanel();
        routesPanel.setBackground(MainFrame.BG_DARK);
        routesPanel.setLayout(new BoxLayout(routesPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(routesPanel);
        scroll.getViewport().setBackground(MainFrame.BG_DARK);
        scroll.setBackground(MainFrame.BG_DARK);
        scroll.setBorder(new LineBorder(MainFrame.BORDER_COLOR));
        content.add(scroll, BorderLayout.CENTER);

        if (topRoutes.isEmpty()) {
            routesPanel.add(new JLabel("No routes to display."));
        } else {
            int num = 1;
            for (Graph.Path path : topRoutes) {
                routesPanel.add(createRoutePanel(path, num++));
                routesPanel.add(Box.createVerticalStrut(8));
            }
        }

        JButton btnClose = gradientButton("Close", MainFrame.DANGER);
        btnClose.addActionListener(e -> dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false);
        btnRow.add(btnClose);
        content.add(btnRow, BorderLayout.SOUTH);
    }

    private JPanel createRoutePanel(Graph.Path path, int num) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(MainFrame.BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                new LineBorder(MainFrame.BORDER_COLOR, 1),
                "Route " + num,
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13),
                MainFrame.ACCENT_HOVER),
            new EmptyBorder(6, 8, 6, 8)));

        JPanel info = new JPanel(new GridLayout(3, 1, 2, 2));
        info.setOpaque(false);
        info.add(label("Duration  : " + Math.abs(path.getTotalDurationMinutes()) + " min"));
        info.add(label("Cost      : " + path.getTotalPrice() + " E"));
        info.add(label("Transfers : " + path.getNumberOfTransfers()));
        panel.add(info, BorderLayout.WEST);

        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"From", "To", "Type", "Dep. Time", "Duration (min)", "Price (E)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Edge edge : path.getEdges()) {
            String dep = "", dur = "", price = "", type = edge.getType();
            if ("departure".equals(type) && edge.getDepartureDetails() != null) {
                dep   = edge.getDepartureDetails().getDepartureTime();
                dur   = String.valueOf(edge.getDepartureDetails().getDuration());
                price = String.valueOf(edge.getDepartureDetails().getPrice());
            } else if ("transfer".equals(type)) {
                dep = "—"; dur = String.valueOf((int)edge.getWeight()); price = "0";
            }
            model.addRow(new Object[]{
                edge.getSource().getStationName() + " (" + edge.getSource().getCity() + ")",
                edge.getDestination().getStationName() + " (" + edge.getDestination().getCity() + ")",
                type, dep, dur, price
            });
        }
        JTable table = new JTable(model);
        table.setBackground(MainFrame.BG_CARD); table.setForeground(MainFrame.TEXT_PRIMARY);
        table.setGridColor(MainFrame.BORDER_COLOR); table.setRowHeight(22);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setSelectionBackground(MainFrame.ACCENT);
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(MainFrame.BG_CARD);
        sp.setPreferredSize(new Dimension(540, 90));
        sp.setBorder(new LineBorder(MainFrame.BORDER_COLOR));
        panel.add(sp, BorderLayout.CENTER);

        JButton buy = gradientButton("Buy Ticket", MainFrame.ACCENT);
        buy.addActionListener(e -> buyTicket(path));
        JPanel bRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bRow.setOpaque(false); bRow.add(buy);
        panel.add(bRow, BorderLayout.SOUTH);

        return panel;
    }

    private void buyTicket(Graph.Path path) {
        String from = path.getNodes().get(0).getStationName() + " (" + path.getNodes().get(0).getCity() + ")";
        String to   = path.getNodes().get(path.getNodes().size()-1).getStationName()
                    + " (" + path.getNodes().get(path.getNodes().size()-1).getCity() + ")";
        StringBuilder segs = new StringBuilder();
        for (Edge e : path.getEdges()) segs.append("  ").append(e).append("\n");
        String receipt = String.format(
            "TRANSIT TICKET RECEIPT (Alternative Route)\n===============================\nRoute     : %s -> %s\nIssued    : %s\nDuration  : %d min\nTotal cost: %d E\nTransfers : %d\n-------------------------------\nSegments:\n%s",
            from, to,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
            path.getTotalDurationMinutes(), path.getTotalPrice(), path.getNumberOfTransfers(), segs);
        try {
            ReceiptManager.saveReceipt(receipt);
            parentWindow.updateReceiptStats();
            JOptionPane.showMessageDialog(this, "Ticket purchased! Receipt saved.", "Purchase Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving receipt: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(MainFrame.TEXT_PRIMARY);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return l;
    }

    private JButton gradientButton(String text, Color base) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? base.darker() : getModel().isRollover() ? base.brighter() : base;
                g2.setPaint(new GradientPaint(0, 0, c.brighter(), 0, getHeight(), c.darker()));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        return btn;
    }
}
