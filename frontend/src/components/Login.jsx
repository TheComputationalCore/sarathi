import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { apiClient } from "../api";

export default function Login() {

  const navigate = useNavigate();
  const location = useLocation();

  const [formData, setFormData] = useState({
    email: "",
    password: ""
  });

  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  /* =====================================================
     HANDLE INPUT
  ===================================================== */

  const handleChange = (e) => {
    const { name, value } = e.target;

    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    if (error) setError("");
  };

  /* =====================================================
     VALIDATION
  ===================================================== */

  const validate = () => {

    if (!formData.email.trim())
      return "Email address is required";

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!emailRegex.test(formData.email))
      return "Please enter a valid email address";

    if (!formData.password)
      return "Password is required";

    return null;
  };

  /* =====================================================
     SUBMIT
  ===================================================== */

  const handleSubmit = async (e) => {

    e.preventDefault();
    if (isSubmitting) return;

    const validationError = validate();

    if (validationError) {
      setError(validationError);
      return;
    }

    setIsSubmitting(true);
    setError("");

    const res = await apiClient.post("/auth/login", formData);

    if (!res.success) {
      setError(res.message || "Invalid email or password");
      setIsSubmitting(false);
      return;
    }

    const { token, role } = res.data || {};

    if (!token) {
      setError("Authentication failed. Please try again.");
      setIsSubmitting(false);
      return;
    }

    /* ================= STORE AUTH ================= */

    localStorage.setItem("token", token);
    localStorage.setItem("role", role || "ROLE_USER");

    /* ================= REDIRECT ================= */

    const params = new URLSearchParams(location.search);
    const redirectTo = params.get("redirect");
    const safeRedirect = redirectTo && redirectTo.startsWith("/")
      ? redirectTo
      : "/journey";

    navigate(role === "ROLE_ADMIN" ? "/admin" : safeRedirect, {
      replace: true
    });

    setIsSubmitting(false);
  };

  /* =====================================================
     UI
  ===================================================== */

  const redirectQuery = location.search || "";

  return (
    <div className="flex flex-col items-center justify-center min-h-[80vh]">

      <div className="w-full max-w-md card-elevated p-10 animate-fadeInUp">

        <div className="text-center mb-8">
          <h2 className="text-3xl font-serif mb-2">
            Welcome Back
          </h2>
          <p className="text-gray-400 text-sm">
            Continue your civilizational journey
          </p>
        </div>

        {error && (
          <div className="mb-6 p-3 text-sm rounded-lg bg-red-500/10 border border-red-500/30 text-red-400">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">

          <input
            name="email"
            type="email"
            required
            value={formData.email}
            onChange={handleChange}
            placeholder="Email address"
            disabled={isSubmitting}
            className="input-modern"
          />

          <div className="relative">

            <input
              name="password"
              type={showPassword ? "text" : "password"}
              required
              value={formData.password}
              onChange={handleChange}
              placeholder="Password"
              disabled={isSubmitting}
              className="input-modern pr-20"
            />

            <button
              type="button"
              onClick={() => setShowPassword(prev => !prev)}
              className="absolute right-4 top-1/2 -translate-y-1/2 text-xs text-gray-400 hover:text-brand-gold transition"
            >
              {showPassword ? "Hide" : "Show"}
            </button>

          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full btn-primary"
          >
            {isSubmitting
              ? "Authenticating..."
              : "Enter Sarathi"}
          </button>

        </form>

        <div className="text-center mt-8 text-sm text-gray-400">
          New to Sarathi?{" "}
          <Link
            to={`/register${redirectQuery}`}
            className="text-brand-gold hover:underline"
          >
            Create an account
          </Link>
        </div>

      </div>

      <div className="mt-10 text-xs opacity-50 tracking-widest uppercase text-gray-500">
        Sarathi — Civilizational Mobility Infrastructure
      </div>

    </div>
  );
}
