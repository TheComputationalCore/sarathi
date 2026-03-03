import { Link, Outlet, useNavigate, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import Button from "../ui/Button";

export default function AdminLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");

  /* ================= ACCESS GUARD ================= */

  useEffect(() => {
    if (!token || role !== "ROLE_ADMIN") {
      navigate("/login", { replace: true });
    }
  }, [token, role, navigate]);

  /* ================= LOCK SCROLL ================= */

  useEffect(() => {
    document.body.style.overflow = sidebarOpen ? "hidden" : "";
  }, [sidebarOpen]);

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    navigate("/login", { replace: true });
  };

  const isActive = (path) =>
    location.pathname === path ||
    location.pathname.startsWith(path + "/");

  /* ========================================================= */

  return (
    <div className="min-h-screen bg-brand-primary text-white flex">

      {/* ================= SIDEBAR ================= */}

      <aside
        className={`
          fixed md:static z-40 top-0 left-0 h-full w-80
          bg-gradient-to-b from-black via-[#0B0F19] to-brand-primary
          border-r border-white/5
          backdrop-blur-xl
          transition-transform duration-300 ease-out
          ${sidebarOpen ? "translate-x-0" : "-translate-x-full md:translate-x-0"}
        `}
      >

        {/* Gold Vertical Rail */}
        <div className="absolute right-0 top-0 h-full w-[2px] bg-gradient-to-b from-transparent via-brand-gold/40 to-transparent" />

        {/* BRAND */}
        <div className="px-10 py-10 border-b border-white/5 space-y-3">

          <div className="text-4xl font-serif text-brand-gold tracking-tight">
            Sarathi
          </div>

          <div className="text-xs uppercase tracking-[0.3em] text-gray-600">
            Executive Console
          </div>

        </div>

        {/* NAVIGATION */}
        <nav className="px-8 py-12 space-y-14 text-sm">

          <NavSection title="Overview">
            <NavItem
              to="/admin"
              active={location.pathname === "/admin"}
              onClick={() => setSidebarOpen(false)}
            >
              Executive Dashboard
            </NavItem>
          </NavSection>

          <NavSection title="Operations">
            <NavItem
              to="/admin/routes"
              active={isActive("/admin/routes")}
              onClick={() => setSidebarOpen(false)}
            >
              Yatra Routes
            </NavItem>

            <NavItem
              to="/admin/bookings"
              active={isActive("/admin/bookings")}
              onClick={() => setSidebarOpen(false)}
            >
              Reservations
            </NavItem>

            <NavItem
              to="/admin/users"
              active={isActive("/admin/users")}
              onClick={() => setSidebarOpen(false)}
            >
              User Registry
            </NavItem>
          </NavSection>

        </nav>

      </aside>

      {/* ================= OVERLAY ================= */}

      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black/70 backdrop-blur-sm z-30 md:hidden transition-opacity"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* ================= MAIN AREA ================= */}

      <div className="flex-1 flex flex-col min-h-screen">

        {/* ================= HEADER ================= */}

        <header className="relative flex items-center justify-between px-12 py-7 border-b border-white/5 bg-black/60 backdrop-blur-2xl">

          {/* Top Gradient Line */}
          <div className="absolute bottom-0 left-0 w-full h-[1px] bg-gradient-to-r from-transparent via-brand-gold/50 to-transparent" />

          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="md:hidden text-2xl text-brand-gold hover:scale-105 transition"
          >
            ☰
          </button>

          <div className="text-gray-500 text-sm tracking-[0.25em] uppercase">
            Administrator Access
          </div>

          <div className="flex items-center gap-6">

            <Link
              to="/"
              className="text-gray-400 hover:text-brand-gold transition text-sm tracking-wide"
            >
              Public Experience
            </Link>

            <Button variant="secondary" onClick={logout}>
              Sign Out
            </Button>

          </div>

        </header>

        {/* ================= CONTENT ================= */}

        <main className="flex-1 px-12 py-16 max-w-7xl w-full mx-auto animate-fadeInUp">
          <Outlet />
        </main>

      </div>

    </div>
  );
}

/* =========================================================
   NAV SECTION
========================================================= */

function NavSection({ title, children }) {
  return (
    <div className="space-y-6">
      <div className="text-xs uppercase tracking-[0.35em] text-gray-600">
        {title}
      </div>
      <div className="space-y-3">
        {children}
      </div>
    </div>
  );
}

/* =========================================================
   NAV ITEM
========================================================= */

function NavItem({ to, active, children, onClick }) {
  return (
    <Link
      to={to}
      onClick={onClick}
      className={`
        relative block px-5 py-3 rounded-xl
        transition-all duration-300 ease-out
        ${active
          ? "bg-brand-gold text-black shadow-glow"
          : "text-gray-400 hover:bg-white/5 hover:text-white hover:translate-x-1"
        }
      `}
    >
      {active && (
        <span className="absolute left-0 top-0 h-full w-1 bg-brand-gold rounded-r-full" />
      )}
      {children}
    </Link>
  );
}