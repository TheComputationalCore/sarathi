import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import api from "../api";

export default function Register() {

  const navigate = useNavigate();
  const location = useLocation();

  const [formData, setFormData] = useState({
    name: "",
    email: "",
    password: "",
    confirmPassword: ""
  });

  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;

    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    if (error) setError("");
  };

  const validate = () => {
    if (!formData.name.trim())
      return "Full name is required";

    if (!formData.email.trim())
      return "Email address is required";

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!emailRegex.test(formData.email))
      return "Please enter a valid email address";

    if (!formData.password)
      return "Password is required";

    if (formData.password.length < 6)
      return "Password must be at least 6 characters";

    if (formData.password !== formData.confirmPassword)
      return "Passwords do not match";

    return null;
  };

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

    try {

      await api.post("/auth/register", {
        name: formData.name,
        email: formData.email,
        password: formData.password
      });

      navigate(`/login${location.search || ""}`, { replace: true });

    } catch (err) {
      setError(err?.message || "Registration failed.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const redirectQuery = location.search || "";

  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh]">

      <div className="w-full max-w-md card-elevated p-12 animate-fadeInUp">

        <div className="text-center mb-8">
          <h2 className="text-3xl font-serif mb-2">
            Begin Your Journey
          </h2>
          <p className="text-gray-400 text-sm">
            Create your Sarathi account
          </p>
        </div>

        {error && (
          <div className="mb-6 p-3 text-sm rounded-lg bg-red-500/10 border border-red-500/30 text-red-400">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">

          <input
            name="name"
            type="text"
            required
            value={formData.name}
            onChange={handleChange}
            placeholder="Full name"
            disabled={isSubmitting}
            className="input-modern"
          />

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

          <input
            name="confirmPassword"
            type="password"
            required
            value={formData.confirmPassword}
            onChange={handleChange}
            placeholder="Confirm password"
            disabled={isSubmitting}
            className="input-modern"
          />

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full btn-primary"
          >
            {isSubmitting ? "Creating Account..." : "Join Sarathi"}
          </button>

        </form>

        <div className="text-center mt-8 text-sm text-gray-400">
          Already have an account?{" "}
          <Link to={`/login${redirectQuery}`} className="text-brand-gold hover:underline">
            Sign in
          </Link>
        </div>

      </div>

      <div className="mt-16 text-xs opacity-40 tracking-[0.2em] uppercase text-gray-500">
        Sarathi — Civilizational Mobility Infrastructure
      </div>

    </div>
  );
}
