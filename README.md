# Sarathi

### Civilizational Mobility Platform

Sarathi is a **production-grade real-time bus booking platform** built with modern full-stack architecture.
It demonstrates **scalable backend engineering, real-time communication, secure payments, and cloud deployment**.

The project showcases how large-scale mobility platforms can handle **seat reservations, ticketing, payments, and real-time updates** while maintaining **consistency, performance, and fault tolerance**.

---

# Live Application

Frontend

https://sarathi-frontend-3zg8.onrender.com

---

# Key Capabilities

Sarathi provides a complete digital mobility platform capable of handling:

• Real-time seat reservation
• Secure authentication and authorization
• Payment gateway integration
• Ticket generation with QR verification
• Live seat updates through WebSockets
• High-availability backend services

The architecture is designed to demonstrate **production-ready system design patterns used in large-scale applications**.

---

# Platform Architecture

Sarathi follows a **layered service architecture** separating concerns across client, service, and infrastructure layers.

Client Layer
React Frontend
User interface, route discovery, seat selection, booking UI

Application Layer
Spring Boot Backend
Business logic, booking orchestration, authentication, payment verification

Data Layer
PostgreSQL Database
Persistent storage for users, buses, bookings, payments

Caching Layer
Redis
Caching, rate limiting, and performance optimization

Communication Layer
WebSockets (STOMP)
Real-time seat availability updates

Infrastructure Layer
Docker
Render Cloud Deployment

---

# Technology Stack

## Backend

Spring Boot 3
Spring Security
Spring Data JPA
PostgreSQL
Redis
WebSockets (STOMP)
JWT Authentication
Resilience4j
Bucket4j
Micrometer Monitoring
Prometheus Metrics
Razorpay Payment Integration

---

## Frontend

React
React Router
React Query
Zustand State Management
TailwindCSS
Framer Motion
Leaflet Maps
STOMP WebSocket Client
Axios

---

## Infrastructure

Render Cloud
Docker
PostgreSQL
Redis

---

# Core Features

## Real-Time Seat Availability

Sarathi uses **WebSocket communication** to broadcast seat availability changes instantly.
This prevents **double booking and race conditions** during concurrent reservations.

---

## Secure Authentication

The backend uses **Spring Security with JWT authentication** to secure all booking operations.

Users receive a signed token after authentication, which is verified for every request.

---

## Atomic Seat Reservation

Seat booking is implemented with transactional database operations to ensure:

• seat locking
• consistent booking state
• protection against race conditions

---

## Payment Gateway Integration

Sarathi integrates **Razorpay** for secure ticket payments.

After successful payment:

• booking is confirmed
• ticket is generated
• payment is verified server-side

---

## Ticket Generation

Each successful booking generates:

• downloadable PDF ticket
• QR verification code
• persistent booking record

This enables **digital ticket validation and verification systems**.

---

# Screenshots

Place your application screenshots inside a directory like:

```
docs/screenshots/
```

Example:

```
docs/screenshots/home.png
docs/screenshots/seat-selection.png
docs/screenshots/booking-confirmation.png
```

Then embed them here:

### Home Interface

![Home](docs/screenshots/home.png)

### Seat Selection

![Seats](docs/screenshots/seat-selection.png)

### Booking Confirmation

![Booking](docs/screenshots/booking-confirmation.png)

---

# Repository Structure

```
sarathi
│
├── src
│   ├── controllers
│   ├── services
│   ├── repositories
│   ├── entities
│   └── security
│
├── frontend
│   ├── public
│   ├── src
│   │   ├── components
│   │   ├── pages
│   │   ├── state
│   │   └── services
│
├── docs
│   ├── architecture
│   ├── api
│   └── database
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

# Local Development

## Clone Repository

```
git clone https://github.com/TheComputationalCore/sarathi.git
cd sarathi
```

---

# Backend Setup

Requirements

Java 17
Maven
PostgreSQL
Redis

Run backend:

```
./mvnw spring-boot:run
```

Backend will start at:

```
http://localhost:8080
```

---

# Frontend Setup

```
cd frontend
npm install
npm start
```

Frontend runs at:

```
http://localhost:3000
```

---

# Environment Configuration

Backend requires the following environment variables:

DATABASE_URL
REDIS_URL
JWT_SECRET
RAZORPAY_KEY_ID
RAZORPAY_SECRET

These variables must be configured in **deployment environments**.

---

# Deployment Architecture

Sarathi is deployed using **Render cloud infrastructure**.

Frontend
Render Static Site

Backend
Render Docker Service

Database
PostgreSQL

Cache
Redis

This setup demonstrates **modern cloud-native deployment patterns**.

---

# Observability

Application metrics are exposed using:

Spring Boot Actuator
Micrometer Prometheus integration

These provide:

• system health monitoring
• application metrics
• production observability

---

# Documentation

Additional technical documentation is available in the `docs` directory.

Architecture diagrams
API specifications
Database schema
Booking flow documentation

---

# Contributing

Contributions are welcome.

Please review:

CONTRIBUTING.md
CODE_OF_CONDUCT.md

before submitting pull requests.

---

# License

This project is licensed under the MIT License.

See the LICENSE file for details.

---

# Author

TheComputationalCore

Engineering scalable platforms and distributed systems.
