package model;

import java.util.List;

import util.TransportDataGenerator.Departure;

public class TransportData {
    private String[][] countryMap;
    private List<Station> stations;
    private List<Departure> departures;

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
    
}
