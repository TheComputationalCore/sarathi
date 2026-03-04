# Sarathi

### Civilizational Mobility Platform

Sarathi is a **production‑grade real‑time bus booking platform**
designed to demonstrate modern full‑stack architecture, distributed
system design, and cloud‑native deployment.

The platform enables **real‑time seat booking, secure payments, ticket
generation, and live availability updates**, simulating how modern
transportation booking systems operate.

------------------------------------------------------------------------

# Live Demo

Frontend Application\
https://sarathi-frontend-3zg8.onrender.com

------------------------------------------------------------------------

# What Sarathi Demonstrates

Sarathi is built to illustrate how real‑world mobility platforms handle:

• high‑concurrency seat reservations\
• real‑time seat availability synchronization\
• secure payment verification\
• distributed service architecture\
• reliable booking workflows

The system architecture mirrors patterns used in **modern booking
platforms and scalable web systems**.

------------------------------------------------------------------------

# System Architecture

Sarathi follows a layered architecture separating presentation, service
logic, and infrastructure.

Client Layer\
React Frontend

Application Layer\
Spring Boot Backend (REST + WebSocket)

Service Layer\
Booking Service\
Payment Service\
Authentication Service

Data Layer\
PostgreSQL Database

Cache Layer\
Redis

Real‑Time Layer\
WebSockets (STOMP)

------------------------------------------------------------------------

# Architecture Diagrams

Place diagrams inside:

docs/architecture/

System Architecture\
docs/architecture/system-architecture.png

Booking Flow\
docs/architecture/booking-flow.png

Realtime Seat Updates\
docs/architecture/realtime-seat-flow.png

Database Schema\
docs/database/schema.png

------------------------------------------------------------------------

# Technology Stack

## Backend

Spring Boot 3\
Spring Security\
Spring Data JPA\
PostgreSQL\
Redis\
WebSockets (STOMP)\
JWT Authentication\
Resilience4j\
Bucket4j Rate Limiting\
Micrometer Monitoring\
Prometheus Metrics\
Razorpay Payment Integration

## Frontend

React\
React Router\
React Query\
Zustand State Management\
TailwindCSS\
Framer Motion\
Leaflet Maps\
Axios\
STOMP WebSocket Client

## Infrastructure

Render Cloud Platform\
Docker\
PostgreSQL\
Redis

------------------------------------------------------------------------

# Core Features

## Real‑Time Seat Synchronization

Seat availability is synchronized using **WebSocket communication**.\
All users receive instant updates when seats are locked or booked.

This prevents **double booking during concurrent reservations**.

------------------------------------------------------------------------

## Transactional Booking Engine

Seat reservations use **database transactions** ensuring:

• atomic booking operations\
• prevention of race conditions\
• consistent seat inventory

------------------------------------------------------------------------

## Secure Authentication

The backend implements **JWT authentication using Spring Security**.

All protected endpoints require valid tokens before accessing booking
functionality.

------------------------------------------------------------------------

## Payment Integration

Payments are processed using **Razorpay**.

After payment:

1.  payment is verified server‑side\
2.  booking is confirmed\
3.  ticket is generated

------------------------------------------------------------------------

## Digital Ticket Generation

Every successful booking produces:

• PDF ticket\
• QR verification code\
• booking record stored in database

------------------------------------------------------------------------

# Screenshots

Store screenshots inside:

docs/screenshots/

Example:

docs/screenshots/home.png\
docs/screenshots/seat-selection.png\
docs/screenshots/booking-success.png

------------------------------------------------------------------------

# Repository Structure

sarathi

src\
controllers\
services\
repositories\
entities\
security

frontend\
public\
src

docs\
architecture\
database\
screenshots

Dockerfile\
docker-compose.yml\
pom.xml\
README.md

------------------------------------------------------------------------

# Local Development

## Clone Repository

git clone https://github.com/TheComputationalCore/sarathi.git\
cd sarathi

------------------------------------------------------------------------

# Backend Setup

Requirements

Java 17\
PostgreSQL\
Redis

Run backend

./mvnw spring-boot:run

Backend runs on

http://localhost:8080

------------------------------------------------------------------------

# Frontend Setup

cd frontend\
npm install\
npm start

Frontend runs on

http://localhost:3000

------------------------------------------------------------------------

# Environment Variables

DATABASE_URL\
REDIS_URL\
JWT_SECRET\
RAZORPAY_KEY_ID\
RAZORPAY_SECRET

------------------------------------------------------------------------

# Deployment

Sarathi is deployed using **Render Cloud Infrastructure**.

Frontend → Render Static Site\
Backend → Render Web Service\
Database → PostgreSQL\
Cache → Redis

------------------------------------------------------------------------

# Observability

Metrics and monitoring provided through:

Spring Boot Actuator\
Micrometer Prometheus integration

------------------------------------------------------------------------

# Documentation

Technical documentation is available inside the docs directory.

Architecture diagrams\
API documentation\
Database schema\
Booking workflow diagrams

------------------------------------------------------------------------

# Contributing

Contributions are welcome.

Please read:

CONTRIBUTING.md\
CODE_OF_CONDUCT.md

before submitting pull requests.

------------------------------------------------------------------------

# License

MIT License

------------------------------------------------------------------------

# Author

TheComputationalCore
