// util/TransportDataMapper.java
package util;

import model.Departure;
import model.Station;
import model.TransportData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransportDataMapper {

    @SuppressWarnings("unchecked")
    public static TransportData mapToTransportData(Object parsedData) {
        if (!(parsedData instanceof Map)) {
            throw new IllegalArgumentException("Parsed data is not a Map.");
        }

        Map<String, Object> dataMap = (Map<String, Object>) parsedData;
        TransportData transportData = new TransportData();

        // Map countryMap
        List<List<String>> rawCountryMap = (List<List<String>>) dataMap.get("countryMap");
        if (rawCountryMap != null && !rawCountryMap.isEmpty()) {
            int numRows = rawCountryMap.size();
            int numCols = rawCountryMap.get(0).size();
            String[][] countryMapArray = new String[numRows][numCols];
            for (int i = 0; i < numRows; i++) {
                countryMapArray[i] = rawCountryMap.get(i).toArray(new String[0]);
            }
            transportData.setCountryMap(countryMapArray);
        }

        // Map stations
        List<Map<String, Object>> rawStations = (List<Map<String, Object>>) dataMap.get("stations");
        List<Station> stations = new ArrayList<>();
        if (rawStations != null) {
            for (Map<String, Object> rawStation : rawStations) {
                Station station = new Station();
                station.setCity((String) rawStation.get("city"));
                station.setBusStation((String) rawStation.get("busStation"));
                station.setTrainStation((String) rawStation.get("trainStation"));
                stations.add(station);
            }
        }
        transportData.setStations(stations);

        // Map departures
        List<Map<String, Object>> rawDepartures = (List<Map<String, Object>>) dataMap.get("departures");
        List<Departure> departures = new ArrayList<>(); // Nova lista za mapirane Departure objekte
        if (rawDepartures != null) {
            for (Map<String, Object> rawDeparture : rawDepartures) {
                Departure departure = new Departure();
                departure.setType((String) rawDeparture.get("type"));
                departure.setFrom((String) rawDeparture.get("from"));
                departure.setTo((String) rawDeparture.get("to"));
                departure.setDepartureTime((String) rawDeparture.get("departureTime"));
                departure.setDuration(((Number) rawDeparture.get("duration")).intValue());
                departure.setPrice(((Number) rawDeparture.get("price")).intValue());
                departure.setMinTransferTime(((Number) rawDeparture.get("minTransferTime")).intValue());
                departures.add(departure); // Dodajemo mapirani objekat u novu listu
            }
        }
        transportData.setDepartures(departures); // Postavljamo ispravnu listu tipa List<Departure>

        return transportData;
    }
}