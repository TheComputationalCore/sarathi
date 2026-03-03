import React, { useState, useMemo, useEffect } from "react";
import { motion } from "framer-motion";
import { useQuery, useMutation } from "@tanstack/react-query";
import { apiClient } from "../../api";
import { useGlobalToast } from "../../core/hooks/useGlobalToast";
import {
  fadeInUp,
  staggerContainer,
  scaleIn
} from "../../core/motion/motionVariants";

import Skeleton from "../../ui/Skeleton";

import {
  MapContainer,
  TileLayer,
  Marker,
  Polyline,
  Popup,
  useMap
} from "react-leaflet";

import L from "leaflet";

/* =====================================================
   NUMBERED MARKER
===================================================== */

const createNumberedIcon = (index) =>
  L.divIcon({
    className: "",
    html: `
      <div class="w-10 h-10 rounded-full bg-black border border-[#D4AF37] flex items-center justify-center text-[#D4AF37] font-semibold shadow-[0_0_20px_rgba(212,175,55,0.5)]">
        ${index}
      </div>
    `,
    iconSize: [40, 40],
    iconAnchor: [20, 40],
  });

/* =====================================================
   FIT BOUNDS
===================================================== */

function FitBounds({ positions }) {
  const map = useMap();

  useEffect(() => {
    if (positions.length > 1) {
      map.fitBounds(positions, { padding: [100, 100] });
    }
  }, [positions, map]);

  return null;
}

/* =====================================================
   ANIMATED POLYLINE
===================================================== */

function AnimatedPolyline({ positions }) {
  const [visiblePositions, setVisiblePositions] = useState([]);

  useEffect(() => {
    if (!positions || positions.length < 2) return;

    setVisiblePositions([]);
    let index = 0;

    const interval = setInterval(() => {
      index++;
      setVisiblePositions(positions.slice(0, index));
      if (index >= positions.length) clearInterval(interval);
    }, 250);

    return () => clearInterval(interval);
  }, [positions]);

  if (visiblePositions.length < 2) return null;

  return (
    <Polyline
      positions={visiblePositions}
      pathOptions={{
        color: "#D4AF37",
        weight: 4,
        opacity: 0.9,
      }}
      className="journey-path"
    />
  );
}

/* =====================================================
   STAGGERED MARKER
===================================================== */

function DelayedMarker({ stop, index }) {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const t = setTimeout(() => setVisible(true), index * 300);
    return () => clearTimeout(t);
  }, [index]);

  if (!visible) return null;

  return (
    <Marker
      position={[stop.latitude, stop.longitude]}
      icon={createNumberedIcon(index + 1)}
    >
      <Popup>
        <div className="text-sm text-gray-300">
          <strong className="text-brand-gold">{stop.name}</strong>
          <p className="mt-2 text-gray-400">
            {stop.shortHistory ||
              "A significant civilizational landmark."}
          </p>
        </div>
      </Popup>
    </Marker>
  );
}

/* =====================================================
   MAIN COMPONENT
===================================================== */

