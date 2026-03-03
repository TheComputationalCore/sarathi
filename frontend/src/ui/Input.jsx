import React from "react";

export default function Input({
  label,
  error,
  description,
  className = "",
  containerClassName = "",
  disabled = false,
  ...props
}) {
  return (
    <div className={`flex flex-col gap-2 ${containerClassName}`}>
      
      {label && (
        <label
          className="
            text-xs uppercase tracking-[0.15em]
            text-gray-500
          "
        >
          {label}
        </label>
      )}

      <div className="relative">
        {/* Sheen Layer */}
        <span
          className="
            pointer-events-none absolute inset-0 rounded-2xl
            bg-gradient-to-tr from-white/5 to-transparent
            opacity-0 focus-within:opacity-100
            transition-opacity duration-300
          "
        />

        <input
          disabled={disabled}
          className={`
            relative z-10
            w-full
            bg-black/40 backdrop-blur-md
            border rounded-2xl
            px-4 py-3
            text-white
            placeholder-gray-500
            transition-all duration-300 ease-out

            border-white/10
            focus:outline-none
            focus:border-brand-gold
            focus:ring-2 focus:ring-brand-gold/30
            focus:-translate-y-[1px]

            disabled:opacity-40
            disabled:cursor-not-allowed

            ${error
              ? "border-red-500 focus:ring-red-500/30 focus:border-red-500"
              : ""
            }

            ${className}
          `}
          {...props}
        />
      </div>

      {description && !error && (
        <span className="text-xs text-gray-500">
          {description}
        </span>
      )}

      {error && (
        <span className="text-xs text-red-400">
          {error}
        </span>
      )}
    </div>
  );
}