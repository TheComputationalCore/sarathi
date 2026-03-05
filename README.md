# Sarathi 🚍

### Real-Time Bus Booking Platform

Sarathi is a **production-grade real-time bus booking platform**
designed to demonstrate modern **distributed system design,
high-concurrency transaction handling, and cloud-native architecture**.

The platform simulates how real-world transportation booking systems
operate with **real-time seat availability, secure payments, and
distributed service architecture**.

------------------------------------------------------------------------

## 🚀 Live Demo

Frontend\
https://sarathi-frontend-3zg8.onrender.com

------------------------------------------------------------------------

## ✨ Key Highlights

-   Real-time seat availability using **WebSockets**
-   High-concurrency **seat reservation system**
-   Distributed **booking transaction engine**
-   Secure **JWT authentication**
-   Integrated **Razorpay payment gateway**
-   **Redis caching and locking**
-   Cloud deployment using **Docker + Render**
-   **Observability with Prometheus metrics**

------------------------------------------------------------------------

## 🏗 System Architecture

Sarathi follows a **layered distributed architecture** separating UI,
application logic, and infrastructure.

Client Layer React Frontend

Application Layer Spring Boot REST APIs

Service Layer Booking Service Payment Service Authentication Service

Data Layer PostgreSQL Database

Cache Layer Redis

Realtime Layer WebSockets

------------------------------------------------------------------------

## 🔁 Booking Flow

1.  User selects route and seats\
2.  Client requests seat reservation\
3.  Backend validates seat availability\
4.  Redis temporarily locks selected seats\
5.  Payment initiated via Razorpay\
6.  Payment verified server-side\
7.  Booking transaction committed\
8.  Ticket generated and stored\
9.  Real-time updates sent to all clients

------------------------------------------------------------------------

## ⚡ Real-Time Seat Synchronization

Sarathi uses **WebSockets with STOMP protocol** for real-time seat
updates.

When a seat is reserved:

-   Seat is temporarily locked in Redis
-   WebSocket event broadcast to all clients
-   UI updates instantly
-   Other users cannot select the locked seat

This prevents **race conditions and double booking**.

------------------------------------------------------------------------

## 🔒 Concurrency Control Strategy

Seat booking is a high contention problem.

Sarathi prevents double booking using:

### Database Transactions

`@Transactional`

Ensures atomic booking operations.

### Redis Seat Locking

Example:

`SET seat_102 locked EX 120`

Seats automatically release after timeout.

### Optimistic Locking

Booking verification ensures seat state consistency before final commit.

------------------------------------------------------------------------

## 🔐 Authentication & Security

Sarathi uses **JWT-based authentication** implemented with Spring
Security.

Flow:

1.  User logs in
2.  Server generates JWT token
3.  Token sent in Authorization header
4.  Backend validates token for protected APIs

------------------------------------------------------------------------

## 💳 Payment Processing

Payments are handled using **Razorpay integration**.

Workflow:

1.  Backend creates Razorpay order
2.  Frontend opens payment gateway
3.  User completes payment
4.  Razorpay sends payment response
5.  Backend verifies payment signature
6.  Booking confirmed

------------------------------------------------------------------------

## 🎟 Ticket Generation

After successful payment:

-   Booking record stored in database
-   Ticket generated as **PDF**
-   QR code created for verification
-   Booking confirmation returned to user

------------------------------------------------------------------------

## 🛠 Technology Stack

### Backend

-   Spring Boot 3
-   Spring Security
-   Spring Data JPA
-   PostgreSQL
-   Redis
-   WebSockets (STOMP)
-   JWT Authentication
-   Resilience4j
-   Bucket4j Rate Limiting
-   Micrometer
-   Prometheus
-   Razorpay

### Frontend

-   React
-   React Router
-   React Query
-   Zustand
-   TailwindCSS
-   Framer Motion
-   Leaflet Maps
-   Axios
-   STOMP WebSocket Client

### Infrastructure

-   Docker
-   Render Cloud
-   PostgreSQL
-   Redis

------------------------------------------------------------------------

## 📂 Repository Structure

sarathi │ ├── src │ ├── controllers │ ├── services │ ├── repositories │
├── entities │ └── security │ ├── frontend │ ├── public │ └── src │ ├──
docs │ ├── architecture │ ├── database │ └── screenshots │ ├──
Dockerfile ├── docker-compose.yml ├── pom.xml └── README.md

------------------------------------------------------------------------

## ⚙️ Local Development

### Clone Repository

git clone https://github.com/TheComputationalCore/sarathi.git cd sarathi

### Backend Setup

Requirements

-   Java 17
-   PostgreSQL
-   Redis

Run backend

./mvnw spring-boot:run

Backend runs at

http://localhost:8080

### Frontend Setup

cd frontend npm install npm start

Frontend runs at

http://localhost:3000

------------------------------------------------------------------------

## 🔑 Environment Variables

DATABASE_URL\
REDIS_URL\
JWT_SECRET\
RAZORPAY_KEY_ID\
RAZORPAY_SECRET

------------------------------------------------------------------------

## 🚀 Deployment

Sarathi is deployed using **Render Cloud Infrastructure**.

Frontend → Render Static Site\
Backend → Render Web Service\
Database → PostgreSQL\
Cache → Redis

------------------------------------------------------------------------

## 📈 Observability & Monitoring

Sarathi exposes metrics using:

-   Spring Boot Actuator
-   Micrometer
-   Prometheus

Metrics include:

-   request latency
-   booking throughput
-   error rates
-   system health

------------------------------------------------------------------------

## 📜 License

MIT License

------------------------------------------------------------------------

## 👨‍💻 Author

**TheComputationalCore**
