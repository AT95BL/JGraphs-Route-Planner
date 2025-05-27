package util;

import java.util.List;

import generator.TransportDataGenerator.Departure;
import generator.TransportDataGenerator.Station;

public class TransportData {
    public String[][] countryMap;
    public List<Station> stations;
    public List<Departure> departures;
    
    // 1) Default Constructor
    public TransportData() {
    }
    
    // 2) Constructor with all fields
    public TransportData(String[][] countryMap, List<Station> stations, List<Departure> departures) {
        this.countryMap = countryMap;
        this.stations = stations;
        this.departures = departures;
    }
}