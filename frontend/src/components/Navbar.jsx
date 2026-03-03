import React, { useState, useEffect, useCallback, useMemo } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";

export default function Navbar() {

  const navigate = useNavigate();
  const location = useLocation();

  const [scrolled, setScrolled] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);

  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");

  const isAuthenticated = !!token;
  const isAdmin = role === "ROLE_ADMIN";
  const isUser = role === "ROLE_USER";

  /* ================= SCROLL ================= */

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  /* ================= CLOSE ON ROUTE CHANGE ================= */

  useEffect(() => {
    setMobileOpen(false);
  }, [location.pathname]);

  /* ================= LOGOUT ================= */

  const handleLogout = useCallback(() => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    navigate("/", { replace: true });
  }, [navigate]);

  const isActive = useCallback(
    (path) => location.pathname.startsWith(path),
    [location.pathname]
  );

  /* ================= LINKS ================= */

  const userLinks = useMemo(() => {
    if (!isAuthenticated) return [];

    if (isAdmin) {
      return [
        { to: "/admin", label: "Admin Console" },
        { to: "/travel", label: "Mobility Routes" }
      ];
    }

    if (isUser) {
      return [
        { to: "/travel", label: "Explore Routes" },
        { to: "/profile", label: "My Journeys" }
      ];
    }

    return [];
  }, [isAuthenticated, isAdmin, isUser]);

  /* ================= UI ================= */

  return (
    <>
      <nav
        className={`fixed w-full z-50 transition-all duration-500
          ${scrolled
            ? "backdrop-blur-xl bg-black/90 shadow-elevated border-b border-white/10"
            : "backdrop-blur-lg bg-black/70"
          }`}
      >
        <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">

          {/* ================= BRAND ================= */}
          <Link to="/" className="flex flex-col group">

            <span className="text-2xl font-serif bg-gradient-to-r from-yellow-200 to-brand-gold bg-clip-text text-transparent transition-all duration-300 group-hover:tracking-wide">
              Sarathi
            </span>

            <span className="text-[10px] uppercase tracking-[0.25em] text-gray-400 mt-1">
              Civilizational Mobility Platform
            </span>

          </Link>

          {/* ================= DESKTOP ================= */}
          <div className="hidden md:flex items-center gap-10 text-sm">

            {isAuthenticated ? (
              <>
                {userLinks.map(link => (
                  <Link
                    key={link.to}
                    to={link.to}
                    className={`relative transition-all duration-300
                      ${isActive(link.to)
                        ? "text-brand-gold"
                        : "text-gray-400 hover:text-brand-gold"
                      }`}
                  >
                    {link.label}

                    {isActive(link.to) && (
                      <span className="absolute -bottom-2 left-0 w-full h-px bg-brand-gold" />
                    )}
                  </Link>
                ))}

                <button
                  onClick={handleLogout}
                  className="px-5 py-2 rounded-full border border-white/20 text-gray-300 hover:border-brand-gold hover:text-brand-gold transition-all duration-300"
                >
                  Exit
                </button>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  className="text-gray-400 hover:text-brand-gold transition"
                >
                  Login
                </Link>

                <Link
                  to="/register"
                  className="px-6 py-2 rounded-full border border-brand-gold text-brand-gold hover:bg-brand-gold hover:text-black transition-all duration-300 shadow-glow"
                >
                  Join Sarathi
                </Link>
              </>
            )}

          </div>

          {/* ================= MOBILE TOGGLE ================= */}
          <button
            onClick={() => setMobileOpen(!mobileOpen)}
            className="md:hidden relative w-8 h-8 flex flex-col justify-center items-center gap-1"
          >
            <span
              className={`w-6 h-0.5 bg-white transition-all duration-300
                ${mobileOpen ? "rotate-45 translate-y-1.5" : ""}
              `}
            />
            <span
              className={`w-6 h-0.5 bg-white transition-all duration-300
                ${mobileOpen ? "opacity-0" : ""}
              `}
            />
            <span
              className={`w-6 h-0.5 bg-white transition-all duration-300
                ${mobileOpen ? "-rotate-45 -translate-y-1.5" : ""}
              `}
            />
          </button>

        </div>
      </nav>

      {/* ================= MOBILE MENU ================= */}
      <div
        className={`fixed inset-0 z-40 transition-all duration-500
          ${mobileOpen
            ? "opacity-100 pointer-events-auto"
            : "opacity-0 pointer-events-none"
          }`}
      >
        {/* Backdrop */}
        <div
          onClick={() => setMobileOpen(false)}
          className="absolute inset-0 bg-black/90 backdrop-blur-xl"
        />

        {/* Menu */}
        <div className={`relative h-full flex flex-col items-center justify-center gap-8 text-xl`}>

          {isAuthenticated ? (
            <>
              {userLinks.map(link => (
                <Link
                  key={link.to}
                  to={link.to}
                  className="text-gray-300 hover:text-brand-gold transition"
                >
                  {link.label}
                </Link>
              ))}

              <button
                onClick={handleLogout}
                className="text-gray-300 hover:text-brand-gold transition"
              >
                Exit
              </button>
            </>
          ) : (
            <>
              <Link
                to="/login"
                className="text-gray-300 hover:text-brand-gold transition"
              >
                Login
              </Link>

              <Link
                to="/register"
                className="text-brand-gold"
              >
                Join Sarathi
              </Link>
            </>
          )}

        </div>
      </div>
    </>
  );
}