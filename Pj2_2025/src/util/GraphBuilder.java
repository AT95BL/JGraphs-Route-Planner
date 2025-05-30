package util;

import model.Departure;

import java.util.*;

public class GraphBuilder {

    public static class Edge {
        public String from;
        public String to;
        public int duration;
        public int price;
        public int transferTime;
        public String type;
        public String departureTime;

        public Edge(String from, String to, int duration, int price, int transferTime, String type, String departureTime) {
            this.from = from;
            this.to = to;
            this.duration = duration;
            this.price = price;
            this.transferTime = transferTime;
            this.type = type;
            this.departureTime = departureTime;
        }

        @Override
        public String toString() {
            return from + " -> " + to + " [" + duration + "min, " + price + "din, " + type + " at " + departureTime + "]";
        }
    }

    // Glavna metoda za izgradnju grafa
    public static Map<String, List<Edge>> buildGraph(List<Departure> departures) {
        Map<String, List<Edge>> graph = new HashMap<>();

        for (Departure dep : departures) {
            String from = dep.getFrom();
            String to = dep.getTo();

            Edge edge = new Edge(
                from,
                to,
                dep.getDuration(),
                dep.getPrice(),
                dep.getMinTransferTime(),
                dep.getType(),
                dep.getDepartureTime()
            );

            graph.computeIfAbsent(from, k -> new ArrayList<>()).add(edge);
        }

        return graph;
    }
}
