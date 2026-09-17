**We should freeze the existing implementation.** The goal is not to improve or refactor the Java project. The goal is to put a **web UI on top of what already works**, with the smallest possible addition to the repository.

I searched the current Java options specifically for a lightweight way to serve a web UI. Java's built-in `com.sun.net.httpserver.HttpServer` is designed for simple embedded HTTP servers, supports URL contexts and request handlers, and does not require bringing in a large web framework. ([Oracle Docs][1])

## The new rule for this project

### We do NOT touch:

* `Room.java`
* `Guest.java`
* `Booking.java`
* `ReservationManager.java`
* `DatabaseManager.java`
* Existing SQL
* Existing business logic
* Existing console workflows
* Existing class relationships
* Existing calculations
* Existing database structure

The current codebase becomes our **frozen core**.

The only additions should be for the web interface.

---

# 1. Final architecture

Keep this:

```text
                 EXISTING CODEBASE
                       │
                       ▼
              ┌─────────────────┐
              │ Reservation     │
              │ Manager         │
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │ Database        │
              │ Manager         │
              └────────┬────────┘
                       │
                       ▼
                    SQLite
```

Add this:

```text
                    NEW WEB UI
                       │
                 HTML / CSS / JS
                       │
                       ▼
                Java Web Server
                       │
                       ▼
              EXISTING CODEBASE
                       │
                       ▼
                    SQLite
```

So:

```text
                    Browser
                       │
                       │ HTTP
                       ▼
              ┌─────────────────┐
              │   WebServer     │  ← NEW
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │ Reservation     │  ← EXISTING
              │ Manager         │
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │ Database        │  ← EXISTING
              │ Manager         │
              └────────┬────────┘
                       │
                       ▼
                    SQLite
```

This is the architecture I recommend.

---

# 2. Why Java's built-in HTTP server

The JDK already provides:

```java
com.sun.net.httpserver.HttpServer
```

It is specifically intended as a simple embedded HTTP server. You create URL contexts and attach `HttpHandler`s to them. ([Oracle Docs][1])

That means you don't need:

* Spring Boot
* Tomcat
* Maven
* Gradle
* Node.js
* React
* Express
* complicated deployment

For your team, that is exactly what we want.

**Less technology = less stuff to learn = more time understanding the actual project.**

---

# 3. What we add

I would add only something like:

```text
Hotel-Booking-System/
│
├── src/
│   └── hotel/
│       ├── existing files...
│       │
│       └── web/
│           ├── WebServer.java
│           ├── WebHandler.java
│           └── JsonUtil.java
│
├── web/
│   ├── index.html
│   ├── rooms.html
│   ├── guests.html
│   ├── bookings.html
│   │
│   ├── css/
│   │   └── style.css
│   │
│   └── js/
│       └── app.js
│
└── existing files...
```

That's it.

No rewriting the existing classes.

---

# 4. Even simpler, we can reduce the Java addition

Potentially:

```text
src/hotel/web/
└── WebServer.java
```

and:

```text
web/
├── index.html
├── style.css
└── app.js
```

`WebServer.java` can contain several small handlers initially.

This is probably the **lowest-change approach**.

We don't need to create:

```text
controllers/
services/
repositories/
interfaces/
policies/
exceptions/
```

because **those would modify the architecture and increase everyone's learning burden.**

---

# 5. The existing `Main.java` stays untouched

This is important.

Currently:

```text
Main.java
    ↓
Console menu
    ↓
ReservationManager
```

Leave it.

Then add:

```text
WebServer.java
    ↓
Browser UI
    ↓
ReservationManager
```

So there are now two entry points:

```text
                 Hotel Booking System
                        │
              ┌─────────┴─────────┐
              │                   │
           Main.java          WebServer.java
              │                   │
           Console             Browser
              │                   │
              └─────────┬─────────┘
                        │
                Existing logic
                        │
                ReservationManager
                        │
                DatabaseManager
                        │
                     SQLite
```

This is actually a very nice thing to explain during evaluation.

---

# 6. The web server's job

The new Java class should have a very narrow responsibility:

> Receive HTTP requests, call the existing methods, and return results to the browser.

That's it.

For example:

```text
GET /api/rooms
        ↓
WebServer
        ↓
ReservationManager.listRooms()
        ↓
JSON response
```

And:

```text
POST /api/bookings
        ↓
WebServer
        ↓
ReservationManager.bookRoom(...)
        ↓
existing DatabaseManager
        ↓
JSON response
```

We are **not moving business logic into WebServer.java**.

---

