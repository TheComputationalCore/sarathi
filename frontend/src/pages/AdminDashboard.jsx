import { useEffect, useMemo, useState, useCallback } from "react";
import api from "../api";
import Button from "../ui/Button";
import Input from "../ui/Input";

export default function AdminDashboard() {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({});
  const [routes, setRoutes] = useState([]);

  const [query, setQuery] = useState("");
  const [debouncedQuery, setDebouncedQuery] = useState("");

  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [confirmDeleteId, setConfirmDeleteId] = useState(null);

  const emptyForm = {
    fromLocation: "",
    toLocation: "",
    departureTime: "",
    arrivalTime: "",
    price: "",
    totalSeats: "",
  };

  const [form, setForm] = useState(emptyForm);

  /* ================= SEARCH DEBOUNCE ================= */

  useEffect(() => {
    const t = setTimeout(() => setDebouncedQuery(query.trim()), 400);
    return () => clearTimeout(t);
  }, [query]);

  /* ================= LOAD ================= */

  const loadAll = useCallback(async () => {
    try {
      setLoading(true);

      let dashboardData;

      try {
        const dash = await api.get("/admin/dashboard");
        dashboardData = dash.data;
      } catch {
        const legacy = await api.get("/admin/stats");
        dashboardData = legacy.data;
      }

      const buses = await api.get("/buses");

      setStats(dashboardData || {});
      setRoutes(Array.isArray(buses.data) ? buses.data : []);
    } catch (e) {
      console.error("Admin load error:", e);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAll();
  }, [loadAll]);

  /* ================= CRUD ================= */

  const openAdd = () => {
    setEditing(null);
    setForm(emptyForm);
    setShowModal(true);
  };

  const openEdit = (route) => {
    setEditing(route);
    setForm({
      fromLocation: route.fromLocation || "",
      toLocation: route.toLocation || "",
      departureTime: toInputDT(route.departureTime),
      arrivalTime: toInputDT(route.arrivalTime),
      price: route.price ?? "",
      totalSeats: route.totalSeats ?? "",
    });
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
    setEditing(null);
  };

  const saveRoute = async () => {
    if (!form.fromLocation || !form.toLocation) return;

    try {
      const payload = {
        ...form,
        price: Number(form.price),
        totalSeats: Number(form.totalSeats),
      };

      if (editing) {
        const res = await api.put(`/admin/buses/${editing.id}`, payload);
        setRoutes(prev =>
          prev.map(r => r.id === editing.id ? res.data : r)
        );
      } else {
        const res = await api.post(`/admin/buses`, payload);
        setRoutes(prev => [...prev, res.data]);
      }

      closeModal();
    } catch (e) {
      console.error("Save route error:", e);
    }
  };

  const deleteRoute = async (id) => {
    try {
      await api.delete(`/admin/buses/${id}`);
      setRoutes(prev => prev.filter(r => r.id !== id));
      setConfirmDeleteId(null);
    } catch (e) {
      console.error("Delete route error:", e);
    }
  };

  /* ================= FILTER ================= */

  const filteredRoutes = useMemo(() =>
    routes.filter(r =>
      `${r.fromLocation} ${r.toLocation}`
        .toLowerCase()
        .includes(debouncedQuery.toLowerCase())
    ),
    [routes, debouncedQuery]
  );

  /* ================= LOADING ================= */

  if (loading) {
    return (
      <div className="space-y-10 animate-pulse">
        <div className="h-10 w-72 bg-white/10 rounded-lg" />
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-5 gap-6">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={i} className="h-32 bg-white/10 rounded-2xl" />
          ))}
        </div>
        <div className="h-80 bg-white/10 rounded-3xl" />
      </div>
    );
  }

  /* ================= UI ================= */

  return (
    <div className="space-y-28 animate-fadeInUp">

      {/* HEADER */}
      <div className="space-y-4">
        <h2 className="text-4xl md:text-5xl font-serif text-brand-gold tracking-tight">
          Executive Operations Overview
        </h2>
        <p className="text-gray-400 max-w-2xl text-lg">
          Real-time performance intelligence and route governance.
        </p>
      </div>

      {/* KPI GRID */}
      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-5 gap-8">
        <KPI title="Total Bookings" value={stats?.totalBookings ?? 0} delay="0ms" />
        <KPI title="Confirmed" value={stats?.confirmed ?? stats?.confirmedBookings ?? 0} delay="50ms" />
        <KPI title="Cancelled" value={stats?.cancelled ?? stats?.cancelledBookings ?? 0} delay="100ms" />
        <KPI title="Revenue" value={`₹${round2(stats?.totalRevenue ?? 0)}`} delay="150ms" />
        <KPI title="Active Routes" value={routes.length} delay="200ms" />
      </div>

      {/* ROUTE GOVERNANCE */}
      <div className="card-elevated p-14 space-y-16">

        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">

          <h3 className="text-2xl font-serif text-brand-gold">
            Yatra Route Governance
          </h3>

          <div className="flex gap-4 items-center">
            <Input
              placeholder="Search routes..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              className="w-64"
            />
            <Button onClick={openAdd}>
              Add Route
            </Button>
          </div>

        </div>

        {filteredRoutes.length === 0 ? (
          <div className="text-center text-gray-500 py-20">
            No routes found.
          </div>
        ) : (
          <div className="grid md:grid-cols-2 xl:grid-cols-3 gap-10">
            {filteredRoutes.map((r) => (
              <RouteCard
                key={r.id}
                route={r}
                confirmDeleteId={confirmDeleteId}
                setConfirmDeleteId={setConfirmDeleteId}
                openEdit={openEdit}
                deleteRoute={deleteRoute}
              />
            ))}
          </div>
        )}

      </div>

      {/* MODAL */}
      {showModal && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-md flex items-center justify-center z-50">

          <div className="card-elevated p-14 w-full max-w-2xl space-y-12 animate-popIn">

            <h4 className="text-2xl font-serif text-brand-gold">
              {editing ? "Edit Yatra Route" : "Create Yatra Route"}
            </h4>

            <div className="grid md:grid-cols-2 gap-6">
              {Object.keys(emptyForm).map((key) => (
                <Input
                  key={key}
                  type={
                    key.includes("Time")
                      ? "datetime-local"
                      : key === "price" || key === "totalSeats"
                      ? "number"
                      : "text"
                  }
                  placeholder={key.replace(/([A-Z])/g, " $1")}
                  value={form[key]}
                  onChange={(e) =>
                    setForm({ ...form, [key]: e.target.value })
                  }
                />
              ))}
            </div>

            <div className="flex justify-end gap-4">
              <Button variant="secondary" onClick={closeModal}>
                Cancel
              </Button>
              <Button variant="primary" onClick={saveRoute}>
                {editing ? "Save Changes" : "Create Route"}
              </Button>
            </div>

          </div>
        </div>
      )}

    </div>
  );
}

