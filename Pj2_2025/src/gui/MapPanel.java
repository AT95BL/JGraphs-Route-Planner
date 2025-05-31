// src/gui/MapPanel.java
package gui;

import graph.Edge;
import graph.Graph;
import graph.Node;
import model.Station;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel za crtanje mape gradova i transportnih ruta.
 */
public class MapPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private String[][] countryMap;
    private List<Station> stations;
    private Graph transportGraph; // Referenca na graf za pristup čvorovima
    private Graph.Path highlightedPath; // Ruta koju treba istaći

    // Dimenzije za crtanje
    private int cellSize = 80; // Veličina ćelije za svaki grad
    private int padding = 20;  // Razmak od ivica panela

    // Mapa za pozicije čvorova na panelu (za crtanje grana)
    private Map<String, Point> nodePositions;

    public MapPanel() {
        this.nodePositions = new HashMap<>();
    }

    public void setCountryMap(String[][] countryMap) {
        this.countryMap = countryMap;
        calculateCellSize(); // Prilagodi veličinu ćelije
        repaint();
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
        repaint();
    }

    public void setTransportGraph(Graph transportGraph) {
        this.transportGraph = transportGraph;
        repaint();
    }

    public void setHighlightedPath(Graph.Path path) {
        this.highlightedPath = path;
        repaint();
    }

    public void clearHighlightedPath() {
        this.highlightedPath = null;
        repaint();
    }

    private void calculateCellSize() {
        if (countryMap == null || countryMap.length == 0 || countryMap[0].length == 0) {
            return;
        }
        int numRows = countryMap.length;
        int numCols = countryMap[0].length;

        int availableWidth = getWidth() - (2 * padding);
        int availableHeight = getHeight() - (2 * padding);

        if (numCols > 0 && numRows > 0) {
            cellSize = Math.min(availableWidth / numCols, availableHeight / numRows);
            cellSize = Math.max(cellSize, 50); // Minimalna veličina ćelije
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        nodePositions.clear(); // Očisti pozicije svaki put kad crtamo

        if (countryMap == null || countryMap.length == 0 || countryMap[0].length == 0) {
            g2d.drawString("Molimo generišite/učitajte podatke o mapi.", padding, padding);
            return;
        }

        int numRows = countryMap.length;
        int numCols = countryMap[0].length;

        // Prilagodi veličinu ćelije pri svakom crtanju za responsivnost
        calculateCellSize();

        // Crtanje gradova (grid)
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                int x = padding + c * cellSize;
                int y = padding + r * cellSize;

                // Crtaj kvadrat za grad
                g2d.setColor(new Color(220, 220, 255)); // Svetlo plava za grad
                g2d.fillRect(x, y, cellSize, cellSize);
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawRect(x, y, cellSize, cellSize);

                // Ispiši ime grada
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                String cityName = countryMap[r][c];
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(cityName);
                int textHeight = fm.getHeight();
                g2d.drawString(cityName, x + (cellSize - textWidth) / 2, y + (cellSize / 2) - (textHeight / 2) + fm.getAscent());

                // Crtanje stanica unutar grada
                if (stations != null) {
                    for (Station s : stations) {
                        if (s.getCity().equals(cityName)) {
                            // Pozicija autobuske stanice (gore levo u ćeliji)
                            Point busPos = new Point(x + cellSize / 4, y + cellSize / 4);
                            g2d.setColor(Color.RED);
                            g2d.fillOval(busPos.x - 5, busPos.y - 5, 10, 10); // Mali crveni krug za autobus
                            nodePositions.put(s.getBusStation(), busPos);

                            // Pozicija željezničke stanice (dole desno u ćeliji)
                            Point trainPos = new Point(x + (3 * cellSize) / 4, y + (3 * cellSize) / 4);
                            g2d.setColor(Color.BLUE);
                            g2d.fillOval(trainPos.x - 5, trainPos.y - 5, 10, 10); // Mali plavi krug za voz
                            nodePositions.put(s.getTrainStation(), trainPos);
                        }
                    }
                }
            }
        }

        // Crtanje svih grana (putovanja) u grafu (tanko, sivo)
        if (transportGraph != null) {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(new BasicStroke(1));
            for (Node node : transportGraph.getAllNodes()) {
                for (Edge edge : transportGraph.getEdgesFrom(node)) {
                    Point p1 = nodePositions.get(edge.getSource().getId());
                    Point p2 = nodePositions.get(edge.getDestination().getId());
                    if (p1 != null && p2 != null) {
                        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                    }
                }
            }
        }

        // Crtanje istaknute rute (debelo, zeleno)
        if (highlightedPath != null && !highlightedPath.getEdges().isEmpty()) {
            g2d.setColor(Color.GREEN.darker());
            g2d.setStroke(new BasicStroke(3)); // Deblja linija za istaknutu rutu
            for (Edge edge : highlightedPath.getEdges()) {
                Point p1 = nodePositions.get(edge.getSource().getId());
                Point p2 = nodePositions.get(edge.getDestination().getId());
                if (p1 != null && p2 != null) {
                    g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                    // Opcionalno: nacrtaj strelicu
                    drawArrow(g2d, p1.x, p1.y, p2.x, p2.y);
                }
            }
        }
    }

    /**
     * Pomoćna metoda za crtanje strelice na kraju linije.
     */
    private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        int ARR_SIZE = 8;
        double dx = x2 - x1;
        double dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx * dx + dy * dy);
        if (len == 0) return; // Avoid division by zero

        // Adjust arrow position to be closer to the destination node
        double arrowX = x2 - (dx / len) * (ARR_SIZE / 2);
        double arrowY = y2 - (dy / len) * (ARR_SIZE / 2);

        g2d.drawLine(x2, y2, (int) (arrowX + ARR_SIZE * Math.cos(angle - Math.PI / 6)), (int) (arrowY + ARR_SIZE * Math.sin(angle - Math.PI / 6)));
        g2d.drawLine(x2, y2, (int) (arrowX + ARR_SIZE * Math.cos(angle + Math.PI / 6)), (int) (arrowY + ARR_SIZE * Math.sin(angle + Math.PI / 6)));
    }


    @Override
    public Dimension getPreferredSize() {
        if (countryMap == null || countryMap.length == 0 || countryMap[0].length == 0) {
            return new Dimension(400, 400); // Default veličina
        }
        int numRows = countryMap.length;
        int numCols = countryMap[0].length;
        return new Dimension(numCols * cellSize + 2 * padding, numRows * cellSize + 2 * padding);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        // Ponovno izračunaj veličinu ćelije kada se panel promijeni
        calculateCellSize();
    }
}