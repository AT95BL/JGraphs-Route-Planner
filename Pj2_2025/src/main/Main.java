package main;

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
        
        String jsonFilePath = "transport_data.json"; 											// Vaš generisani fajl

        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
            SimpleJsonParser parser = new SimpleJsonParser(jsonContent);
            Object parsedData = parser.parse();

            System.out.println("Parsed data (raw Java objects):");
            System.out.println(parsedData);

            // Ovdje sada pozivate vaš mapper!
            model.TransportData data = TransportDataMapper.mapToTransportData(parsedData);

            // Sada možete raditi sa 'data' objektom koji ima tipsku sigurnost
            System.out.println("Učitano " + data.getStations().size() + " stanica i " + data.getDepartures().size() + " polazaka.");
            data.printTransportData(); 															// Pozovite metodu da ispišete mapirane podatke
            
            // --- IZGRADNJA GRAFA I TESTIRANJE ALGORITMA ---
            Graph transportGraph = new Graph();
            transportGraph.buildGraph(data);
            System.out.println("Graf uspješno izgrađen sa " + transportGraph.getAllNodes().size() + " čvorova.");

            // Primjer pretrage rute
            // Vaši gradovi i stanice su npr. G_0_0, G_0_1, G_1_0...
            // Stanice su A_0_0, Z_0_0 itd.
            String startStationId = "A_0_0"; 													// Autobuska stanica u gradu G_0_0
            String endStationId = "Z_1_0";   													// Željeznička stanica u gradu G_1_0

            System.out.println("\nPokušavam pronaći najkraću rutu od " + startStationId + " do " + endStationId + " po VREMENU:");
            Graph.Path shortestTimePath = transportGraph.findOptimalRoute(startStationId, endStationId, Graph.OptimizationCriteria.TIME);
            if (shortestTimePath != null) {
                System.out.println("Pronađena ruta po vremenu: " + shortestTimePath.getTotalDurationMinutes() + " minuta, cijena: " + shortestTimePath.getTotalPrice() + ", presjedanja: " + shortestTimePath.getNumberOfTransfers());
                System.out.println("Detalji rute:");
                for (Edge edge : shortestTimePath.getEdges()) {
                    System.out.println("  " + edge);
                }
            } else {
                System.out.println("Ruta nije pronađena po vremenu.");
            }

            System.out.println("\nPokušavam pronaći najkraću rutu od " + startStationId + " do " + endStationId + " po CIJENI:");
            Graph.Path cheapestPricePath = transportGraph.findOptimalRoute(startStationId, endStationId, Graph.OptimizationCriteria.PRICE);
            if (cheapestPricePath != null) {
                System.out.println("Pronađena ruta po cijeni: " + cheapestPricePath.getTotalPrice() + "€, trajanje: " + cheapestPricePath.getTotalDurationMinutes() + " minuta, presjedanja: " + cheapestPricePath.getNumberOfTransfers());
                System.out.println("Detalji rute:");
                for (Edge edge : cheapestPricePath.getEdges()) {
                    System.out.println("  " + edge);
                }
            } else {
                System.out.println("Ruta nije pronađena po cijeni.");
            }

            System.out.println("\nPokušavam pronaći najkraću rutu od " + startStationId + " do " + endStationId + " po BROJU PRESJEDANJA:");
            Graph.Path fewestTransfersPath = transportGraph.findOptimalRoute(startStationId, endStationId, Graph.OptimizationCriteria.TRANSFERS);
            if (fewestTransfersPath != null) {
                System.out.println("Pronađena ruta po presjedanjima: " + fewestTransfersPath.getNumberOfTransfers() + ", trajanje: " + fewestTransfersPath.getTotalDurationMinutes() + " minuta, cijena: " + fewestTransfersPath.getTotalPrice() + "€");
                System.out.println("Detalji rute:");
                for (Edge edge : fewestTransfersPath.getEdges()) {
                    System.out.println("  " + edge);
                }
            } else {
                System.out.println("Ruta nije pronađena po presjedanjima.");
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }   
	}
}
*/


import gui.MainFrame; // Uverite se da putanja paketa odgovara lokaciji MainWindow klase

import javax.swing.SwingUtilities;

/**
* Glavna klasa za pokretanje GUI aplikacije.
*/
public class Main {
 public static void main(String[] args) {
     // Pokreni Swing GUI u Event Dispatch Thread-u (EDT)
     // Ovo osigurava da sve GUI operacije budu bezbedno izvršene na glavnom thread-u Swing-a.
     SwingUtilities.invokeLater(new Runnable() {
         public void run() {
             try {
                 // Kreiraj instancu glavnog prozora aplikacije
                 MainFrame frame = new MainFrame();
                 // Postavi prozor vidljivim
                 frame.setVisible(true);
             } catch (Exception e) {
                 // Štampaj stack trace ako dođe do bilo kakve greške pri pokretanju GUI-ja
                 e.printStackTrace();
             }
         }
     });
 }
}
