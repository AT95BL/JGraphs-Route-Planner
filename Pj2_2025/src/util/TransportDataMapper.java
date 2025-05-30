package util;

import java.util.*;
import util.TransportDataGenerator.*;

public class TransportDataMapper {

    @SuppressWarnings("unchecked")
    public static TransportData mapToTransportData(Object parsedData) {
        if (!(parsedData instanceof Map)) {
            throw new IllegalArgumentException("Root JSON element must be an object");
        }

        Map<String, Object> dataMap = (Map<String, Object>) parsedData;
        TransportData data = new TransportData();

        // --- 1) Mapiranje countryMap ---
        List<List<String>> countryMapRaw = (List<List<String>>) dataMap.get("countryMap");
        int rows = countryMapRaw.size();
        int cols = countryMapRaw.get(0).size();
        String[][] countryMap = new String[rows][cols];

        for (int i = 0; i < rows; i++) {
            List<String> row = countryMapRaw.get(i);
            for (int j = 0; j < row.size(); j++) {
                countryMap[i][j] = row.get(j);
            }
        }
        data.countryMap = countryMap;

        // --- 2) Mapiranje stations ---
        List<Map<String, Object>> stationsRaw = (List<Map<String, Object>>) dataMap.get("stations");
        List<Station> stations = new ArrayList<>();
        for (Map<String, Object> s : stationsRaw) {
            Station station = new Station();
            station.city = (String) s.get("city");
            station.busStation = (String) s.get("busStation");
            station.trainStation = (String) s.get("trainStation");
            stations.add(station);
        }
        data.stations = stations;

        // --- 3) Mapiranje departures ---
        List<Map<String, Object>> departuresRaw = (List<Map<String, Object>>) dataMap.get("departures");
        List<Departure> departures = new ArrayList<>();
        for (Map<String, Object> d : departuresRaw) {
            Departure departure = new Departure();
            departure.type = (String) d.get("type");
            departure.from = (String) d.get("from");
            departure.to = (String) d.get("to");
            departure.departureTime = (String) d.get("departureTime");
            departure.duration = ((Number) d.get("duration")).intValue();
            departure.price = ((Number) d.get("price")).intValue();
            departure.minTransferTime = ((Number) d.get("minTransferTime")).intValue();
            departures.add(departure);
        }
        data.departures = departures;

        return data;
    }
    
}
