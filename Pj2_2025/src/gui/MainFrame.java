// src/gui/MainWindow.java
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
 * Glavni prozor aplikacije za pronalaženje transportnih ruta.
 * Omogućava unos dimenzija mape, odabir polazne i odredišne stanice,
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
    public Graph.Path currentOptimalPath; // Čuva trenutno pronađenu optimalnu rutu

    /**
     * Konstruktor glavnog prozora.
     */
    public MainFrame() {
        setTitle("Optimalne Transportne Rute");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800); // Povećana veličina prozora
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

        JButton btnGenerateLoad = new JButton("Generiši/Učitaj Podatke");
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

        JButton btnFindRoute = new JButton("Pronađi Rutu");
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

        // Inicijalno učitavanje statistike
        updateReceiptStats();

        // Podrazumevano učitaj podatke pri pokretanju (možeš i da pozoveš btnGenerateLoad.doClick() ovde)
        // loadTransportData(); // Možeš ovo zakomentarisati ako želiš da korisnik prvo klikne dugme
    }

    /**
     * Učitava podatke o transportu iz JSON fajla ili ih generiše.
     * Ažurira combo box-ove za stanice i mapu.
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
            System.out.println("Podaci su generisani i sačuvani kao transport_data.json");

            // Parsiranje i mapiranje podataka
            String jsonFilePath = "transport_data.json";
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
            SimpleJsonParser parser = new SimpleJsonParser(jsonContent);
            Object parsedData = parser.parse();
            transportData = TransportDataMapper.mapToTransportData(parsedData);

            // Izgradnja grafa
            transportGraph = new Graph();
            transportGraph.buildGraph(transportData);
            System.out.println("Graf uspešno izgrađen sa " + transportGraph.getAllNodes().size() + " čvorova.");

            // Popunjavanje combo box-ova stanicama
            populateStationComboBoxes();

            // Ažuriranje mape
            mapPanel.setCountryMap(transportData.getCountryMap());
            mapPanel.setStations(transportData.getStations());
            mapPanel.setTransportGraph(transportGraph); // Prosleđujemo graf za vizualizaciju ruta
            mapPanel.repaint(); // Ponovno iscrtavanje mape

            JOptionPane.showMessageDialog(this, "Podaci uspešno generisani i učitani.", "Uspeh", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Molimo unesite validne brojeve za dimenzije mape.", "Greška unosa", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Greška pri čitanju/pisanju fajla: " + ex.getMessage(), "Greška fajla", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Došlo je do neočekivane greške: " + ex.getMessage(), "Greška", JOptionPane.ERROR_MESSAGE);
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
        // Dodajemo sve autobuske i željezničke stanice
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
     * Pronalazi i prikazuje optimalnu rutu na osnovu korisničkog unosa.
     */
    public void findAndDisplayRoute() {
        if (transportGraph == null || cmbStartStation.getSelectedItem() == null || cmbEndStation.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Molimo prvo generišite/učitajte podatke i odaberite stanice.", "Greška", JOptionPane.WARNING_MESSAGE);
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

        // Očisti prethodne rezultate
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
                    departureTime = "N/A"; // Transfer nema specifično vreme polaska
                    duration = String.valueOf((int)edge.getWeight()); // Težina je vreme transfera
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

            // Ažuriraj mapu da prikaže rutu
            mapPanel.setHighlightedPath(currentOptimalPath);
            mapPanel.repaint();

            JOptionPane.showMessageDialog(this,
                    String.format("Pronađena ruta po %s:\nUkupno trajanje: %d min\nUkupna cena: %d€\nBroj presjedanja: %d",
                            criteria.toString().toLowerCase(),
                            currentOptimalPath.getTotalDurationMinutes(),
                            currentOptimalPath.getTotalPrice(),
                            currentOptimalPath.getNumberOfTransfers()),
                    "Ruta Pronađena",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Ruta nije pronađena za odabrane kriterijume.", "Ruta Nije Pronađena", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Logika za kupovinu karte. Generiše račun i ažurira statistiku.
     */
    public void buyTicket() {
        if (currentOptimalPath == null) {
            JOptionPane.showMessageDialog(this, "Prvo pronađite rutu koju želite kupiti.", "Greška", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String receiptDetails = String.format(
                "Račun za kupovinu karte:\n" +
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
            JOptionPane.showMessageDialog(this, "Karta uspešno kupljena! Račun sačuvan.", "Kupovina Uspešna", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Greška pri čuvanju računa: " + ex.getMessage(), "Greška", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Otvara novi prozor sa prikazom top 5 ruta.
     */
    public void showAdditionalRoutes() {
        if (transportGraph == null || cmbStartStation.getSelectedItem() == null || cmbEndStation.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Molimo prvo generišite/učitajte podatke i odaberite stanice.", "Greška", JOptionPane.WARNING_MESSAGE);
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

        // Ovde bi trebalo implementirati logiku za pronalaženje top 5 ruta.
        // Dijkstra pronalazi samo jednu optimalnu rutu. Za top 5, možda ćeš morati
        // da pokreneš Dijkstru više puta sa različitim početnim uslovima ili
        // da koristiš algoritam za K-najkraće puteve (što je kompleksnije).
        // Za sada, simuliraćemo top 5 tako što ćemo prikazati samo jednu rutu.
        // U stvarnosti, ovo bi zahtevalo napredniju logiku.

        // Privremeno: samo ponovo prikaži trenutnu optimalnu rutu kao "jednu od top ruta"
        // U realnom projektu, ovde bi se pozivao algoritam za K-najkraćih puteva.
        List<Graph.Path> top5Routes = new java.util.ArrayList<>();
        Graph.Path path1 = transportGraph.findOptimalRoute(startStationId, endStationId, criteria);
        if (path1 != null) {
            top5Routes.add(path1);
            // Simulacija dodatnih ruta (za pravi top 5, ovo bi bila stvarna logika)
            // Za demo, možemo dodati istu rutu više puta ili generisati neke varijacije
            // top5Routes.add(path1);
            // top5Routes.add(path1);
        }


        AdditionalRoutesWindow additionalRoutesWindow = new AdditionalRoutesWindow(this, top5Routes, criteria);
        additionalRoutesWindow.setVisible(true);
    }

    /**
     * Ažurira prikaz ukupnog broja prodatih karata i prihoda.
     */
    public void updateReceiptStats() {
        try {
            int totalTickets = ReceiptManager.getTotalTicketsSold();
            double totalRevenue = ReceiptManager.getTotalRevenue();
            lblTotalTickets.setText("Ukupno prodatih karata: " + totalTickets);
            lblTotalRevenue.setText(String.format("Ukupan prihod: %.2f€", totalRevenue));
        } catch (IOException ex) {
            System.err.println("Greška pri učitavanju statistike računa: " + ex.getMessage());
            lblTotalTickets.setText("Ukupno prodatih karata: N/A");
            lblTotalRevenue.setText("Ukupan prihod: N/A");
        }
    }
}