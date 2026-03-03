import {
  useEffect,
  useRef
} from "react";

import { motion, AnimatePresence } from "framer-motion";

/* ========================================================= */

export default function Modal({
  open,
  onClose,
  title,
  children,
  width = "max-w-2xl"
}) {

  const overlayRef = useRef(null);
  const modalRef = useRef(null);
  const previouslyFocused = useRef(null);

  /* =========================
     SCROLL LOCK
  ========================= */

  useEffect(() => {
    if (!open) return;

    const original = document.body.style.overflow;
    document.body.style.overflow = "hidden";

    return () => {
      document.body.style.overflow = original;
    };
  }, [open]);

  /* =========================
     ESC KEY CLOSE
  ========================= */

  useEffect(() => {
    if (!open) return;

    const handleKey = (e) => {
      if (e.key === "Escape") {
        onClose?.();
      }
    };

    document.addEventListener("keydown", handleKey);
    return () => {
      document.removeEventListener("keydown", handleKey);
    };
  }, [open, onClose]);

  /* =========================
     FOCUS MANAGEMENT
  ========================= */

  useEffect(() => {
    if (!open) return;

    previouslyFocused.current = document.activeElement;

    const focusable =
      modalRef.current?.querySelectorAll(
        "button, [href], input, select, textarea, [tabindex]:not([tabindex='-1'])"
      );

    focusable?.[0]?.focus();

    return () => {
      previouslyFocused.current?.focus?.();
    };
  }, [open]);

  /* =========================
     FOCUS TRAP
  ========================= */

  const handleTabTrap = (e) => {
    if (e.key !== "Tab") return;

    const focusable =
      modalRef.current?.querySelectorAll(
        "button, [href], input, select, textarea, [tabindex]:not([tabindex='-1'])"
      );

    if (!focusable || focusable.length === 0) return;

    const first = focusable[0];
    const last = focusable[focusable.length - 1];

    if (e.shiftKey) {
      if (document.activeElement === first) {
        e.preventDefault();
        last.focus();
      }
    } else {
      if (document.activeElement === last) {
        e.preventDefault();
        first.focus();
      }
    }
  };

  /* =========================
     MOTION VARIANTS
  ========================= */

  const backdrop = {
    hidden: { opacity: 0 },
    visible: { opacity: 1 }
  };

  const modal = {
    hidden: { opacity: 0, scale: 0.95, y: 20 },
    visible: { opacity: 1, scale: 1, y: 0 },
    exit: { opacity: 0, scale: 0.95, y: 10 }
  };

  /* ========================= */

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          ref={overlayRef}
          className="fixed inset-0 z-50 flex items-center justify-center"
          initial="hidden"
          animate="visible"
          exit="hidden"
          variants={backdrop}
          transition={{ duration: 0.2 }}
        >

          {/* BACKDROP */}
          <motion.div
            className="absolute inset-0 bg-black/80 backdrop-blur-md"
            onClick={onClose}
          />

          {/* MODAL */}
          <motion.div
            ref={modalRef}
            role="dialog"
            aria-modal="true"
            aria-labelledby="modal-title"
            onKeyDown={handleTabTrap}
            className={`
              relative w-full ${width}
              bg-black border border-white/10
              rounded-3xl p-10 shadow-elevated
              focus:outline-none
            `}
            variants={modal}
            initial="hidden"
            animate="visible"
            exit="exit"
            transition={{
              duration: 0.25,
              ease: [0.22, 1, 0.36, 1]
            }}
          >

            {title && (
              <h3
                id="modal-title"
                className="text-2xl font-serif text-brand-gold mb-8"
              >
                {title}
              </h3>
            )}

            {children}

          </motion.div>

        </motion.div>
      )}
    </AnimatePresence>
  );
}