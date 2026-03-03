import React,
{
  useEffect,
  useState,
  useRef,
  lazy,
  Suspense
} from "react";

import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";

import { apiClient } from "../api";
import { useGlobalToast } from "../core/hooks/useGlobalToast";
import {
  fadeInUp,
  staggerContainer
} from "../core/motion/motionVariants";

import Skeleton from "../ui/Skeleton";
import ThemeSelector from "../components/ThemeSelector";
import TrailSummary from "../components/TrailSummary";
import NarrativeTimeline from "../components/NarrativeTimeline";

const HeritageMap = lazy(() => import("../components/HeritageMap"));

/* ========================================================= */

export default function CivilizationalExperience() {

  const navigate = useNavigate();
  const { error: showError } = useGlobalToast();

  const resultRef = useRef(null);
  const themeRef = useRef(null);

  const [availableThemes, setAvailableThemes] = useState([]);
  const [selectedThemes, setSelectedThemes] = useState([]);
  const [trail, setTrail] = useState(null);
  const [loading, setLoading] = useState(false);

  /* ================= FETCH THEMES ================= */

  useEffect(() => {
    const loadThemes = async () => {
      const res = await apiClient.get("/themes");

      if (!res.success) {
        showError("Unable to load civilizational themes.");
        return;
      }

      setAvailableThemes(res.data || []);
    };

    loadThemes();
  }, [showError]);

  /* ================= GENERATE TRAIL ================= */

  const generateTrail = async () => {

    if (!selectedThemes.length || loading) return;

    setLoading(true);
    setTrail(null);

    const res = await apiClient.post("/trails/generate", {
      themes: selectedThemes,
      maxStops: 6,
    });

    if (!res.success) {
      showError("We couldn’t compose your journey.");
      setLoading(false);
      return;
    }

    const trailData = res?.data?.data;

    if (!trailData?.stops?.length) {
      showError("No heritage nodes matched your themes.");
      setLoading(false);
      return;
    }

    setTrail(trailData);
    setLoading(false);

    setTimeout(() => {
      resultRef.current?.scrollIntoView({
        behavior: "smooth",
        block: "start",
      });
    }, 300);
  };

  /* ================= NAVIGATION ================= */

  const handleTravel = () => {
    const themeParam = selectedThemes.join(",");
    const travelPath = `/travel${themeParam ? `?themes=${encodeURIComponent(themeParam)}` : ""}`;
    const token = localStorage.getItem("token");

    if (!token) {
      return navigate(`/login?redirect=${encodeURIComponent(travelPath)}`);
    }

    navigate(travelPath);
  };

  const resetJourney = () => {
    setTrail(null);

    setTimeout(() => {
      themeRef.current?.scrollIntoView({
        behavior: "smooth",
        block: "start",
      });
    }, 250);
  };

  /* ========================================================= */

  return (
    <div className="bg-black text-white">

      {/* ================= HERO ================= */}

      <motion.section
  variants={fadeInUp}
  initial="hidden"
  animate="visible"
  className="relative min-h-screen flex items-center justify-center text-center overflow-hidden"
>

  {/* Background Video */}
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

 

  

  {/* Content */}
  <div className="relative z-10 max-w-5xl mx-auto space-y-10 px-6">

    <h1
      className="text-5xl md:text-6xl font-serif text-brand-gold leading-tight"
      style={{
        textShadow: "0 4px 30px rgba(0,0,0,0.6)"
      }}
    >
      5,000 Years.<br />
      One Living Civilization.
    </h1>

    <p className="text-gray-200 text-lg max-w-3xl mx-auto leading-relaxed">
      Compose an intelligent passage across Bharat’s sacred geography —
      where devotion, continuity, and movement converge.
    </p>

    {!trail ? (
      <button
        onClick={() =>
          themeRef.current?.scrollIntoView({
            behavior: "smooth",
          })
        }
        className="px-10 py-4 rounded-full bg-brand-gold text-black font-semibold hover:scale-105 transition"
      >
        Begin Composition
      </button>
    ) : (
      <button
        onClick={resetJourney}
        className="px-10 py-4 rounded-full border border-brand-gold text-brand-gold hover:bg-brand-gold hover:text-black transition"
      >
        Recompose Journey
      </button>
    )}

  </div>
</motion.section>

      {/* ================= THEME SECTION ================= */}

      {!trail && (
        <motion.section
          ref={themeRef}
          variants={fadeInUp}
          initial="hidden"
          animate="visible"
          className="py-28 px-6 max-w-6xl mx-auto"
        >
          <ThemeSelector
            availableThemes={availableThemes}
            selectedThemes={selectedThemes}
            setSelectedThemes={setSelectedThemes}
            onGenerate={generateTrail}
            loading={loading}
          />
        </motion.section>
      )}

      {/* ================= LOADING ================= */}

      {loading && (
        <section className="px-6 pb-20 max-w-4xl mx-auto text-center space-y-8">
          <Skeleton className="h-12 w-64 mx-auto" />
          <Skeleton className="h-64 w-full" />
          <Skeleton className="h-64 w-full" />
        </section>
      )}

      {/* ================= RESULT ================= */}

      {trail && !loading && (
        <motion.section
          ref={resultRef}
          variants={staggerContainer}
          initial="hidden"
          animate="visible"
          className="px-6 pb-32 max-w-7xl mx-auto space-y-20"
        >

          <motion.div variants={fadeInUp}>
            <TrailSummary trail={trail} />
          </motion.div>

          <div className="grid lg:grid-cols-2 gap-16 items-start">

            <motion.div variants={fadeInUp}>
              <NarrativeTimeline stops={trail.stops} />
            </motion.div>

            <motion.div
              variants={fadeInUp}
              className="sticky top-32"
            >
              <Suspense
                fallback={<Skeleton className="h-[500px] w-full" />}
              >
                <HeritageMap stops={trail.stops} />
              </Suspense>
            </motion.div>

          </div>

          <motion.div
            variants={fadeInUp}
            className="pt-12 text-center"
          >
            <button
              onClick={handleTravel}
              className="px-12 py-5 rounded-full bg-brand-gold text-black font-semibold hover:scale-105 transition"
            >
              Continue This Civilizational Path →
            </button>
          </motion.div>

        </motion.section>
      )}

    </div>
  );
}