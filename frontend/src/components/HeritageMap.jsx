import React, { useEffect, useMemo } from "react";
import {
  MapContainer,
  TileLayer,
  Marker,
  Popup,
  Polyline,
  useMap
} from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

/* ================= NUMBERED MARKER ================= */

const createNumberedIcon = (index) =>
  L.divIcon({
    className: "civil-map-marker-wrapper",
    html: `
      <div class="civil-map-marker-inner">
        <span>${index}</span>
      </div>
      <div class="civil-map-marker-pulse"></div>
    `,
    iconSize: [44, 44],
    iconAnchor: [22, 44]
  });

/* ================= AUTO FIT ================= */

function FitBounds({ positions }) {
  const map = useMap();

  useEffect(() => {
    if (positions.length > 1) {
      map.fitBounds(positions, {
        padding: [120, 120],
        animate: true,
        duration: 1.2
      });
    } else if (positions.length === 1) {
      map.setView(positions[0], 7, { animate: true });
    }
  }, [positions, map]);

  return null;
}

/* ================= MAIN ================= */

export default function HeritageMap({ stops = [] }) {

  const validStops = useMemo(() => {
    return stops.filter(
      s =>
        typeof s.latitude === "number" &&
        typeof s.longitude === "number"
    );
  }, [stops]);

  if (!validStops.length) return null;

  const polylinePositions = validStops.map(
    p => [p.latitude, p.longitude]
  );

  return (
    <section className="space-y-12">

      {/* Header */}
      <div className="text-center space-y-4">
        <span className="text-xs uppercase tracking-widest text-gray-500">
          Spatial Continuity
        </span>

        <h2 className="text-4xl font-serif text-brand-gold">
          Geographic Procession
        </h2>

        <p className="text-gray-400 max-w-2xl mx-auto">
          A sacred line connecting memory, devotion, and geography.
        </p>
      </div>

      {/* Map Stage */}
      <div className="relative rounded-3xl overflow-hidden border border-white/10 shadow-elevated">

        <MapContainer
          center={[23.5937, 80.9629]}
          zoom={5}
          scrollWheelZoom
          className="h-[600px] w-full contrast-105 brightness-95"
        >

          <TileLayer
            attribution="© OpenStreetMap"
            url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
          />

          {polylinePositions.length > 1 && (
            <Polyline
              positions={polylinePositions}
              pathOptions={{
                color: "#D4AF37",
                weight: 5,
                opacity: 0.9
              }}
            />
          )}

          {validStops.map((point, index) => (
            <Marker
              key={point.id}
              position={[point.latitude, point.longitude]}
              icon={createNumberedIcon(index + 1)}
            >
              <Popup>
                <div className="max-w-xs space-y-2">
                  <h4 className="font-semibold text-brand-gold">
                    {point.name}
                  </h4>
                  <p className="text-sm text-gray-400 leading-relaxed">
                    {point.shortHistory ||
                      "A significant civilizational landmark."}
                  </p>
                </div>
              </Popup>
            </Marker>
          ))}

          <FitBounds positions={polylinePositions} />

        </MapContainer>

      </div>

    </section>
  );
}