# 7. The browser becomes the new console

Think of the website as a graphical replacement for:

```text
1. Room Management
2. Guest Management
3. Booking
4. Cancellation
5. View Bookings
6. Exit
```

The existing console asks:

```text
Enter room number:
Enter room type:
Enter price:
```

The website provides:

```text
Room Number
[____________]

Room Type
[ Deluxe ▼ ]

Base Price
[____________]

[ Add Room ]
```

Same functionality.

Different presentation.

---

# 8. Website pages

Keep it to **four main areas**.

## Dashboard

```text
┌────────────────────────────────────────────┐
│ Hotel Booking System                       │
├──────────┬─────────────────────────────────┤
│ Dashboard│                                 │
│ Rooms    │   Total Rooms       20          │
│ Guests   │   Available         14          │
│ Bookings │   Guests             8          │
│          │   Active Bookings    6          │
└──────────┴─────────────────────────────────┘
```

---

## Rooms

```text
ROOMS

[ + Add Room ]

Room | Type | Price | Availability
101  | Single | ₹2000 | Available
102  | Double | ₹3000 | Booked
103  | Deluxe | ₹4500 | Available
```

Buttons:

```text
Edit
Delete
```

These call the **existing room-management methods**.

---

## Guests

```text
GUESTS

[ + Add Guest ]

ID | Name | Contact | Loyalty | Bookings
1  | Anugrah | ... | GOLD | 7
2  | Alex    | ... | SILVER | 4
```

Again, no new guest logic.

---

## Bookings

This should be the main screen.

```text
BOOKINGS

[ + New Booking ]

ID | Guest | Room | Check-in | Check-out | Bill
-------------------------------------------------
1  | Anu   | 101  | 18/09    | 21/09     | ₹8910
2  | Alex  | 103  | 20/09    | 22/09     | ₹6000
```

Cancel button calls the existing cancellation functionality.

---

# 9. Booking form

This is where the website can make the project look significantly better without changing the backend.

```text
CREATE BOOKING

Guest
┌─────────────────────────────┐
│ Select Guest             ▼  │
└─────────────────────────────┘

Room
┌─────────────────────────────┐
│ Select Room              ▼  │
└─────────────────────────────┘

Check-in
┌─────────────────────────────┐
│ 18/09/2026                  │
└─────────────────────────────┘

Check-out
┌─────────────────────────────┐
│ 21/09/2026                  │
└─────────────────────────────┘

             [ Calculate / Book ]
```

The Java backend continues doing the existing calculation.

---

# 10. Don't duplicate billing logic in JavaScript

This is **very important**.

Do NOT do:

```javascript
bill = nights * price;
bill += surcharge;
bill -= discount;
```

in the frontend.

The existing Java implementation already handles billing.

The browser should only send:

```text
guest
room
check-in
check-out
```

and Java calculates the final amount.

Architecture:

```text
Browser
  │
  │ booking details
  ▼
WebServer
  │
  ▼
Existing ReservationManager
  │
  ▼
Existing billing logic
  │
  ▼
Database
```

That ensures there is only one source of truth.

---

# 11. API endpoints

Keep them simple.

### Rooms

```text
GET  /api/rooms
POST /api/rooms
PUT  /api/rooms
POST /api/rooms/delete
```

### Guests

```text
GET  /api/guests
POST /api/guests
```

### Bookings

```text
GET  /api/bookings
POST /api/bookings
POST /api/bookings/cancel
```

### Dashboard

```text
GET /api/dashboard
```

The exact endpoints can be adjusted to match the existing methods.

---

# 12. GET vs POST, simple explanation

You'll need to understand this for the viva.

### GET

Used for retrieving information.

```text
GET /api/rooms
```

means:

> Give me the rooms.

### POST

Used when sending information to perform an operation.

```text
POST /api/bookings
```

means:

> Create this booking.

You don't need to learn REST architecture deeply for this project.

---

# 13. JSON

The browser needs structured data.

For example:

```json
[
  {
    "roomNo": 101,
    "roomType": "Deluxe",
    "basePrice": 4500,
    "available": true
  }
]
```

The browser receives this and creates the table.

The existing Java model isn't changed.

The web layer simply converts the existing object data to JSON.

---

# 14. Static files

The Java server can also serve:

```text
/index.html
/style.css
/app.js
```

So the entire application can run locally:

```text
java ...
       ↓
http://localhost:8080
       ↓
Browser
```

The Java HTTP server supports registering contexts for URL paths and dispatching requests to handlers. ([Oracle Docs][1])

---

