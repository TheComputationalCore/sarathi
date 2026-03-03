export const fadeInUp = {
  hidden: { opacity: 0, y: 15 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.4 }
  }
};

export const staggerContainer = {
  hidden: {},
  visible: {
    transition: {
      staggerChildren: 0.08
    }
  }
};

export const scaleIn = {
  hidden: { opacity: 0, scale: 0.95 },
  visible: {
    opacity: 1,
    scale: 1,
    transition: { duration: 0.25 }
  }
};

export const slideInRight = {
  hidden: { x: 40, opacity: 0 },
  visible: {
    x: 0,
    opacity: 1,
    transition: { duration: 0.3 }
  }
};