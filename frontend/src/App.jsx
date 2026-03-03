import React, {
  Suspense,
  lazy,
  useEffect,
  useRef
} from "react";

import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
  useLocation
} from "react-router-dom";

import { AnimatePresence, motion } from "framer-motion";
import { Toaster } from "react-hot-toast";
import toast from "react-hot-toast";

import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

import "leaflet/dist/leaflet.css";
import "./styles/leaflet-overrides.css";
import "./App.css";

/* =========================================================
   CORE UI
========================================================= */

import Navbar from "./components/Navbar";
import AdminLayout from "./layouts/AdminLayout";
import CivilizationalExperience from "./pages/CivilizationalExperience";

/* =========================================================
   LAZY LOAD
========================================================= */

const Login = lazy(() => import("./components/Login"));
const Register = lazy(() => import("./components/Register"));
const Booking = lazy(() => import("./components/Booking"));
const BusList = lazy(() => import("./components/BusList"));
const Profile = lazy(() => import("./components/Profile"));
const Payment = lazy(() => import("./components/Payment"));

const AdminDashboard = lazy(() => import("./pages/AdminDashboard"));
const AdminRoutes = lazy(() => import("./pages/AdminRoutes"));
const AdminBookings = lazy(() => import("./pages/AdminBookings"));
const AdminUsers = lazy(() => import("./pages/AdminUsers"));

/* =========================================================
   PAGE TRANSITION WRAPPER
========================================================= */

const pageVariants = {
  initial: { opacity: 0, y: 10 },
  animate: { opacity: 1, y: 0 },
  exit: { opacity: 0, y: -10 }
};

function PageTransition({ children }) {
  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      exit="exit"
      transition={{ duration: 0.25, ease: "easeInOut" }}
    >
      {children}
    </motion.div>
  );
}

/* =========================================================
   LOADING SKELETON
========================================================= */

function SkeletonPage() {
  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="animate-pulse space-y-6 w-full max-w-4xl px-6">
        <div className="h-12 bg-neutral-800 rounded w-1/3" />
        <div className="h-64 bg-neutral-900 rounded-xl" />
        <div className="h-64 bg-neutral-900 rounded-xl" />
      </div>
    </div>
  );
}

/* =========================================================
   SCROLL RESTORE
========================================================= */

function ScrollToTop() {
  const { pathname } = useLocation();

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: "instant" });
  }, [pathname]);

  return null;
}

/* =========================================================
   PUBLIC LAYOUT
========================================================= */

function PublicLayout({ children }) {
  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />
      <main className="flex-1 w-full max-w-7xl mx-auto px-6 pt-32 pb-20">
        {children}
      </main>
    </div>
  );
}

/* =========================================================
   ROUTE GUARDS
========================================================= */

const PrivateRoute = ({ children }) => {
  const location = useLocation();
  const token = localStorage.getItem("token");
  if (token) return children;

  const redirect = encodeURIComponent(`${location.pathname}${location.search}`);
  return <Navigate to={`/login?redirect=${redirect}`} replace />;
};

const AdminRoute = ({ children }) => {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");

  if (!token) return <Navigate to="/login" replace />;
  if (role !== "ROLE_ADMIN") return <Navigate to="/journey" replace />;

  return children;
};

/* =========================================================
   404 PAGE
========================================================= */

function NotFound() {
  return (
    <div className="min-h-screen flex items-center justify-center text-center px-6">
      <div>
        <h1 className="text-5xl font-bold text-gold mb-4">404</h1>
        <p className="text-neutral-400 mb-6">
          The path you seek is not on this civilizational map.
        </p>
        <button
          onClick={() => (window.location.href = "/")}
          className="px-6 py-3 bg-gold text-black rounded-lg"
        >
          Return Home
        </button>
      </div>
    </div>
  );
}

/* =========================================================
   LIVE BOOKING SOCKET
========================================================= */

