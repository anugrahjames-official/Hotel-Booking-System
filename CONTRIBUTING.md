# Contributing Guide — Hotel Room Booking System

## Team Members & Assignments

| Member | Assignment |
|---|---|
| **Anugrah** | SQLite JDBC connection (`DatabaseManager.connect()`), bill calculation, seasonal surcharge, loyalty discount, loyalty tier upgrade (`updateLoyaltyTier()`) |
| **Ann** | `Room.java` model, rooms table init, add/remove/update/view rooms |
| **Rishik** | `Guest.java` model, guests table init, add/update/search guest details |
| **Asitha** (Leader) | `Booking.java` model, `Main.java` menu loop, bookings table init, book/cancel/view bookings, check availability |

---

## Project Structure

```
Hotel-Booking-System/
├── src/hotel/
│   ├── Main.java                  ← Asitha
│   ├── ReservationManager.java    ← Shared (see method owners)
│   ├── DatabaseManager.java       ← Shared (see method owners)
│   └── model/
│       ├── Room.java              ← Ann
│       ├── Guest.java             ← Rishik
│       └── Booking.java           ← Asitha
├── lib/
│   └── sqlite-jdbc-3.49.1.0.jar  ← SQLite JDBC driver
├── database/                      ← hotel.db created at runtime
├── docs/
│   ├── Project-Topic.md           ← Assignment brief
│   ├── design_document.md         ← Full design doc
│   ├── test_cases.md              ← Test cases (to be written)
│   └── user_manual.md             ← User manual (to be written)
├── compile.bat
├── run.bat
└── .gitignore
```

---

## Setup Instructions

### Prerequisites
- Java JDK 8 or higher installed
- `javac` and `java` on your system PATH

### Compile
```batch
javac -cp "lib/*" -d out src/hotel/model/*.java src/hotel/*.java
```

### Run
```batch
java -cp "out;lib/*" hotel.Main
```

Or use the provided batch files:
- `compile.bat` — compiles all source files
- `run.bat` — runs the application

---

## What You Need to Implement

### Anugrah — Billing, DB Connection & Loyalty

#### 1. `src/hotel/DatabaseManager.java` ✅ (Already implemented)

- **`private static final String URL`** — SQLite connection string: `"jdbc:sqlite:database/hotel.db"`
- **`public static Connection connect()`** — Returns a JDBC `Connection` to the SQLite database. All other DB methods depend on this.
- **`private static void updateLoyaltyTier(Connection conn, int guestId)`** — Called by Asitha's `bookRoom()` after a booking transaction commits. Checks the guest's `booking_count` and upgrades their tier:
  - 3+ bookings → `SILVER`
  - 7+ bookings → `GOLD`
  - Prints congratulations message on upgrade

#### 2. `src/hotel/ReservationManager.java` ✅ (Already implemented)

