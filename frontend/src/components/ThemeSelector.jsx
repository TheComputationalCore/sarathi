import React from "react";

export default function ThemeSelector({
  availableThemes = [],
  selectedThemes = [],
  setSelectedThemes,
  onGenerate,
  loading = false,
  maxSelectable = 5,
}) {
  /* =====================================================
     TOGGLE LOGIC
  ===================================================== */
  const toggleTheme = (themeName) => {
    setSelectedThemes((prev) => {
      if (prev.includes(themeName)) {
        return prev.filter((t) => t !== themeName);
      }

      if (prev.length >= maxSelectable) return prev;

      return [...prev, themeName];
    });
  };

  const isLimitReached = selectedThemes.length >= maxSelectable;
  const canGenerate = selectedThemes.length > 0 && !loading;

  /* =====================================================
     UI
  ===================================================== */

  return (
    <section className="space-y-20">

      {/* =====================================================
          HEADER
      ===================================================== */}
      <header className="text-center space-y-4">

        <span className="text-xs uppercase tracking-widest text-gray-500">
          Curated Civilizational Dimensions
        </span>

        <h2 className="text-4xl font-serif text-brand-gold">
          Choose the Lens of Your Journey
        </h2>

        <p className="text-gray-400 max-w-3xl mx-auto">
          Select up to {maxSelectable} thematic dimensions that will
          shape the narrative arc, spatial continuity, and experiential
          depth of your civilizational path.
        </p>

      </header>

      {/* =====================================================
          EMPTY STATE
      ===================================================== */}
      {availableThemes.length === 0 && !loading && (
        <div className="text-center text-gray-500 bg-black/60 border border-white/10 rounded-3xl py-12">
          Civilizational dimensions are currently unavailable.
        </div>
      )}

      {/* =====================================================
          GRID
      ===================================================== */}
      <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-8">

        {availableThemes.map((theme, index) => {
          const isSelected = selectedThemes.includes(theme.name);

          return (
            <button
              key={theme.id}
              type="button"
              onClick={() => toggleTheme(theme.name)}
              disabled={!isSelected && isLimitReached}
              className={`
                relative text-left rounded-3xl p-8 border
                transition-all duration-300
                animate-fadeIn
                ${isSelected
                  ? "bg-brand-gold/10 border-brand-gold shadow-glow"
                  : "bg-black/70 border-white/10 hover:border-brand-gold/60 hover:-translate-y-1"
                }
                ${!isSelected && isLimitReached
                  ? "opacity-40 cursor-not-allowed"
                  : ""
                }
              `}
              style={{ animationDelay: `${index * 60}ms` }}
            >

              {/* ICON */}
              {theme.icon && (
                <div className="text-2xl mb-4 text-brand-gold">
                  {theme.icon}
                </div>
              )}

              {/* BODY */}
              <div className="space-y-3">

                <h4 className="text-lg font-semibold">
                  {theme.name}
                </h4>

                {theme.description && (
                  <p className="text-gray-400 text-sm leading-relaxed">
                    {theme.description}
                  </p>
                )}

              </div>

              {/* SELECTED BADGE */}
              {isSelected && (
                <div className="absolute top-4 right-4 text-xs uppercase tracking-widest text-brand-gold">
                  Selected
                </div>
              )}

            </button>
          );
        })}
      </div>

      {/* =====================================================
          FOOTER
      ===================================================== */}
      <footer className="flex flex-col md:flex-row items-center justify-between gap-6">

        <div className="text-sm text-gray-400 flex items-center gap-4">

          <span>
            {selectedThemes.length} / {maxSelectable} selected
          </span>

          {isLimitReached && (
            <span className="text-brand-gold uppercase tracking-wider text-xs">
              Selection limit reached
            </span>
          )}

        </div>

        <button
          type="button"
          onClick={onGenerate}
          disabled={!canGenerate}
          className={`
            px-10 py-4 rounded-full font-semibold transition-all duration-300
            ${canGenerate
              ? "bg-brand-gold text-black hover:shadow-glow hover:scale-105"
              : "bg-white/10 text-gray-500 cursor-not-allowed"
            }
          `}
        >
          {loading
            ? "Composing Civilizational Journey..."
            : "Compose Civilizational Path →"}
        </button>

      </footer>

    </section>
  );
}