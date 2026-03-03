import React, { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import api from "../api";

export default function BusList() {

  const [buses, setBuses] = useState([]);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(true);

  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const themeFilter = searchParams.get("themes") || "";

  /* ================= FETCH ROUTES ================= */

  useEffect(() => {

    const fetchBuses = async () => {
      try {
        setIsLoading(true);
        setError("");

        const res = await api.get("/buses", {
          params: themeFilter ? { themes: themeFilter } : {}
        });

        const list = Array.isArray(res.data)
          ? res.data
          : res.data?.data || [];

        setBuses(list);

      } catch (err) {

        if (err.response?.status === 401) {
          localStorage.removeItem("token");
          localStorage.removeItem("role");
          const redirect = encodeURIComponent(`/travel${window.location.search || ""}`);
          navigate(`/login?redirect=${redirect}`);
        } else {
          setError("Unable to load routes. Please try again.");
        }

      } finally {
        setIsLoading(false);
      }
    };

    fetchBuses();

  }, [navigate, themeFilter]);

  /* ================= NAVIGATION ================= */

  const handleBook = (busId) => {
    if (!busId) return;
    const params = new URLSearchParams();
    params.set("busId", busId);
    if (themeFilter) params.set("themes", themeFilter);
    navigate(`/booking?${params.toString()}`);
  };

  /* ================= FORMAT ================= */

  const formatPrice = (price) => {
    if (!price && price !== 0) return "—";
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 0
    }).format(price);
  };

  /* ================= UI ================= */

  return (
    <div className="min-h-screen px-6 pt-32 pb-20">

      <div className="max-w-6xl mx-auto space-y-16">

        {/* Header */}
        <div className="text-center space-y-4 animate-fadeIn">
          <h2 className="text-4xl font-serif text-brand-gold">
            Available Journeys
          </h2>
          <p className="text-gray-400">
            Choose your route and begin your sacred journey
          </p>
          {themeFilter && (
            <p className="text-sm text-brand-gold/90">
              Filtered by themes: {decodeURIComponent(themeFilter)}
            </p>
          )}
        </div>

        {/* Error */}
        {error && (
          <div className="bg-red-500/10 border border-red-500/30 text-red-400 p-4 rounded-xl text-center">
            {error}
          </div>
        )}

        {/* Loading */}
        {isLoading ? (

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">

            {[...Array(6)].map((_, i) => (
              <div
                key={i}
                className="h-40 rounded-2xl bg-gradient-to-r from-gray-800 via-gray-700 to-gray-800 bg-[length:400%_100%] animate-skeleton"
              />
            ))}

          </div>

        ) : buses.length === 0 ? (

          <div className="card-elevated p-16 text-center space-y-4">
            <h4 className="text-xl font-semibold">
              No routes available
            </h4>
            <p className="text-gray-400">
              Please check back later. New journeys are added regularly.
            </p>
          </div>

        ) : (

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">

            {buses.map((bus) => {

              const {
                id,
                fromLocation,
                toLocation,
                price,
                totalSeats
              } = bus;

              return (
                <div
                  key={id}
                  className="card-elevated p-8 flex flex-col justify-between hover:-translate-y-2 transition-all duration-300"
                >

                  {/* Top */}
                  <div className="flex justify-between items-start gap-4">

                    <h4 className="text-lg font-semibold leading-snug">
                      {fromLocation || "—"}
                      <span className="mx-2 text-brand-gold">→</span>
                      {toLocation || "—"}
                    </h4>

                    <div className="px-4 py-2 rounded-full text-sm font-semibold bg-brand-gold text-black whitespace-nowrap">
                      {formatPrice(price)}
                    </div>

                  </div>

                  <div className="h-px bg-white/10 my-6" />

                  {/* Bottom */}
                  <div className="flex justify-between items-center">

                    <div>
                      <span className="text-xs text-gray-400 block mb-1">
                        Total Seats
                      </span>
                      <div className="font-semibold text-lg">
                        {totalSeats ?? "—"}
                      </div>
                    </div>

                    <button
                      onClick={() => handleBook(id)}
                      className="btn-primary"
                    >
                      Select Date
                    </button>

                  </div>

                </div>
              );
            })}

          </div>

        )}

      </div>

    </div>
  );
}
