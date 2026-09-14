package hotel;

import hotel.model.Room;
import hotel.model.Guest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class ReservationManager {

    // Use dd-MM-yyyy format for user input
    private static final DateTimeFormatter inputFormatter =
            DateTimeFormatter.ofPattern("dd-MM-uuuu")
                    .withResolverStyle(
                            java.time.format.ResolverStyle.STRICT);

    // Use yyyy-MM-dd format for database storage
    private static final DateTimeFormatter dbFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");


    // ================= ROOM INVENTORY OPERATIONS =================

    // Adds a new room
    public static void addRoom(
            int roomNo,
            String roomType,
            double basePrice) {

        DatabaseManager.addRoom(
                roomNo,
                roomType,
                basePrice);
    }


    // Removes an existing room
    public static void removeRoom(int roomNo) {

        DatabaseManager.removeRoom(roomNo);
    }


    // Updates room type and price
    public static void updateRoom(
            int roomNo,
            String roomType,
            double basePrice) {

        DatabaseManager.updateRoom(
                roomNo,
                roomType,
                basePrice);
    }


    // Retrieves available rooms
    // Optionally filtered by room type
    public static void getAvailableRooms(String typeFilter) {

        ArrayList<Room> rooms =
                DatabaseManager.getAvailableRooms();

        String normalizedTypeFilter =
                typeFilter == null
                        ? null
                        : typeFilter.trim();

        boolean found = false;

        for (Room room : rooms) {

            if (normalizedTypeFilter == null
                    || normalizedTypeFilter.isEmpty()
                    || room.getRoomType()
                            .equalsIgnoreCase(normalizedTypeFilter)) {

                System.out.println(room);
                System.out.println("--------------------");

                found = true;
            }
        }

        if (!found) {
            System.out.println("No available rooms found.");
        }
    }


    // ================= BOOKING OPERATIONS =================

    /**
     * Processes a room booking request.
     *
     * @return true if booking is successful,
     *         false otherwise
     */
    public static boolean bookRoom(
            int guestId,
            int roomNo,
            String checkInStr,
            String checkOutStr) {

        try {

            // 1. Verify guest exists
            Guest guest =
                    DatabaseManager.getGuest(guestId);

            if (guest == null) {

                System.out.println("Guest not found.");

                return false;
            }


            // 2. Verify room exists and is available
            Room room =
                    DatabaseManager.getRoom(roomNo);

            if (room == null || !room.isAvailable()) {

                System.out.println(
                        "Room not available or doesn't exist.");

                return false;
            }


            // 3. Parse and validate dates
            LocalDate checkIn =
                    LocalDate.parse(
                            checkInStr,
                            inputFormatter);

            LocalDate checkOut =
                    LocalDate.parse(
                            checkOutStr,
                            inputFormatter);


            // 4. Calculate number of nights
            long nights =
                    ChronoUnit.DAYS.between(
                            checkIn,
                            checkOut);

            if (nights <= 0) {

                System.out.println(
                        "Check-out must be after check-in.");

                return false;
            }


            // 5. Calculate base amount
            double baseAmount =
                    nights * room.getBasePrice();


            // 6. Peak season surcharge
            // January, May and December
            double surcharge = 0;

            int month =
                    checkIn.getMonthValue();

            if (month == 1
                    || month == 5
                    || month == 12) {

                surcharge =
                        baseAmount * 0.10;
            }


            // 7. Calculate subtotal
            double subtotal =
                    baseAmount + surcharge;


            // 8. Loyalty discount
            double discount = 0;

            if ("GOLD".equalsIgnoreCase(
                    guest.getLoyaltyTier())) {

                discount =
                        subtotal * 0.10;

            } else if ("SILVER".equalsIgnoreCase(
                    guest.getLoyaltyTier())) {

                discount =
                        subtotal * 0.05;
            }


            // 9. Calculate final bill
            double finalBill =
                    subtotal - discount;


            // 10. Convert dates to database format
            String dbIn =
                    checkIn.format(dbFormatter);

            String dbOut =
                    checkOut.format(dbFormatter);


            // 11. Save booking
            // DatabaseManager now returns true/false
            return DatabaseManager.bookRoom(
                    guestId,
                    roomNo,
                    dbIn,
                    dbOut,
                    finalBill);


        } catch (Exception e) {

            System.out.println(
                    "Error processing booking: "
                    + "please check your inputs.");

            return false;
        }
    }


    // Cancel booking
    public static void cancelBooking(int bookingId) {

        DatabaseManager.cancelBooking(bookingId);
    }
}

