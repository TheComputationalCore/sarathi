import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import ErrorBoundary from "../error/ErrorBoundary";
import ToastProvider from "../toast/ToastProvider";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 2,
      refetchOnWindowFocus: false,
      staleTime: 1000 * 60 * 5,
    },
  },
});

export default function AppProviders({ children }) {
  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <ToastProvider>
          {children}
        </ToastProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  );
}