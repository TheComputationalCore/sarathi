import React from "react";

export default function TrailSummary({ trail }) {

  if (!trail) return null;

  const stopsCount = trail.stops?.length ?? 0;
  const themes = Array.isArray(trail.themes) ? trail.themes : [];

  const narrative =
    trail.narrativeSummary ||
    "A curated civilizational arc connecting geography, devotion, and historical continuity.";

  return (
    <section className="py-24">

      <div className="max-w-5xl mx-auto px-6 text-center space-y-12">

        {/* =====================================================
            PRELUDE
        ===================================================== */}
        <div className="flex flex-col items-center gap-3">

          <span className="text-xs uppercase tracking-widest text-gray-500">
            Civilizational Composition
          </span>

          <span className="text-sm text-brand-gold uppercase tracking-wider">
            {stopsCount} Heritage {stopsCount === 1 ? "Node" : "Nodes"}
          </span>

        </div>

        {/* =====================================================
            TITLE
        ===================================================== */}
        <h1 className="text-5xl md:text-6xl font-serif text-brand-gold leading-tight">
          {trail.trailName || "Untitled Civilizational Trail"}
        </h1>

        {/* =====================================================
            THEMES
        ===================================================== */}
        {themes.length > 0 && (
          <div className="flex flex-wrap justify-center gap-4">

            {themes.map((theme, index) => (
              <span
                key={`${theme}-${index}`}
                className="px-5 py-2 rounded-full text-xs uppercase tracking-widest bg-brand-gold/10 border border-brand-gold/40 text-brand-gold"
              >
                {theme}
              </span>
            ))}

          </div>
        )}

        {/* =====================================================
            DIVIDER
        ===================================================== */}
        <div className="h-px bg-gradient-to-r from-transparent via-brand-gold/40 to-transparent" />

        {/* =====================================================
            NARRATIVE
        ===================================================== */}
        <p className="text-gray-400 text-lg leading-relaxed max-w-3xl mx-auto">
          {narrative}
        </p>

      </div>

    </section>
  );
}