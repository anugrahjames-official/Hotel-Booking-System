# Hotel Booking System — Web Application

## Purpose

This web application provides a browser-based interface for the Hotel Booking System. It is an **additional presentation layer** that sits on top of the existing Java console application. The underlying business logic, database, and console application remain completely unchanged.

## Architecture

```
                    BROWSER
                       │
                 HTML / CSS / JS
                       │
                     fetch()
                       │
                       ▼
              ┌─────────────────┐
              │   WebServer     │  ← NEW (HttpServer on port 8080)
              │   JsonUtil      │  ← NEW (JSON string builder)
              └────────┬────────┘
                       │
          calls existing public methods
                       │
              ┌────────┴────────┐
              │                 │
    ReservationManager    DatabaseManager     ← EXISTING (unchanged)
              │                 │
              └────────┬────────┘
                       │
                    SQLite                    ← EXISTING (unchanged)
```

The web interface and the console application share the **same Java implementation and database**.

## How to Start

1. Open a terminal in the project root directory.
2. Run:
   ```
   run-web.bat
   ```
3. The script compiles the project and starts the server.
4. Open a browser and navigate to:
   ```
   http://localhost:8080
   ```

## How to Stop

Press `Ctrl+C` in the terminal where the server is running.

## URL

```
http://localhost:8080
```

## API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/rooms` | Retrieve all rooms |
| POST | `/api/rooms` | Add a new room |
| POST | `/api/rooms/update` | Update room type and price |
| POST | `/api/rooms/delete` | Remove a room |
| GET | `/api/guests` | Retrieve all guests |
| POST | `/api/guests` | Register a new guest |
| GET | `/api/bookings` | Retrieve all bookings |
| POST | `/api/bookings` | Create a new booking |
| POST | `/api/bookings/cancel` | Cancel a booking |

### Request Format

POST requests use `application/x-www-form-urlencoded` bodies (standard HTML form encoding).

### Response Format

All API responses are JSON:

**Success:**
```json
{"success": true, "message": "Room added successfully."}
```

**Success with data:**
```json
{"success": true, "data": [...]}
```

**Failure:**
```json
{"success": false, "message": "Room already exists."}
```

### Date Conversion

The browser sends dates in `yyyy-MM-dd` format (HTML date input standard). The web server converts them to `dd-MM-yyyy` before calling `ReservationManager.bookRoom()`, which expects that format.

## Frontend Structure

```
web/
├── index.html      Single-page dashboard application
├── css/
│   └── style.css   All styling (CSS custom properties, responsive)
└── js/
    └── app.js      All frontend logic (API calls, rendering, modals)
```

The frontend is a single-page application with four sections:
- **Dashboard** — Summary statistics, recent bookings, quick actions
- **Rooms** — Room list with search, filter, add/edit/delete
- **Guests** — Guest list with search, register new guests
- **Bookings** — Booking list with search, create/cancel bookings

## Backend Structure

```
src/hotel/web/
├── WebServer.java   HTTP server, API routing, static file serving
└── JsonUtil.java    JSON string escaping and object serialization
```

## How the Web Layer Calls Existing Code

The web server delegates all business operations to the existing Java classes:

| Operation | Existing Method Called |
|-----------|----------------------|
| Add Room | `ReservationManager.addRoom(int, String, double)` |
| Update Room | `ReservationManager.updateRoom(int, String, double)` |
| Remove Room | `ReservationManager.removeRoom(int)` |
| List Rooms | Direct SQL via `DatabaseManager.connect()` |
| Register Guest | `DatabaseManager.addGuest(String, String, String)` |
| List Guests | `DatabaseManager.getAllGuests()` |
| Create Booking | `ReservationManager.bookRoom(int, int, String, String)` |
| Cancel Booking | `ReservationManager.cancelBooking(int)` |
| List Bookings | Direct SQL via `DatabaseManager.connect()` |

### Why Direct SQL for Rooms and Bookings

Two read-only SQL queries exist in the web layer because:

1. **All Rooms**: `DatabaseManager.getAvailableRooms()` only returns rooms where `available = 1`. The web UI needs all rooms including booked ones.

2. **Booking Details**: `Booking.java` has no getter methods (all fields are private, only `toString()` is public). The web layer cannot serialize `Booking` objects to JSON through the public API.

These are minimal, read-only queries that do not modify any data.

### Mutation Verification

Since several existing methods (`addRoom`, `removeRoom`, `updateRoom`, `cancelBooking`) return `void` and report results only through `System.out.println()`, the web layer uses **before/after database state verification**:

1. Capture relevant state before the operation.
2. Call the existing method.
3. Re-query the database.
4. Compare the resulting state.
5. Only return `success: true` if the expected change is confirmed.

## How It Uses the Existing Database

The web application uses the same SQLite database at `database/hotel.db`. The database is initialized using the existing `DatabaseManager.initializeDatabase()` method. No schema changes are made.

## Troubleshooting

### Port 8080 already in use
Close the application using that port, or stop any previously running instance of the web server.

### Web directory not found
Run `run-web.bat` from the project root directory (the directory containing the `web/` folder).

### Database not found
Run the application from the project root so the relative path `database/hotel.db` resolves correctly.

### SQLite driver not found
Ensure `lib/sqlite-jdbc-3.53.2.0.jar` is present and the classpath in `run-web.bat` is correct.

### Compilation errors
Ensure a compatible Java Development Kit (JDK) is installed and `javac` is available on the system PATH.

