# Contributing to Sarathi

Thank you for your interest in contributing to **Sarathi — Civilizational Mobility Platform**.

Sarathi aims to build an open infrastructure layer for **heritage mobility, pilgrimage logistics, and civilizational transport networks**.

We welcome contributions that improve the platform’s:

* reliability
* scalability
* documentation
* data modeling
* developer experience

---

# Development Workflow

### 1. Fork the Repository

Create your own fork of the repository.

### 2. Clone the Repository

```bash
git clone https://github.com/<your-username>/sarathi.git
```

---

### 3. Create a Branch

Create a feature branch:

```bash
git checkout -b feature/your-feature-name
```

Examples:

```
feature/seat-map-ui
feature/bus-search-optimization
feature/circuit-analytics
```

---

### 4. Make Changes

Follow the project conventions:

* clear commit messages
* meaningful variable names
* minimal breaking changes

---

### 5. Run the Project Locally

Backend:

```bash
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

---

### 6. Run Tests

```bash
mvn test
```

Ensure the build passes before submitting a pull request.

---

### 7. Submit Pull Request

Push your branch and open a pull request against:

```
main
```

Include:

* description of change
* screenshots if UI change
* relevant documentation updates

---

# Contribution Guidelines

Please follow these guidelines:

* keep pull requests focused and small
* avoid unrelated refactors
* update documentation when needed
* maintain backward compatibility where possible

---

# Areas for Contribution

We welcome contributions in the following areas:

### Platform

* route optimization
* seat layout engine
* booking concurrency improvements

### Data

* heritage route datasets
* pilgrimage circuit definitions
* yatra point enrichment

### Frontend

* map experience
* mobile optimization
* seat map UX

### Infrastructure

* containerization
* deployment automation
* monitoring

---

# Code Style

Backend:

```
Java 21
Spring Boot conventions
Layered architecture
```

Frontend:

```
React
Component-based design
```

---

# Reporting Issues

Please use **GitHub Issues** to report:

* bugs
* feature requests
* documentation improvements

Include:

* steps to reproduce
* expected behavior
* screenshots (if applicable)

---

# Thank You

Your contributions help improve Sarathi as a platform for **civilizational mobility infrastructure**.
