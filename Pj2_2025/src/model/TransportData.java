package model;

import java.util.List;

// import util.TransportDataGenerator.Departure;

public class TransportData {
    public String[][] countryMap;
    public List<Station> stations;
    public List<Departure> departures;

    public String[][] getCountryMap() {
        return countryMap;
    }

    public void setCountryMap(String[][] countryMap) {
        this.countryMap = countryMap;
    }

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    public List<Departure> getDepartures() {
        return departures;
    }

    public void setDepartures(List<Departure> departures) {
        this.departures = departures;
    }
    
 // Metoda printTransportData()
    public void printTransportData() {
        System.out.println("--- Transport Data ---");

        // Ispis sadržaja matrice countryMap
        System.out.println("\n--- Country Map ---");
        if (countryMap == null || countryMap.length == 0) {
            System.out.println("Country Map is empty or not initialized.");
        } else {
            for (int i = 0; i < countryMap.length; i++) {
                for (int j = 0; j < countryMap[i].length; j++) {
                    System.out.print(countryMap[i][j] + " ");
                }
                System.out.println(); // Novi red nakon svakog reda matrice
            }
        }

        // Ispis sadržaja liste stanica (stations)
        System.out.println("\n--- Stations ---");
        if (stations == null || stations.isEmpty()) {
            System.out.println("No stations available.");
        } else {
            for (Station station : stations) {
                System.out.println(station); // Pretpostavlja se da Station ima toString() metodu
            }
        }

        // Ispis sadržaja liste polazaka (departures)
        System.out.println("\n--- Departures ---");
        if (departures == null || departures.isEmpty()) {
            System.out.println("No departures available.");
        } else {
            for (Departure departure : departures) {
                System.out.println(departure); // Pretpostavlja se da Departure ima toString() metodu
            }
        }

        System.out.println("\n--- End of Transport Data ---");
    }

	
}	
