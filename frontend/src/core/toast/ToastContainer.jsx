export default function ToastContainer({ toasts }) {
  return (
    <div className="fixed top-6 right-6 z-[9999] space-y-3">

      {toasts.map((toast) => (

        <div
          key={toast.id}
          className={`
            px-5 py-3 rounded-xl shadow-elevated
            backdrop-blur-md border text-sm font-medium
            animate-fadeInUp
            ${
              toast.type === "success"
                ? "bg-green-600/20 border-green-400/30 text-green-300"
                : toast.type === "error"
                ? "bg-red-600/20 border-red-400/30 text-red-300"
                : "bg-blue-600/20 border-blue-400/30 text-blue-300"
            }
          `}
        >
          {toast.message}
        </div>

      ))}

    </div>
  );
}