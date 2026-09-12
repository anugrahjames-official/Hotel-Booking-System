package hotel.model;

public class Room {

    private final int roomNo;
    private final String roomType;
    private final double basePrice;
    private final boolean available;

    // Constructor
    public Room(int roomNo, String roomType, double basePrice, boolean available) {
        this.roomNo= roomNo;
        this.roomType = roomType;
        this.basePrice = basePrice;
        this.available = available;
    }

    // Getters
    public int getRoomNo() {
        return roomNo;
    }

    public String getRoomType() {
        return roomType;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public boolean isAvailable() {
        return available;
    }

    @Override
    public String toString() {
        return String.format(
                "Room Details:%nRoom Number: %d%nRoom Type: %s%nBase Price: %.2f%nAvailable: %s",
                roomNo,
                roomType,
                basePrice,
                available ? "Yes" : "No");
    }

}