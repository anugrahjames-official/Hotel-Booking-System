package hotel;

import hotel.model.Room;
import hotel.model.Guest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class ReservationManager {
    // Use dd-MM-yyyy format for user input
    private static final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    // Use yyyy-MM-dd format for database storage
    private static final DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Room Inventory Operations

    // TODO (Ann): Adds a new room to the hotel inventory.
    // public static void addRoom(int roomNo, String roomType, double basePrice) { ... }

    // TODO (Ann): Removes an existing room from the hotel inventory.
    // public static void removeRoom(int roomNo) { ... }

    // TODO (Ann): Updates details (type and price) of an existing room.
    // public static void updateRoom(int roomNo, String roomType, double basePrice) { ... }

    // TODO (Ann): Retrieves all available rooms, optionally filtered by room type.
    // public static void getAvailableRooms(String typeFilter) { ... }

    // Booking / Reservation Operations

    /**
     * Processes a room booking request for a guest.
     * Performs validations, calculates the total bill (with peak seasonal surcharges 
     * and loyalty discounts), and records the transaction.
     * 2814330003
     * @return true if the booking was processed successfully, false otherwise.
     */
    public static boolean bookRoom(int guestId, int roomNo, String checkInStr, String checkOutStr) {
        try {
            // 1. Verify guest exists
            Guest guest = DatabaseManager.getGuest(guestId);
            if (guest == null) {
                System.out.println("guest not found");
                return false;
            }

            // 2. Verify room exists and is available
            Room room = DatabaseManager.getRoom(roomNo);
            if (room == null || !room.isAvailable()) {
                System.out.println("room not available or doesn't exist");
                return false;
            }

            // 3. Parse and validate dates
            LocalDate checkIn = LocalDate.parse(checkInStr, inputFormatter);
            LocalDate checkOut = LocalDate.parse(checkOutStr, inputFormatter);

            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
            if (nights <= 0) {
                System.out.println("check-out must be after check-in");
                return false;
            }

            // 4. Calculate Bill
            double baseAmount = nights * room.getBasePrice();

            // Surcharge during peak months: January (1), May (5), December (12)
            double surcharge = 0;
            int month = checkIn.getMonthValue();
            if (month == 5 || month == 12 || month == 1) {
                surcharge = baseAmount * 0.10; // 10% peak season surcharge
            }

            double subtotal = baseAmount + surcharge;

            // Loyalty Discount based on Guest's loyalty tier
            double discount = 0;
            if ("GOLD".equalsIgnoreCase(guest.getLoyaltyTier())) {
                discount = subtotal * 0.10; // 10% discount
            } else if ("SILVER".equalsIgnoreCase(guest.getLoyaltyTier())) {
                discount = subtotal * 0.05; // 5% discount
            }

            double finalBill = subtotal - discount;

            // 5. Convert to Database format & persist booking
            String dbIn = checkIn.format(dbFormatter);
            String dbOut = checkOut.format(dbFormatter);

            DatabaseManager.bookRoom(guestId, roomNo, dbIn, dbOut, finalBill);
            return true;

        } catch (Exception e) {
            System.out.println("error processing booking: please check your inputs");
            return false;
        }
    }

    // TODO (Asitha): Cancels a booking and frees up the room.
    // public static void cancelBooking(int bookingId) { ... }
}
