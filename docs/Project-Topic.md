Design and implement a Hotel Room Booking System using Object-Oriented
Programming (**OOP**) principles in Java and limited use of **SQL**. The system should
manage room reservations, guest details, seasonal and loyalty-based billing, and room
availability tracking.
## Class Design
- Room: room number, room type (e.g., single/double/deluxe), base price per night, and
availability status.

- Guest: guest name, ID, contact information, loyalty tier, and booking history.
- Booking: booking ID, guest, room, check-in date, check-out date, and final bill
amount.
- ReservationManager: manages room inventory and booking operations (e.g.,
adding/removing rooms, processing reservations).
## Functionalities
- Room Management
– Add, remove, and update room details.
– View room availability by type.
- Booking Operations
– Book a room: check room availability and register the booking.
– Cancel a booking: update room status and remove the booking record.
– Calculate the total bill, applying a seasonal surcharge during peak periods and a
loyalty discount for guests with a qualifying tier.

## Deliverables

- Design Document: A description of all classes, their attributes, methods, and
relationships.
- Source Code: Well-documented Java code implementing the functionalities described
above.
- User Manual: Instructions on how to set up, run, and use the system.
- Test Cases: A set of sample inputs and outputs that demonstrate the system's
behavior.