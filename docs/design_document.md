# Design Document — Hotel Room Booking System

## 1. System Overview

A console-based Hotel Room Booking System built with Java and SQLite. The system manages room reservations, guest details, seasonal and loyalty-based billing, and room availability tracking.

### Architecture

```
┌─────────────────────────────────────────────────────────┐
│                      Main.java                          │
│               (Console UI / Menu Loop)                  │
│                    [Asitha]                              │
└────────────────────┬────────────────────────────────────┘
                     │ calls
┌────────────────────▼────────────────────────────────────┐
│                ReservationManager.java                   │
│          (Business Logic / Orchestration)                │
│                                                         │
│  Room CRUD pass-throughs [Ann]                          │
│  bookRoom() with billing [Anugrah]                      │
│  cancelBooking() [Asitha]                               │
└────────────────────┬────────────────────────────────────┘
                     │ calls
┌────────────────────▼────────────────────────────────────┐
│                DatabaseManager.java                      │
│            (Data Access / Persistence)                   │
│                                                         │
│  connect() + URL [Anugrah]                              │
│  initializeDatabase() [All — each adds their table]     │
│  Room DB ops [Ann]                                      │
│  Guest DB ops [Rishik]                                  │
│  Booking DB ops [Asitha]                                │
│  updateLoyaltyTier() [Anugrah]                          │
└────────────────────┬────────────────────────────────────┘
                     │ uses
┌────────────────────▼────────────────────────────────────┐
│                  hotel.model.*                           │
│              (Data Model Classes)                        │
│                                                         │
│  Room.java [Ann]                                        │
│  Guest.java [Rishik]                                    │
│  Booking.java [Asitha]                                  │
└─────────────────────────────────────────────────────────┘
                     │
              ┌──────▼──────┐
              │  SQLite DB   │
              │  hotel.db    │
              └─────────────┘
```

### Technology Stack
- **Language**: Java (JDK 8+)
- **Database**: SQLite via JDBC (`sqlite-jdbc` library in `lib/`)
- **Build**: Manual compilation with `compile.bat` and `run.bat`

---

## 2. Database Schema

### Table: `rooms` (Owner: Ann)

| Column      | Type    | Constraints                | Description                        |
|-------------|---------|----------------------------|------------------------------------|
| room_no     | INTEGER | PRIMARY KEY                | Unique room number                 |
| room_type   | TEXT    | NOT NULL                   | e.g., single, double, deluxe       |
| base_price  | REAL    | NOT NULL                   | Price per night (Rs.)              |
| available   | INTEGER | DEFAULT 1 NOT NULL         | 1 = available, 0 = booked          |

### Table: `guests` (Owner: Rishik)

| Column        | Type    | Constraints                  | Description                         |
|---------------|---------|------------------------------|-------------------------------------|
| guest_id      | INTEGER | PRIMARY KEY AUTOINCREMENT    | Auto-generated guest ID             |
| name          | TEXT    | NOT NULL                     | Guest full name                     |
| id_proof      | TEXT    | NOT NULL                     | Aadhar ID or similar                |
| contact       | TEXT    | NOT NULL                     | Phone number                        |
| loyalty_tier  | TEXT    | DEFAULT 'NONE' NOT NULL      | NONE, SILVER, or GOLD               |
| booking_count | INTEGER | DEFAULT 0 NOT NULL           | Total bookings made                 |

### Table: `bookings` (Owner: Asitha)

| Column     | Type    | Constraints                              | Description                  |
|------------|---------|------------------------------------------|------------------------------|
| booking_id | INTEGER | PRIMARY KEY AUTOINCREMENT                | Auto-generated booking ID    |
| guest_id   | INTEGER | NOT NULL, FOREIGN KEY → guests(guest_id) | Reference to the guest       |
| room_no    | INTEGER | NOT NULL, FOREIGN KEY → rooms(room_no)   | Reference to the room        |
| check_in   | TEXT    | NOT NULL                                 | Check-in date (yyyy-MM-dd)   |
| check_out  | TEXT    | NOT NULL                                 | Check-out date (yyyy-MM-dd)  |
| bill       | REAL    | NOT NULL                                 | Final calculated bill (Rs.)  |

