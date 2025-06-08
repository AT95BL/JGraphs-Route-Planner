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
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;


/**
 * Glavni prozor aplikacije za pronalazenje transportnih ruta.
 * Omogucava unos dimenzija mape, odabir polazne i odredišne stanice,
 * izbor kriterijuma optimizacije i prikaz rezultata.
 */
public class MainFrame extends JFrame {

    public static final long serialVersionUID = 1L;
    public JPanel contentPane;

    // GUI komponente za unos
    public JTextField txtNRows;
    public JTextField txtMCols;
    public JComboBox<String> cmbStartStation;
    public JComboBox<String> cmbEndStation;
    public ButtonGroup criteriaGroup; // Grupa za radio dugmad
    public JRadioButton rbTime;
    public JRadioButton rbPrice;
    public JRadioButton rbTransfers;

    // GUI komponente za prikaz rezultata
    public JTable routeTable;
    public DefaultTableModel routeTableModel;
    public JLabel lblTotalTickets;
    public JLabel lblTotalRevenue;
    public MapPanel mapPanel; // Panel za prikaz mape

    // Podaci i logika
    public TransportData transportData;
    public Graph transportGraph;
    public Graph.Path currentOptimalPath; // cuva trenutno pronadjenu optimalnu rutu

    /**
     * Konstruktor glavnog prozora.
     */
    public MainFrame() {
        setTitle("Optimalne Transportne Rute");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800); // Povecana velicina prozora
        setLocationRelativeTo(null); // Centriraj prozor na ekranu

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.setLayout(new BorderLayout(10, 10)); // Dodatni razmak
        setContentPane(contentPane);

        // --- Gornji panel za unos podataka ---
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        contentPane.add(inputPanel, BorderLayout.NORTH);

        inputPanel.add(new JLabel("Broj redova (n):"));
        txtNRows = new JTextField("5", 5);
        inputPanel.add(txtNRows);

        inputPanel.add(new JLabel("Broj kolona (m):"));
        txtMCols = new JTextField("5", 5);
        inputPanel.add(txtMCols);

