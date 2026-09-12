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
        java.io.File dbDir = new java.io.File("database");
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }
        return DriverManager.getConnection(URL);
    }

    // 2. Initialize tables
    // TODO (All): Each member adds their table creation here
    public static void initializeDatabase() {
         String createRooms = """
            CREATE TABLE IF NOT EXISTS rooms (
                room_no INTEGER PRIMARY KEY,
                room_type TEXT NOT NULL,
                base_price REAL NOT NULL,
                available INTEGER NOT NULL
            )
            """;
        // TODO (Rishik): Add CREATE TABLE guests
        String createBookings = """
    CREATE TABLE IF NOT EXISTS bookings (
        booking_id INTEGER PRIMARY KEY AUTOINCREMENT,
        guest_id INTEGER NOT NULL,
        room_no INTEGER NOT NULL,
        check_in TEXT NOT NULL,
        check_out TEXT NOT NULL,
        bill REAL NOT NULL,
        FOREIGN KEY (guest_id) REFERENCES guests(guest_id),
        FOREIGN KEY (room_no) REFERENCES rooms(room_no)
    )
    """;

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(createRooms);
            // TODO (Rishik): stmt.execute(createGuests);
             stmt.execute(createBookings);
        } catch (SQLException e) {
            System.out.println("db initialization error: " + e.getMessage());
        }
    }
     
       // 3. Fetch all available rooms
     public static ArrayList<Room> getAvailableRooms() {

        ArrayList<Room> rooms = new ArrayList<>();

        String sql = """
            SELECT room_no, room_type, base_price, available
            FROM rooms
            WHERE available = 1
            """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {

                Room room = new Room(
                    rs.getInt("room_no"),
                    rs.getString("room_type"),
                    rs.getDouble("base_price"),
                    rs.getInt("available") == 1
                );

                rooms.add(room);
            }
        } catch (SQLException e) {
            System.out.println("error fetching available rooms: "
                    + e.getMessage());
        }

        return rooms;
    }

      // Fetch a specific room to get its price
    public static Room getRoom(int roomNo) {

        String sql = """
            SELECT room_no, room_type, base_price, available
            FROM rooms
            WHERE room_no = ?
            """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomNo);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {

                    Room room = new Room(
                        rs.getInt("room_no"),
                        rs.getString("room_type"),
                        rs.getDouble("base_price"),
                        rs.getInt("available") == 1
                    );

                    return room;
                }
            }
        } catch (SQLException e) {
            System.out.println("error fetching room: "
                    + e.getMessage());
        }

        return null;
    }

    // Add a new room
    public static void addRoom(
            int roomNo,
            String roomType,
            double basePrice) {

        String sql = """
            INSERT INTO rooms
            (room_no, room_type, base_price, available)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomNo);
            pstmt.setString(2, roomType);
            pstmt.setDouble(3, basePrice);
            pstmt.setInt(4, 1);

            pstmt.executeUpdate();

            System.out.println("Room added successfully.");

        } catch (SQLException e) {
            System.out.println("error adding room: "
                    + e.getMessage());
        }
    }

    // Remove a room only when it is currently available
    public static void removeRoom(int roomNo) {

        String deleteRoom = """
            DELETE FROM rooms
            WHERE room_no = ? AND available = 1
            """;

           try (Connection conn = connect();
               PreparedStatement deleteStmt =
                         conn.prepareStatement(deleteRoom)) {

                deleteStmt.setInt(1, roomNo);

                int rows = deleteStmt.executeUpdate();

                if (rows > 0) {
                    System.out.println("Room removed successfully.");
                } else {
                    System.out.println(
                        "Room not found or currently booked."
                    );
                }

        } catch (SQLException e) {
            System.out.println("error removing room: "
                    + e.getMessage());
        }
    }

    // Update room type and price
    public static void updateRoom(
            int roomNo,
            String roomType,
            double basePrice) {

        String sql = """
            UPDATE rooms
            SET room_type = ?, base_price = ?
            WHERE room_no = ?
            """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomType);
            pstmt.setDouble(2, basePrice);
            pstmt.setInt(3, roomNo);

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("Room updated successfully.");
            } else {
                System.out.println("Room not found.");
            }

        } catch (SQLException e) {
            System.out.println("error updating room: "
                    + e.getMessage());
        }
    }

    // TODO (Rishik): 4. Register a new guest
    // public static void addGuest(String name, String idProof, String contact) { ... }

    // TODO (Rishik): 5. Fetch all guests
    // public static ArrayList<Guest> getAllGuests() { ... }

    // TODO (Rishik): Fetch specific guest to check their tier
    // public static Guest getGuest(int guestId) { ... }

    public static void bookRoom(int guestId, int roomNo, String checkIn,
                               String checkOut, double bill) {

    String insertBooking = """
        INSERT INTO bookings
        (guest_id, room_no, check_in, check_out, bill)
        VALUES (?, ?, ?, ?, ?)
        """;

    String updateRoom =
            "UPDATE rooms SET available = 0 WHERE room_no = ?";

    String incrementBookingCount =
            "UPDATE guests SET booking_count = booking_count + 1 " +
            "WHERE guest_id = ?";

    try (Connection conn = connect()) {

        // Start transaction
        conn.setAutoCommit(false);

        try (PreparedStatement bookingStmt =
                     conn.prepareStatement(insertBooking);
             PreparedStatement roomStmt =
                     conn.prepareStatement(updateRoom);
             PreparedStatement countStmt =
                     conn.prepareStatement(incrementBookingCount)) {

            // 1. Insert booking
            bookingStmt.setInt(1, guestId);
            bookingStmt.setInt(2, roomNo);
            bookingStmt.setString(3, checkIn);
            bookingStmt.setString(4, checkOut);
            bookingStmt.setDouble(5, bill);

            if (bookingStmt.executeUpdate() == 0) {
                throw new SQLException("Booking insertion failed.");
            }

            // 2. Make room unavailable
            roomStmt.setInt(1, roomNo);

            if (roomStmt.executeUpdate() == 0) {
                throw new SQLException("Room update failed.");
            }

            // 3. Increase guest booking count
            countStmt.setInt(1, guestId);

            if (countStmt.executeUpdate() == 0) {
                throw new SQLException(
                        "Guest booking count update failed.");
            }

            // 4. FIRST COMMIT
            conn.commit();

            // 5. Update loyalty tier
            updateLoyaltyTier(conn, guestId);

            // 6. SECOND COMMIT
            conn.commit();

            // 7. Success message
            System.out.println("Room booked successfully!");
            System.out.println("Bill: Rs." + bill);

            

        } catch (SQLException e) {

            System.out.println("Booking failed.");
            System.out.println("Error: " + e.getMessage());

            // Rollback in separate try-catch
            try {
                conn.rollback();
                System.out.println("Rollback done.");

            } catch (SQLException rollbackError) {

                System.out.println("Rollback failed.");
                System.out.println("Rollback error: "
                        + rollbackError.getMessage());
            }

           

        } finally {

            // Restore auto-commit
            conn.setAutoCommit(true);
        }

    } catch (SQLException e) {

        System.out.println("Database connection error: "
                + e.getMessage());

       
    }
}

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

    public static ArrayList<Booking> getAllBookings() {

    ArrayList<Booking> bookings = new ArrayList<>();

    String sql = "SELECT * FROM bookings";

    try (Connection conn = connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {

            Booking booking = new Booking(
                rs.getInt("booking_id"),
                rs.getInt("guest_id"),
                rs.getInt("room_no"),
                rs.getString("check_in"),
                rs.getString("check_out"),
                rs.getDouble("bill")
            );

            bookings.add(booking);
        }

    } catch (SQLException e) {
        System.out.println("Error fetching bookings: " + e.getMessage());
    }

    return bookings;
    }
    public static void cancelBooking(int bookingId) {

    String selectRoom =
            "SELECT room_no FROM bookings WHERE booking_id = ?";

    String deleteBooking =
            "DELETE FROM bookings WHERE booking_id = ?";

    String updateRoom =
            "UPDATE rooms SET available = 1 WHERE room_no = ?";

    try (Connection conn = connect();
         PreparedStatement selectStmt = conn.prepareStatement(selectRoom)) {

        // 1. SELECT first
        selectStmt.setInt(1, bookingId);

        int roomNo;

        try (ResultSet rs = selectStmt.executeQuery()) {

            // If booking does not exist, stop here
            if (!rs.next()) {
                System.out.println("Booking not found.");
                return;
            }

            roomNo = rs.getInt("room_no");
        }

        // 2. Booking exists, so start transaction
        conn.setAutoCommit(false);

        try {

            // 3. Delete booking
            try (PreparedStatement deleteStmt =
                         conn.prepareStatement(deleteBooking)) {

                deleteStmt.setInt(1, bookingId);

                int deletedRows = deleteStmt.executeUpdate();

                if (deletedRows == 0) {
                    throw new SQLException("Booking deletion failed.");
                }
            }

            // 4. Make room available
            try (PreparedStatement updateStmt =
                         conn.prepareStatement(updateRoom)) {

                updateStmt.setInt(1, roomNo);

                int updatedRows = updateStmt.executeUpdate();

                if (updatedRows == 0) {
                    throw new SQLException(
                            "Room update failed. Room number: " + roomNo
                    );
                }
            }

            // 5. Both operations successful
            conn.commit();

            System.out.println("Booking cancelled successfully!");

        } catch (SQLException e) {

            // 6. Something failed → rollback
            try {
                conn.rollback();
                System.out.println("Booking cancellation failed.");
                System.out.println("Error: " + e.getMessage());
                System.out.println("Rollback done.");

            } catch (SQLException rollbackError) {
                System.out.println("Rollback failed.");
                System.out.println("Rollback error: "
                        + rollbackError.getMessage());
            }

        } finally {

            // 7. Restore auto-commit
            conn.setAutoCommit(true);
        }

    } catch (SQLException e) {

        // Database connection / SELECT error
        System.out.println("Database error.");
        System.out.println("Error: " + e.getMessage());
    }
}
}
