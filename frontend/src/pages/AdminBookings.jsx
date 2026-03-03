import { useEffect, useState, useCallback, useMemo } from "react";
import api from "../api";

export default function AdminBookings() {

  const [bookings, setBookings] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [statusFilter, setStatusFilter] = useState("ALL");
  const [search, setSearch] = useState("");
  const [debouncedSearch, setDebouncedSearch] = useState("");

  const [loading, setLoading] = useState(true);
  const [confirmCancelId, setConfirmCancelId] = useState(null);

  /* ================= DEBOUNCE ================= */
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(search.trim());
    }, 400);
    return () => clearTimeout(timer);
  }, [search]);

  useEffect(() => {
    setPage(0);
  }, [debouncedSearch, statusFilter]);

  /* ================= LOAD ================= */
  const loadBookings = useCallback(async () => {

    try {
      setLoading(true);

      const res = await api.get(`/admin/bookings`, {
        params: { page, size: 10 }
      });

      setBookings(res.data?.content || []);
      setTotalPages(res.data?.totalPages || 0);

    } catch (err) {
      console.error("Bookings load error:", err);
      setBookings([]);
      setTotalPages(0);
    } finally {
      setLoading(false);
    }

  }, [page]);

  useEffect(() => {
    loadBookings();
  }, [loadBookings]);

  /* ================= CANCEL ================= */
  const cancelBooking = async (bookingId) => {

    try {
      await api.delete(`/admin/bookings/${bookingId}`);

      setBookings(prev =>
        prev.map(b =>
          b.bookingId === bookingId
            ? { ...b, status: "CANCELLED" }
            : b
        )
      );

      setConfirmCancelId(null);

    } catch (err) {
      console.error("Cancel failed:", err);
    }
  };

  /* ================= FILTER ================= */
  const filteredBookings = useMemo(() => {

    return bookings
      .filter(b =>
        statusFilter === "ALL" || b.status === statusFilter
      )
      .filter(b =>
        debouncedSearch === "" ||
        String(b.bookingId).includes(debouncedSearch)
      );

  }, [bookings, statusFilter, debouncedSearch]);

  /* ================= LOADER ================= */
  if (loading) {
    return (
      <div className="flex items-center justify-center py-32">
        <div className="w-10 h-10 border-4 border-white/10 border-t-brand-gold rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="space-y-12">

      {/* ================= HEADER ================= */}
      <div className="space-y-3">
        <h2 className="text-3xl font-serif text-brand-gold">
          Journey Reservation Oversight
        </h2>
        <p className="text-gray-400 max-w-2xl">
          Monitor booking states, payment flows, and operational integrity.
        </p>
      </div>

      {/* ================= FILTER PANEL ================= */}
      <div className="card-elevated p-10">

        <div className="grid md:grid-cols-2 gap-8">

          {/* Search */}
          <div className="space-y-3">
            <label className="text-xs uppercase tracking-widest text-gray-500">
              Booking ID
            </label>
            <input
              type="text"
              placeholder="Search by ID..."
              value={search}
              onChange={e => setSearch(e.target.value)}
              className="input-modern"
            />
          </div>

          {/* Status */}
          <div className="space-y-3">
            <label className="text-xs uppercase tracking-widest text-gray-500">
              Status
            </label>
            <select
              value={statusFilter}
              onChange={e => setStatusFilter(e.target.value)}
              className="w-full bg-black/60 border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:border-brand-gold transition"
            >
              <option value="ALL">All Status</option>
              <option value="CONFIRMED">Confirmed</option>
              <option value="CANCELLED">Cancelled</option>
              <option value="PAYMENT_PENDING">Payment Pending</option>
              <option value="PENDING">Pending</option>
            </select>
          </div>

        </div>

      </div>

      {/* ================= TABLE ================= */}
      <div className="card-elevated overflow-hidden">

        {filteredBookings.length === 0 ? (

          <div className="py-24 text-center text-gray-500">
            No bookings match current filters.
          </div>

        ) : (

          <div className="overflow-x-auto">

            <table className="w-full text-sm">

              <thead className="bg-black/40 backdrop-blur-sm text-gray-400 uppercase tracking-[0.15em] text-xs">
                <tr>
                  <th className="px-6 py-4 text-left">ID</th>
                  <th className="px-6 py-4 text-left">Route</th>
                  <th className="px-6 py-4 text-left">Travel Date</th>
                  <th className="px-6 py-4 text-left">Status</th>
                  <th className="px-6 py-4 text-right">Actions</th>
                </tr>
              </thead>

              <tbody className="divide-y divide-white/5">

                {filteredBookings.map(b => (

                  <tr
                    key={b.bookingId}
                    className="hover:bg-white/5 transition-all duration-300"
                  >

                    <td className="px-6 py-5 font-semibold">
                      #{b.bookingId}
                    </td>

                    <td className="px-6 py-5 text-gray-300">
                      {b.fromLocation || "—"} → {b.toLocation || "—"}
                    </td>

                    <td className="px-6 py-5 text-gray-400">
                      {b.travelDate || "—"}
                    </td>

                    <td className="px-6 py-5">
                      <StatusBadge status={b.status} />
                    </td>

                    <td className="px-6 py-5 text-right">

                      {b.status !== "CANCELLED" && (

                        confirmCancelId === b.bookingId ? (

                          <div className="flex justify-end gap-3">

                            <button
                              className="px-5 py-2 rounded-full border border-red-500 text-red-400 hover:bg-red-500 hover:text-white text-xs font-semibold transition-all duration-300"
                              onClick={() => cancelBooking(b.bookingId)}
                            >
                              Confirm
                            </button>

                            <button
                              className="px-4 py-2 rounded-full border border-white/20 text-xs hover:border-brand-gold hover:text-brand-gold transition"
                              onClick={() => setConfirmCancelId(null)}
                            >
                              Undo
                            </button>

                          </div>

                        ) : (

                          <button
                            className="px-4 py-2 rounded-full border border-red-500 text-red-400 hover:bg-red-500 hover:text-white text-xs transition"
                            onClick={() => setConfirmCancelId(b.bookingId)}
                          >
                            Cancel
                          </button>

                        )

                      )}

                    </td>

                  </tr>

                ))}

              </tbody>

            </table>

          </div>

        )}

      </div>

      {/* ================= PAGINATION ================= */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-6">

          <button
            disabled={page === 0}
            onClick={() => setPage(p => p - 1)}
            className="px-6 py-2 rounded-full border border-white/20 hover:border-brand-gold hover:text-brand-gold transition-all duration-300 disabled:opacity-30"
          >
            Previous
          </button>

          <span className="text-gray-400 text-sm">
            Page {page + 1} of {totalPages}
          </span>

          <button
            disabled={page + 1 >= totalPages}
            onClick={() => setPage(p => p + 1)}
            className="px-5 py-2 rounded-full border border-white/20 disabled:opacity-30 hover:border-brand-gold hover:text-brand-gold transition"
          >
            Next
          </button>

        </div>
      )}

    </div>
  );
}

/* ================= STATUS BADGE ================= */

function StatusBadge({ status }) {

  const base = "px-4 py-1.5 rounded-full text-xs font-semibold tracking-wide";

  if (!status)
    return <span className={`${base} bg-gray-700 text-gray-300`}>Unknown</span>;

  if (status === "CONFIRMED")
    return <span className={`${base} bg-green-500/15 text-green-400`}>Confirmed</span>;

  if (status === "CANCELLED")
    return <span className={`${base} bg-red-500/15 text-red-400`}>Cancelled</span>;

  if (status === "PAYMENT_PENDING")
    return <span className={`${base} bg-yellow-500/15 text-yellow-400`}>Payment Pending</span>;

  return <span className={`${base} bg-brand-gold/15 text-brand-gold`}>{status}</span>;
}