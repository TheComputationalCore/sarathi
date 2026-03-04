# Sarathi — System Architecture

Sarathi is a **civilizational mobility platform** designed to connect heritage circuits, pilgrimage routes, and cultural destinations through an intelligent bus booking infrastructure.

The system combines:

* Real-time seat locking
* Heritage route intelligence
* Secure payments
* Scalable microservice-ready backend

---

# High Level Architecture

```
React Frontend
       │
       ▼
Spring Boot Backend API
       │
 ┌─────┴───────────────┐
 │                     │
 ▼                     ▼
PostgreSQL (Neon)     Redis
Primary Data          Seat Locks + Cache
```

---

# Core Components

## Frontend

Technology stack:

* React
* Map visualization
* WebSocket integration
* Razorpay checkout

Responsibilities:

* Bus search
* Route exploration
* Seat selection
* Booking and payment
* Real-time seat updates

---

## Backend

Technology stack:

* Spring Boot
* Spring Security
* JWT authentication
* Redis
* WebSocket messaging
* Razorpay integration

Responsibilities:

* Authentication and authorization
* Bus search and route intelligence
* Booking lifecycle
* Seat locking
* Payment verification
* Ticket generation

---

## Database

Primary storage:

* PostgreSQL (Neon)

Stores:

* users
* buses
* schedules
* circuits
* route stops
* yatra points
* bookings
* payments

---

## Redis

Used for:

* seat locking
* short-lived booking state
* caching

Seat locks expire automatically after **5 minutes**.

---

# Booking Flow

```
User selects seats
       │
       ▼
SeatLockController
       │
       ▼
Redis Lock
       │
       ▼
Booking Creation
       │
       ▼
Razorpay Order
       │
       ▼
Payment Verification
       │
       ▼
Booking Confirmed
```

---

# Real-Time Updates

Seat updates are broadcast using:

```
WebSocket
/topic/seat-updates
```

This ensures all users see **live seat availability**.

---

# Scalability Considerations

The system is designed for scale:

* Redis handles concurrency
* JWT enables stateless authentication
* PostgreSQL indexing supports fast search
* Seat locking prevents race conditions

Future improvements:

* horizontal backend scaling
* event-driven booking pipeline
* distributed seat locking
