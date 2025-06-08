package util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import model.*;

public class TransportDataGenerator {
	
    // 1) UKLONITI: tekstom klase, hardcode-ovane vrijednosti!!; 
    public static int DEPARTURES_PER_STATION = 5;				
    public static final Random random = new Random();

    // 2) Dodati nove varijable za dimenzije matrice 
    /**
     * The number of rows (n) in the country map grid.
    */
    public int numRows; 									// Broj redova (n)
    /**
     * The number of columns (m) in the country map grid.
    */
    public int numCols; 									// Broj kolona (m)
    
    public int departuresPerStation;
    
    /**
     * 3)
     * Konstruktor za TransportDataGenerator.
     * @param numRows Broj redova matrice gradova (n).
     * @param numCols Broj kolona matrice gradova (m).
    */
    public TransportDataGenerator(int numRows, int numCols) {
        this.numRows = numRows;
        this.numCols = numCols;
        departuresPerStation = DEPARTURES_PER_STATION;
    }
    
    // 4)	generisao sam json-file, ovaj main mi za sada vise ne treba..
    /*
    public static void main(String[] args) {
        // Primjer poziva s dinamickim dimenzijama
        // U realnoj aplikaciji, ove dimenzije bi dolazile iz GUI unosa korisnika
        int n = 5; // 
        int m = 5; // 
        TransportDataGenerator generator = new TransportDataGenerator(n, m); // Poziv konstruktora
        TransportData data = generator.generateData();
        generator.saveToJson(data, "transport_data.json");
        System.out.println("Podaci su generisani i sacuvani kao transport_data.json");
    }
    */

    // struktura podataka koja sadrzi sve trazene ulazne podatke
    /*	6) IZMJENA: Uvoz Klasa TransportData, Station i Departure iz paketa module..
    public static class TransportData {
        public String[][] countryMap;
        public List<Station> stations;
        public List<Departure> departures;
        
        public List<Departure> getDepartures() {
            return this.departures;
        }
    }

    public static class Station {
        public String city;
        public String busStation;
        public String trainStation;
    }

    public static class Departure {
        public String type; 						// "autobus" ili "voz"
        public String from;
        public String to;
        public String departureTime;
        public int duration; 						// u minutama
        public int price;
        public int minTransferTime; 				// vrijeme potrebno za transfer (u minutama)
    }
    */
    
    /*	5) IZMJENA:
	    public TransportData generateData() {
	        TransportData data = new TransportData();
	        data.countryMap = generateCountryMap();
	        data.stations = generateStations();
	        data.departures = generateDepartures(data.stations);
	        return data;
	    }
    */
    /**
     * Generates a complete set of transportation data, including the country map,
     * stations within each city, and various departures from these stations.
     *
     * @return A {@link TransportData} object containing the generated map, stations, and departures.
    */
    public TransportData generateData() {
        TransportData data = new TransportData();
        data.countryMap = generateCountryMap();
        data.stations = generateStations();
        data.departures = generateDepartures(data.stations);
        return data;
    }
    
    
    // generisanje gradova (G_X_Y)
    /*	6) IZMJENA:S
	    public String[][] generateCountryMap() {
	        String[][] countryMap = new String[SIZE][SIZE];
	        for (int x = 0; x < SIZE; x++) {
	            for (int y = 0; y < SIZE; y++) {
	                countryMap[x][y] = "G_" + x + "_" + y;
	            }
	        }
	        return countryMap;
	    }
    */
    /**
     * Generates a 2D array representing the country map. Each element in the array
     * is a string in the format "G_X_Y", where X and Y are the coordinates of the city
     * within the grid. The dimensions of the map are determined by {@link #numRows}
     * and {@link #numCols}.
     *
     * @return A {@code String[][]} representing the grid-based country map.
    */
    public String[][] generateCountryMap() {
        String[][] countryMap = new String[this.numRows][this.numCols]; // Koristimo numRows i numCols
        for (int x = 0; x < this.numRows; x++) { // Koristimo numRows
            for (int y = 0; y < this.numCols; y++) { // Koristimo numCols
                countryMap[x][y] = "G_" + x + "_" + y;
            }
        }
        return countryMap;
    }

