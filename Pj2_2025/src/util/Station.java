package util;

public class Station {
    public String city;
    public String busStation;
    public String trainStation;

    // 1. Default Constructor
    //    Initializes all String fields to null.
    public Station() {
    }

    // 2. Constructor with all fields
    //    Allows you to set the city, busStation, and trainStation when creating a Station object.
    public Station(String city, String busStation, String trainStation) {
        this.city = city;
        this.busStation = busStation;
        this.trainStation = trainStation;
    }

}