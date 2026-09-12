package hotel;

import hotel.model.Guest;
import hotel.model.Booking;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        // Initialize database and tables
        DatabaseManager.initializeDatabase();

        System.out.println("======================================");
        System.out.println("     WELCOME TO HOTEL BOOKING SYSTEM");
        System.out.println("======================================");

        boolean running = true;

        while (running) {

            displayMenu();

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    viewAvailableRooms();
                    break;

                case "2":
                    registerGuest();
                    break;

                case "3":
                    viewAllGuests();
                    break;

                case "4":
                    bookRoom();
                    break;

                case "5":
                    viewAllBookings();
                    break;

                case "6":
                    cancelBooking();
                    break;

                case "7":
                    addRoom();
                    break;

                case "8":
                    removeRoom();
                    break;

                case "9":
                    updateRoom();
                    break;

                case "0":
                    running = false;
                    System.out.println("Thank you for using the Hotel Booking System!");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }

            System.out.println();
        }

        scanner.close();
    }


    // Display main menu
    private static void displayMenu() {

        System.out.println("\n========== MAIN MENU ==========");
        System.out.println("1. View Available Rooms");
        System.out.println("2. Register Guest");
        System.out.println("3. View All Guests");
        System.out.println("4. Book Room");
        System.out.println("5. View All Bookings");
        System.out.println("6. Cancel Booking");
        System.out.println("7. Add Room");
        System.out.println("8. Remove Room");
        System.out.println("9. Update Room");
        System.out.println("0. Exit");
        System.out.println("===============================");
    }


    // View available rooms
    private static void viewAvailableRooms() {

        System.out.print("Enter room type to filter (or press Enter for all): ");
        String type = scanner.nextLine();

        ReservationManager.getAvailableRooms(type);
    }


    // Register a new guest
    private static void registerGuest() {

        System.out.println("\n---------- Register Guest ----------");

        System.out.print("Enter guest name: ");
        String name = scanner.nextLine();

        System.out.print("Enter ID proof: ");
        String idProof = scanner.nextLine();

        System.out.print("Enter contact number: ");
        String contact = scanner.nextLine();

        DatabaseManager.addGuest(name, idProof, contact);
    }


    // View all guests
    private static void viewAllGuests() {

        System.out.println("\n---------- All Guests ----------");

        try {

            ArrayList<Guest> guests = DatabaseManager.getAllGuests();

            if (guests.isEmpty()) {
                System.out.println("No guests found.");
                return;
            }

            for (Guest guest : guests) {
                System.out.println(guest);
            }

        } catch (SQLException e) {

            System.out.println("Error fetching guests: "
                    + e.getMessage());
        }
    }


    // Book a room
    private static void bookRoom() {
        System.out.println("\n---------- Book Room ----------");

        try {

            System.out.print("Enter guest ID: ");
            int guestId = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter room number: ");
            int roomNo = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter check-in date (dd-MM-yyyy): ");
            String checkIn = scanner.nextLine();

            System.out.print("Enter check-out date (dd-MM-yyyy): ");
            String checkOut = scanner.nextLine();

            boolean success = ReservationManager.bookRoom(
                    guestId,
                    roomNo,
                    checkIn,
                    checkOut
            );

            if (success) {
                System.out.println("Booking completed successfully.");
            }

        } catch (NumberFormatException e) {

            System.out.println("Please enter valid numbers.");

        } catch (Exception e) {

            System.out.println("Error booking room: "
                    + e.getMessage());
        }
    }


    // View all bookings
    private static void viewAllBookings() {

        System.out.println("\n---------- All Bookings ----------");

        ArrayList<Booking> bookings =
                DatabaseManager.getAllBookings();

        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }

        for (Booking booking : bookings) {
            System.out.println(booking);
        }
    }


    // Cancel a booking
    private static void cancelBooking() {

        System.out.println("\n---------- Cancel Booking ----------");

        try {

            System.out.print("Enter booking ID: ");
            int bookingId = Integer.parseInt(scanner.nextLine());

            ReservationManager.cancelBooking(bookingId);

        } catch (NumberFormatException e) {

            System.out.println("Please enter a valid booking ID.");
        }
    }


    // Add a new room
    private static void addRoom() {

        System.out.println("\n---------- Add Room ----------");

        try {

            System.out.print("Enter room number: ");
            int roomNo = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter room type: ");
            String roomType = scanner.nextLine();

            System.out.print("Enter base price: ");
            double basePrice =
                    Double.parseDouble(scanner.nextLine());

            ReservationManager.addRoom(
                    roomNo,
                    roomType,
                    basePrice
            );

        } catch (NumberFormatException e) {

            System.out.println("Please enter valid numbers.");
        }
    }


    // Remove a room
    private static void removeRoom() {

        System.out.println("\n---------- Remove Room ----------");

        try {

            System.out.print("Enter room number: ");
            int roomNo = Integer.parseInt(scanner.nextLine());

            ReservationManager.removeRoom(roomNo);

        } catch (NumberFormatException e) {

            System.out.println("Please enter a valid room number.");
        }
    }


    // Update room
    private static void updateRoom() {

        System.out.println("\n---------- Update Room ----------");

        try {

            System.out.print("Enter room number: ");
            int roomNo = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter new room type: ");
            String roomType = scanner.nextLine();

            System.out.print("Enter new base price: ");
            double basePrice =
                    Double.parseDouble(scanner.nextLine());

            ReservationManager.updateRoom(
                    roomNo,
                    roomType,
                    basePrice
            );

        } catch (NumberFormatException e) {

            System.out.println("Please enter valid numbers.");
        }
    }
}
