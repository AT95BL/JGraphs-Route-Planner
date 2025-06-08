package model;

/**
 * Represents a single departure for a mode of transport (bus or train) from one station to another.
 * This class encapsulates all relevant information about a specific journey, including its type,
 * origin, destination, departure time, duration, price, and minimum transfer time.
 */
public class Departure {
    /**
     * The type of transportation, e.g., "autobus" (bus) or "voz" (train).
     */
    public String type;
    /**
     * The identifier of the departure station.
     */
    public String from;
    /**
     * The identifier of the arrival station.
     */
    public String to;
    /**
     * The scheduled departure time, typically in "HH:MM" format.
     */
    public String departureTime;
    /**
     * The estimated duration of the journey in minutes.
     */
    public int duration;
    /**
     * The price of the journey.
     */
    public int price;
    /**
     * The minimum required transfer time at the destination station, in minutes,
     * if this departure is part of a larger multi-segment journey.
     */
    public int minTransferTime;

    /**
     * Returns the type of transportation for this departure.
     * @return The type of transport (e.g., "autobus", "voz").
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of transportation for this departure.
     * @param type The type of transport (e.g., "autobus", "voz").
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the identifier of the departure station.
     * @return The 'from' station identifier.
     */
    public String getFrom() {
        return from;
    }

    /**
     * Sets the identifier of the departure station.
     * @param from The 'from' station identifier.
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Returns the identifier of the arrival station.
     * @return The 'to' station identifier.
     */
    public String getTo() {
        return to;
    }

    /**
     * Sets the identifier of the arrival station.
     * @param to The 'to' station identifier.
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Returns the scheduled departure time.
     * @return The departure time in "HH:MM" format.
     */
    public String getDepartureTime() {
        return departureTime;
    }

    /**
     * Sets the scheduled departure time.
     * @param departureTime The departure time in "HH:MM" format.
     */
    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    /**
     * Returns the duration of the journey in minutes.
     * @return The duration in minutes.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Sets the duration of the journey in minutes.
     * @param duration The duration in minutes.
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Returns the price of the journey.
     * @return The price.
     */
    public int getPrice() {
        return price;
    }

    /**
     * Sets the price of the journey.
     * @param price The price.
     */
    public void setPrice(int price) {
        this.price = price;
    }

    /**
     * Returns the minimum required transfer time at the destination station in minutes.
     * This is relevant if this departure is a segment of a longer trip.
     * @return The minimum transfer time in minutes.
     */
    public int getMinTransferTime() {
        return minTransferTime;
    }

    /**
     * Sets the minimum required transfer time at the destination station in minutes.
     * @param minTransferTime The minimum transfer time in minutes.
     */
    public void setMinTransferTime(int minTransferTime) {
        this.minTransferTime = minTransferTime;
    }

    /**
     * Overrides the default {@code toString()} method to provide a human-readable string representation
     * of a {@code Departure} object. This is useful for debugging and logging, offering a quick overview
     * of the departure's key attributes.
     *
     * @return A string detailing the type, origin, destination, time, duration, price, and minimum transfer time of the departure.
     */
    @Override
    public String toString() {
        return "Departure{" +
                "type='" + type + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", departureTime='" + departureTime + '\'' +
                ", duration=" + duration + " min" +
                ", price=" + price +
                ", minTransferTime=" + minTransferTime + " min" +
                '}';
    }
    
    /**
     * Converts the departure time from "HH:MM" format to minutes since midnight.
     * @return Departure time in minutes since midnight, or -1 if format is invalid.
     */
    public int getDepartureTimeInMinutes() {
        try {
            String[] parts = departureTime.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return hours * 60 + minutes;
        } catch (Exception e) {
            return -1; // greška u formatu
        }
    }

}