# 15. What the professor sees

Instead of:

```text
C:\> run.bat

===== HOTEL BOOKING =====

1. Add Room
2. Remove Room
3. ...
```

they see:

```text
╔══════════════════════════════════════════════╗
║              HOTEL BOOKING                   ║
║                                              ║
║  Dashboard   Rooms   Guests   Bookings       ║
╚══════════════════════════════════════════════╝
```

Then you demonstrate the exact same functionality.

That gives you the UI/UX improvement without changing the implementation everyone already knows.

---

# 16. UI should be the main area of improvement

Since you don't want to touch the core, put the effort here:

### Visual design

* professional dashboard
* consistent typography
* clean cards
* tables
* status badges
* proper spacing
* responsive layout

### UX

* dropdowns instead of manually entering IDs
* date pickers
* confirmation dialogs
* success notifications
* error messages
* search
* filters

### Presentation

* dashboard statistics
* bill preview
* booking confirmation
* guest details

These don't require changing your underlying implementation.

---

# 17. Add search and filtering only in the frontend

For example:

```text
Rooms

Search: [ Deluxe             ]

Availability:
[ All ▼ ]

Type:
[ All ▼ ]
```

If the existing backend only returns all rooms, JavaScript can filter the **already retrieved display data**.

That doesn't modify your database logic.

Same with:

```text
Bookings
[ Search guest... ]
```

This is a safe UI-only enhancement.

---

# 18. Don't add authentication

Not necessary.

You don't need:

```text
Login
JWT
Passwords
Roles
Admin authentication
```

Those would create a lot of new concepts unrelated to the project.

---

# 19. Don't add payments

No:

```text
Razorpay
Stripe
PayPal
```

The assignment only requires calculation of the final bill.

A beautiful bill summary is enough.

---

# 20. Don't add deployment complexity

For the college evaluation:

```text
localhost:8080
```

is perfectly reasonable.

You can run:

```text
run-web.bat
```

and open:

```text
http://localhost:8080
```

That's enough for demonstration.

---

# 21. We can make a new `run-web.bat`

This is a safe addition.

Existing:

```text
run.bat
```

remains untouched.

New:

```text
run-web.bat
```

does:

```text
compile
↓
start WebServer
↓
browser
```

So your original project continues to work.

---

# 22. We can even keep the current compile process

Your repository already has:

```text
compile.bat
run.bat
```

We don't need to migrate to Maven or Gradle.

That is another unnecessary change we should avoid.

---

# 23. Final repository structure

I would make the smallest possible addition:

```text
Hotel-Booking-System/
│
├── src/
│   └── hotel/
│       ├── Main.java                 EXISTING
│       ├── DatabaseManager.java      EXISTING
│       ├── ReservationManager.java   EXISTING
│       │
│       ├── model/                    EXISTING
│       │   ├── Room.java
│       │   ├── Guest.java
│       │   └── Booking.java
│       │
│       └── web/                      NEW
│           └── WebServer.java
│
├── web/                              NEW
│   ├── index.html
│   ├── css/
│   │   └── style.css
│   └── js/
│       └── app.js
│
├── run-web.bat                       NEW
│
├── compile.bat                       EXISTING
├── run.bat                            EXISTING
├── lib/                               EXISTING
├── docs/                              EXISTING
└── ...
```

That's the version I would actually recommend.

---

# 24. How the new `WebServer.java` works

Conceptually:

```java
HttpServer server =
    HttpServer.create(new InetSocketAddress(8080), 0);
```

Then:

```java
server.createContext("/", ...);
server.createContext("/api/rooms", ...);
server.createContext("/api/guests", ...);
server.createContext("/api/bookings", ...);
```

Then:

```java
server.start();
```

The JDK's `HttpServer` API is specifically built around this model of registering contexts and handlers. ([Oracle Docs][1])

The handler receives an `HttpExchange`, which represents the request and response. It provides access to the HTTP method, headers, request body and response handling. ([Oracle Docs][2])

---

# 25. What the new code should NOT contain

`WebServer.java` should **not** contain:

```text
peak season calculation
loyalty calculation
room availability rules
SQL queries
booking calculations
database schema
```

Those already belong to the existing project.

The web layer should essentially do:

```text
HTTP
 ↓
Input
 ↓
Existing method
 ↓
Output
 ↓
HTTP
```

That's it.

---

# 26. The team-learning advantage

This is exactly what you were concerned about.

Everyone already knows:

```text
Room
Guest
Booking
ReservationManager
DatabaseManager
```

Those don't change.

