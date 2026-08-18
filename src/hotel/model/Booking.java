package hotel.model;

// Represents a room reservation made by a guest

public class Booking {
    private int bookingId;
    private int guestId;
    private int roomNo;
    private String checkIn;
    private String checkOut;
    private double bill;

    // Constructor
    public Booking(int bookingId, int guestId, int roomNo,
                   String checkIn, String checkOut, double bill) {
        this.bookingId = bookingId;
        this.guestId = guestId;
        this.roomNo = roomNo;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.bill = bill;
    }

    @Override
    public String toString() {
        return "Booking " + bookingId +
               " | Guest ID: " + guestId +
               " | Room No: " + roomNo +
               " | Dates: " + checkIn + " to " + checkOut +
               " | Bill: Rs." + bill;
    }
}