function useLiveBookingSocket() {
  const clientRef = useRef(null);
  const token = localStorage.getItem("token");

  useEffect(() => {
    if (!token) return;
    if (clientRef.current?.active) return;

    const baseUrl =
      process.env.REACT_APP_WS_URL ||
      process.env.REACT_APP_API_URL?.replace("/api", "") ||
      window.location.origin;

    const socket = new SockJS(`${baseUrl}/ws`);

    const client = new Client({
      webSocketFactory: () => socket,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: () => {},

      onConnect: () => {
        client.subscribe("/topic/live-bookings", (message) => {
          if (!message.body) return;

          try {
            const event = JSON.parse(message.body);

            toast(event.message || "A traveler joined the journey.", {
              icon: "🛕",
              duration: 4000,
            });

          } catch (err) {
            console.error("Invalid WebSocket message", err);
          }
        });
      },

      onStompError: (frame) => {
        console.error("Broker error:", frame.headers["message"]);
      }
    });

    client.activate();
    clientRef.current = client;

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
        clientRef.current = null;
      }
    };

  }, [token]);
}

/* =========================================================
   APP CONTENT
========================================================= */

function AppContent() {
  const location = useLocation();
  useLiveBookingSocket();

  return (
    <>
      <ScrollToTop />

      <Suspense fallback={<SkeletonPage />}>
        <AnimatePresence mode="wait">
          <Routes location={location} key={location.pathname}>

            {/* PUBLIC */}
            <Route
              path="/"
              element={
                <Navigate to="/journey" replace />
              }
            />

            <Route
              path="/journey"
              element={
                <PageTransition>
                  <PublicLayout>
                    <CivilizationalExperience />
                  </PublicLayout>
                </PageTransition>
              }
            />

            <Route
              path="/login"
              element={
                <PageTransition>
                  <PublicLayout>
                    <Login />
                  </PublicLayout>
                </PageTransition>
              }
            />

            <Route
              path="/register"
              element={
                <PageTransition>
                  <PublicLayout>
                    <Register />
                  </PublicLayout>
                </PageTransition>
              }
            />

            {/* PRIVATE */}
            <Route
              path="/travel"
              element={
                <PrivateRoute>
                  <PageTransition>
                    <PublicLayout>
                      <BusList />
                    </PublicLayout>
                  </PageTransition>
                </PrivateRoute>
              }
            />

            <Route
              path="/booking"
              element={
                <PrivateRoute>
                  <PageTransition>
                    <PublicLayout>
                      <Booking />
                    </PublicLayout>
                  </PageTransition>
                </PrivateRoute>
              }
            />

            <Route
              path="/payment"
              element={
                <PrivateRoute>
                  <PageTransition>
                    <PublicLayout>
                      <Payment />
                    </PublicLayout>
                  </PageTransition>
                </PrivateRoute>
              }
            />

            <Route
              path="/profile"
              element={
                <PrivateRoute>
                  <PageTransition>
                    <PublicLayout>
                      <Profile />
                    </PublicLayout>
                  </PageTransition>
                </PrivateRoute>
              }
            />

            {/* ADMIN */}
            <Route
              path="/admin"
              element={
                <AdminRoute>
                  <AdminLayout />
                </AdminRoute>
              }
            >
              <Route index element={<AdminDashboard />} />
              <Route path="overview" element={<AdminDashboard />} />
              <Route path="routes" element={<AdminRoutes />} />
              <Route path="bookings" element={<AdminBookings />} />
              <Route path="users" element={<AdminUsers />} />
            </Route>

            {/* 404 */}
            <Route path="*" element={<NotFound />} />

          </Routes>
        </AnimatePresence>
      </Suspense>
    </>
  );
}

/* =========================================================
   ROOT
========================================================= */

function App() {
  return (
    <Router>
      <Toaster
        position="top-right"
        toastOptions={{
          style: {
            background: "#111",
            color: "#fff",
            border: "1px solid rgba(212,175,55,0.4)"
          }
        }}
      />
      <AppContent />
    </Router>
  );
}

export default App;
