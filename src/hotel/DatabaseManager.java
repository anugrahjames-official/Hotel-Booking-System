package hotel;

import hotel.model.Room;
import hotel.model.Guest;
import hotel.model.Booking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;

// Centralized database logic to keep the project simple
public class DatabaseManager {
    // Relative path to SQLite DB file
    private static final String URL = "jdbc:sqlite:database/hotel.db";

    // 1. Establish connection to SQLite
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // 2. Initialize tables
    // TODO (All): Each member adds their table creation here
    public static void initializeDatabase() {
        // TODO (Ann): Add CREATE TABLE rooms
        // TODO (Rishik): Add CREATE TABLE guests
        // TODO (Asitha): Add CREATE TABLE bookings

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            // TODO (Ann): stmt.execute(createRooms);
            // TODO (Rishik): stmt.execute(createGuests);
            // TODO (Asitha): stmt.execute(createBookings);
        } catch (SQLException e) {
            System.out.println("db initialization error: " + e.getMessage());
        }
    }

    // TODO (Ann): 3. Fetch all available rooms
    // public static ArrayList<Room> getAvailableRooms() { ... }

    // TODO (Ann): Fetch a specific room to get its price
    // public static Room getRoom(int roomNo) { ... }

    // TODO (Ann): Add a new room
    // public static void addRoom(int roomNo, String roomType, double basePrice) { ... }

    // TODO (Ann): Remove a room (only if not booked)
    // public static void removeRoom(int roomNo) { ... }

    // TODO (Ann): Update room type and price
    // public static void updateRoom(int roomNo, String roomType, double basePrice) { ... }

    // TODO (Rishik): 4. Register a new guest
    // public static void addGuest(String name, String idProof, String contact) { ... }

    // TODO (Rishik): 5. Fetch all guests
    // public static ArrayList<Guest> getAllGuests() { ... }

    // TODO (Rishik): Fetch specific guest to check their tier
    // public static Guest getGuest(int guestId) { ... }

    // TODO (Asitha): 6. Book a room
    // public static void bookRoom(int guestId, int roomNo, String checkIn, String checkOut, double bill) { ... }
    // NOTE: Call updateLoyaltyTier(conn, guestId) after the booking transaction commits.

    // Internal method to update loyalty tier
    private static void updateLoyaltyTier(Connection conn, int guestId) throws SQLException {
        Guest g = getGuest(guestId);
        if (g == null)
            return;

        String newTier = "NONE";
        if (g.getBookingCount() >= 7)
            newTier = "GOLD";
        else if (g.getBookingCount() >= 3)
            newTier = "SILVER";

        if (!newTier.equals(g.getLoyaltyTier())) {
            String updateTier = "UPDATE guests SET loyalty_tier = ? WHERE guest_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateTier)) {
                pstmt.setString(1, newTier);
                pstmt.setInt(2, guestId);
                pstmt.executeUpdate();
                System.out.println("congratulations guest upgraded to " + newTier + " tier");
            }
        }
    }

    // TODO (Asitha): 7. View all bookings
    // public static ArrayList<Booking> getAllBookings() { ... }

    // TODO (Asitha): 8. Cancel a booking
    // public static void cancelBooking(int bookingId) { ... }
}
