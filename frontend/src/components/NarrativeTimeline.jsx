import React from "react";

export default function NarrativeTimeline({ stops = [] }) {

  if (!stops.length) return null;

  return (
    <section className="space-y-20">

      {/* ================= HEADER ================= */}
      <div className="text-center space-y-4">

        <span className="text-xs uppercase tracking-widest text-gray-500">
          Temporal Passage
        </span>

        <h2 className="text-4xl font-serif text-brand-gold">
          The Journey Unfolds
        </h2>

        <p className="text-gray-400 max-w-2xl mx-auto">
          Each stop represents a civilizational node —
          layered with memory, continuity, and meaning.
        </p>

      </div>

      {/* ================= TIMELINE ================= */}
      <div className="relative max-w-4xl mx-auto">

        {/* Vertical Spine */}
        <div className="absolute left-6 top-0 bottom-0 w-px bg-gradient-to-b from-transparent via-brand-gold/40 to-transparent" />

        <div className="space-y-16">

          {stops.map((stop, index) => (

            <div
              key={stop.id}
              className="relative flex gap-10 opacity-0 animate-fadeInUp"
              style={{ animationDelay: `${index * 120}ms` }}
            >

              {/* Marker */}
              <div className="relative z-10">

                <div className="w-12 h-12 rounded-full bg-gray-900 border border-brand-gold flex items-center justify-center text-brand-gold font-semibold shadow-glow">
                  {index + 1}
                </div>

                <div className="absolute inset-0 rounded-full border border-brand-gold/50 animate-ping opacity-30" />

              </div>

              {/* Content */}
              <div className="space-y-3">

                <h3 className="text-xl font-semibold">
                  {stop.name}
                </h3>

                {stop.eraName && (
                  <div className="text-xs uppercase tracking-widest text-brand-gold/70">
                    {stop.eraName}
                  </div>
                )}

                <p className="text-gray-400 leading-relaxed max-w-2xl">
                  {stop.shortHistory ||
                    "A significant civilizational landmark shaping the continuity of Bharat."}
                </p>

              </div>

            </div>

          ))}

        </div>

      </div>

    </section>
  );
}