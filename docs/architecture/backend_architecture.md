# Backend Architecture

Sarathi backend follows a **layered architecture**.

```
Controller
   │
   ▼
Service
   │
   ▼
Repository
   │
   ▼
Database
```

---

# Controllers

Controllers expose REST APIs.

Examples:

* `BusController`
* `BookingController`
* `SeatLockController`
* `PaymentController`

Responsibilities:

* input validation
* request mapping
* authentication context
* response formatting

---

# Services

Business logic resides in services.

Examples:

* `BusService`
* `BookingService`
* `SeatLockService`
* `PaymentService`

Responsibilities:

* seat availability logic
* booking lifecycle
* payment orchestration
* route computation

---

# Repositories

Repositories interact with the database using **Spring Data JPA**.

Examples:

* `BusRepository`
* `BookingRepository`
* `UserRepository`
* `BusScheduleRepository`

---

# Security Layer

Authentication:

```
JWT
```

Key components:

* `JwtAuthenticationFilter`
* `JwtUtil`
* `SecurityConfig`

Users authenticate via:

```
/api/auth/login
```

---

# Seat Locking

Seat locking prevents race conditions during booking.

Process:

```
User selects seats
        │
SeatLockController
        │
SeatLockService
        │
      Redis
```

Locks expire after **5 minutes**.

---

# Real-Time Communication

Seat updates are pushed via WebSocket.

```
SeatLockController
      │
      ▼
SimpMessagingTemplate
      │
      ▼
/topic/seat-updates
```

Clients subscribed to this topic receive:

* seat locked
* seat released
* seat booked
