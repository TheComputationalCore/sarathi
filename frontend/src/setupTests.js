/**
 * =========================================================
 * Sarathi – Test Setup
 * =========================================================
 * Global test configuration for React Testing Library
 */

// Optional: Silence React 18 act() warnings in tests
// Uncomment only if needed
// globalThis.IS_REACT_ACT_ENVIRONMENT = true;

// Optional: Mock window.scrollTo to prevent errors in tests
if (!window.scrollTo) {
  window.scrollTo = () => {};
}
