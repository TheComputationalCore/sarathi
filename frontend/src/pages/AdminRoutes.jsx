import {
  useEffect,
  useMemo,
  useState,
  useCallback,
  useRef
} from "react";

import { apiClient } from "../api";
import { useGlobalToast } from "../core/hooks/useGlobalToast";

import Button from "../ui/Button";
import Input from "../ui/Input";
import Modal from "../ui/Modal";

/* ========================================================= */

export default function AdminRoutes() {

  const { success, error } = useGlobalToast();

  const [buses, setBuses] = useState([]);
  const [loading, setLoading] = useState(true);

  const [search, setSearch] = useState("");
  const [debouncedSearch, setDebouncedSearch] = useState("");

  const [showModal, setShowModal] = useState(false);
  const [editingBus, setEditingBus] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);

  const emptyForm = {
    fromLocation: "",
    toLocation: "",
    departureTime: "",
    arrivalTime: "",
    price: "",
    totalSeats: 40,
  };

  const [form, setForm] = useState(emptyForm);

  const initialLoadRef = useRef(true);

  /* ================= SEARCH DEBOUNCE ================= */

  useEffect(() => {
    const t = setTimeout(() => {
      setDebouncedSearch(search.trim());
    }, 350);
    return () => clearTimeout(t);
  }, [search]);

  /* ================= LOAD ================= */

  const loadBuses = useCallback(async () => {
    setLoading(true);

    const res = await apiClient.get("/buses");

    if (res.success) {
      setBuses(Array.isArray(res.data) ? res.data : []);
    } else {
      error(res.message);
      setBuses([]);
    }

    setLoading(false);
  }, [error]);

  useEffect(() => {
    if (initialLoadRef.current) {
      initialLoadRef.current = false;
      loadBuses();
    }
  }, [loadBuses]);

  /* ================= FILTER ================= */

  const filtered = useMemo(() => {
    return buses.filter((b) =>
      `${b.fromLocation} ${b.toLocation}`
        .toLowerCase()
        .includes(debouncedSearch.toLowerCase())
    );
  }, [buses, debouncedSearch]);

  /* ================= MODAL CONTROL ================= */

  const openAdd = () => {
    setEditingBus(null);
    setForm(emptyForm);
    setShowModal(true);
  };

  const openEdit = (bus) => {
    setEditingBus(bus);
    setForm({
      fromLocation: bus.fromLocation || "",
      toLocation: bus.toLocation || "",
      departureTime: toInputDT(bus.departureTime),
      arrivalTime: toInputDT(bus.arrivalTime),
      price: bus.price ?? "",
      totalSeats: bus.totalSeats ?? 40,
    });
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
    setEditingBus(null);
  };

  /* ================= SAVE (OPTIMISTIC) ================= */

  const saveBus = async () => {

    if (!form.fromLocation || !form.toLocation) {
      error("Please complete required fields.");
      return;
    }

    const payload = {
      ...form,
      price: Number(form.price),
      totalSeats: Number(form.totalSeats),
    };

    if (editingBus) {

      const original = buses;

      const optimistic = buses.map(b =>
        b.id === editingBus.id
          ? { ...b, ...payload }
          : b
      );

      setBuses(optimistic);

      const res = await apiClient.put(
        `/admin/buses/${editingBus.id}`,
        payload
      );

      if (!res.success) {
        setBuses(original);
        error(res.message);
        return;
      }

      success("Route updated successfully.");
    } else {

      const res = await apiClient.post(`/admin/buses`, payload);

      if (!res.success) {
        error(res.message);
        return;
      }

      setBuses(prev => [...prev, res.data]);
      success("Route created successfully.");
    }

    closeModal();
  };

  /* ================= DELETE (OPTIMISTIC) ================= */

  const deleteBus = async () => {

    if (!deleteTarget) return;

    const original = buses;

    setBuses(prev =>
      prev.filter(b => b.id !== deleteTarget.id)
    );

    const res = await apiClient.delete(
      `/admin/buses/${deleteTarget.id}`
    );

    if (!res.success) {
      setBuses(original);
      error(res.message);
      return;
    }

    success("Route removed.");
    setDeleteTarget(null);
  };

  /* ================= LOADING SKELETON ================= */

  if (loading) {
    return (
      <div className="space-y-8 animate-pulse">
        <div className="h-12 w-64 bg-white/10 rounded-lg" />
        <div className="h-12 w-80 bg-white/10 rounded-xl" />
        <div className="grid md:grid-cols-2 xl:grid-cols-3 gap-6">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="h-40 bg-white/10 rounded-2xl" />
          ))}
        </div>
      </div>
    );
  }

  /* ================= UI ================= */

  return (
    <div className="space-y-16 animate-fadeInUp">

      {/* HEADER */}

      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">

        <div className="space-y-2">
          <h2 className="text-4xl font-serif text-brand-gold">
            Route Operations
          </h2>
          <p className="text-gray-400">
            Active mobility corridors across the network.
          </p>
        </div>

        <div className="flex gap-4 items-center flex-wrap">
          <Input
            placeholder="Search routes..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-64"
          />
          <Button onClick={openAdd}>
            Add Route
          </Button>
        </div>
      </div>

      {/* CONTENT (Card View Only Shown Here For Brevity) */}

      <div className="grid md:grid-cols-2 xl:grid-cols-3 gap-10">
        {filtered.map((b) => (
          <div
            key={b.id}
            className="
              bg-black/40 backdrop-blur-lg
              border border-white/10
              rounded-2xl p-6
              transition-all duration-300
              hover:-translate-y-1
              hover:border-brand-gold/40
              hover:shadow-glow
              space-y-4
            "
          >
            <div>
              <div className="text-lg font-semibold">
                {b.fromLocation} → {b.toLocation}
              </div>
              <div className="text-xs text-gray-400 mt-1">
                {fmtDT(b.departureTime)} — {fmtDT(b.arrivalTime)}
              </div>
            </div>

            <div className="flex justify-between text-sm text-gray-400">
              <span>₹{b.price}</span>
              <span>{b.totalSeats} seats</span>
            </div>

            <div className="flex gap-3 pt-4">
              <Button
                variant="secondary"
                className="flex-1"
                onClick={() => openEdit(b)}
              >
                Edit
              </Button>

              <Button
                variant="danger"
                className="flex-1"
                onClick={() => setDeleteTarget(b)}
              >
                Remove
              </Button>
            </div>
          </div>
        ))}
      </div>

      {/* CREATE / EDIT MODAL */}

      <Modal
        open={showModal}
        onClose={closeModal}
        title={editingBus ? "Edit Route" : "Create Route"}
      >
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

        <div className="flex justify-end gap-4 mt-8">
          <Button variant="secondary" onClick={closeModal}>
            Cancel
          </Button>
          <Button variant="primary" onClick={saveBus}>
            {editingBus ? "Save Changes" : "Create Route"}
          </Button>
        </div>
      </Modal>

      {/* DELETE CONFIRM MODAL */}

      <Modal
        open={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        title="Confirm Removal"
      >
        <p className="text-gray-400">
          This action cannot be undone.
        </p>

        <div className="flex justify-end gap-4 mt-8">
          <Button
            variant="secondary"
            onClick={() => setDeleteTarget(null)}
          >
            Cancel
          </Button>
          <Button
            variant="danger"
            onClick={deleteBus}
          >
            Confirm Delete
          </Button>
        </div>
      </Modal>

    </div>
  );
}

/* ========================================================= */

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
