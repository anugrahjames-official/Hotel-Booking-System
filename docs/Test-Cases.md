# Test Cases: Hotel Room Booking System

---

## Starting State (Fresh Database)
To ensure the following tests execute correctly, start with a fresh database by deleting `database/hotel.db` if it exists. Start the application and execute the following setup via the Console Menu:
1.  **Add Room 101:** (Type: Single, Price: 1000)
2.  **Add Room 102:** (Type: Double, Price: 2000)
3.  **Add Room 201:** (Type: Deluxe, Price: 4000)

## 1. Room Management

### Test Case 1.1: View Available Rooms
- **Action:** Select "View Available Rooms", press Enter for all.
- **Expected Output:**
  ```text
  room 101 (Single) - Rs.1000.0 - available
  room 102 (Double) - Rs.2000.0 - available
  room 201 (Deluxe) - Rs.4000.0 - available
  ```

### Test Case 1.2: Remove Available Room
- **Action:** Select "Remove Room", enter `102`
- **Expected Output:** `Room removed successfully.`

### Test Case 1.3: Update Room Details
- **Action:** Select "Update Room", enter `101`, `Premium`, `1500`
- **Expected Output:** `Room updated successfully.`

---

## 2. Guest Registration

### Test Case 2.1: Register a Guest
- **Action:** Select "Register Guest", enter `Anugrah James`, `ID-1234`, `9876543210`
- **Expected Output:** `Guest registered successfully.`

### Test Case 2.2: Register a Guest (Invalid Contact)
- **Action:** Select "Register Guest", enter `Jane Doe`, `ID-5678`, `12345`
- **Expected Output:** `Error: Guest contact must be a valid 10-digit number`

---

## 3. Booking Operations

*(Requires Guest ID 1 and Room 201 to exist and be available).*

### Test Case 3.1: Standard Booking (No peak season, no loyalty)
- **Action:** Select "Book Room", enter Guest ID `1`, Room `201`, Check-in `10-06-2026`, Check-out `12-06-2026`.
- **Expected Bill:** 2 nights × 4000 = Rs. 8000.
- **Expected Output:** `Room booked successfully! Bill: Rs.8000.0`

### Test Case 3.2: Prevent Double Booking
- **Action:** Attempt to book Room `201` again.
- **Expected Output:** `room not available or doesn't exist`

### Test Case 3.3: Invalid Dates
- **Action:** Select "Book Room", Guest ID `1`, Room `101`, Check-in `15-06-2026`, Check-out `10-06-2026`.
- **Expected Output:** `check-out must be after check-in`

### Test Case 3.4: Cancel Booking
- **Action:** Select "Cancel Booking", enter Booking ID `1` (from Test Case 3.1).
- **Expected Output:** `Booking cancelled successfully!` (Room 201 becomes available again).

---

## 4. Peak Season & Loyalty Surcharges

### Test Case 4.1: Peak Season Surcharge (December)
- **Action:** Select "Book Room", enter Guest ID `1`, Room `201`, Check-in `20-12-2026`, Check-out `22-12-2026`.
- **Expected Bill:** 2 nights × 4000 = 8000 base. Peak (December) adds 10% (800). Total = 8800.
- **Expected Output:** `Room booked successfully! Bill: Rs.8800.0`

### Test Case 4.2: Loyalty Tier Upgrade
- **Pre-Condition:** Guest 1 has made 2 prior bookings (Test 3.1 and 4.1).
- **Action:** Cancel Bookings. Then book Room `201` for Guest 1.
- **Expected Output:** `Room booked successfully!` followed by `congratulations guest upgraded to SILVER tier`.

### Test Case 4.3: Loyalty Tier Discount Application
- **Pre-Condition:** Guest 1 is now SILVER. Room 201 is available. Non-peak season.
- **Action:** Select "Book Room", enter Guest ID `1`, Room `201`, Check-in `01-08-2026`, Check-out `02-08-2026`.
- **Expected Bill:** 1 night × 4000 = 4000 base. Silver discount is 5% (-200). Total = 3800.
- **Expected Output:** `Room booked successfully! Bill: Rs.3800.0`

---

## 5. Web Application Testing
1. **Start the Web Server:** Run `run-web.bat` and navigate to `http://localhost:8080`.
2. **Retrieve Data:** Go to the "Rooms" tab. Ensure the table populates with Rooms 101 and 201.
3. **Add Data via Web:** In "Rooms", use the "Add Room" modal to add Room `301`, type `Suite`, price `5000`. Observe the table updating in real-time.
4. **Make Booking via Web:** Go to "Bookings", click "New Booking", select Guest 1, Room 301, and choose dates from the calendar. Observe success notification and the bill calculation returning correctly.
