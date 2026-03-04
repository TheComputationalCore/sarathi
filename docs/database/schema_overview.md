# Database Schema Overview

Sarathi uses **PostgreSQL (Neon)**.

The schema is optimized with:

* indexing
* idempotent bookings
* seat concurrency handling

---

# Core Tables

```
users
booking
passenger
payment
bus
bus_schedule
circuits
route_stops
yatra_points
themes
eras
```

---

# Key Relationships

```
Circuit
   │
   ▼
Bus
   │
   ▼
BusSchedule
   │
   ▼
Passenger
   │
   ▼
Booking
```

---

# Important Constraints

## Bus Schedule Uniqueness

```
(bus_id, travel_date)
```

Prevents duplicate schedules.

---

## Route Stop Ordering

```
(bus_id, sequence_order)
```

Ensures deterministic route maps.

---

## Booking Idempotency

```
idempotency_key
```

Prevents duplicate booking creation.

---

# Index Strategy

Indexes exist for:

* route search
* booking queries
* schedule lookups
* seat availability

Examples:

```
idx_bus_route
idx_schedule_date
idx_booking_user_id
```
