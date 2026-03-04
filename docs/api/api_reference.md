# API Reference

Base URL:

```
/api
```

---

# Authentication

Authentication uses **JWT tokens**.

Login:

```
POST /api/auth/login
```

Response:

```
{
  "token": "JWT_TOKEN"
}
```

Use header:

```
Authorization: Bearer TOKEN
```

---

# Bus APIs

### Search Buses

```
GET /api/buses
```

Optional parameters:

```
from
to
circuitId
circuitSlug
themes
```

Example:

```
GET /api/buses?from=Delhi&to=Varanasi
```

---

### Get Bus

```
GET /api/buses/{id}
```

---

### Route Map

```
GET /api/buses/{id}/route-map
```

Returns:

* stops
* coordinates
* route order

---

# Booking APIs

### Create Booking

```
POST /api/bookings
```

Requires authentication.

---

### Booking History

```
GET /api/bookings/history
```

---

### Cancel Booking

```
DELETE /api/bookings/{id}
```

---

# Seat APIs

### Lock Seats

```
POST /api/seats/lock
```

Seats remain locked for **5 minutes**.

---

### Release Seats

```
POST /api/seats/release
```

---

### Seat Status

```
GET /api/seats/status
```

Returns:

* available seats
* locked seats
* booked seats

---

# Payment APIs

### Create Razorpay Order

```
POST /api/payments/create-order/{bookingId}
```

---

### Verify Payment

```
POST /api/payments/verify
```

This confirms booking after successful Razorpay payment.
