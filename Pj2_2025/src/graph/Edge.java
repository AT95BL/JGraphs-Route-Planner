// src/graph/Edge.java
package graph;

import model.Departure; // Koristimo model.Departure za detalje putovanja

/**
 * Predstavlja granu u transportnom grafu. Grana je usmjerena i moze predstavljati direktan polazak ili transfer unutar grada.
 */
public class Edge {
    private Node source;       // Izvor cvora (polazna stanica)
    private Node destination;  // Odredišni cvor (dolazna stanica)
    private double weight;     // Tezina grane (zavisno od kriterijuma: vrijeme, cijena, presjedanje)
    private Departure departureDetails; // Detalji polaska ako se radi o direktnom putovanju
    private String type;       // Tip grane: "departure" (direktan polazak) ili "transfer" (transfer unutar grada)

    /**
     * Konstruktor za kreiranje grane koja predstavlja direktan polazak.
     * @param source Izvor cvora.
     * @param destination Odredišni cvor.
     * @param departureDetails Detalji polaska iz model.Departure.
     */
    public Edge(Node source, Node destination, Departure departureDetails) {
        this.source = source;
        this.destination = destination;
        this.departureDetails = departureDetails;
        this.type = "departure";
    }

    /**
     * Konstruktor za kreiranje grane koja predstavlja transfer unutar grada.
     * @param source Izvor cvora (npr. autobuska stanica).
     * @param destination Odredišni cvor (npr. zeljeznicka stanica u istom gradu).
     * @param transferTime Vrijeme potrebno za transfer (u minutama).
     */
    public Edge(Node source, Node destination, int transferTime) {
        this.source = source;
        this.destination = destination;
        this.weight = transferTime; // Pocetna tezina za transfer je samo vrijeme
        this.type = "transfer";
        this.departureDetails = null; // Transfer nema direktne detalje polaska
    }


    // Getteri i setteri
    public Node getSource() {
        return source;
    }

    public Node getDestination() {
        return destination;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Departure getDepartureDetails() {
        return departureDetails;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        if ("departure".equals(type) && departureDetails != null) {
            return String.format("%s (%s) -> %s (%s) [%s, %s, %.0fmin, %.0f€]",
                    source.getStationName(), source.getCity(), destination.getStationName(), destination.getCity(),
                    departureDetails.getType(), departureDetails.getDepartureTime(), (double)departureDetails.getDuration(), (double)departureDetails.getPrice());
        } else if ("transfer".equals(type)) {
            return String.format("Transfer %s -> %s (%.0f min)", source.getStationName(), destination.getStationName(), weight);
        }
        return "Unknown Edge";
    }
}