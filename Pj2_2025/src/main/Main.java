package main;

// TESTIRANJE(Main klasa prije GUI-ja):
/*
import model.*;
import util.*;
import graph.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
	public static void main(String[] args) {
		int n, m;
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("n: ");
		n = scanner.nextInt();
		System.out.println("m: ");
		m = scanner.nextInt();
		
		TransportDataGenerator generator = new TransportDataGenerator(n, m); 					// Poziv konstruktora
        TransportData dataT = generator.generateData();
        generator.saveToJson(dataT, "transport_data.json");
        System.out.println("Podaci su generisani i sacuvani kao transport_data.json");
        
        String jsonFilePath = "transport_data.json"; 											// Vas generisani fajl

        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
            SimpleJsonParser parser = new SimpleJsonParser(jsonContent);
            Object parsedData = parser.parse();

            System.out.println("Parsed data (raw Java objects):");
            System.out.println(parsedData);

            // Ovdje sada pozivate vas mapper!
            model.TransportData data = TransportDataMapper.mapToTransportData(parsedData);

            // Sada mozete raditi sa 'data' objektom koji ima tipsku sigurnost
            System.out.println("Ucitano " + data.getStations().size() + " stanica i " + data.getDepartures().size() + " polazaka.");
            data.printTransportData(); 															// Pozovite metodu da ispisete mapirane podatke
            
            // --- IZGRADNJA GRAFA I TESTIRANJE ALGORITMA ---
            Graph transportGraph = new Graph();
            transportGraph.buildGraph(data);
            System.out.println("Graf uspjesno izgradjen sa " + transportGraph.getAllNodes().size() + " cvorova.");

            // Primjer pretrage rute
            // Vasi gradovi i stanice su npr. G_0_0, G_0_1, G_1_0...
            // Stanice su A_0_0, Z_0_0 itd.
            String startStationId = "A_0_0"; 													// Autobuska stanica u gradu G_0_0
            String endStationId = "Z_1_0";   													// zeljeznicka stanica u gradu G_1_0

            System.out.println("\nPokusavam pronaci najkracu rutu od " + startStationId + " do " + endStationId + " po VREMENU:");
            Graph.Path shortestTimePath = transportGraph.findOptimalRoute(startStationId, endStationId, Graph.OptimizationCriteria.TIME);
            if (shortestTimePath != null) {
                System.out.println("Pronadjena ruta po vremenu: " + shortestTimePath.getTotalDurationMinutes() + " minuta, cijena: " + shortestTimePath.getTotalPrice() + ", presjedanja: " + shortestTimePath.getNumberOfTransfers());
                System.out.println("Detalji rute:");
                for (Edge edge : shortestTimePath.getEdges()) {
                    System.out.println("  " + edge);
                }
            } else {
                System.out.println("Ruta nije pronadjena po vremenu.");
            }

            System.out.println("\nPokusavam pronaci najkracu rutu od " + startStationId + " do " + endStationId + " po CIJENI:");
            Graph.Path cheapestPricePath = transportGraph.findOptimalRoute(startStationId, endStationId, Graph.OptimizationCriteria.PRICE);
            if (cheapestPricePath != null) {
                System.out.println("Pronadjena ruta po cijeni: " + cheapestPricePath.getTotalPrice() + "€, trajanje: " + cheapestPricePath.getTotalDurationMinutes() + " minuta, presjedanja: " + cheapestPricePath.getNumberOfTransfers());
                System.out.println("Detalji rute:");
                for (Edge edge : cheapestPricePath.getEdges()) {
                    System.out.println("  " + edge);
                }
            } else {
                System.out.println("Ruta nije pronadjena po cijeni.");
            }

            System.out.println("\nPokusavam pronaci najkracu rutu od " + startStationId + " do " + endStationId + " po BROJU PRESJEDANJA:");
            Graph.Path fewestTransfersPath = transportGraph.findOptimalRoute(startStationId, endStationId, Graph.OptimizationCriteria.TRANSFERS);
            if (fewestTransfersPath != null) {
                System.out.println("Pronadjena ruta po presjedanjima: " + fewestTransfersPath.getNumberOfTransfers() + ", trajanje: " + fewestTransfersPath.getTotalDurationMinutes() + " minuta, cijena: " + fewestTransfersPath.getTotalPrice() + "€");
                System.out.println("Detalji rute:");
                for (Edge edge : fewestTransfersPath.getEdges()) {
                    System.out.println("  " + edge);
                }
            } else {
                System.out.println("Ruta nije pronadjena po presjedanjima.");
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }   
	}
}
*/


import gui.MainFrame;

import javax.swing.SwingUtilities;

/**
* Glavna klasa za pokretanje GUI aplikacije.
*/
public class Main {
 public static void main(String[] args) {
     // Pokreni Swing GUI u Event Dispatch Thread-u (EDT)
     // Ovo osigurava da sve GUI operacije budu bezbedno izvrsene na glavnom thread-u Swing-a.
     SwingUtilities.invokeLater(new Runnable() {
         public void run() {
             try {
                 // Kreiraj instancu glavnog prozora aplikacije
                 MainFrame frame = new MainFrame();
                 // Postavi prozor vidljivim
                 frame.setVisible(true);
             } catch (Exception e) {
                 // stampaj stack trace ako dodje do bilo kakve greske pri pokretanju GUI-ja
                 e.printStackTrace();
             }
         }
     });
 }
}