---

## 3. Class Design

### 3.1 `Room` (Owner: Ann)

Represents a hotel room.

| Attribute   | Type    | Description                   |
|-------------|---------|-------------------------------|
| roomNo      | int     | Unique room number            |
| roomType    | String  | single / double / deluxe      |
| basePrice   | double  | Price per night               |
| available   | boolean | Availability status           |

**Constructor**: `Room(int roomNo, String roomType, double basePrice, boolean available)`

**Methods**:
| Method          | Returns  | Description                           |
|-----------------|----------|---------------------------------------|
| getRoomNo()     | int      | Returns room number                   |
| getRoomType()   | String   | Returns room type                     |
| getBasePrice()  | double   | Returns base price per night          |
| isAvailable()   | boolean  | Returns availability status           |
| toString()      | String   | Formatted room display string         |

---

### 3.2 `Guest` (Owner: Rishik)

Represents a hotel guest.

| Attribute     | Type   | Description                       |
|---------------|--------|-----------------------------------|
| guestId       | int    | Auto-generated ID                 |
| name          | String | Full name                         |
| idProof       | String | Aadhar ID                         |
| contact       | String | Phone number                      |
| loyaltyTier   | String | NONE, SILVER, or GOLD             |
| bookingCount  | int    | Number of bookings made           |

**Constructor**: `Guest(int guestId, String name, String idProof, String contact, String loyaltyTier, int bookingCount)`

**Methods**:
| Method            | Returns | Description                         |
|-------------------|---------|-------------------------------------|
| getGuestId()      | int     | Returns guest ID                    |
| getName()         | String  | Returns name                        |
| getIdProof()      | String  | Returns ID proof                    |
| getContact()      | String  | Returns contact number              |
| getLoyaltyTier()  | String  | Returns loyalty tier                |
| getBookingCount() | int     | Returns number of bookings          |
| toString()        | String  | Formatted guest display string      |

---

### 3.3 `Booking` (Owner: Asitha)

Represents a room reservation.

| Attribute  | Type   | Description                      |
|------------|--------|----------------------------------|
| bookingId  | int    | Auto-generated booking ID        |
| guestId    | int    | Reference to the guest           |
| roomNo     | int    | Reference to the room            |
| checkIn    | String | Check-in date (yyyy-MM-dd)       |
| checkOut   | String | Check-out date (yyyy-MM-dd)      |
| bill       | double | Final calculated bill amount     |

**Constructor**: `Booking(int bookingId, int guestId, int roomNo, String checkIn, String checkOut, double bill)`

**Methods**:
| Method      | Returns | Description                         |
|-------------|---------|-------------------------------------|
| toString()  | String  | Formatted booking display string    |

---

### 3.4 `DatabaseManager` (Shared — see owners per method)

Centralized database access layer. All SQL operations go through this class.

| Method | Owner | Description |
|--------|-------|-------------|
| `connect()` | Anugrah | Returns a JDBC `Connection` to `database/hotel.db` |
| `initializeDatabase()` | All (each adds their table) | Creates all 3 tables if not exists |
| `getAvailableRooms()` | Ann | Returns `ArrayList<Room>` of available rooms |
| `getRoom(int roomNo)` | Ann | Returns a specific `Room` by number |
| `addRoom(int, String, double)` | Ann | Inserts a new room |
| `removeRoom(int)` | Ann | Deletes a room (only if available) |
| `updateRoom(int, String, double)` | Ann | Updates room type and price |
| `addGuest(String, String, String)` | Rishik | Inserts a new guest |
| `getAllGuests()` | Rishik | Returns `ArrayList<Guest>` of all guests |
| `getGuest(int guestId)` | Rishik | Returns a specific `Guest` by ID |
| `bookRoom(int, int, String, String, double)` | Asitha | Inserts booking, updates room availability, increments booking count (transactional). **Must call `updateLoyaltyTier(conn, guestId)` after commit.** |
| `updateLoyaltyTier(Connection, int)` | Anugrah | Checks booking count and upgrades tier (3+ → SILVER, 7+ → GOLD) |
| `getAllBookings()` | Asitha | Returns `ArrayList<Booking>` of all bookings |
| `cancelBooking(int)` | Asitha | Deletes booking, makes room available again (transactional) |

