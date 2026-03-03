export default function Card({
  children,
  variant = "elevated",
  className = "",
  ...props
}) {
  const base =
    "rounded-3xl transition-all duration-300";

  const variants = {
    elevated:
      "bg-white/5 backdrop-blur-xl border border-white/10 shadow-elevated hover:border-brand-gold/40",

    subtle:
      "bg-black/40 border border-white/10",

    glass:
      "bg-white/5 backdrop-blur-2xl border border-white/10",

    danger:
      "bg-red-500/5 border border-red-500/30"
  };

  return (
    <div
      className={`${base} ${variants[variant]} ${className}`}
      {...props}
    >
      {children}
    </div>
  );
}