Now you only need to teach the team:

```text
WebServer
HTML
CSS
JavaScript
HTTP
JSON
```

instead of:

```text
new architecture
new domain model
new database
new billing system
new business logic
new OOP hierarchy
```

That's a **much smaller learning surface**.

---

# 27. What each teammate needs to understand

For the existing code:

> Everyone continues learning the same codebase.

For the web addition everyone should understand the basic request flow:

```text
Button
 ↓
JavaScript
 ↓
HTTP request
 ↓
WebServer
 ↓
Existing ReservationManager
 ↓
DatabaseManager
 ↓
SQLite
 ↓
Response
 ↓
JavaScript
 ↓
UI
```

That's manageable.

---

# 28. What counts as "web app" for your project

You don't need:

```text
Cloud deployment
Domain
HTTPS
Authentication
React
Node
Docker
AWS
```

A working application at:

```text
http://localhost:8080
```

with:

```text
HTML
CSS
JavaScript
Java backend
SQLite
```

is a genuine web application for your demonstration.

---

# 39. The project-development sequence

### STEP 1

Add:

```text
src/hotel/web/WebServer.java
```

### STEP 2

Create:

```text
web/index.html
web/css/style.css
web/js/app.js
```

### STEP 3

Make the Java server serve `index.html`.

### STEP 4

Make:

```text
GET /api/rooms
```

work.

### STEP 5

Display rooms in the browser.

### STEP 6

Add guest API.

### STEP 7

Add booking API.

### STEP 8

Add cancellation.

### STEP 9

Build dashboard.

### STEP 10

Polish UI/UX.

### STEP 11

Create demo/test data.

### STEP 12

Document only the **new web layer** in addition to the existing project documentation.

---

# 30. The implementation priority

If time is limited:

```text
Priority 1
Dashboard

Priority 2
Rooms

Priority 3
Guests

Priority 4
Bookings

Priority 5
Cancellation

Priority 6
Bill display

Priority 7
Search/filter

Priority 8
UI polish
```

Don't spend your time building obscure features.

---

# 31. What I would explicitly NOT do

Given your new constraint, I retract the earlier suggestions around modifying the current implementation.

**No:**

* changing `Booking`
* changing `Room`
* changing `Guest`
* changing date types
* changing `double`
* introducing enums
* refactoring `DatabaseManager`
* splitting repositories
* adding service layers
* adding billing interfaces
* changing loyalty logic
* changing availability logic
* changing SQL schema
* adding custom exceptions
* rewriting `ReservationManager`
* replacing SQLite
* replacing the console

**Only add the web layer.**

The existing code remains the team's common foundation.

---

# 36. The final architecture should therefore be deliberately simple

```text
                  ┌──────────────────────┐
                  │      WEB UI          │
                  │                      │
                  │ HTML                 │
                  │ CSS                  │
                  │ JavaScript           │
                  └──────────┬───────────┘
                             │
                            HTTP
                             │
                             ▼
                  ┌──────────────────────┐
                  │     WebServer        │
                  │       NEW            │
                  └──────────┬───────────┘
                             │
                             ▼
              ┌──────────────────────────────┐
              │      EXISTING JAVA CODE      │
              │                              │
              │ ReservationManager           │
              │ DatabaseManager              │
              │ Room                         │
              │ Guest                        │
              │ Booking                      │
              └──────────────┬───────────────┘
                             │
                             ▼
                       ┌───────────┐
                       │  SQLite   │
                       └───────────┘
```

**That is the plan.**

The webapp should be an **additional presentation layer**, not a rewrite of your project.

And because the JDK already provides a lightweight embedded HTTP server with request handlers and URL contexts, we can implement this without introducing a major framework or dependency. ([Oracle Docs][1])

From this point onward, I'll treat the existing Java implementation as **read-only/frozen** and only propose additions around it.

[1]: https://docs.oracle.com/en/java/javase/26/docs/api/jdk.httpserver/com/sun/net/httpserver/HttpServer.html?utm_source=chatgpt.com "HttpServer (Java SE 26 & JDK 26)"
[2]: https://docs.oracle.com/en/java/javase/26/docs/api/jdk.httpserver/com/sun/net/httpserver/HttpExchange.html?utm_source=chatgpt.com "HttpExchange (Java SE 26 & JDK 26)"
[3]: https://docs.oracle.com/en/java/javase/17/docs/api/jdk.httpserver/com/sun/net/httpserver/package-summary.html?utm_source=chatgpt.com "com.sun.net.httpserver (Java SE 17 & JDK 17)"