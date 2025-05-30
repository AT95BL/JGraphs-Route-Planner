package model;

public class Departure {
    public String type;           // "autobus" ili "voz"
    public String from;
    public String to;
    public String departureTime;
    public int duration;          // u minutama
    public int price;
    public int minTransferTime;   // u minutama

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getMinTransferTime() {
        return minTransferTime;
    }

    public void setMinTransferTime(int minTransferTime) {
        this.minTransferTime = minTransferTime;
    }
    
    /**
     * Overrides the default toString() method to provide a human-readable representation
     * of a Departure object.
     * This is useful for printing object details directly (e.g., with System.out.println()).
     *
     * @return A string representation of the Departure object.
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
}