- **`public static boolean bookRoom(int guestId, int roomNo, String checkInStr, String checkOutStr)`** — The core booking + billing method:
  1. Verifies guest exists (calls Rishik's `DatabaseManager.getGuest()`)
  2. Verifies room is available (calls Ann's `DatabaseManager.getRoom()`)
  3. Parses dates from `dd-MM-yyyy` to `yyyy-MM-dd`
  4. Calculates base amount: `nights × basePrice`
  5. Applies 10% seasonal surcharge for January, May, December
  6. Applies loyalty discount: 10% for GOLD, 5% for SILVER
  7. Calls Asitha's `DatabaseManager.bookRoom()` to persist

#### Dependencies on teammates
- **Ann** must implement `Room.java` with `getBasePrice()` and `isAvailable()` getters
- **Rishik** must implement `Guest.java` with `getLoyaltyTier()` and `getBookingCount()` getters
- **Rishik** must implement `DatabaseManager.getGuest(int guestId)`
- **Ann** must implement `DatabaseManager.getRoom(int roomNo)`
- **Asitha** must implement `DatabaseManager.bookRoom(int, int, String, String, double)`

---

### Ann — Room Management

#### 1. `src/hotel/model/Room.java`
Create the Room model class with these exact specifications:

```java
// Fields: int roomNo, String roomType, double basePrice, boolean available
// Constructor: Room(int roomNo, String roomType, double basePrice, boolean available)
// Getters: getRoomNo(), getRoomType(), getBasePrice(), isAvailable()
// Override: toString() → "room 101 (deluxe) - Rs.3000.0 - available"
```

#### 2. `src/hotel/DatabaseManager.java`
Uncomment and implement these methods (look for `// TODO (Ann)` comments):

- **`initializeDatabase()`** — Add the rooms table creation SQL:
  ```sql
  CREATE TABLE IF NOT EXISTS rooms (
      room_no INTEGER PRIMARY KEY,
      room_type TEXT NOT NULL,
      base_price REAL NOT NULL,
      available INTEGER DEFAULT 1 NOT NULL
  )
  ```

- **`getAvailableRooms()`** — Query `SELECT * FROM rooms WHERE available = 1`, return `ArrayList<Room>`
- **`getRoom(int roomNo)`** — Query by room_no, return a single `Room` or `null`
- **`addRoom(int roomNo, String roomType, double basePrice)`** — Insert new room
- **`removeRoom(int roomNo)`** — Delete room (only if `available = 1`, refuse if booked)
- **`updateRoom(int roomNo, String roomType, double basePrice)`** — Update type and price

#### 3. `src/hotel/ReservationManager.java`
Uncomment and implement these methods (look for `// TODO (Ann)` comments):

- **`addRoom(int, String, double)`** — Delegates to `DatabaseManager.addRoom()`
- **`removeRoom(int)`** — Delegates to `DatabaseManager.removeRoom()`
- **`updateRoom(int, String, double)`** — Delegates to `DatabaseManager.updateRoom()`
- **`getAvailableRooms(String typeFilter)`** — Calls `DatabaseManager.getAvailableRooms()`, filters by type if provided

---

### Rishik — Guest Management

#### 1. `src/hotel/model/Guest.java`
Create the Guest model class with these exact specifications:

```java
// Fields: int guestId, String name, String idProof, String contact,
//         String loyaltyTier, int bookingCount
// Constructor: Guest(int guestId, String name, String idProof, String contact,
//                    String loyaltyTier, int bookingCount)
// Getters: getGuestId(), getName(), getIdProof(), getContact(),
//          getLoyaltyTier(), getBookingCount()
// Override: toString() → "guest 1: John (contact: 9876543210, tier: NONE, bookings: 0)"
```

> **IMPORTANT**: `getLoyaltyTier()` and `getBookingCount()` are used by Anugrah's billing and tier upgrade logic. Make sure these getters exist exactly as specified.

#### 2. `src/hotel/DatabaseManager.java`
Uncomment and implement these methods (look for `// TODO (Rishik)` comments):

- **`initializeDatabase()`** — Add the guests table creation SQL:
  ```sql
  CREATE TABLE IF NOT EXISTS guests (
      guest_id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      id_proof TEXT NOT NULL,
      contact TEXT NOT NULL,
      loyalty_tier TEXT DEFAULT 'NONE' NOT NULL,
      booking_count INTEGER DEFAULT 0 NOT NULL
  )
  ```

- **`addGuest(String name, String idProof, String contact)`** — Insert new guest
- **`getAllGuests()`** — Return `ArrayList<Guest>` of all guests
- **`getGuest(int guestId)`** — Return a specific `Guest` by ID, or `null` if not found

---

### Asitha — Booking Operations & Main Menu

#### 1. `src/hotel/model/Booking.java`
Create the Booking model class with these exact specifications:

```java
// Fields: int bookingId, int guestId, int roomNo, String checkIn,
//         String checkOut, double bill
// Constructor: Booking(int bookingId, int guestId, int roomNo,
//                      String checkIn, String checkOut, double bill)
// Override: toString() → "booking 1 | guest id: 1 | room no: 101 | dates: 2026-01-15 to 2026-01-18 | bill: Rs.9405.0"
```

#### 2. `src/hotel/DatabaseManager.java`
Uncomment and implement these methods (look for `// TODO (Asitha)` comments):

- **`initializeDatabase()`** — Add the bookings table creation SQL:
  ```sql
  CREATE TABLE IF NOT EXISTS bookings (
      booking_id INTEGER PRIMARY KEY AUTOINCREMENT,
      guest_id INTEGER NOT NULL,
      room_no INTEGER NOT NULL,
      check_in TEXT NOT NULL,
      check_out TEXT NOT NULL,
      bill REAL NOT NULL,
      FOREIGN KEY(guest_id) REFERENCES guests(guest_id),
      FOREIGN KEY(room_no) REFERENCES rooms(room_no)
  )
  ```

- **`bookRoom(int guestId, int roomNo, String checkIn, String checkOut, double bill)`**:
  Must be **transactional** (use `conn.setAutoCommit(false)` and `conn.commit()`):
  1. Insert into bookings table
  2. Update room: `SET available = 0`
  3. Update guest: `SET booking_count = booking_count + 1`
  4. Commit
  5. **Call `updateLoyaltyTier(conn, guestId)`** after commit — this is Anugrah's method, already implemented
  6. Commit again for the tier update

- **`getAllBookings()`** — Return `ArrayList<Booking>` of all bookings
- **`cancelBooking(int bookingId)`**:
  Must be **transactional**:
  1. Find room_no from the booking
  2. Delete the booking
  3. Update room: `SET available = 1`

#### 3. `src/hotel/Main.java`
Build the console menu loop:

```
Menu:
1 view available rooms
2 register guest
3 view guests
4 book room
5 view bookings
6 manage rooms (sub-menu: add / remove / update / back)
7 cancel booking
8 exit
```

- Call `DatabaseManager.initializeDatabase()` at startup
- Route each option to the appropriate `ReservationManager` method
- For booking: collect guest ID, room number, check-in/check-out dates (format: `dd-MM-yyyy`) and call `ReservationManager.bookRoom()`

---

## Integration Dependencies

Your code calls someone else's code in these places:

```
Anugrah's ReservationManager.bookRoom()
    ├── calls Ann's    → DatabaseManager.getRoom()
    ├── calls Rishik's → DatabaseManager.getGuest()
    └── calls Asitha's → DatabaseManager.bookRoom()

Asitha's DatabaseManager.bookRoom()
    └── calls Anugrah's → updateLoyaltyTier()

Asitha's Main.bookRoom()
    └── calls Anugrah's → ReservationManager.bookRoom()
```

**What this means**: The project won't fully compile until everyone's parts are merged. This is expected. Focus on writing your methods correctly and the integration will work.

---

## Git Workflow

### Branching
Each member works on their own branch:
```
main
├── anugrah/billing
├── ann/room-management
├── rishik/guest-management
└── asitha/booking-operations
```

### Steps
1. Pull the latest `main`
2. Create your branch: `git checkout -b yourname/feature`
3. Implement your assigned methods
4. Commit with clear messages: `Add room CRUD operations in DatabaseManager`
5. Push and create a Pull Request to `main`
6. Get at least one teammate's review before merging

### Merge Order (Recommended)
1. **Ann** (Room model + DB ops) — no dependencies
2. **Rishik** (Guest model + DB ops) — no dependencies
3. **Anugrah** (connect + billing + tier) — depends on Room & Guest models
4. **Asitha** (Booking + Main + DB booking ops) — depends on all above

---

## SQLite JDBC

The SQLite JDBC driver (`sqlite-jdbc-3.49.1.0.jar`) is already in the `lib/` folder. No additional installation needed.

The database file `database/hotel.db` is auto-created at runtime when `initializeDatabase()` runs. It is excluded from Git via `.gitignore`.