---

### 3.5 `ReservationManager` (Shared — see owners per method)

Business logic layer that orchestrates operations between the UI and database.

| Method | Owner | Description |
|--------|-------|-------------|
| `addRoom(int, String, double)` | Ann | Delegates to `DatabaseManager.addRoom()` |
| `removeRoom(int)` | Ann | Delegates to `DatabaseManager.removeRoom()` |
| `updateRoom(int, String, double)` | Ann | Delegates to `DatabaseManager.updateRoom()` |
| `getAvailableRooms(String)` | Ann | Fetches and filters rooms by type |
| `bookRoom(int, int, String, String)` | Anugrah | Validates guest/room, calculates bill with surcharge & discount, calls `DatabaseManager.bookRoom()` |
| `cancelBooking(int)` | Asitha | Delegates to `DatabaseManager.cancelBooking()` |

---

### 3.6 `Main` (Owner: Asitha)

Console UI with the main menu loop.

**Menu Options**:
1. View available rooms
2. Register guest
3. View guests
4. Book room
5. View bookings
6. Manage rooms (sub-menu: add / remove / update / back)
7. Cancel booking
8. Exit

---

## 4. Billing Logic (Owner: Anugrah)

The billing calculation is performed inside `ReservationManager.bookRoom()` and follows this formula:

### Step 1: Base Amount
```
baseAmount = numberOfNights × room.getBasePrice()
```
Where `numberOfNights = ChronoUnit.DAYS.between(checkIn, checkOut)`

### Step 2: Seasonal Surcharge
A 10% surcharge is applied during peak months:
- **January** (month == 1)
- **May** (month == 5)
- **December** (month == 12)

```
if (month == 1 || month == 5 || month == 12):
    surcharge = baseAmount × 0.10
else:
    surcharge = 0

subtotal = baseAmount + surcharge
```

### Step 3: Loyalty Discount
A discount is applied based on the guest's loyalty tier:

| Tier   | Discount | Qualification        |
|--------|----------|----------------------|
| NONE   | 0%       | Default              |
| SILVER | 5%       | 3+ bookings          |
| GOLD   | 10%      | 7+ bookings          |

```
if tier == GOLD:
    discount = subtotal × 0.10
else if tier == SILVER:
    discount = subtotal × 0.05
else:
    discount = 0

finalBill = subtotal - discount
```

### Step 4: Loyalty Tier Upgrade
After a booking is successfully recorded, `updateLoyaltyTier()` checks the guest's updated `booking_count` and promotes their tier if they cross a threshold:
- `booking_count >= 3` → SILVER
- `booking_count >= 7` → GOLD

This runs inside `DatabaseManager.bookRoom()` after the transaction commits.

### Billing Example
> Guest (SILVER tier) books a Deluxe room (Rs.3000/night) for 3 nights checking in on 5th January:
> - Base: 3 × 3000 = Rs.9000
> - Surcharge (January): 9000 × 0.10 = Rs.900
> - Subtotal: Rs.9900
> - Loyalty discount (SILVER 5%): 9900 × 0.05 = Rs.495
> - **Final bill: Rs.9405**

### Date Handling
- **User input format**: `dd-MM-yyyy` (e.g., `15-08-2026`)
- **Database storage format**: `yyyy-MM-dd` (e.g., `2026-08-15`)
- Conversion happens inside `ReservationManager.bookRoom()`

