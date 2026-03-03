import { useContext } from "react";
import { ToastContext } from "../toast/ToastProvider";

export function useToast() {
  return useContext(ToastContext);
}