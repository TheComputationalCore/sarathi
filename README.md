# Sarathi

### Civilizational Mobility Platform

<p align="center">
A production-grade real-time bus booking system demonstrating scalable system design, modern full-stack architecture, and cloud-native deployment.
</p>

<p align="center">

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.x-brightgreen)
![React](https://img.shields.io/badge/React-18-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue)
![Redis](https://img.shields.io/badge/Redis-Cache-red)
![License](https://img.shields.io/badge/license-MIT-green)

</p>

---

# Live Demo

Frontend Application

https://sarathi-frontend-3zg8.onrender.com

---

# What is Sarathi?

Sarathi is a **real-time mobility booking platform** designed to simulate the architecture of modern transportation systems.

It demonstrates how production platforms manage:

• high-concurrency seat reservations
• real-time availability updates
• secure ticket payments
• distributed system reliability

The system architecture reflects patterns used in **modern mobility platforms and large-scale service platforms**.

---

# Platform Capabilities

Sarathi supports a complete ticketing workflow:

1. Discover bus routes and schedules
2. View live seat availability
3. Select and reserve seats
4. Complete payment through Razorpay
5. Receive ticket with QR verification

The platform ensures **consistency, security, and reliability** throughout the booking lifecycle.

---

# System Architecture

Sarathi follows a **layered architecture designed for scalability**.

```
Client (React)
        │
        ▼
REST API + WebSocket Layer
(Spring Boot)
        │
        ▼
Application Services
        │
        ▼
Persistence Layer
(PostgreSQL)
        │
        ▼
Cache & Rate Limiting
(Redis)
```

Key architectural elements:

• Stateless backend services
• Transactional booking system
• Real-time WebSocket communication
• Secure payment verification

---

# Technology Stack

## Backend

Spring Boot
Spring Security
Spring Data JPA
PostgreSQL
Redis
JWT Authentication
WebSocket (STOMP)
Resilience4j
Bucket4j Rate Limiting
Micrometer Monitoring
Razorpay Payment Gateway

---

## Frontend

React
React Router
React Query
Zustand State Management
TailwindCSS
Framer Motion
Leaflet Maps
Axios
STOMP WebSocket Client

---

## Infrastructure

Render Cloud Platform
Docker Containerization
PostgreSQL Database
Redis Cache

---

# Core Engineering Features

## Real-Time Seat Synchronization

Seat availability is synchronized using **WebSockets**.

This prevents race conditions and ensures that seat availability updates are instantly reflected for all users.

---

## Transactional Booking Engine

Seat reservations are processed using **database transactions**, guaranteeing:

• atomic booking operations
• prevention of double reservations
• consistent booking state

---

## Secure Payment Processing

Payments are handled via **Razorpay integration**.

The backend verifies every payment before confirming bookings.

---

## Digital Ticket Generation

Each successful booking produces:

• PDF ticket
• QR verification code
• booking record stored in the database

This enables digital validation workflows.

---

# Screenshots

Place screenshots inside:

```
docs/screenshots/
```

Example:

```
docs/screenshots/home.png
docs/screenshots/seat-selection.png
docs/screenshots/booking-success.png
```

### Home Interface

![Home](docs/screenshots/home.png)

### Seat Selection

![Seats](docs/screenshots/seat-selection.png)

### Booking Confirmation

![Booking](docs/screenshots/booking-success.png)

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
│   └── src
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
PostgreSQL
Redis

Run backend

```
./mvnw spring-boot:run
```

Backend runs on

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

Frontend runs on

```
http://localhost:3000
```

---

# Environment Variables

Backend requires:

```
DATABASE_URL
REDIS_URL
JWT_SECRET
RAZORPAY_KEY_ID
RAZORPAY_SECRET
```

These should be configured in the deployment environment.

---

# Deployment

Sarathi is deployed using **Render Cloud Services**.

Frontend
Render Static Site

Backend
Render Docker Service

Database
PostgreSQL

Cache
Redis

---

# Documentation

Additional technical documentation is available in the `docs` directory.

Architecture documentation
API specification
Database schema
Booking workflow diagrams

---

# Contributing

Contributions are welcome.

Please review:

CODE_OF_CONDUCT.md
CONTRIBUTING.md

before submitting pull requests.

---

# License

MIT License

---

# Author

TheComputationalCore

Building scalable systems and distributed software platforms.
