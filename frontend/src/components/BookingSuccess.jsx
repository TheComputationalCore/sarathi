import React, { useEffect, useState } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import api from "../api";

export default function BookingSuccess() {

  const location = useLocation();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const bookingFromState = location.state;
  const bookingIdFromQuery = searchParams.get("bookingId");

  const [booking, setBooking] = useState(bookingFromState || null);
  const [loading, setLoading] = useState(!bookingFromState);
  const [error, setError] = useState("");

  /* ================= FETCH BOOKING ================= */

  useEffect(() => {

    if (bookingFromState) return;

    if (!bookingIdFromQuery) {
      setLoading(false);
      return;
    }

    const fetchBooking = async () => {
      try {
        const res = await api.get(`/bookings/${bookingIdFromQuery}`);
        setBooking(res.data);
      } catch {
        setError("Unable to retrieve booking details.");
        setBooking(null);
      } finally {
        setLoading(false);
      }
    };

    fetchBooking();

  }, [bookingFromState, bookingIdFromQuery]);

  /* ================= DOWNLOAD ================= */

  const downloadTicket = async () => {
    if (!booking?.bookingId) return;

    try {
      const res = await api.get(`/tickets/${booking.bookingId}`, {
        responseType: "blob"
      });

      const blob = new Blob([res.data]);
      const url = window.URL.createObjectURL(blob);

      const link = document.createElement("a");
      link.href = url;
      link.download = `Sarathi_Ticket_${booking.bookingId}.pdf`;

      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

    } catch {
      alert("Unable to download ticket.");
    }
  };

  /* ================= LOADING ================= */

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center px-6 pt-32 pb-20">
        <div className="card-elevated p-10 text-center space-y-6">
          <div className="loader-ring mx-auto" />
          <p className="text-gray-400">
            Retrieving booking details...
          </p>
        </div>
      </div>
    );
  }

  /* ================= ERROR ================= */

  if (!booking) {
    return (
      <div className="min-h-screen flex items-center justify-center px-6 pt-32 pb-20">
        <div className="card-elevated p-10 text-center space-y-6 max-w-lg w-full">
          <h2 className="text-2xl font-serif text-brand-gold">
            Booking Not Found
          </h2>
          <p className="text-gray-400">
            {error || "We couldn’t locate this booking."}
          </p>
          <button
            onClick={() => navigate("/")}
            className="btn-primary"
          >
            Return Home
          </button>
        </div>
      </div>
    );
  }

  /* ================= DATA ================= */

  const formattedDate = booking.bookingTime
    ? new Date(booking.bookingTime).toLocaleString()
    : "-";

  const seatList = Array.isArray(booking.seatNumbers)
    ? booking.seatNumbers.join(", ")
    : "-";

  /* ================= SUCCESS ================= */

  return (
    <div className="min-h-screen flex items-center justify-center px-6 pt-32 pb-20">

      <div className="card-elevated p-12 max-w-xl w-full text-center space-y-8 animate-fadeIn">

        {/* Icon */}
        <div className="w-20 h-20 mx-auto rounded-full bg-green-600 flex items-center justify-center text-2xl text-white shadow-glow animate-popIn">
          ✓
        </div>

        <div>
          <h2 className="text-3xl font-serif text-brand-gold mb-2">
            Journey Confirmed
          </h2>
          <p className="text-gray-400">
            Your sacred path has been successfully reserved.
          </p>
        </div>

        <div className="h-px bg-white/10" />

        {/* Route */}
        <div className="text-lg font-semibold">
          {booking.fromLocation || "—"}
          <span className="mx-3 text-brand-gold">→</span>
          {booking.toLocation || "—"}
        </div>

        {/* Ticket Details */}
        <div className="bg-brand-elevated/60 rounded-2xl p-6 space-y-4 text-left">

          <DetailRow label="Booking ID">
            <strong>#{booking.bookingId}</strong>
          </DetailRow>

          <DetailRow label="Status">
            <span className="px-3 py-1 text-xs rounded-full bg-green-600/20 text-green-400">
              {booking.status || "CONFIRMED"}
            </span>
          </DetailRow>

          <DetailRow label="Travel Date">
            <strong>{booking.travelDate || "-"}</strong>
          </DetailRow>

          <DetailRow label="Seats">
            <strong>{seatList}</strong>
          </DetailRow>

          <DetailRow label="Booked At">
            <strong>{formattedDate}</strong>
          </DetailRow>

        </div>

        {/* Actions */}
        <div className="flex flex-wrap gap-4 justify-center">

          <button
            onClick={() => navigate("/profile")}
            className="border border-white/20 px-5 py-3 rounded-xl hover:-translate-y-1 transition"
          >
            View My Bookings
          </button>

          <button
            onClick={downloadTicket}
            className="border border-white/20 px-5 py-3 rounded-xl hover:-translate-y-1 transition"
          >
            Download Ticket
          </button>

          <button
            onClick={() => navigate("/")}
            className="btn-primary"
          >
            Book Another Journey
          </button>

        </div>

      </div>

    </div>
  );
}

function DetailRow({ label, children }) {
  return (
    <div className="flex justify-between text-sm">
      <span className="text-gray-400">{label}</span>
      {children}
    </div>
  );
}