# Domain Model

Sarathi models **heritage transportation networks**.

---

# Core Entities

## Circuit

Represents a **heritage travel corridor**.

Example:

* Sacred River Circuit
* Temple Architecture Spine

A circuit contains:

* buses
* themes

---

## Bus

Represents a transport route.

Attributes:

* origin
* destination
* departure time
* arrival time
* seat capacity

Relationships:

```
Bus
 ├ schedules
 ├ routeStops
 └ circuit
```

---

## RouteStop

Represents intermediate stops along a bus route.

Each stop connects to a **YatraPoint**.

```
Bus → RouteStop → YatraPoint
```

---

## YatraPoint

A heritage location.

Examples:

* Varanasi
* Nalanda
* Kanchipuram

Contains:

* latitude
* longitude
* historical context
* cultural metadata

---

## BusSchedule

Represents a specific **travel date** for a bus.

Fields:

* travelDate
* availableSeats

Each schedule contains:

```
passengers
```

---

## Booking

Represents a booking transaction.

Key properties:

* idempotencyKey
* booking status
* payment details

Statuses:

```
PAYMENT_PENDING
CONFIRMED
CANCELLED
PAYMENT_FAILED
```

---

## Passenger

Each booking may contain multiple passengers.

Each passenger reserves **one seat**.

---

# Domain Relationship Diagram

```
Circuit
  │
  ▼
Bus
  │
  ├── RouteStop ──► YatraPoint
  │
  └── BusSchedule
          │
          ▼
        Passenger
          │
          ▼
        Booking
```