export default function CivilizationalExperience() {

  const toast = useGlobalToast();

  const [selectedThemes, setSelectedThemes] = useState([]);
  const [trail, setTrail] = useState(null);

  /* ================= THEMES QUERY ================= */

  const {
    data: themes = [],
    isLoading: themesLoading,
    isError: themesError
  } = useQuery({
    queryKey: ["themes"],
    queryFn: async () => {
      const res = await apiClient.get("/themes");
      if (!res.success) throw new Error(res.message);
      return res.data;
    },
    staleTime: 1000 * 60 * 5,
  });

  /* ================= TRAIL MUTATION ================= */

  const generateMutation = useMutation({
    mutationFn: async (themes) => {
      if (!themes.length) {
        throw new Error("Select at least one theme.");
      }

      const res = await apiClient.post("/trails/generate", {
        themes,
        maxStops: 6,
      });

      if (!res.success) throw new Error(res.message);

      const trailData = res?.data?.data;
      if (!trailData?.stops?.length) {
        throw new Error("No heritage nodes matched your selection.");
      }

      return trailData;
    },
    onSuccess: (data) => setTrail(data),
    onError: (err) =>
      toast.error(err.message || "Unable to generate journey.")
  });

  const toggleTheme = (name) => {
    setSelectedThemes(prev =>
      prev.includes(name)
        ? prev.filter(t => t !== name)
        : [...prev, name]
    );
  };

  const stops = trail?.stops || [];

  const polylinePositions = useMemo(
    () => stops.map(p => [p.latitude, p.longitude]),
    [stops]
  );

  /* =====================================================
     UI
  ===================================================== */

  return (
    <div className="space-y-36 pb-36">

      {/* ================= HERO ================= */}
      <motion.section
  variants={fadeInUp}
  initial="hidden"
  animate="visible"
  className="relative min-h-screen flex items-center justify-center text-center overflow-hidden"
>

  <video
    autoPlay
    muted
    loop
    playsInline
    preload="none"
    className="absolute inset-0 w-full h-full object-cover"
  >
    <source src="/videos/hero.mp4" type="video/mp4" />
  </video>

  {/* REMOVE OVERLAY TEMPORARILY */}

  <div className="relative z-10 text-white">
    TEST
  </div>

</motion.section>

      {/* ================= THEMES ================= */}

      <motion.section
        variants={staggerContainer}
        initial="hidden"
        animate="visible"
        className="container-elite space-y-28"
      >

        <div className="text-center space-y-6">
          <h2 className="text-5xl md:text-6xl font-serif tracking-tight">
            Choose Civilizational Dimensions
          </h2>

          <div className="w-24 h-px bg-brand-gold mx-auto opacity-60" />

          <p className="text-gray-400 max-w-2xl mx-auto text-lg">
            Select the civilizational forces that will shape your journey.
          </p>
        </div>

        <div className="grid md:grid-cols-3 gap-10">

          {themesLoading &&
            Array.from({ length: 3 }).map((_, i) => (
              <Skeleton key={i} className="h-48 w-full rounded-3xl" />
            ))}

          {themesError && (
            <div className="text-center text-red-400 col-span-full py-20">
              Unable to load themes.
            </div>
          )}

          {themes.map(theme => {
            const selected = selectedThemes.includes(theme.name);

            return (
              <motion.div
                key={theme.id}
                variants={scaleIn}
                whileTap={{ scale: 0.97 }}
                onClick={() => toggleTheme(theme.name)}
                className={`
                  card-elevated p-12 cursor-pointer transition-all duration-300 space-y-6
                  ${selected
                    ? "border-brand-gold shadow-glow"
                    : "hover:-translate-y-1 hover:border-brand-gold/40"}
                `}
              >
                <div className="space-y-4">
                  <h4 className="text-xl font-semibold tracking-tight">
                    {theme.name}
                  </h4>

                  {theme.description && (
                    <p className="text-gray-400 text-sm leading-relaxed">
                      {theme.description}
                    </p>
                  )}
                </div>

                {selected && (
                  <div className="pt-4 text-xs uppercase tracking-[0.2em] text-brand-gold">
                    Selected Dimension
                  </div>
                )}
              </motion.div>
            );
          })}

        </div>

      </motion.section>

      {/* ================= RESULTS ================= */}

      {trail && (
        <motion.section
          variants={staggerContainer}
          initial="hidden"
          animate="visible"
          className="container-elite space-y-24"
        >

          <motion.div variants={fadeInUp} className="text-center space-y-6">
            <h2 className="text-5xl md:text-6xl font-serif tracking-tight">
              {trail.trailName}
            </h2>

            <div className="w-24 h-px bg-brand-gold mx-auto opacity-60" />

            <p className="text-gray-400 max-w-3xl mx-auto text-lg">
              {trail.narrativeSummary}
            </p>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, scale: 0.98 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.6 }}
            className="card-elevated overflow-hidden"
          >
            <MapContainer
              center={[23.5937, 80.9629]}
              zoom={5}
              scrollWheelZoom
              className="h-[650px] w-full"
            >
              <TileLayer
                attribution="© OpenStreetMap contributors"
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />

              {stops.map((stop, index) => (
                <DelayedMarker
                  key={stop.id}
                  stop={stop}
                  index={index}
                />
              ))}

              <AnimatedPolyline positions={polylinePositions} />
              <FitBounds positions={polylinePositions} />
            </MapContainer>
          </motion.div>

        </motion.section>
      )}

    </div>
  );
}