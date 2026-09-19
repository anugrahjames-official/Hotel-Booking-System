# Design Document: Hotel Room Booking System

---

## 1. Introduction

This document describes the complete class design of the Hotel Room Booking System. The application manages room reservations, guest details, seasonal and loyalty-based billing, and room availability tracking. The system uses SQLite as its database and provides both a console-based user interface and a web-based presentation layer.

### 1.1 Purpose
This design document describes **all classes, their attributes, methods, and relationships**, including OOP concepts applied and database schemas.

### 1.2 Technology Stack
| Technology           | Role                         |
| -------------------- | ---------------------------- |
| Java (JDK)           | Programming language         |
| SQLite               | Embedded database            |
| SQLite JDBC          | Database connectivity driver |
| Built-in HTTP Server | Web Application Server       |

---

## 2. Package Structure

```
hotel/                      ← Root package
├── model/                  ← Data model classes
│   ├── Room.java
│   ├── Guest.java
│   └── Booking.java
├── web/                    ← Web application layer
│   ├── WebServer.java
│   └── JsonUtil.java
├── DatabaseManager.java    ← All database (JDBC) operations
├── ReservationManager.java ← Business logic and validation operations
└── Main.java               ← Entry point, menus, and console UI
```

| Package       | Responsibility                                                                                                                                                                                                          |
| ------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `hotel.model` | Contains data classes that represent entities (Room, Guest, Booking). These classes hold data using private fields and provide getters, demonstrating **Encapsulation**.                        |
| `hotel`       | Contains application logic: `DatabaseManager` handles SQL, `ReservationManager` coordinates logic, and `Main` handles console UI. |
| `hotel.web`   | Contains web server logic exposing application functionalities via HTTP endpoints. |

---

## 3. Class Descriptions

### 3.1 Class: `Room`
**Package:** `hotel.model`
**Purpose:** Represents a single room in the hotel.
#### Attributes
| Access    | Type      | Name        | Description                                                     |
| --------- | --------- | ----------- | --------------------------------------------------------------- |
| `private` | `int`     | `roomNo`    | Unique room number                                              |
| `private` | `String`  | `roomType`  | Type of room (e.g., Single, Double, Deluxe)                     |
| `private` | `double`  | `basePrice` | Base price per night                                            |
| `private` | `boolean` | `available` | `true` if the room can be booked                                |

#### OOP Concepts Demonstrated
*   **Encapsulation:** Attributes are private, accessed via getters.
*   **Polymorphism:** Overrides `Object.toString()`.

### 3.2 Class: `Guest`
**Package:** `hotel.model`
**Purpose:** Represents a hotel guest.
#### Attributes
| Access    | Type     | Name           | Description                                              |
| --------- | -------- | -------------- | -------------------------------------------------------- |
| `private` | `int`    | `guestId`      | Unique guest ID                                          |
| `private` | `String` | `name`         | Guest name                                               |
| `private` | `String` | `idProof`      | ID proof number                                          |
| `private` | `String` | `contact`      | Phone number                                             |
| `private` | `String` | `loyaltyTier`  | NONE, SILVER, or GOLD                                    |
| `private` | `int`    | `bookingCount` | Total number of bookings made                            |

#### OOP Concepts Demonstrated
*   **Encapsulation:** Attributes are private.
*   **Polymorphism:** Overrides `Object.toString()`.

### 3.3 Class: `Booking`
**Package:** `hotel.model`
**Purpose:** Represents a room reservation linking a Guest to a Room.
#### Attributes
| Access    | Type     | Name        | Description                                        |
| --------- | -------- | ----------- | -------------------------------------------------- |
| `private` | `int`    | `bookingId` | Unique booking ID                                  |
| `private` | `int`    | `guestId`   | ID of the guest (FK)                               |
| `private` | `int`    | `roomNo`    | Room number (FK)                                   |
| `private` | `String` | `checkIn`   | Check-in date                                      |
| `private` | `String` | `checkOut`  | Check-out date                                     |
| `private` | `double` | `bill`      | Final bill amount                                  |

#### OOP Concepts Demonstrated
*   **Association:** Many-to-one relationship with `Guest` and `Room`.

### 3.4 Class: `DatabaseManager`
**Package:** `hotel`
**Purpose:** Centralized JDBC SQLite database operations.
*   **Methods:** `initializeDatabase()`, `connect()`, CRUD for rooms, guests, bookings.
*   **OOP Concepts:** **Abstraction** (hides SQL complexity from callers).

### 3.5 Class: `ReservationManager`
**Package:** `hotel`
**Purpose:** Core business logic for inventory and bookings. Calculates final bills with peak season surcharges (Jan, May, Dec) and loyalty discounts.
*   **OOP Concepts:** **Separation of Concerns**.

### 3.6 Class: `Main`
**Package:** `hotel`
**Purpose:** Console UI and user input routing.

### 3.7 Web Classes (`WebServer`, `JsonUtil`)
**Package:** `hotel.web`
**Purpose:** Embeds a simple HTTP server handling API routing and JSON serialization to bridge the browser frontend with the Java backend.

---

## 4. Class Relationships

### 4.1 Relationship Summary
| Relationship                                   | Type                          | Description                                                                                             |
| ---------------------------------------------- | ----------------------------- | ------------------------------------------------------------------------------------------------------- |
| `Main` / `WebServer` → `ReservationManager`    | **Dependency (uses)**         | Calls business logic methods to execute operations.                                                     |
| `ReservationManager` → `DatabaseManager`       | **Dependency (uses)**         | Queries or updates tables.                                                                              |
| `DatabaseManager` → Models                     | **Dependency (creates)**      | Constructs `Room`, `Guest`, and `Booking` objects from database records.                                |
| `Booking` → `Guest` / `Room`                   | **Association (many-to-one)** | Each booking references one guest and one room.                                                         |

---

## 5. Database Schema

### 5.1 ER Diagram
```
    ┌──────────┐          ┌──────────────┐          ┌──────────┐
    │  guests  │ 1      N │   bookings   │ N      1 │  rooms   │
    │──────────│──────────│──────────────│──────────│──────────│
    │ guest_id │◄─────────│ guest_id(FK) │          │ room_no  │
    │ name     │          │ room_no(FK)  │─────────►│ room_type│
    │ id_proof │          │ check_in     │          │base_price│
    │ contact  │          │ check_out    │          │available │
    │loyalty_  │          │ bill         │          └──────────┘
    │  tier    │          │ booking_id   │
    │booking_  │          └──────────────┘
    │  count   │
    └──────────┘
```

---

## 6. Data Flow

1. **Console Interface:** User input via `Scanner` -> `Main.java` -> `ReservationManager` -> `DatabaseManager` -> SQLite.
2. **Web Interface:** Browser JS `fetch()` -> `WebServer.java` -> `ReservationManager` -> `DatabaseManager` -> SQLite -> `JsonUtil` formatting -> HTTP JSON Response -> Browser DOM updates.
