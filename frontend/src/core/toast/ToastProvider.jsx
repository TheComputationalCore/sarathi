import { createContext, useCallback, useState } from "react";
import ToastContainer from "./ToastContainer";

export const ToastContext = createContext(null);

export default function ToastProvider({ children }) {

  const [toasts, setToasts] = useState([]);

  const addToast = useCallback((type, message) => {
    const id = Date.now() + Math.random();

    setToasts(prev => [...prev, { id, type, message }]);

    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, 4000);
  }, []);

  const value = {
    success: (msg) => addToast("success", msg),
    error: (msg) => addToast("error", msg),
    info: (msg) => addToast("info", msg),
  };

  return (
    <ToastContext.Provider value={value}>
      {children}
      <ToastContainer toasts={toasts} />
    </ToastContext.Provider>
  );
}