    // generisanje autobuskih i zeljeznickih stanica
    /*	6) IZMJENA:
	    public List<Station> generateStations() {
	        List<Station> stations = new ArrayList<>();
	        for (int x = 0; x < SIZE; x++) {
	            for (int y = 0; y < SIZE; y++) {
	                Station station = new Station();
	                station.city = "G_" + x + "_" + y;
	                station.busStation = "A_" + x + "_" + y;
	                station.trainStation = "Z_" + x + "_" + y;
	                stations.add(station);
	            }
	        }
	        return stations;
	    }
    */
    /**
     * Generates a list of {@link Station} objects, one for each city in the country map.
     * Each station includes the city name, a bus station name (e.g., "A_X_Y"),
     * and a train station name (e.g., "Z_X_Y").
     *
     * @return A {@link List} of {@link Station} objects.
     */
    public List<Station> generateStations() {
        List<Station> stations = new ArrayList<>();
        for (int x = 0; x < this.numRows; x++) { // Koristimo numRows
            for (int y = 0; y < this.numCols; y++) { // Koristimo numCols
                Station station = new Station();
                station.city = "G_" + x + "_" + y;
                station.busStation = "A_" + x + "_" + y;
                station.trainStation = "Z_" + x + "_" + y;
                stations.add(station);
            }
        }
        return stations;
    }

    // generisanje vremena polazaka
    /**
     * Generates a list of {@link Departure} objects based on the provided list of stations.
     * For each station, it generates a fixed number of bus and train departures
     * to neighboring cities.
     *
     * @param stations A {@link List} of {@link Station} objects from which departures will be generated.
     * @return A {@link List} of {@link Departure} objects.
     */
    public List<Departure> generateDepartures(List<Station> stations) {
        List<Departure> departures = new ArrayList<>();

        for (Station station : stations) {
            int x = Integer.parseInt(station.city.split("_")[1]);
            int y = Integer.parseInt(station.city.split("_")[2]);

            // generisanje polazaka autobusa
            for (int i = 0; i < departuresPerStation; i++) {
                departures.add(generateDeparture("autobus", station.busStation, x, y));
            }

            // generisanje polazaka vozova
            for (int i = 0; i < departuresPerStation; i++) {
                departures.add(generateDeparture("voz", station.trainStation, x, y));
            }
        }
        return departures;
    }
    
    /**
     * Generates a single {@link Departure} object with random characteristics.
     * The destination of the departure is a randomly chosen neighboring city.
     * Other attributes like departure time, duration, price, and minimum transfer time
     * are also randomized.
     *
     * @param type The type of transport (e.g., "autobus" or "voz").
     * @param from The name of the departure station.
     * @param x The X coordinate of the city where the departure originates.
     * @param y The Y coordinate of the city where the departure originates.
     * @return A {@link Departure} object with generated details.
     */
    public Departure generateDeparture(String type, String from, int x, int y) {
        Departure departure = new Departure();
        departure.type = type;
        departure.from = from;

        // generisanje susjeda
        List<String> neighbors = getNeighbors(x, y);
        departure.to = neighbors.isEmpty() ? from : neighbors.get(random.nextInt(neighbors.size()));

        // generisanje vremena
        int hour = random.nextInt(24);
        int minute = random.nextInt(4) * 15; // 0, 15, 30, 45
        departure.departureTime = String.format("%02d:%02d", hour, minute);

        // geneirsanje cijene
        departure.duration = 30 + random.nextInt(151);
        departure.price = 100 + random.nextInt(901);

        // generisanje vremena transfera
        departure.minTransferTime = 5 + random.nextInt(26);

        return departure;
    }