---

### 4.1 Anugrah's Methods Summary

Since Anugrah's code lives inside shared classes (not a standalone model), here is a consolidated view of all methods owned by Anugrah:

#### In `DatabaseManager.java`:
| Method | Signature | Status | Description |
|--------|-----------|--------|-------------|
| `URL` | `private static final String` | ✅ Implemented | Connection string: `"jdbc:sqlite:database/hotel.db"` |
| `connect()` | `public static Connection connect() throws SQLException` | ✅ Implemented | Returns JDBC connection. Used by every other DB method. |
| `updateLoyaltyTier()` | `private static void updateLoyaltyTier(Connection conn, int guestId) throws SQLException` | ✅ Implemented | Checks `booking_count` and upgrades tier (3+ → SILVER, 7+ → GOLD). Called by Asitha's `bookRoom()`. |

#### In `ReservationManager.java`:
| Method | Signature | Status | Description |
|--------|-----------|--------|-------------|
| `inputFormatter` | `private static final DateTimeFormatter` | ✅ Implemented | Parses user input: `dd-MM-yyyy` |
| `dbFormatter` | `private static final DateTimeFormatter` | ✅ Implemented | Formats for DB storage: `yyyy-MM-dd` |
| `bookRoom()` | `public static boolean bookRoom(int guestId, int roomNo, String checkInStr, String checkOutStr)` | ✅ Implemented | Validates inputs, calculates bill (base + surcharge − discount), persists booking |

---

## 5. Work Assignment Matrix

| Team Member | Role | Files Owned | Key Responsibilities |
|---|---|---|---|
| **Anugrah** | DB Connection & Billing | `DatabaseManager.java` (partial), `ReservationManager.java` (partial) | SQLite JDBC connection, bill calculation, seasonal surcharge, loyalty discount, loyalty tier upgrade |
| **Ann** | Room Management | `Room.java`, `DatabaseManager.java` (room ops), `ReservationManager.java` (room ops) | Room model, add/remove/update/view rooms, rooms table init |
| **Rishik** | Guest Management | `Guest.java`, `DatabaseManager.java` (guest ops), `ReservationManager.java` (guest ops if needed) | Guest model, register/update/search guests, guests table init |
| **Asitha** (Leader) | Booking Operations & Main Menu | `Booking.java`, `Main.java`, `DatabaseManager.java` (booking ops) | Booking model, book/cancel/view bookings, main menu loop, bookings table init |

---

## 6. Integration Dependencies

These are the cross-team dependencies where one person's code calls another person's code:

```
Anugrah's bookRoom()
    ├── calls Ann's DatabaseManager.getRoom()
    ├── calls Rishik's DatabaseManager.getGuest()
    └── calls Asitha's DatabaseManager.bookRoom()

Asitha's DatabaseManager.bookRoom()
    └── calls Anugrah's updateLoyaltyTier()

Asitha's Main.bookRoom()
    └── calls Anugrah's ReservationManager.bookRoom()
```

---

## 7. Class Relationship Diagram

```
┌──────────┐     uses      ┌──────────┐
│   Room   │◄──────────────│ Database │
│  (Ann)   │               │ Manager  │
└──────────┘               │ (Shared) │
                           │          │
┌──────────┐     uses      │          │
│  Guest   │◄──────────────│          │
│ (Rishik) │               │          │
└──────────┘               │          │
                           │          │
┌──────────┐     uses      │          │
│ Booking  │◄──────────────│          │
│ (Asitha) │               └────┬─────┘
└──────────┘                    │
                           called by
                                │
                           ┌────▼─────────────┐
                           │  Reservation      │
                           │  Manager (Shared) │
                           └────┬──────────────┘
                                │
                           called by
                                │
                           ┌────▼─────┐
                           │   Main   │
                           │ (Asitha) │
                           └──────────┘
```
