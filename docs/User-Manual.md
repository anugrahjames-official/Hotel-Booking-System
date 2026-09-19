# User Manual: Hotel Room Booking System

---

## Table of Contents
1. [What is This System?](#1-what-is-this-system)
2. [Prerequisites](#2-prerequisites)
3. [Installation and Running](#3-installation-and-running)
4. [Using the Console System](#4-using-the-console-system)
5. [Using the Web Application](#5-using-the-web-application)
6. [Understanding the Bill](#6-understanding-the-bill)
7. [Loyalty Tiers](#7-loyalty-tiers)
8. [Common Errors and Troubleshooting](#8-common-errors-and-troubleshooting)

---

## 1. What is This System?
The Hotel Room Booking System manages room availability, guest registration, bookings, and auto-calculates bills with loyalty and seasonal discounts. It features both a **Command-Line Interface (CLI)** and a **Web Dashboard**. All data is persisted in a local SQLite database (`database/hotel.db`).

---

## 2. Prerequisites
- **Java Development Kit (JDK):** Version 17+ (JDK 21+ recommended).
- **SQLite JDBC Driver:** Placed in the `lib` folder (already provided).

---

## 3. Installation and Running

### Option 1: Running the Console Application
1. Open a terminal/Command Prompt at the project root folder.
2. **Compile the files:**
   ```bash
   javac -d out src/hotel/model/*.java src/hotel/web/*.java src/hotel/*.java
   ```
3. **Run the program:**
   - **Windows:** `java -cp "out;lib\sqlite-jdbc-3.53.2.0.jar" hotel.Main`
   - **Mac/Linux:** `java -cp "out:lib/sqlite-jdbc-3.53.2.0.jar" hotel.Main`

### Option 2: Running the Web Application
1. Open a terminal in the project root directory.
2. Run the provided script:
   - **Windows:** `run-web.bat`
3. Open a browser and visit: `http://localhost:8080`
4. Press `Ctrl+C` in the terminal to stop the server.

---

## 4. Using the Console System
Upon running `Main`, you will see a Main Menu:
1.  **View Available Rooms:** See all rooms that are free to book. Filter by type if desired.
2.  **Register Guest:** Add a guest's name, ID proof, and contact number. Take note of the assigned Guest ID.
3.  **View All Guests:** See registered guests, their total bookings, and current loyalty tiers.
4.  **Book Room:** Provide Guest ID, Room Number, Check-In and Check-Out dates (`dd-MM-yyyy`). The system will calculate the final bill.
5.  **View All Bookings:** See all past and active bookings.
6.  **Cancel Booking:** Enter a booking ID to cancel it. The room becomes available again.
7.  **Add Room:** Add new rooms to the inventory (e.g., Room 101, Single, ₹1000).
8.  **Remove Room:** Delete a room (fails if the room is currently booked or has past booking history).
9.  **Update Room:** Change a room's type or base price.
0.  **Exit:** Save and safely exit the application.

*Note: For a fresh run, no rooms exist. Add rooms using Option 7 first.*

---

## 5. Using the Web Application
The single-page web dashboard operates using the same database as the console application.
- **Dashboard tab:** View top-level hotel statistics and recent bookings.
- **Rooms tab:** View, add, update, or remove rooms visually.
- **Guests tab:** Register guests and view loyalty statuses.
- **Bookings tab:** Process new bookings visually using standard calendar date pickers.

---

## 6. Understanding the Bill
```text
Step 1: Base Amount  = Nights × Base Price
Step 2: Surcharge    = +10% of Base Amount (if checking in during Jan, May, Dec)
Step 3: Subtotal     = Base Amount + Surcharge
Step 4: Discount     = -10% for GOLD guests, -5% for SILVER guests (applied to Subtotal)
Step 5: Final Bill   = Subtotal − Discount
```

---

## 7. Loyalty Tiers
*   **NONE:** 0-2 prior bookings (0% discount)
*   **SILVER:** 3-6 prior bookings (5% discount)
*   **GOLD:** 7+ prior bookings (10% discount)
*   *Note: Tier upgrades are automatically applied after the qualifying booking is completed.*

---

## 8. Common Errors and Troubleshooting

| What You See / Error | How to Fix It |
| :--- | :--- |
| `guest not found` | Ensure Guest ID is correct or register guest first. |
| `room not available` | Verify room exists and isn't booked using View Available Rooms. |
| `cannot remove booked room` | Cancel the active booking before deleting the room. |
| `Port 8080 already in use` | Close other web apps or existing instances of `run-web.bat`. |
| Database errors | Run the application from the project root directory. |
| Dates rejected | In Console, dates must be exactly `dd-MM-yyyy` (e.g. `25-12-2026`). |
