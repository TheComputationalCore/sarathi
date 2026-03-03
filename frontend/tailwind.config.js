/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    container: {
      center: true,
      padding: "1.5rem",
      screens: {
        "2xl": "1280px",
      },
    },
    extend: {
      colors: {
        brand: {
          gold: "#D4AF37",
          goldSoft: "#E6C766",
          primary: "#0B0F19",
          elevated: "#161E2E",
          surface: "#111827",
        },
      },

      fontFamily: {
        serif: ["Playfair Display", "serif"],
        sans: ["Inter", "system-ui", "sans-serif"],
      },

      boxShadow: {
        glow: "0 0 40px rgba(212,175,55,0.25)",
        elevated: "0 20px 60px rgba(0,0,0,0.45)",
        soft: "0 10px 30px rgba(0,0,0,0.35)",
      },

      borderRadius: {
        xl2: "1.75rem",
      },

      /* ===============================
         ANIMATIONS — CLEAN CONSOLIDATED
      =============================== */

      animation: {
        fadeIn: "fadeIn 0.6s ease forwards",
        fadeInUp: "fadeInUp 0.6s ease forwards",
        popIn: "popIn 0.4s ease forwards",
        seatPulse: "seatPulse 2.2s ease-out infinite",
        skeleton: "skeleton 1.4s ease infinite",
      },

      keyframes: {
        fadeIn: {
          "0%": { opacity: "0", transform: "translateY(20px)" },
          "100%": { opacity: "1", transform: "translateY(0)" },
        },

        fadeInUp: {
          "0%": { opacity: "0", transform: "translateY(20px)" },
          "100%": { opacity: "1", transform: "translateY(0)" },
        },

        popIn: {
          "0%": { transform: "scale(0.85)", opacity: "0" },
          "100%": { transform: "scale(1)", opacity: "1" },
        },

        seatPulse: {
          "0%": { boxShadow: "0 0 0 0 rgba(212,175,55,0.4)" },
          "70%": { boxShadow: "0 0 0 14px rgba(212,175,55,0)" },
          "100%": { boxShadow: "0 0 0 0 rgba(212,175,55,0)" },
        },

        skeleton: {
          "0%": { backgroundPosition: "100% 50%" },
          "100%": { backgroundPosition: "0 50%" },
        },
      },
    },
  },
  plugins: [],
};