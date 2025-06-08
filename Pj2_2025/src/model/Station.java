package model;

/**
 * Represents a transportation hub within a city, encompassing both a bus station and a train station.
 * This class stores the name of the city it belongs to, along with unique identifiers for its
 * bus and train stations.
 */
public class Station {
    /**
     * The name of the city where this station is located.
     */
    public String city;
    /**
     * The unique identifier or name of the bus station within this city.
     */
    public String busStation;
    /**
     * The unique identifier or name of the train station within this city.
     */
    public String trainStation;

    /**
     * Returns the name of the city associated with this station.
     * @return The city name.
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the name of the city for this station.
     * @param city The city name to set.
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Returns the unique identifier of the bus station.
     * @return The bus station's identifier.
     */
    public String getBusStation() {
        return busStation;
    }

    /**
     * Sets the unique identifier for the bus station.
     * @param busStation The bus station's identifier to set.
     */
    public void setBusStation(String busStation) {
        this.busStation = busStation;
    }

    /**
     * Returns the unique identifier of the train station.
     * @return The train station's identifier.
     */
    public String getTrainStation() {
        return trainStation;
    }

    /**
     * Sets the unique identifier for the train station.
     * @param trainStation The train station's identifier to set.
     */
    public void setTrainStation(String trainStation) {
        this.trainStation = trainStation;
    }

    /**
     * Provides a string representation of the {@code Station} object,
     * displaying the city, bus station, and train station names.
     * This method is useful for debugging and logging purposes.
     *
     * @return A formatted string containing the station's details.
     */
    @Override
    public String toString() {
        return "City: " + city + ", Bus Station: " + busStation + ", Train Station: " + trainStation;
    }
}