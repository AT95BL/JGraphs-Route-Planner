package model;

import java.util.List;

/**
 * A comprehensive data structure that holds all generated transportation information.
 * This includes a representation of the country map (grid of cities),
 * a list of all bus and train stations, and a list of all available departures between stations.
 * This class serves as a central container for the application's core data.
 */
public class TransportData {
    /**
     * A 2D array of strings representing the grid-based map of cities in the country.
     * Each element typically holds a city identifier like "G_X_Y".
     */
    public String[][] countryMap;
    /**
     * A list of {@link Station} objects, where each object represents a transportation hub
     * (containing both bus and train stations) in a specific city.
     */
    public List<Station> stations;
    /**
     * A list of {@link Departure} objects, each detailing a specific bus or train journey
     * between two stations.
     */
    public List<Departure> departures;

    /**
     * Returns the 2D array representing the country map.
     * @return The country map as a {@code String[][]}.
     */
    public String[][] getCountryMap() {
        return countryMap;
    }

    /**
     * Sets the 2D array representing the country map.
     * @param countryMap The {@code String[][]} to set as the country map.
     */
    public void setCountryMap(String[][] countryMap) {
        this.countryMap = countryMap;
    }

    /**
     * Returns the list of all stations.
     * @return A {@link List} of {@link Station} objects.
     */
    public List<Station> getStations() {
        return stations;
    }

    /**
     * Sets the list of stations.
     * @param stations The {@link List} of {@link Station} objects to set.
     */
    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    /**
     * Returns the list of all departures.
     * @return A {@link List} of {@link Departure} objects.
     */
    public List<Departure> getDepartures() {
        return departures;
    }

    /**
     * Sets the list of departures.
     * @param departures The {@link List} of {@link Departure} objects to set.
     */
    public void setDepartures(List<Departure> departures) {
        this.departures = departures;
    }

    /**
     * Prints a formatted summary of all transportation data contained within this object
     * to the console. This includes the country map, details of each station, and details
     * of each departure. This method is useful for verifying the loaded or generated data.
     */
    public void printTransportData() {
        System.out.println("--- Transport Data ---");

        // Print countryMap content
        System.out.println("\n--- Country Map ---");
        if (countryMap == null || countryMap.length == 0) {
            System.out.println("Country Map is empty or not initialized.");
        } else {
            for (int i = 0; i < countryMap.length; i++) {
                for (int j = 0; j < countryMap[i].length; j++) {
                    System.out.print(countryMap[i][j] + " ");
                }
                System.out.println(); // New line after each row of the matrix
            }
        }

        // Print stations list content
        System.out.println("\n--- Stations ---");
        if (stations == null || stations.isEmpty()) {
            System.out.println("No stations available.");
        } else {
            for (Station station : stations) {
                System.out.println(station); // Assumes Station has a meaningful toString() method
            }
        }

        // Print departures list content
        System.out.println("\n--- Departures ---");
        if (departures == null || departures.isEmpty()) {
            System.out.println("No departures available.");
        } else {
            for (Departure departure : departures) {
                System.out.println(departure); // Assumes Departure has a meaningful toString() method
            }
        }

        System.out.println("\n--- End of Transport Data ---");
    }
}