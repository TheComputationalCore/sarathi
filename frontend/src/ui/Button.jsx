import React from "react";

export default function Button({
  children,
  variant = "primary",
  size = "md",
  loading = false,
  disabled = false,
  className = "",
  type = "button",
  ...props
}) {
  const isDisabled = loading || disabled;

  const base =
    `
    relative inline-flex items-center justify-center gap-2
    font-medium rounded-full
    transition-all duration-300 ease-out
    active:scale-[0.97]
    disabled:opacity-40 disabled:cursor-not-allowed
    focus:outline-none focus:ring-2 focus:ring-brand-gold/40
    `;

  const sizes = {
    sm: "px-4 py-2 text-xs",
    md: "px-6 py-3 text-sm",
    lg: "px-8 py-4 text-base"
  };

  const variants = {
    primary: `
      border border-brand-gold
      text-brand-gold
      hover:bg-brand-gold hover:text-black
      shadow-glow hover:shadow-[0_0_60px_rgba(212,175,55,0.35)]
      hover:-translate-y-[2px]
    `,

    secondary: `
      border border-white/20
      text-white
      hover:border-brand-gold
      hover:text-brand-gold
      hover:-translate-y-[2px]
    `,

    danger: `
      border border-red-500/50
      text-red-400
      hover:bg-red-500/10
      hover:-translate-y-[2px]
    `,

    solid: `
      bg-brand-gold
      text-black
      hover:brightness-110
      shadow-glow
      hover:-translate-y-[2px]
    `,

    ghost: `
      text-white
      hover:text-brand-gold
      hover:-translate-y-[2px]
    `
  };

  const resolvedVariant = variants[variant] || variants.primary;
  const resolvedSize = sizes[size] || sizes.md;

  return (
    <button
      type={type}
      disabled={isDisabled}
      aria-disabled={isDisabled}
      className={`${base} ${resolvedSize} ${resolvedVariant} ${className}`}
      {...props}
    >
      {/* Subtle internal sheen layer */}
      {!isDisabled && (
        <span className="
          pointer-events-none absolute inset-0 rounded-full
          bg-gradient-to-tr from-white/5 to-transparent
          opacity-0 hover:opacity-100
          transition-opacity duration-300
        " />
      )}

      {loading ? (
        <>
          <span
            className="
              w-4 h-4
              border-2 border-current border-t-transparent
              rounded-full animate-spin
            "
          />
          <span>Processing...</span>
        </>
      ) : (
        <span className="relative z-10">
          {children}
        </span>
      )}
    </button>
  );
}