/* ================= KPI ================= */

function KPI({ title, value, delay }) {
  return (
    <div
      style={{ animationDelay: delay }}
      className="
        bg-gradient-to-br from-brand-elevated to-black/40
        border border-white/5
        rounded-2xl p-8
        shadow-soft
        hover:shadow-glow
        transition-all duration-300
        animate-fadeInUp
      "
    >
      <div className="text-gray-500 text-xs uppercase tracking-widest mb-4">
        {title}
      </div>
      <div className="text-4xl font-semibold text-brand-gold tracking-tight">
        {value}
      </div>
    </div>
  );
}

/* ================= ROUTE CARD ================= */

function RouteCard({
  route,
  confirmDeleteId,
  setConfirmDeleteId,
  openEdit,
  deleteRoute
}) {
  return (
    <div className="card-elevated p-8 space-y-6 transition-all duration-300 hover:-translate-y-1">
      <div>
        <div className="text-lg font-semibold">
          {route.fromLocation} → {route.toLocation}
        </div>
        <div className="text-xs text-gray-400 mt-1">
          {fmtDT(route.departureTime)} – {fmtDT(route.arrivalTime)}
        </div>
      </div>

      <div className="flex justify-between text-sm text-gray-400">
        <span>₹{route.price}</span>
        <span>{route.totalSeats} seats</span>
      </div>

      <div className="flex gap-3 pt-4">
        <Button variant="secondary" className="flex-1" onClick={() => openEdit(route)}>
          Edit
        </Button>

        {confirmDeleteId === route.id ? (
          <>
            <Button variant="danger" className="flex-1" onClick={() => deleteRoute(route.id)}>
              Confirm
            </Button>
            <Button variant="secondary" className="flex-1" onClick={() => setConfirmDeleteId(null)}>
              Undo
            </Button>
          </>
        ) : (
          <Button variant="danger" className="flex-1" onClick={() => setConfirmDeleteId(route.id)}>
            Remove
          </Button>
        )}
      </div>
    </div>
  );
}

/* ================= HELPERS ================= */

function fmtDT(v) {
  if (!v) return "—";
  try { return new Date(v).toLocaleString(); }
  catch { return v; }
}

function toInputDT(v) {
  if (!v) return "";
  const d = new Date(v);
  const pad = (n) => `${n}`.padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function round2(n) {
  return Math.round((Number(n) + Number.EPSILON) * 100) / 100;
}