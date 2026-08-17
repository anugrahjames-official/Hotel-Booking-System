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
        return "Booking ID : " + bookingId + "\n" +
               "Guest ID   : " + guestId + "\n" +
               "Room No    : " + roomNo + "\n" +
               "Check-in   : " + checkIn + "\n" +
               "Check-out  : " + checkOut + "\n" +
               "Final Bill : ₹" + bill;
    }
}