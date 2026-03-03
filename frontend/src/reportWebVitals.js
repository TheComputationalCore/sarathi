/**
 * =========================================================
 * Sarathi – Performance Monitoring Utility
 * =========================================================
 * Usage:
 *   import reportWebVitals from "./reportWebVitals";
 *   reportWebVitals(console.log);
 *
 * Or integrate with analytics:
 *   reportWebVitals(sendToAnalytics);
 */

const reportWebVitals = (onPerfEntry) => {
  if (typeof onPerfEntry !== "function") return;

  // Lazy-load only when needed
  import("web-vitals")
    .then(({ onCLS, onFID, onFCP, onLCP, onTTFB }) => {
      try {
        onCLS(onPerfEntry);
        onFID(onPerfEntry);
        onFCP(onPerfEntry);
        onLCP(onPerfEntry);
        onTTFB(onPerfEntry);
      } catch (err) {
        console.error("Web Vitals error:", err);
      }
    })
    .catch((err) => {
      console.error("Failed to load web-vitals:", err);
    });
};

export default reportWebVitals;
