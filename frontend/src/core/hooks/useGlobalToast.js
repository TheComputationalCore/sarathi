import { useContext } from "react";
import { ToastContext } from "../toast/ToastProvider";

export function useGlobalToast() {

  const context = useContext(ToastContext);

  if (!context) {
    throw new Error(
      "useGlobalToast must be used inside ToastProvider"
    );
  }

  return context;
}