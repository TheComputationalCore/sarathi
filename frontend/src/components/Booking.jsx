import {
  useEffect,
  useState,
  useMemo,
  useCallback,
  useRef
} from "react";

import { useSearchParams, useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import api from "../api";

export default function Booking() {

  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const busId = searchParams.get("busId");
  const themes = searchParams.get("themes");
  const travelFallback = `/travel${themes ? `?themes=${encodeURIComponent(themes)}` : ""}`;

  const clientRef = useRef(null);
  const selectedSeatsRef = useRef([]);

  const [travelDate, setTravelDate] = useState("");
  const [bookedSeats, setBookedSeats] = useState([]);
  const [lockedSeats, setLockedSeats] = useState([]);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [passengers, setPassengers] = useState({});
  const [bus, setBus] = useState(null);
  const [lockTimer, setLockTimer] = useState(null);

  useEffect(() => {
    selectedSeatsRef.current = selectedSeats;
  }, [selectedSeats]);

  const seatNumbers = useMemo(() => {
    if (!bus?.totalSeats) return [];
    return Array.from({ length: bus.totalSeats }, (_, i) => String(i + 1));
  }, [bus]);

  /* ================= FETCH BUS ================= */

  useEffect(() => {
    if (!busId) {
      navigate(travelFallback, { replace: true });
      return;
    }

    api.get(`/buses/${busId}`)
      .then(res => setBus(res.data))
      .catch(() => {
        toast.error("Unable to load selected journey.");
        navigate(travelFallback, { replace: true });
      });

  }, [busId, navigate, travelFallback]);

  /* ================= FETCH SEATS ================= */

  const fetchSeatStatus = useCallback(async () => {

    if (!travelDate) return;

    try {
      const res = await api.get("/seats/status", {
        params: { busId, travelDate }
      });

      setBookedSeats(res.data.bookedSeats || []);
      setLockedSeats(res.data.lockedSeats || []);
      setSelectedSeats([]);
      setPassengers({});
      setLockTimer(null);

    } catch {
      toast.error("Failed to load seat availability");
    }

  }, [busId, travelDate]);

  useEffect(() => {
    fetchSeatStatus();
  }, [fetchSeatStatus]);

  /* ================= SOCKET ================= */

  useEffect(() => {

    if (!travelDate || !busId) return;
    const token = localStorage.getItem("token");
    if (!token) return;

    const baseUrl =
      process.env.REACT_APP_WS_URL ||
      process.env.REACT_APP_API_URL?.replace("/api", "") ||
      window.location.origin;

    const socket = new SockJS(`${baseUrl}/ws`);

    const client = new Client({
      webSocketFactory: () => socket,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      debug: () => {},
      onConnect: () => {

        client.subscribe("/topic/seat-updates", (message) => {

          if (!message.body) return;

          const event = JSON.parse(message.body);

          if (
            String(event.busId) !== String(busId) ||
            event.travelDate !== travelDate
          ) return;

          const currentSelected = selectedSeatsRef.current;

          if (event.action === "LOCKED") {
            setLockedSeats(prev => {
              const updated = new Set(prev);
              event.seatNumbers.forEach(seat => {
                if (!currentSelected.includes(seat)) {
                  updated.add(seat);
                }
              });
              return Array.from(updated);
            });
          }

          if (event.action === "RELEASED") {
            setLockedSeats(prev =>
              prev.filter(seat =>
                !event.seatNumbers.includes(seat)
              )
            );
          }

          if (event.action === "BOOKED") {

            setLockedSeats(prev =>
              prev.filter(seat =>
                !event.seatNumbers.includes(seat)
              )
            );

            setBookedSeats(prev => {
              const updated = new Set(prev);
              event.seatNumbers.forEach(seat => updated.add(seat));
              return Array.from(updated);
            });

            setSelectedSeats(prev =>
              prev.filter(seat =>
                !event.seatNumbers.includes(seat)
              )
            );
          }

        });
      }
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };

  }, [travelDate, busId]);

  /* ================= LOCK TIMER ================= */

  const releaseAllSeats = useCallback(async () => {

    if (!selectedSeatsRef.current.length) return;

    try {
      await api.post("/seats/release", {
        busId,
        travelDate,
        seatNumbers: selectedSeatsRef.current
      });
    } catch {}

    setSelectedSeats([]);
    setPassengers({});
    setLockTimer(null);
  }, [busId, travelDate]);

  const handleLockExpiry = useCallback(async () => {
    await releaseAllSeats();
    toast.error("Seat lock expired");
  }, [releaseAllSeats]);

  useEffect(() => {

    if (!lockTimer) return;

    const interval = setInterval(() => {

      setLockTimer(prev => {

        if (prev <= 1) {
          clearInterval(interval);
          handleLockExpiry();
          return null;
        }

        return prev - 1;
      });

    }, 1000);

    return () => clearInterval(interval);

  }, [lockTimer, handleLockExpiry]);

  /* ================= TOGGLE ================= */

  const toggleSeat = async (seat) => {

    if (!travelDate) {
      toast.error("Select travel date first");
      return;
    }

    if (bookedSeats.includes(seat)) {
      toast.error("Seat already booked");
      return;
    }

    if (lockedSeats.includes(seat)) {
      toast.error("Seat locked by another user");
      return;
    }

    try {

      if (selectedSeats.includes(seat)) {

        await api.post("/seats/release", {
          busId,
          travelDate,
          seatNumbers: [seat]
        });

        setSelectedSeats(prev => prev.filter(s => s !== seat));

        setPassengers(prev => {
          const copy = { ...prev };
          delete copy[seat];
          return copy;
        });

        return;
      }

      await api.post("/seats/lock", {
        busId,
        travelDate,
        seatNumbers: [seat]
      });

      setSelectedSeats(prev => [...prev, seat]);

      if (!lockTimer) setLockTimer(300);

    } catch {
      toast.error("Seat action failed");
    }
  };

  /* ================= PASSENGER INPUT ================= */

  const handlePassengerChange = (seat, field, value) => {
    setPassengers(prev => ({
      ...prev,
      [seat]: {
        ...prev[seat],
        [field]: value
      }
    }));
  };

  /* ================= PAYMENT ================= */

  const proceedToPayment = async () => {

    if (!bus) {
      toast.error("Bus data not loaded");
      return;
    }

    if (!selectedSeats.length) {
      toast.error("Select seats first");
      return;
    }

    const passengerList = selectedSeats.map(seat => ({
      seatNumber: seat,
      name: passengers[seat]?.name?.trim(),
      age: passengers[seat]?.age
    }));

    if (passengerList.some(p => !p.name || !p.age)) {
      toast.error("Fill all passenger details");
      return;
    }

    try {

      const idempotencyKey =
        crypto?.randomUUID?.() ||
        Date.now().toString();

      const res = await api.post("/bookings", {
        busId,
        travelDate,
        passengers: passengerList,
        idempotencyKey
      });

      toast.success("Booking created");
      navigate(`/payment?bookingId=${res.data.bookingId}`);

    } catch (err) {
      toast.error(
        err?.response?.data?.message ||
        "Booking failed"
      );
    }
  };

  const totalAmount = bus
    ? selectedSeats.length * bus.price
    : 0;

  const today = new Date().toISOString().split("T")[0];

  /* ================= UI ================= */

  return (
    <div className="space-y-16">

      <div>
        <h2 className="text-4xl font-serif text-brand-gold">
          Select Your Seat
        </h2>
        <p className="text-gray-400 mt-3">
          {bus?.fromLocation} → {bus?.toLocation}
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-14">

        {/* LEFT */}
        <div className="lg:col-span-2 space-y-12">

          {/* Travel Date */}
          <div className="card-elevated p-8">
            <label className="block text-xs uppercase tracking-widest text-gray-400 mb-4">
              Travel Date
            </label>
            <input
              type="date"
              min={today}
              value={travelDate}
              onChange={e => setTravelDate(e.target.value)}
              className="input-modern"
            />
          </div>

          {/* Seat Grid */}
          <div className="card-elevated p-8 space-y-10">

            <div className="grid grid-cols-4 sm:grid-cols-6 gap-5">

              {seatNumbers.map(seat => {

                const isBooked = bookedSeats.includes(seat);
                const isLocked = lockedSeats.includes(seat);
                const isSelected = selectedSeats.includes(seat);

                let base =
                  "w-14 h-14 rounded-xl flex items-center justify-center text-sm font-semibold transition-all duration-200";

                let style =
                  "bg-green-600 hover:scale-105 cursor-pointer text-white";

                if (isBooked)
                  style =
                    "bg-red-700 opacity-80 cursor-not-allowed text-white";

                else if (isLocked)
                  style =
                    "bg-yellow-500 opacity-80 cursor-not-allowed text-black";

                else if (isSelected)
                  style =
                    "bg-brand-gold text-black shadow-glow scale-105";

                return (
                  <div
                    key={seat}
                    onClick={() => toggleSeat(seat)}
                    className={`${base} ${style}`}
                  >
                    {seat}
                  </div>
                );
              })}

            </div>

            {lockTimer && (
              <div className="text-center text-sm text-yellow-400">
                Lock expires in{" "}
                {Math.floor(lockTimer / 60)}:
                {(lockTimer % 60).toString().padStart(2, "0")}
              </div>
            )}

            {/* Passenger Forms */}
            {selectedSeats.length > 0 && (
              <div className="space-y-6">
                {selectedSeats.map(seat => (
                  <div key={seat} className="space-y-3">
                    <h4 className="text-sm text-gray-400">
                      Passenger — Seat {seat}
                    </h4>
                    <div className="grid grid-cols-2 gap-4">
                      <input
                        placeholder="Full Name"
                        className="input-modern"
                        value={passengers[seat]?.name || ""}
                        onChange={e =>
                          handlePassengerChange(seat, "name", e.target.value)
                        }
                      />
                      <input
                        type="number"
                        placeholder="Age"
                        className="input-modern"
                        value={passengers[seat]?.age || ""}
                        onChange={e =>
                          handlePassengerChange(seat, "age", e.target.value)
                        }
                      />
                    </div>
                  </div>
                ))}
              </div>
            )}

          </div>

        </div>

        {/* RIGHT SUMMARY */}
        <div>

          <div className="sticky top-40 card-elevated p-8 space-y-6">

            <div className="flex justify-between text-gray-400">
              <span>Seats</span>
              <span>{selectedSeats.join(", ") || "-"}</span>
            </div>

            <div className="flex justify-between text-gray-400">
              <span>Price / Seat</span>
              <span>₹ {bus?.price || 0}</span>
            </div>

            <div className="h-px bg-white/10" />

            <div className="flex justify-between text-xl font-semibold">
              <span>Total</span>
              <span>₹ {totalAmount}</span>
            </div>

            <button
              disabled={!selectedSeats.length}
              onClick={proceedToPayment}
              className="w-full btn-primary disabled:opacity-40"
            >
              Proceed to Payment
            </button>

          </div>

        </div>

      </div>

    </div>
  );
}
