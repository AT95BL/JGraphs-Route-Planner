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



/**
 * A simple, custom JSON parser designed to read and interpret JSON strings.
 * This parser supports parsing JSON objects, arrays, strings, numbers, booleans (true/false), and null values.
 * It's built for demonstration purposes and handles basic JSON structures without relying on external libraries.
 */
public class SimpleJsonParser {
	
	/**
     * The JSON string to be parsed.
     */
    public String jsonString;
    /**
     * The current parsing index within the {@code jsonString}.
     */
    public int index;

    /**
     * Constructs a new {@code SimpleJsonParser} with the given JSON string.
     *
     * @param jsonString The JSON string that will be parsed.
     */
    public SimpleJsonParser(String jsonString) {
        this.jsonString = jsonString;
        this.index = 0;
    }
    
    /**
     * Skips any whitespace characters from the current {@link #index} position
     * until a non-whitespace character is encountered or the end of the string is reached.
    */
    public void skipWhitespace() {
        while (index < jsonString.length() && Character.isWhitespace(jsonString.charAt(index))) {
            index++;
        }
    }
    
    /**
     * Peeks at the next non-whitespace character without advancing the {@link #index}.
     *
     * @return The next non-whitespace character in the JSON string.
     * @throws IndexOutOfBoundsException if the end of the string is reached while skipping whitespace.
    */
    public char peek() {
        skipWhitespace();
        return jsonString.charAt(index);
    }
    
    /**
     * Consumes (reads and advances the index past) the next non-whitespace character.
     *
     * @return The consumed character.
     * @throws IndexOutOfBoundsException if the end of the string is reached while skipping whitespace.
    */
    public char consume() {
        skipWhitespace();
        return jsonString.charAt(index++);
    }
    
    /**
     * Parses the JSON string starting from the current {@link #index}.
     * This method acts as the entry point for parsing and delegates to specific
     * parsing methods based on the type of the first encountered JSON token
     * (object, array, string, number, boolean, or null).
     *
     * @return A Java object representing the parsed JSON value (e.g., {@code Map<String, Object>} for objects,
     * {@code List<Object>} for arrays, {@code String}, {@code Number}, {@code Boolean}, or {@code null}).
     * @throws RuntimeException if an unexpected character or invalid JSON structure is encountered.
    */
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
    
    /**
     * Parses a JSON object from the current {@link #index}.
     * Assumes the current character is '{' (which will be consumed).
     * It reads key-value pairs until the closing '}' is encountered.
     *
     * @return A {@link Map}{@code <String, Object>} representing the parsed JSON object.
     * @throws RuntimeException if the object structure is invalid (e.g., missing colon after key).
    */
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
    
    /**
     * Parses a JSON array from the current {@link #index}.
     * Assumes the current character is '[' (which will be consumed).
     * It reads elements until the closing ']' is encountered.
     *
     * @return A {@link List}{@code <Object>} representing the parsed JSON array.
     * @throws RuntimeException if the array structure is invalid.
    */
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
    
    /**
     * Parses a JSON string from the current {@link #index}.
     * Assumes the current character is '"' (which will be consumed).
     * It reads characters until the closing '"' is encountered.
     *
     * @return A {@link String} representing the parsed JSON string value.
     * @throws RuntimeException if the string is not properly closed with a double quote.
    */
    public String parseString() {
        consume(); // '"'
        StringBuilder sb = new StringBuilder();
        while (peek() != '"') {
            sb.append(consume());
        }
        consume(); // '"'
        return sb.toString();
    }
    
    /**
     * Parses a JSON number (integer or floating-point) from the current {@link #index}.
     * It reads digits, a potential minus sign, and a potential decimal point.
     *
     * @return A {@link Number} object (either {@link Long} for integers or {@link Double} for decimals)
     * representing the parsed numerical value.
     * @throws RuntimeException if the parsed string is not a valid number.
    */
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

    // Main metoda za testiranje (samo za demonstraciju parsiranja generickih JSON struktura)
    /*
    public static void main(String[] args) {
        String jsonFilePath = "transport_data.json"; // Vas generisani fajl

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
            data.printTransportData(); // Pozovite metodu da ispisete mapirane podatke
            
            // --- IZGRADNJA GRAFA I TESTIRANJE ALGORITMA ---
            Graph transportGraph = new Graph();
            transportGraph.buildGraph(data);
            System.out.println("Graf uspjesno izgradjen sa " + transportGraph.getAllNodes().size() + " cvorova.");

            // Primjer pretrage rute
            // Vasi gradovi i stanice su npr. G_0_0, G_0_1, G_1_0...
            // Stanice su A_0_0, Z_0_0 itd.
            String startStationId = "A_0_0"; // Autobuska stanica u gradu G_0_0
            String endStationId = "Z_1_0";   // zeljeznicka stanica u gradu G_1_0

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
    }*/
}