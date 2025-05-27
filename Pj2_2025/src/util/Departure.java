package util;

public class Departure {
    public String type;             // "autobus" ili "voz"
    public String from;
    public String to;
    public String departureTime;
    public int duration;            // u minutama
    public int price;
    public int minTransferTime;     // vrijeme potrebno za transfer (u minutama)

    // 1. Default Constructor
    //    Initializes String fields to null and int fields to 0.
    public Departure() {
    }

    // 2. Constructor with all fields
    //    Allows you to set all properties of a departure when creating an object.
    public Departure(String type, String from, String to, String departureTime,
                     int duration, int price, int minTransferTime) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.departureTime = departureTime;
        this.duration = duration;
        this.price = price;
        this.minTransferTime = minTransferTime;
    }

    // 3. Constructor for essential departure details (type, from, to, time)
    //    Useful if duration, price, and transfer time are calculated or added later.
    public Departure(String type, String from, String to, String departureTime) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.departureTime = departureTime;
        this.duration = 0; // Default to 0, or set to a meaningful default
        this.price = 0;    // Default to 0
        this.minTransferTime = 0; // Default to 0
    }

    // 4. Constructor for main transport details including duration and price
    public Departure(String type, String from, String to, String departureTime,
                     int duration, int price) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.departureTime = departureTime;
        this.duration = duration;
        this.price = price;
        this.minTransferTime = 0; // Default to 0 if not specified
    }
}