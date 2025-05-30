// src/graph/Node.java
package graph;

import model.Station; // Koristimo model.Station za podatke o gradu i stanicama

/**
 * Predstavlja čvor u transportnom grafu. Svaki čvor odgovara jednoj autobuskoj ili željezničkoj stanici.
 */
public class Node {
    public String id; // Jedinstveni ID čvora, npr. "A_0_0" (autobuska stanica u gradu G_0_0) ili "Z_0_0" (željeznička stanica u gradu G_0_0)
    public String city; // Grad kojem stanica pripada (npr. "G_0_0")
    public String stationName; // Naziv stanice (npr. "A_0_0" ili "Z_0_0")
    public String type; // Tip stanice: "bus" ili "train"

    /**
     * Konstruktor za kreiranje čvora grafa.
     * @param id Jedinstveni identifikator čvora (npr. "A_0_0" ili "Z_0_0").
     * @param city Grad kojem stanica pripada.
     * @param stationName Naziv stanice.
     * @param type Tip stanice ("bus" ili "train").
     */
    public Node(String id, String city, String stationName, String type) {
        this.id = id;
        this.city = city;
        this.stationName = stationName;
        this.type = type;
    }

    // Getteri
    public String getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public String getStationName() {
        return stationName;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return id.equals(node.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return stationName + " (" + city + ")";
    }
}