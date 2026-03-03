import { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api";

export default function Profile() {

  const navigate = useNavigate();
  const token = localStorage.getItem("token");

  const [user, setUser] = useState(null);
  const [bookings, setBookings] = useState([]);
  const [error, setError] = useState("");

  const [isLoading, setIsLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);

  const [formData, setFormData] = useState({
    name: "",
    email: "",
    password: ""
  });

  /* =========================================================
     LOAD PROFILE
  ========================================================= */

  const loadProfile = useCallback(async () => {

    setIsLoading(true);
    setError("");

    try {

      const [userRes, bookingsRes] = await Promise.all([
        api.get("/users/profile"),
        api.get("/bookings/history")
      ]);

      const userData = userRes.data?.data || null;

      if (!userData) {
        throw new Error("Invalid profile response.");
      }

      setUser(userData);

      setFormData({
        name: userData?.name || "",
        email: userData?.email || "",
        password: ""
      });

      setBookings(Array.isArray(bookingsRes.data)
        ? bookingsRes.data
        : []);

    } catch (err) {

      if (err?.status === 401) {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        navigate("/login", { replace: true });
      } else {
        setError(err?.message || "Failed to load account details.");
      }

    } finally {
      setIsLoading(false);
    }

  }, [navigate]);

  useEffect(() => {
    if (!token) {
      navigate("/login", { replace: true });
      return;
    }
    loadProfile();
  }, [token, navigate, loadProfile]);

  /* =========================================================
     PROFILE UPDATE
  ========================================================= */

  const handleChange = (e) => {
    const { name, value } = e.target;

    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const toggleEdit = () => {
    setIsEditing(prev => !prev);
    setFormData(prev => ({ ...prev, password: "" }));
  };

  const handleUpdate = async (e) => {

    e.preventDefault();
    if (isUpdating) return;

    setIsUpdating(true);
    setError("");

    try {

      const res = await api.put("/users/profile", {
        name: formData.name.trim(),
        email: formData.email.trim(),
        password: formData.password || null
      });

      setUser(res.data?.data || null);
      setIsEditing(false);

    } catch (err) {
      setError(err?.message || "Profile update failed.");
    } finally {
      setIsUpdating(false);
    }
  };

  /* =========================================================
     BOOKING ACTIONS
  ========================================================= */

  const handleCancel = async (bookingId) => {
    if (!window.confirm("Cancel this reservation?")) return;

    try {
      await api.delete(`/bookings/${bookingId}`);

      setBookings(prev =>
        prev.map(b =>
          b.bookingId === bookingId
            ? { ...b, status: "CANCELLED" }
            : b
        )
      );

    } catch (err) {
      alert(err?.message || "Cancellation failed.");
    }
  };

  const downloadTicket = async (bookingId) => {

    try {

      const res = await api.get(`/tickets/${bookingId}`, {
        responseType: "blob"
      });

      const blob = new Blob([res.data], { type: "application/pdf" });
      const url = window.URL.createObjectURL(blob);

      const link = document.createElement("a");
      link.href = url;
      link.download = `Sarathi_Ticket_${bookingId}.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();

      window.URL.revokeObjectURL(url);

    } catch {
      alert("Ticket download failed.");
    }
  };

  const goToPayment = (bookingId) => {
    navigate(`/payment?bookingId=${bookingId}`);
  };

  /* =========================================================
     LOADING STATE
  ========================================================= */

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-brand-primary">
        <div className="w-14 h-14 rounded-full border-4 border-white/10 border-t-brand-gold animate-spin" />
      </div>
    );
  }

  /* =========================================================
     UI
  ========================================================= */

  return (
    <div className="min-h-screen px-6 pt-32 pb-20 bg-gradient-to-br from-black via-brand-primary to-black">

      <div className="max-w-6xl mx-auto space-y-20">

        {/* HEADER */}
        <header className="text-center space-y-4">
          <h2 className="text-4xl font-serif text-brand-gold">
            Personal Command Center
          </h2>
          <p className="text-gray-400">
            Manage your identity and monitor your civilizational journeys.
          </p>
        </header>

        {error && (
          <div className="bg-red-500/10 border border-red-500/30 text-red-400 p-4 rounded-xl">
            {error}
          </div>
        )}

        {/* PROFILE CARD */}
        {user && (
          <section className="bg-black/80 backdrop-blur-xl border border-white/10 rounded-3xl p-12 shadow-elevated">

            {!isEditing ? (
              <div className="flex flex-col md:flex-row items-center justify-between gap-8">

                <div className="flex items-center gap-6">

                  <div className="w-20 h-20 rounded-full bg-brand-gold text-black flex items-center justify-center text-2xl font-bold shadow-glow">
                    {user.name?.charAt(0)?.toUpperCase() || "U"}
                  </div>

                  <div>
                    <h4 className="text-xl font-semibold">
                      {user.name}
                    </h4>
                    <span className="text-gray-400 text-sm">
                      {user.email}
                    </span>
                  </div>

                </div>

                <button
                  onClick={toggleEdit}
                  className="px-6 py-3 rounded-full border border-brand-gold text-brand-gold hover:bg-brand-gold hover:text-black transition-all duration-300"
                >
                  Edit Details
                </button>

              </div>
            ) : (
              <form onSubmit={handleUpdate} className="space-y-10">

                <div className="grid md:grid-cols-3 gap-6">

                  <input
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    required
                    placeholder="Full Name"
                    className="bg-black/60 border border-white/10 rounded-xl px-5 py-4 focus:outline-none focus:border-brand-gold"
                  />

                  <input
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    required
                    placeholder="Email Address"
                    className="bg-black/60 border border-white/10 rounded-xl px-5 py-4 focus:outline-none focus:border-brand-gold"
                  />

                  <input
                    type="password"
                    name="password"
                    placeholder="New password (optional)"
                    value={formData.password}
                    onChange={handleChange}
                    className="bg-black/60 border border-white/10 rounded-xl px-5 py-4 focus:outline-none focus:border-brand-gold"
                  />

                </div>

                <div className="flex gap-6 flex-wrap">

                  <button
                    disabled={isUpdating}
                    className="px-8 py-3 rounded-full bg-brand-gold text-black font-semibold hover:shadow-glow transition-all"
                  >
                    {isUpdating ? "Saving..." : "Save Changes"}
                  </button>

                  <button
                    type="button"
                    onClick={toggleEdit}
                    className="px-8 py-3 rounded-full border border-white/20 text-gray-300 hover:border-brand-gold hover:text-brand-gold transition"
                  >
                    Cancel
                  </button>

                </div>

              </form>
            )}

          </section>
        )}

        {/* BOOKING HISTORY */}
        <section className="space-y-8">

          <h3 className="text-2xl font-serif text-brand-gold">
            Journey Archive
          </h3>

          {bookings.length === 0 ? (
            <div className="bg-black/70 border border-white/10 rounded-3xl p-12 text-center text-gray-400">
              No journeys recorded yet.
            </div>
          ) : (
            <div className="overflow-x-auto bg-black/80 border border-white/10 rounded-3xl p-8">

              <table className="w-full text-sm">

                <thead className="text-left text-gray-400 uppercase text-xs tracking-wider">
                  <tr>
                    <th className="pb">ID</th>
                    <th>Route</th>
                    <th>Seats</th>
                    <th>Date</th>
                    <th>Status</th>
                    <th></th>
                  </tr>
                </thead>

                <tbody className="divide-y divide-white/10">

                  {bookings.map((b) => (
                    <tr key={b.bookingId} className="hover:bg-white/5 transition">

                      <td className="py-4">#{b.bookingId}</td>

                      <td>{b.fromLocation} → {b.toLocation}</td>

                      <td>
                        {Array.isArray(b.seatNumbers)
                          ? b.seatNumbers.join(", ")
                          : "—"}
                      </td>

                      <td>{b.travelDate || "—"}</td>

                      <td>
                        <span className={`px-4 py-1 rounded-full text-xs font-semibold
                          ${b.status === "CONFIRMED" && "bg-emerald-500/20 text-emerald-400"}
                          ${b.status === "CANCELLED" && "bg-red-500/20 text-red-400"}
                          ${(b.status === "PAYMENT_PENDING" || b.status === "PENDING") && "bg-brand-gold/20 text-brand-gold"}
                        `}>
                          {b.status}
                        </span>
                      </td>

                      <td className="text-right space-x-3">

                        {b.status === "PAYMENT_PENDING" && (
                          <button
                            onClick={() => goToPayment(b.bookingId)}
                            className="text-brand-gold hover:underline"
                          >
                            Complete Payment
                          </button>
                        )}

                        {b.status === "CONFIRMED" && (
                          <>
                            <button
                              onClick={() => downloadTicket(b.bookingId)}
                              className="text-emerald-400 hover:underline"
                            >
                              Download
                            </button>

                            <button
                              onClick={() => handleCancel(b.bookingId)}
                              className="text-red-400 hover:underline"
                            >
                              Cancel
                            </button>
                          </>
                        )}

                      </td>

                    </tr>
                  ))}

                </tbody>

              </table>

            </div>
          )}

        </section>

      </div>
    </div>
  );
}
