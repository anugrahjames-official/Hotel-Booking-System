package hotel.model;

public class Guest {

    private int guestId;
    private String name;
    private String idProof;
    private String contact;
    private String loyaltyTier;
    private int bookingCount;

    public Guest(int guestId, String name, String idProof, String contact,
                 String loyaltyTier, int bookingCount) {
        this.guestId = guestId;
        this.name = name;
        this.idProof = idProof;
        this.contact = contact;
        this.loyaltyTier = loyaltyTier;
        this.bookingCount = bookingCount;
    }

    public int getGuestId() {
        return guestId;
    }

    public String getName() {
        return name;
    }

    public String getIdProof() {
        return idProof;
    }

    public String getContact() {
        return contact;
    }

    public String getLoyaltyTier() {
        return loyaltyTier;
    }

    public int getBookingCount() {
        return bookingCount;
    }

    @Override
    public String toString() {
        return "guest " + guestId + ": " + name
                + " (contact: " + contact
                + ", tier: " + loyaltyTier
                + ", bookings: " + bookingCount + ")";
    }
}