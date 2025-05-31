package util;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.TransportData;
import graph.*;

// Ovu klasu biste morali ručno da parsirate iz JSON-a
// Bez Gson-a, to bi bilo dosta posla
// public static class TransportData { ... }
// public static class Station { ... }
// public static class Departure { ... }


public class SimpleJsonParser {

    public String jsonString;
    public int index;

    public SimpleJsonParser(String jsonString) {
        this.jsonString = jsonString;
        this.index = 0;
    }

    public void skipWhitespace() {
        while (index < jsonString.length() && Character.isWhitespace(jsonString.charAt(index))) {
            index++;
        }
    }

    public char peek() {
        skipWhitespace();
        return jsonString.charAt(index);
    }

    public char consume() {
        skipWhitespace();
        return jsonString.charAt(index++);
    }

    public Object parse() {
        char c = peek();
        if (c == '{') {
            return parseObject();
        } else if (c == '[') {
            return parseArray();
        } else if (c == '"') {
            return parseString();
        } else if (Character.isDigit(c) || c == '-') {
            return parseNumber();
        } else if (jsonString.startsWith("true", index)) {
            consume(); consume(); consume(); consume(); // "true"
            return true;
        } else if (jsonString.startsWith("false", index)) {
            consume(); consume(); consume(); consume(); consume(); // "false"
            return false;
        } else if (jsonString.startsWith("null", index)) {
            consume(); consume(); consume(); consume(); // "null"
            return null;
        } else {
            throw new RuntimeException("Unexpected character: " + c);
        }
    }

    public Map<String, Object> parseObject() {
        Map<String, Object> object = new HashMap<>();
        consume(); // '{'
        skipWhitespace();

        while (peek() != '}') {
            String key = parseString();
            consume(); // ':'
            Object value = parse();
            object.put(key, value);

            skipWhitespace();
            if (peek() == ',') {
                consume(); // ','
            }
        }
        consume(); // '}'
        return object;
    }

    public List<Object> parseArray() {
        List<Object> array = new ArrayList<>();
        consume(); // '['
        skipWhitespace();

        while (peek() != ']') {
            array.add(parse());
            skipWhitespace();
            if (peek() == ',') {
                consume(); // ','
            }
        }
        consume(); // ']'
        return array;
    }

    public String parseString() {
        consume(); // '"'
        StringBuilder sb = new StringBuilder();
        while (peek() != '"') {
            sb.append(consume());
        }
        consume(); // '"'
        return sb.toString();
    }

    public Number parseNumber() {
        StringBuilder sb = new StringBuilder();
        while (index < jsonString.length() && (Character.isDigit(jsonString.charAt(index)) || jsonString.charAt(index) == '.' || jsonString.charAt(index) == '-')) {
            sb.append(jsonString.charAt(index++));
        }
        try {
            if (sb.toString().contains(".")) {
                return Double.parseDouble(sb.toString());
            } else {
                return Long.parseLong(sb.toString());
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number format: " + sb.toString());
        }
    }

    // Main metoda za testiranje (samo za demonstraciju parsiranja generičkih JSON struktura)
    /*
    public static void main(String[] args) {
        String jsonFilePath = "transport_data.json"; // Vaš generisani fajl

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
            data.printTransportData(); // Pozovite metodu da ispišete mapirane podatke
            
            // --- IZGRADNJA GRAFA I TESTIRANJE ALGORITMA ---
            Graph transportGraph = new Graph();
            transportGraph.buildGraph(data);
            System.out.println("Graf uspješno izgrađen sa " + transportGraph.getAllNodes().size() + " čvorova.");

            // Primjer pretrage rute
            // Vaši gradovi i stanice su npr. G_0_0, G_0_1, G_1_0...
            // Stanice su A_0_0, Z_0_0 itd.
            String startStationId = "A_0_0"; // Autobuska stanica u gradu G_0_0
            String endStationId = "Z_1_0";   // Željeznička stanica u gradu G_1_0

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
    }*/
}