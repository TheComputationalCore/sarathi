import { useEffect, useState, useRef } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import api from "../api";

export default function Payment() {

  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const bookingId = searchParams.get("bookingId");

  const [status, setStatus] = useState("initializing");
  const [amount, setAmount] = useState(null);
  const [errorMessage, setErrorMessage] = useState("");

  const hasStarted = useRef(false);

  /* =========================================================
     PAYMENT INITIALIZATION
  ========================================================= */

  useEffect(() => {

    if (!bookingId) {
      navigate("/profile", { replace: true });
      return;
    }

    if (hasStarted.current) return;
    hasStarted.current = true;

    const startPayment = async () => {

      try {
        const res = await api.post(
          `/payments/create-order/${bookingId}`
        );

        const { orderId, amount, currency, key } = res.data;

        if (!orderId || !amount || !key) {
          throw new Error("Invalid payment response.");
        }

        setAmount(amount / 100);

        const isMockPayment = typeof key === "string" && key.startsWith("mock_");

        if (isMockPayment) {
          setStatus("verifying");

          await api.post("/payments/verify", {
            bookingId,
            razorpayOrderId: orderId,
            razorpayPaymentId: `mock_payment_${bookingId}`,
            razorpaySignature: "MOCK_SIGNATURE",
          });

          setStatus("success");
          setTimeout(() => {
            navigate("/profile", { replace: true });
          }, 1200);
          return;
        }

        if (!window.Razorpay) {
          throw new Error("Payment gateway not loaded.");
        }

        const razorpay = new window.Razorpay({

          key,
          amount,
          currency,
          name: "Sarathi",
          description: "Civilizational Journey Booking",
          order_id: orderId,

          handler: async function (response) {

            try {

              setStatus("verifying");

              await api.post("/payments/verify", {
                bookingId,
                razorpayOrderId: response.razorpay_order_id,
                razorpayPaymentId: response.razorpay_payment_id,
                razorpaySignature: response.razorpay_signature,
              });

              setStatus("success");

              setTimeout(() => {
                navigate("/profile", { replace: true });
              }, 2500);

            } catch {
              setStatus("error");
              setErrorMessage(
                "Payment verification failed. Please contact support."
              );
            }

          },

          modal: {
            ondismiss: function () {
              navigate("/profile", { replace: true });
            },
          },

          theme: {
            color: "#D4AF37"
          }

        });

        razorpay.open();

      } catch (err) {
        setStatus("error");
        setErrorMessage(
          err.message || "Unable to initiate payment."
        );
      }

    };

    startPayment();

  }, [bookingId, navigate]);

  const retry = () => {
    window.location.reload();
  };

  /* =========================================================
     UI
  ========================================================= */

  return (
    <div className="min-h-screen flex items-center justify-center px-6 py-32 bg-gradient-to-br from-black via-brand-primary to-black">

      <div className="w-full max-w-xl bg-black/90 backdrop-blur-xl border border-brand-gold/20 rounded-3xl p-14 shadow-elevated relative overflow-hidden">

        {/* Gold glow layer */}
        <div className="absolute inset-0 bg-gradient-to-b from-brand-gold/5 to-transparent pointer-events-none" />

        {/* HEADER */}
        <div className="flex justify-between items-center mb-10 relative z-10">
          <h4 className="text-xl font-semibold tracking-wide">
            Secure Transaction
          </h4>

          <span className="bg-emerald-500/15 text-emerald-400 text-xs px-4 py-1 rounded-full uppercase tracking-wider font-semibold">
            🔒 Encrypted
          </span>
        </div>

        {/* META */}
        {bookingId && (
          <div className="flex justify-between text-sm mb-8 relative z-10">
            <div>
              <span className="block text-xs uppercase tracking-widest text-gray-500">
                Booking ID
              </span>
              <strong className="block text-brand-gold mt-1">
                #{bookingId}
              </strong>
            </div>

            {amount !== null && (
              <div className="text-right">
                <span className="block text-xs uppercase tracking-widest text-gray-500">
                  Amount
                </span>
                <strong className="block text-brand-gold mt-1">
                  ₹ {amount.toFixed(2)}
                </strong>
              </div>
            )}
          </div>
        )}

        <div className="h-px bg-gradient-to-r from-transparent via-brand-gold/40 to-transparent mb-10 relative z-10" />

        <StateBlock
          type={status}
          title={
            status === "initializing"
              ? "Opening Secure Gateway"
              : status === "verifying"
              ? "Verifying Transaction"
              : status === "success"
              ? "Payment Confirmed"
              : "Transaction Failed"
          }
          subtitle={
            status === "initializing"
              ? "Establishing encrypted connection…"
              : status === "verifying"
              ? "Confirming payment authenticity…"
              : status === "success"
              ? "Your journey has been secured."
              : errorMessage || "An unexpected issue occurred."
          }
          note={
            status === "success"
              ? "Redirecting to your dashboard…"
              : null
          }
          action={
            status === "error"
              ? (
                <button
                  onClick={retry}
                  className="mt-8 px-8 py-3 rounded-full border border-brand-gold text-brand-gold hover:bg-brand-gold hover:text-black transition-all duration-300"
                >
                  Retry Payment
                </button>
              )
              : null
          }
        />

      </div>
    </div>
  );
}

/* =========================================================
   STATE BLOCK
========================================================= */

function StateBlock({ type, title, subtitle, note, action }) {

  return (
    <div className="text-center space-y-6 animate-fadeIn">

      {/* ICON */}
      <div className="flex justify-center">

        {type === "success" && (
          <div className="w-20 h-20 rounded-full bg-emerald-600 flex items-center justify-center text-3xl font-bold text-white shadow-lg">
            ✓
          </div>
        )}

        {type === "error" && (
          <div className="w-20 h-20 rounded-full bg-red-700 flex items-center justify-center text-3xl font-bold text-white shadow-lg">
            !
          </div>
        )}

        {(type === "initializing" || type === "verifying") && (
          <div className="w-20 h-20 rounded-full border-4 border-white/10 border-t-brand-gold animate-spin" />
        )}

      </div>

      <h3 className="text-lg font-semibold">
        {title}
      </h3>

      <p className="text-gray-400 text-sm leading-relaxed">
        {subtitle}
      </p>

      {note && (
        <span className="block text-xs uppercase tracking-widest text-gray-500">
          {note}
        </span>
      )}

      {action}

    </div>
  );
}
