// src/gui/MapPanel.java
package gui;

import graph.Edge;
import graph.Graph;
import graph.Node;
import model.Station;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel that renders the city grid, bus/train stations, all graph edges,
 * and a highlighted optimal route — all in a dark, epic visual style.
 */
public class MapPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // === Color Palette (mirrors MainFrame) ===
    private static final Color BG_DARK      = new Color(18, 18, 30);
    private static final Color CITY_FILL    = new Color(38, 38, 68);
    private static final Color CITY_BORDER  = new Color(70, 70, 110);
    private static final Color CITY_LABEL   = new Color(180, 180, 230);
    private static final Color BUS_DOT      = new Color(251, 191, 36);   // amber
    private static final Color TRAIN_DOT    = new Color(99, 102, 241);   // indigo
    private static final Color EDGE_COLOR   = new Color(255, 255, 255, 30);
    private static final Color PATH_COLOR   = new Color(52, 211, 153);   // emerald
    private static final Color GLOW_COLOR   = new Color(52, 211, 153, 60);

    private String[][] countryMap;
    private List<Station> stations;
    private Graph transportGraph;
    private Graph.Path highlightedPath;

    private int cellSize = 80;
    private int padding  = 24;
    private final Map<String, Point> nodePositions = new HashMap<>();

    public MapPanel() { setBackground(BG_DARK); }

    public void setCountryMap(String[][] m)  { this.countryMap = m; calculateCellSize(); repaint(); }
    public void setStations(List<Station> s) { this.stations = s; repaint(); }
    public void setTransportGraph(Graph g)   { this.transportGraph = g; repaint(); }
    public void setHighlightedPath(Graph.Path p) { this.highlightedPath = p; repaint(); }
    public void clearHighlightedPath()       { this.highlightedPath = null; repaint(); }

    private void calculateCellSize() {
        if (countryMap == null || countryMap.length == 0) return;
        int w = getWidth() - 2 * padding, h = getHeight() - 2 * padding;
        int nc = countryMap[0].length, nr = countryMap.length;
        if (nc > 0 && nr > 0) {
            cellSize = Math.max(50, Math.min(w / nc, h / nr));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        nodePositions.clear();

        // Dark gradient background
        g2.setPaint(new GradientPaint(0, 0, new Color(12, 12, 24), 0, getHeight(), new Color(22, 22, 42)));
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (countryMap == null || countryMap.length == 0) {
            g2.setColor(new Color(99, 102, 241, 120));
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            String msg = "Generate or load map data to begin";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
            return;
        }

        int numRows = countryMap.length, numCols = countryMap[0].length;
        calculateCellSize();

        // ── 1. Draw city grid ──
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                int x = padding + c * cellSize, y = padding + r * cellSize;
                String city = countryMap[r][c];

                // City cell with gradient fill
                GradientPaint gp = new GradientPaint(x, y, CITY_FILL.brighter(), x, y + cellSize, CITY_FILL.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(x + 2, y + 2, cellSize - 4, cellSize - 4, 8, 8);
                g2.setColor(CITY_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(x + 2, y + 2, cellSize - 4, cellSize - 4, 8, 8);

                // City label
                g2.setColor(CITY_LABEL);
                g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(9, cellSize / 7)));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(city, x + (cellSize - fm.stringWidth(city)) / 2,
                              y + cellSize / 2 - fm.getHeight() / 2 + fm.getAscent());

                // Station dots
                if (stations != null) {
                    for (Station s : stations) {
                        if (s.getCity().equals(city)) {
                            Point busPos   = new Point(x + cellSize / 4,       y + cellSize * 3 / 4);
                            Point trainPos = new Point(x + cellSize * 3 / 4,   y + cellSize / 4);

                            drawStationDot(g2, busPos,   BUS_DOT,   "B");
                            drawStationDot(g2, trainPos, TRAIN_DOT, "T");

                            nodePositions.put(s.getBusStation(),   busPos);
                            nodePositions.put(s.getTrainStation(), trainPos);
                        }
                    }
                }
            }
        }

        // ── 2. Draw all graph edges (dim) ──
        if (transportGraph != null) {
            g2.setColor(EDGE_COLOR);
            g2.setStroke(new BasicStroke(1f));
            for (Node node : transportGraph.getAllNodes()) {
                for (Edge edge : transportGraph.getEdgesFrom(node)) {
                    Point p1 = nodePositions.get(edge.getSource().getId());
                    Point p2 = nodePositions.get(edge.getDestination().getId());
                    if (p1 != null && p2 != null && !p1.equals(p2))
                        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }

        // ── 3. Draw highlighted route with glow ──
        if (highlightedPath != null && !highlightedPath.getEdges().isEmpty()) {
            // Glow pass
            g2.setColor(GLOW_COLOR);
            g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (Edge edge : highlightedPath.getEdges()) {
                Point p1 = nodePositions.get(edge.getSource().getId());
                Point p2 = nodePositions.get(edge.getDestination().getId());
                if (p1 != null && p2 != null && !p1.equals(p2))
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
            // Sharp pass
            g2.setColor(PATH_COLOR);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (Edge edge : highlightedPath.getEdges()) {
                Point p1 = nodePositions.get(edge.getSource().getId());
                Point p2 = nodePositions.get(edge.getDestination().getId());
                if (p1 != null && p2 != null && !p1.equals(p2)) {
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                    drawArrow(g2, p1.x, p1.y, p2.x, p2.y);
                }
            }
        }

        // ── 4. Legend ──
        drawLegend(g2);
    }

    private void drawStationDot(Graphics2D g2, Point pos, Color color, String label) {
        int r = 6;
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
        g2.fillOval(pos.x - r - 2, pos.y - r - 2, (r + 2) * 2, (r + 2) * 2);
        g2.setColor(color);
        g2.fillOval(pos.x - r, pos.y - r, r * 2, r * 2);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 8));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, pos.x - fm.stringWidth(label) / 2, pos.y + fm.getAscent() / 2 - 1);
    }

    private void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2) {
        int sz = 8;
        double dx = x2 - x1, dy = y2 - y1, len = Math.sqrt(dx*dx + dy*dy);
        if (len == 0) return;
        double angle = Math.atan2(dy, dx);
        g2.drawLine(x2, y2, (int)(x2 - sz * Math.cos(angle - Math.PI/6)), (int)(y2 - sz * Math.sin(angle - Math.PI/6)));
        g2.drawLine(x2, y2, (int)(x2 - sz * Math.cos(angle + Math.PI/6)), (int)(y2 - sz * Math.sin(angle + Math.PI/6)));
    }

    private void drawLegend(Graphics2D g2) {
        int lx = getWidth() - 150, ly = 16, lw = 135, lh = 60;
        g2.setColor(new Color(18, 18, 40, 200));
        g2.fillRoundRect(lx, ly, lw, lh, 8, 8);
        g2.setColor(new Color(70, 70, 110));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(lx, ly, lw, lh, 8, 8);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        int ty = ly + 16;
        drawLegendItem(g2, lx + 10, ty, BUS_DOT,   "Bus station");   ty += 16;
        drawLegendItem(g2, lx + 10, ty, TRAIN_DOT, "Train station"); ty += 16;
        drawLegendItem(g2, lx + 10, ty, PATH_COLOR, "Active route");
    }

    private void drawLegendItem(Graphics2D g2, int x, int y, Color color, String label) {
        g2.setColor(color);
        g2.fillOval(x, y - 6, 10, 10);
        g2.setColor(new Color(200, 200, 230));
        g2.drawString(label, x + 15, y + 3);
    }

    @Override
    public Dimension getPreferredSize() {
        if (countryMap == null || countryMap.length == 0) return new Dimension(500, 500);
        return new Dimension(countryMap[0].length * cellSize + 2 * padding,
                             countryMap.length    * cellSize + 2 * padding);
    }

    @Override
    public void doLayout() { super.doLayout(); calculateCellSize(); }
}