        JButton btnGenerateLoad = new JButton("Generiši/Ucitaj Podatke");
        btnGenerateLoad.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadTransportData();
            }
        });
        inputPanel.add(btnGenerateLoad);

        // --- Centralni panel za mapu i kontrole ---
        JSplitPane centerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        contentPane.add(centerSplitPane, BorderLayout.CENTER);

        // Lijeva strana: Mapa
        mapPanel = new MapPanel();
        mapPanel.setBackground(new Color(240, 240, 240)); // Svjetlo siva pozadina
        centerSplitPane.setLeftComponent(mapPanel);

        // Desna strana: Kontrole i rezultati
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout(5, 5));
        centerSplitPane.setRightComponent(controlPanel);

        // Gornji dio desnog panela: Odabir stanica i kriterijuma
        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new GridLayout(4, 2, 5, 5)); // 4 reda, 2 kolone
        controlPanel.add(selectionPanel, BorderLayout.NORTH);

        selectionPanel.add(new JLabel("Polazna stanica:"));
        cmbStartStation = new JComboBox<>();
        selectionPanel.add(cmbStartStation);

        selectionPanel.add(new JLabel("Odredišna stanica:"));
        cmbEndStation = new JComboBox<>();
        selectionPanel.add(cmbEndStation);

        selectionPanel.add(new JLabel("Kriterijum optimizacije:"));
        JPanel criteriaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        criteriaGroup = new ButtonGroup();
        rbTime = new JRadioButton("Vreme");
        rbPrice = new JRadioButton("Cena");
        rbTransfers = new JRadioButton("Presjedanja");

        rbTime.setSelected(true); // Podrazumevano odabrano
        criteriaGroup.add(rbTime);
        criteriaGroup.add(rbPrice);
        criteriaGroup.add(rbTransfers);

        criteriaPanel.add(rbTime);
        criteriaPanel.add(rbPrice);
        criteriaPanel.add(rbTransfers);
        selectionPanel.add(criteriaPanel);

        JButton btnFindRoute = new JButton("Pronadji Rutu");
        btnFindRoute.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                findAndDisplayRoute();
            }
        });
        selectionPanel.add(btnFindRoute);

        // Srednji dio desnog panela: Tabela rute
        routeTableModel = new DefaultTableModel(new Object[]{"Polazak", "Dolazak", "Tip", "Vreme Polaska", "Trajanje (min)", "Cena (€)"}, 0);
        routeTable = new JTable(routeTableModel);
        JScrollPane scrollPane = new JScrollPane(routeTable);
        controlPanel.add(scrollPane, BorderLayout.CENTER);

        // Donji dio desnog panela: Dugmad za akcije i statistika
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        controlPanel.add(bottomPanel, BorderLayout.SOUTH);

        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnBuyTicket = new JButton("Kupi Kartu");
        btnBuyTicket.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buyTicket();
            }
        });
        actionButtonsPanel.add(btnBuyTicket);

        JButton btnShowAdditionalRoutes = new JButton("Prikaz Dodatnih Ruta");
        btnShowAdditionalRoutes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showAdditionalRoutes();
            }
        });
        actionButtonsPanel.add(btnShowAdditionalRoutes);
        bottomPanel.add(actionButtonsPanel, BorderLayout.CENTER);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        lblTotalTickets = new JLabel("Ukupno prodatih karata: 0");
        lblTotalRevenue = new JLabel("Ukupan prihod: 0.00€");
        statsPanel.add(lblTotalTickets);
        statsPanel.add(lblTotalRevenue);
        bottomPanel.add(statsPanel, BorderLayout.SOUTH);

        // Inicijalno ucitavanje statistike
        updateReceiptStats();

        // Podrazumevano ucitaj podatke pri pokretanju (mozeš i da pozoveš btnGenerateLoad.doClick() ovde)
        // loadTransportData(); // Mozeš ovo zakomentarisati ako zeliš da korisnik prvo klikne dugme
    }

    /**
     * Ucitava podatke o transportu iz JSON fajla ili ih generiše.
     * Azurira combo box-ove za stanice i mapu.
     */
    public void loadTransportData() {
        try {
            int n = Integer.parseInt(txtNRows.getText());
            int m = Integer.parseInt(txtMCols.getText());

            if (n <= 0 || m <= 0) {
                JOptionPane.showMessageDialog(this, "Dimenzije mape moraju biti pozitivni brojevi.", "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Generisanje podataka
            TransportDataGenerator generator = new TransportDataGenerator(n, m);
            TransportData generatedData = generator.generateData();
            generator.saveToJson(generatedData, "transport_data.json");
            System.out.println("Podaci su generisani i sacuvani kao transport_data.json");

            // Parsiranje i mapiranje podataka
            String jsonFilePath = "transport_data.json";
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
            SimpleJsonParser parser = new SimpleJsonParser(jsonContent);
            Object parsedData = parser.parse();
            transportData = TransportDataMapper.mapToTransportData(parsedData);

            // Izgradnja grafa
            transportGraph = new Graph();
            transportGraph.buildGraph(transportData);
            System.out.println("Graf uspešno izgradjen sa " + transportGraph.getAllNodes().size() + " cvorova.");

            // Popunjavanje combo box-ova stanicama
            populateStationComboBoxes();

            // Azuriranje mape
            mapPanel.setCountryMap(transportData.getCountryMap());
            mapPanel.setStations(transportData.getStations());
            mapPanel.setTransportGraph(transportGraph); // Prosledjujemo graf za vizualizaciju ruta
            mapPanel.repaint(); // Ponovno iscrtavanje mape

            JOptionPane.showMessageDialog(this, "Podaci uspešno generisani i ucitani.", "Uspeh", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Molimo unesite validne brojeve za dimenzije mape.", "Greška unosa", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Greška pri citanju/pisanju fajla: " + ex.getMessage(), "Greška fajla", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Došlo je do neocekivane greške: " + ex.getMessage(), "Greška", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Popunjava JComboBox-ove sa dostupnim stanicama.
     */
    public void populateStationComboBoxes() {
        if (transportData == null || transportData.getStations().isEmpty()) {
            cmbStartStation.removeAllItems();
            cmbEndStation.removeAllItems();
            return;
        }

        // Koristimo Vector jer je JComboBox kompatibilan sa njim
        Vector<String> stationIds = new Vector<>();
        // Dodajemo sve autobuske i zeljeznicke stanice
        for (Station s : transportData.getStations()) {
            stationIds.add(s.getBusStation());
            stationIds.add(s.getTrainStation());
        }

        // Ukloni duplikate i sortiraj
        stationIds = stationIds.stream().distinct().sorted().collect(Collectors.toCollection(Vector::new));

        cmbStartStation.setModel(new DefaultComboBoxModel<>(stationIds));
        cmbEndStation.setModel(new DefaultComboBoxModel<>(stationIds));

        if (!stationIds.isEmpty()) {
            cmbStartStation.setSelectedIndex(0);
            cmbEndStation.setSelectedIndex(stationIds.size() > 1 ? 1 : 0); // Odaberi drugu ako postoji
        }
    }

    /**
     * Pronalazi i prikazuje optimalnu rutu na osnovu korisnickog unosa.
     */
    public void findAndDisplayRoute() {
        if (transportGraph == null || cmbStartStation.getSelectedItem() == null || cmbEndStation.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Molimo prvo generišite/ucitajte podatke i odaberite stanice.", "Greška", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String startStationId = (String) cmbStartStation.getSelectedItem();
        String endStationId = (String) cmbEndStation.getSelectedItem();

        if (startStationId.equals(endStationId)) {
            JOptionPane.showMessageDialog(this, "Polazna i odredišna stanica ne mogu biti iste.", "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        OptimizationCriteria criteria;
        if (rbTime.isSelected()) {
            criteria = OptimizationCriteria.TIME;
        } else if (rbPrice.isSelected()) {
            criteria = OptimizationCriteria.PRICE;
        } else {
            criteria = OptimizationCriteria.TRANSFERS;
        }

        currentOptimalPath = transportGraph.findOptimalRoute(startStationId, endStationId, criteria);

        // Ocisti prethodne rezultate
        routeTableModel.setRowCount(0);
        mapPanel.clearHighlightedPath();

        if (currentOptimalPath != null) {
            // Popuni tabelu sa detaljima rute
            for (Edge edge : currentOptimalPath.getEdges()) {
                String departureTime = "";
                String duration = "";
                String price = "";
                String type = edge.getType();

                if ("departure".equals(edge.getType()) && edge.getDepartureDetails() != null) {
                    departureTime = edge.getDepartureDetails().getDepartureTime();
                    duration = String.valueOf(edge.getDepartureDetails().getDuration());
                    price = String.valueOf(edge.getDepartureDetails().getPrice());
                } else if ("transfer".equals(edge.getType())) {
                    departureTime = "N/A"; // Transfer nema specificno vreme polaska
                    duration = String.valueOf((int)edge.getWeight()); // Tezina je vreme transfera
                    price = "0"; // Transferi su besplatni
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

            // Azuriraj mapu da prikaze rutu
            mapPanel.setHighlightedPath(currentOptimalPath);
            mapPanel.repaint();

            JOptionPane.showMessageDialog(this,
                    String.format("Pronadjena ruta po %s:\nUkupno trajanje: %d min\nUkupna cena: %d€\nBroj presjedanja: %d",
                            criteria.toString().toLowerCase(),
                            currentOptimalPath.getTotalDurationMinutes() > 0 ? currentOptimalPath.getTotalDurationMinutes() : (-1)*currentOptimalPath.getTotalDurationMinutes(),
                            currentOptimalPath.getTotalPrice(),
                            currentOptimalPath.getNumberOfTransfers()),
                    "Ruta Pronadjena",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Ruta nije pronadjena za odabrane kriterijume.", "Ruta Nije Pronadjena", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Logika za kupovinu karte. Generiše racun i azurira statistiku.
     */
    public void buyTicket() {
        if (currentOptimalPath == null) {
            JOptionPane.showMessageDialog(this, "Prvo pronadjite rutu koju zelite kupiti.", "Greška", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String receiptDetails = String.format(
                "Racun za kupovinu karte:\n" +
                "-----------------------------------\n" +
                "Relacija: %s do %s\n" +
                "Vreme kupovine: %s\n" +
                "Ukupno trajanje: %d min\n" +
                "Ukupna cena: %d€\n" +
                "Broj presjedanja: %d\n" +
                "-----------------------------------\n" +
                "Detalji rute:\n",
                currentOptimalPath.getNodes().get(0).getStationName() + " (" + currentOptimalPath.getNodes().get(0).getCity() + ")",
                currentOptimalPath.getNodes().get(currentOptimalPath.getNodes().size() - 1).getStationName() + " (" + currentOptimalPath.getNodes().get(currentOptimalPath.getNodes().size() - 1).getCity() + ")",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm")),
                currentOptimalPath.getTotalDurationMinutes(),
                currentOptimalPath.getTotalPrice(),
                currentOptimalPath.getNumberOfTransfers()
        );

        StringBuilder routeDetails = new StringBuilder();
        for (Edge edge : currentOptimalPath.getEdges()) {
            routeDetails.append("  ").append(edge.toString()).append("\n");
        }
        receiptDetails += routeDetails.toString();

        try {
            ReceiptManager.saveReceipt(receiptDetails);
            updateReceiptStats();
            JOptionPane.showMessageDialog(this, "Karta uspešno kupljena! Racun sacuvan.", "Kupovina Uspešna", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Greška pri cuvanju racuna: " + ex.getMessage(), "Greška", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Otvara novi prozor sa prikazom top N ruta korišcenjem findMultiplePaths.
     */
    public void showAdditionalRoutes() {
        if (transportGraph == null || cmbStartStation.getSelectedItem() == null || cmbEndStation.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Molimo prvo generišite/ucitajte podatke i odaberite stanice.", "Greška", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String startStationId = (String) cmbStartStation.getSelectedItem();
        String endStationId = (String) cmbEndStation.getSelectedItem();

        if (startStationId.equals(endStationId)) {
            JOptionPane.showMessageDialog(this, "Polazna i odredišna stanica ne mogu biti iste.", "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        OptimizationCriteria criteria;
        if (rbTime.isSelected()) {
            criteria = OptimizationCriteria.TIME;
        } else if (rbPrice.isSelected()) {
            criteria = OptimizationCriteria.PRICE;
        } else {
            criteria = OptimizationCriteria.TRANSFERS;
        }

        // Pozivamo findMultiplePaths za dobijanje više ruta
        List<Graph.Path> topRoutes = transportGraph.findMultiplePaths(startStationId, endStationId, criteria, 5); // Dohvati do 5 ruta

        if (topRoutes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nije pronadjena nijedna dodatna ruta za odabrane kriterijume.", "Nema Ruta", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Otvori prozor sa dodatnim rutama
        AdditionalRoutesWindow additionalRoutesWindow = new AdditionalRoutesWindow(this, topRoutes, criteria);
        additionalRoutesWindow.setVisible(true);
    }

    /**
     * Azurira prikaz ukupnog broja prodatih karata i prihoda.
     */
    public void updateReceiptStats() {
        try {
            int totalTickets = ReceiptManager.getTotalTicketsSold();
            double totalRevenue = ReceiptManager.getTotalRevenue();
            lblTotalTickets.setText("Ukupno prodatih karata: " + totalTickets);
            lblTotalRevenue.setText(String.format("Ukupan prihod: %.2f€", totalRevenue));
        } catch (IOException ex) {
            System.err.println("Greška pri ucitavanju statistike racuna: " + ex.getMessage());
            lblTotalTickets.setText("Ukupno prodatih karata: N/A");
            lblTotalRevenue.setText("Ukupan prihod: N/A");
        }
    }
}