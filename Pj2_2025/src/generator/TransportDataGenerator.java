package generator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TransportDataGenerator {
	
    // 1) UKLONITI: private static final int SIZE = 10; 
    private static final int DEPARTURES_PER_STATION = 5;
    private static final Random random = new Random();

    // 2) Dodati nove varijable za dimenzije
    private int numRows; 									// Broj redova (n)
    private int numCols; 									// Broj kolona (m)
    
    /**
     * 3)
     * Konstruktor za TransportDataGenerator.
     * @param numRows Broj redova matrice gradova (n).
     * @param numCols Broj kolona matrice gradova (m).
    */
    public TransportDataGenerator(int numRows, int numCols) {
        this.numRows = numRows;
        this.numCols = numCols;
    }
    
    // 4)	generisao sam file, ovaj main mi za sada više ne treba..
    /*
    public static void main(String[] args) {
        // Primjer poziva s dinamičkim dimenzijama
        // U realnoj aplikaciji, ove dimenzije bi dolazile iz GUI unosa korisnika
        int n = 10; // 
        int m = 10; // 
        TransportDataGenerator generator = new TransportDataGenerator(n, m); // Poziv konstruktora
        TransportData data = generator.generateData();
        generator.saveToJson(data, "transport_data.json");
        System.out.println("Podaci su generisani i sacuvani kao transport_data.json");
    }
    */

    // struktura podataka koja sadrzi sve trazene ulazne podatke
    public static class TransportData {
        public String[][] countryMap;
        public List<Station> stations;
        public List<Departure> departures;
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
    
    /*	5) IZMJENA:
	    public TransportData generateData() {
	        TransportData data = new TransportData();
	        data.countryMap = generateCountryMap();
	        data.stations = generateStations();
	        data.departures = generateDepartures(data.stations);
	        return data;
	    }
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
	    private String[][] generateCountryMap() {
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
     * Generise matricu gradova dimenzija n x m.
     * Gradovi su nazvani G_X_Y, gdje su X i Y njihove koordinate.
     * @return Dvodimenzionalni niz stringova koji predstavlja mapu države.
     */
    private String[][] generateCountryMap() {
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
	    private List<Station> generateStations() {
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
     * Generise listu autobuskih i željezničkih stanica za svaki grad u mapi.
     * @return Lista Station objekata.
     */
    private List<Station> generateStations() {
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
    private List<Departure> generateDepartures(List<Station> stations) {
        List<Departure> departures = new ArrayList<>();

        for (Station station : stations) {
            int x = Integer.parseInt(station.city.split("_")[1]);
            int y = Integer.parseInt(station.city.split("_")[2]);

            // generisanje polazaka autobusa
            for (int i = 0; i < DEPARTURES_PER_STATION; i++) {
                departures.add(generateDeparture("autobus", station.busStation, x, y));
            }

            // generisanje polazaka vozova
            for (int i = 0; i < DEPARTURES_PER_STATION; i++) {
                departures.add(generateDeparture("voz", station.trainStation, x, y));
            }
        }
        return departures;
    }

    private Departure generateDeparture(String type, String from, int x, int y) {
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
	    private List<String> getNeighbors(int x, int y) {
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
     * Pronalazi susjedne gradove za dati grad na koordinatama (x, y).
     * Susjedni gradovi su oni koji su direktno gore, dole, lijevo ili desno.
     * @param x X koordinata trenutnog grada.
     * @param y Y koordinata trenutnog grada.
     * @return Lista stringova sa nazivima susjednih gradova.
     */
    private List<String> getNeighbors(int x, int y) {
        List<String> neighbors = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            // Provjera granica pomoću numRows i numCols
            if (nx >= 0 && nx < this.numRows && ny >= 0 && ny < this.numCols) { // Koristimo numRows i numCols
                neighbors.add("G_" + nx + "_" + ny);
            }
        }
        return neighbors;
    }

    // cuvanje podataka u JSON mapu
    /*	8)	IZMJENA
	    private void saveToJson(TransportData data, String filename) {
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
     * Čuva generisane transportne podatke u JSON fajl.
     * @param data Objekat TransportData koji sadrži sve generisane podatke.
     * @param filename Ime fajla u koji će se podaci sačuvati.
     */
    private void saveToJson(TransportData data, String filename) {
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