    // pronalazak susjednih gradova
    /*	7) IZMJENA
	    public List<String> getNeighbors(int x, int y) {
	        List<String> neighbors = new ArrayList<>();
	        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; 
	
	        for (int[] dir : directions) {
	            int nx = x + dir[0];
	            int ny = y + dir[1];
	            if (nx >= 0 && nx < SIZE && ny >= 0 && ny < SIZE) {
	                neighbors.add("G_" + nx + "_" + ny);
	            }
	        }
	        return neighbors;
	    }
    */
    /**
     * Retrieves a list of direct neighboring cities for a given city at coordinates (x, y).
     * Neighbors are considered to be cities immediately to the left, right, top, or bottom
     * within the grid, respecting the map boundaries defined by {@link #numRows} and {@link #numCols}.
     *
     * @param x The X coordinate of the current city.
     * @param y The Y coordinate of the current city.
     * @return A {@link List} of strings, where each string is the name of a neighboring city
     * (e.g., "G_X_Y").
     */
    public List<String> getNeighbors(int x, int y) {
        List<String> neighbors = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            // Provjera granica pomocu numRows i numCols
            if (nx >= 0 && nx < this.numRows && ny >= 0 && ny < this.numCols) { // Koristimo numRows i numCols
                neighbors.add("G_" + nx + "_" + ny);
            }
        }
        return neighbors;
    }

    // cuvanje podataka u JSON mapu
    /*	8)	IZMJENA
	    public void saveToJson(TransportData data, String filename) {
	        try (FileWriter file = new FileWriter(filename)) {
	            StringBuilder json = new StringBuilder();
	            json.append("{\n");
	
	            // mapa drzave
	            json.append("  \"countryMap\": [\n");
	            for (int i = 0; i < SIZE; i++) {
	                json.append("    [");
	                for (int j = 0; j < SIZE; j++) {
	                    json.append("\"").append(data.countryMap[i][j]).append("\"");
	                    if (j < SIZE - 1) json.append(", ");
	                }
	                json.append("]");
	                if (i < SIZE - 1) json.append(",");
	                json.append("\n");
	            }
	            json.append("  ],\n");
	
	            // stanice
	            json.append("  \"stations\": [\n");
	            for (int i = 0; i < data.stations.size(); i++) {
	                Station s = data.stations.get(i);
	                json.append("    {\"city\": \"").append(s.city)
	                    .append("\", \"busStation\": \"").append(s.busStation)
	                    .append("\", \"trainStation\": \"").append(s.trainStation)
	                    .append("\"}");
	                if (i < data.stations.size() - 1) json.append(",");
	                json.append("\n");
	            }
	            json.append("  ],\n");
	
	            // vremena polazaka
	            json.append("  \"departures\": [\n");
	            for (int i = 0; i < data.departures.size(); i++) {
	                Departure d = data.departures.get(i);
	                json.append("    {\"type\": \"").append(d.type)
	                    .append("\", \"from\": \"").append(d.from)
	                    .append("\", \"to\": \"").append(d.to)
	                    .append("\", \"departureTime\": \"").append(d.departureTime)
	                    .append("\", \"duration\": ").append(d.duration)
	                    .append(", \"price\": ").append(d.price)
	                    .append(", \"minTransferTime\": ").append(d.minTransferTime)
	                    .append("}");
	                if (i < data.departures.size() - 1) json.append(",");
	                json.append("\n");
	            }
	            json.append("  ]\n");
	
	            json.append("}");
	            file.write(json.toString());
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
    */
    /**
     * Saves the generated transport data (country map, stations, and departures)
     * into a JSON file. The data is formatted as a JSON object with three main arrays:
     * "countryMap", "stations", and "departures".
     *
     * @param data The {@link TransportData} object containing all the data to be saved.
     * @param filename The name of the file (including path, if necessary) where the JSON data will be written.
     * @throws IOException If an I/O error occurs during file writing.
     */
    public void saveToJson(TransportData data, String filename) {
        try (FileWriter file = new FileWriter(filename)) {
            StringBuilder json = new StringBuilder();
            json.append("{\n");

            // mapa drzave
            json.append("  \"countryMap\": [\n");
            // Iteriramo kroz stvarne dimenzije mape, ne fiksnu SIZE
            for (int i = 0; i < this.numRows; i++) { 									// Koristimo numRows
                json.append("    [");
                for (int j = 0; j < this.numCols; j++) { 								// Koristimo numCols
                    json.append("\"").append(data.countryMap[i][j]).append("\"");
                    if (j < this.numCols - 1) json.append(", "); 						// Koristimo numCols
                }
                json.append("]");
                if (i < this.numRows - 1) json.append(","); 							// Koristimo numRows
                json.append("\n");
            }
            json.append("  ],\n");

            // ... (ostatak saveToJson metode ostaje isti jer koristi data.stations.size() i data.departures.size()
            // a ne direktno dimenzije matrice) ...

            // stanice
            json.append("  \"stations\": [\n");
            for (int i = 0; i < data.stations.size(); i++) {
                Station s = data.stations.get(i);
                json.append("    {\"city\": \"").append(s.city)
                    .append("\", \"busStation\": \"").append(s.busStation)
                    .append("\", \"trainStation\": \"").append(s.trainStation)
                    .append("\"}");
                if (i < data.stations.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("  ],\n");

            // vremena polazaka
            json.append("  \"departures\": [\n");
            for (int i = 0; i < data.departures.size(); i++) {
                Departure d = data.departures.get(i);
                json.append("    {\"type\": \"").append(d.type)
                    .append("\", \"from\": \"").append(d.from)
                    .append("\", \"to\": \"").append(d.to)
                    .append("\", \"departureTime\": \"").append(d.departureTime)
                    .append("\", \"duration\": ").append(d.duration)
                    .append(", \"price\": ").append(d.price)
                    .append(", \"minTransferTime\": ").append(d.minTransferTime)
                    .append("}");
                if (i < data.departures.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("  ]\n");

            json.append("}");
            file.write(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
