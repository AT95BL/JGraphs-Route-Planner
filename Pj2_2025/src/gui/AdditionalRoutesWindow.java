// src/gui/AdditionalRoutesWindow.java
package gui;

import graph.Edge;
import graph.Graph;
import manager.ReceiptManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Prozor za prikaz dodatnih (top 5) ruta.
 */
public class AdditionalRoutesWindow extends JDialog {

    private static final long serialVersionUID = 1L;
    private final MainFrame parentWindow;
    private List<Graph.Path> topRoutes;
    private Graph.OptimizationCriteria criteria;

    /**
     * Konstruktor za prozor dodatnih ruta.
     * @param parent Glavni prozor aplikacije.
     * @param topRoutes Lista ruta za prikaz.
     * @param criteria Kriterijum optimizacije koji je korišćen.
     */
    public AdditionalRoutesWindow(MainFrame parent, List<Graph.Path> topRoutes, Graph.OptimizationCriteria criteria) {
        super(parent, "Dodatne Rute", true); // Modalni dijalog
        this.parentWindow = parent;
        this.topRoutes = topRoutes;
        this.criteria = criteria;

        setSize(800, 600);
        setLocationRelativeTo(parent); // Centriraj u odnosu na roditeljski prozor

        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPanel.setLayout(new BorderLayout(10, 10));
        setContentPane(contentPanel);

        JLabel lblTitle = new JLabel("Top Rute po " + criteria.toString().toLowerCase() + ":", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        contentPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel routesPanel = new JPanel();
        routesPanel.setLayout(new BoxLayout(routesPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(routesPanel);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        if (topRoutes.isEmpty()) {
            routesPanel.add(new JLabel("Nema dodatnih ruta za prikaz."));
        } else {
            int routeNumber = 1;
            for (Graph.Path path : topRoutes) {
                JPanel routeEntryPanel = createRouteEntryPanel(path, routeNumber++);
                routesPanel.add(routeEntryPanel);
                routesPanel.add(Box.createVerticalStrut(10)); // Razmak izmedju ruta
            }
        }

        JButton btnClose = new JButton("Zatvori");
        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // Zatvori dijalog
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnClose);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Kreira panel za prikaz pojedinacne rute.
     * @param path Ruta koju treba prikazati.
     * @param routeNumber Redni broj rute.
     * @return JPanel sa detaljima rute i dugmetom "Kupi kartu".
     */
    private JPanel createRouteEntryPanel(Graph.Path path, int routeNumber) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Ruta " + routeNumber));
        panel.setLayout(new BorderLayout(5, 5));

        // Panel za osnovne informacije o ruti
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.add(new JLabel("Trajanje: " + (path.getTotalDurationMinutes() > 0 ? path.getTotalDurationMinutes(): (-1)*path.getTotalDurationMinutes()) + " min"));
        infoPanel.add(new JLabel("Cena: " + path.getTotalPrice() + "€"));
        infoPanel.add(new JLabel("Presjedanja: " + path.getNumberOfTransfers()));
        panel.add(infoPanel, BorderLayout.WEST);

        // Tabela za detalje rute
        DefaultTableModel routeTableModel = new DefaultTableModel(new Object[]{"Polazak", "Dolazak", "Tip", "Vreme Polaska", "Trajanje (min)", "Cena (€)"}, 0);
        JTable routeTable = new JTable(routeTableModel);
        for (Edge edge : path.getEdges()) {
            String departureTime = "";
            String duration = "";
            String price = "";
            String type = edge.getType();

            if ("departure".equals(edge.getType()) && edge.getDepartureDetails() != null) {
                departureTime = edge.getDepartureDetails().getDepartureTime();
                duration = String.valueOf(edge.getDepartureDetails().getDuration());
                price = String.valueOf(edge.getDepartureDetails().getPrice());
            } else if ("transfer".equals(edge.getType())) {
                departureTime = "N/A";
                duration = String.valueOf((int)edge.getWeight());
                price = "0";
            }
            routeTableModel.addRow(new Object[]{
                edge.getSource().getStationName() + " (" + edge.getSource().getCity() + ")",
                edge.getDestination().getStationName() + " (" + edge.getDestination().getCity() + ")",
                type,
                departureTime,
                duration,
                price
            });
        }
        JScrollPane scrollPane = new JScrollPane(routeTable);
        scrollPane.setPreferredSize(new Dimension(500, 100)); // Fiksna velicina za scroll pane
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton btnBuyTicket = new JButton("Kupi Kartu");
        btnBuyTicket.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buyTicket(path); // Pozovi metodu za kupovinu karte za ovu specificnu rutu
            }
        });
        JPanel buyButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buyButtonPanel.add(btnBuyTicket);
        panel.add(buyButtonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Logika za kupovinu karte iz prozora dodatnih ruta.
     */
    private void buyTicket(Graph.Path path) {
        String receiptDetails = String.format(
                "Racun za kupovinu karte (Dodatna ruta):\n" +
                "-----------------------------------\n" +
                "Relacija: %s do %s\n" +
                "Vreme kupovine: %s\n" +
                "Ukupno trajanje: %d min\n" +
                "Ukupna cena: %d€\n" +
                "Broj presjedanja: %d\n" +
                "-----------------------------------\n" +
                "Detalji rute:\n",
                path.getNodes().get(0).getStationName() + " (" + path.getNodes().get(0).getCity() + ")",
                path.getNodes().get(path.getNodes().size() - 1).getStationName() + " (" + path.getNodes().get(path.getNodes().size() - 1).getCity() + ")",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm")),
                path.getTotalDurationMinutes(),
                path.getTotalPrice(),
                path.getNumberOfTransfers()
        );

        StringBuilder routeDetails = new StringBuilder();
        for (Edge edge : path.getEdges()) {
            routeDetails.append("  ").append(edge.toString()).append("\n");
        }
        receiptDetails += routeDetails.toString();

        try {
            ReceiptManager.saveReceipt(receiptDetails);
            parentWindow.updateReceiptStats(); // Azuriraj statistiku u glavnom prozoru
            JOptionPane.showMessageDialog(this, "Karta uspešno kupljena! Racun sacuvan.", "Kupovina Uspešna", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Greška pri cuvanju racuna: " + ex.getMessage(), "Greška", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}