# Sample Dataset — Civilizational Transport Network

This repository includes a **sample transport dataset** that simulates a civilizational mobility network connecting major heritage destinations.

The dataset demonstrates how Sarathi can model:

* pilgrimage routes
* heritage corridors
* knowledge networks
* sacred geography

---

# Dataset Overview

The sample dataset includes:

| Entity        | Count |
| ------------- | ----- |
| Circuits      | 6     |
| Yatra Points  | 25    |
| Buses         | 50    |
| Bus Schedules | 200   |
| Route Stops   | 120+  |

---

# Example Circuits

### Sacred River Corridor

Varanasi → Prayagraj → Ayodhya → Haridwar

### Knowledge Corridor

Delhi → Nalanda → Bodh Gaya

### Temple Architecture Spine

Chennai → Kanchipuram → Thanjavur → Madurai

### Buddhist Heritage Route

Sarnath → Kushinagar → Lumbini

### Maritime Spice Route

Kochi → Muziris → Calicut

---

# Why This Dataset Exists

The dataset demonstrates:

* multi-stop route modeling
* heritage-aware mobility planning
* geographically meaningful transport routes

This makes Sarathi suitable for:

* pilgrimage logistics
* heritage tourism
* cultural infrastructure planning

---

# How to Load Dataset

Execute the SQL file:

```
database/civilizational_network.sql
```

inside your PostgreSQL database.

Example using Neon console:

```
Paste SQL → Run
```

---

# Important

The dataset is **optional** and used for:

* local development
* demo environments
* showcasing the platform

Production environments may load data through **admin APIs or migration pipelines**.
