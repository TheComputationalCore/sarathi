import axios from "axios";

/* =========================================================
   BASE URL RESOLUTION
========================================================= */

function resolveBaseUrl() {
  const envUrl = process.env.REACT_APP_API_URL?.trim();
  if (envUrl) return envUrl.replace(/\/$/, "");

  const isLocalhost =
    window.location.hostname === "localhost" ||
    window.location.hostname === "127.0.0.1";

  if (!isLocalhost) {
    return `${window.location.origin}/api`;
  }

  return "http://localhost:8082/api";
}

const BASE_URL = resolveBaseUrl();

/* =========================================================
   AXIOS INSTANCE
========================================================= */

const api = axios.create({
  baseURL: BASE_URL,
  timeout: 20000,
  withCredentials: false,
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
});

/* =========================================================
   TOKEN UTILITIES
========================================================= */

function getValidToken() {
  const token = localStorage.getItem("token");
  if (!token || token === "undefined" || token === "null") {
    return null;
  }
  return token;
}

function clearAuth() {
  localStorage.removeItem("token");
  localStorage.removeItem("role");
}

/* =========================================================
   REQUEST INTERCEPTOR
========================================================= */

api.interceptors.request.use(
  (config) => {
    const token = getValidToken();

    if (token) {
      config.headers = {
        ...config.headers,
        Authorization: `Bearer ${token}`,
      };
    }

    return config;
  },
  (error) => Promise.reject(normalizeError(error))
);

/* =========================================================
   RESPONSE INTERCEPTOR
========================================================= */

let redirecting = false;

api.interceptors.response.use(
  (response) => response,
  async (error) => {

    // NETWORK FAILURE
    if (!error.response) {
      return Promise.reject(buildNetworkError());
    }

    const { status, config } = error.response;

    /* =========================
       RETRY LOGIC (5xx + Network)
    ========================= */

    if (
      status >= 500 &&
      config &&
      !config.__isRetryRequest
    ) {
      config.__isRetryRequest = true;

      await delay(800); // small exponential backoff
      return api(config);
    }

    /* =========================
       401 — AUTH EXPIRED
    ========================= */

    if (status === 401) {
      clearAuth();

      if (
        !redirecting &&
        window.location.pathname !== "/login" &&
        window.location.pathname !== "/register"
      ) {
        redirecting = true;
        const redirect = encodeURIComponent(
          `${window.location.pathname}${window.location.search}`
        );

        setTimeout(() => {
          window.location.replace(`/login?redirect=${redirect}`);
          redirecting = false;
        }, 50);
      }
    }

    /* =========================
       DEV LOGGING
    ========================= */

    if (process.env.NODE_ENV === "development") {
      if (status === 403) {
        console.warn("403 Forbidden:", error.response.data);
      }
      if (status >= 500) {
        console.error("Server Error:", error.response.data);
      }
    }

    return Promise.reject(normalizeError(error));
  }
);

/* =========================================================
   HELPERS
========================================================= */

function delay(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function buildNetworkError() {
  return {
    status: 0,
    message: "Network error. Please check your connection.",
    data: null,
  };
}

function normalizeError(error) {

  if (!error.response) {
    return buildNetworkError();
  }

  const { status, data } = error.response;

  return {
    status,
    message:
      data?.message ||
      data?.error ||
      "Unexpected server response.",
    data: data ?? null,
  };
}

/* =========================================================
   REQUEST WRAPPER (CLEANER CALLS)
========================================================= */

async function request(method, url, options = {}) {
  try {
    const response = await api({
      method,
      url,
      ...options,
    });

    return {
      success: true,
      data: response.data,
      status: response.status,
    };

  } catch (error) {
    return {
      success: false,
      ...error,
    };
  }
}

/* =========================================================
   STRUCTURED API METHODS
========================================================= */

export const apiClient = {
  get: (url, config) =>
    request("get", url, config),

  post: (url, data, config) =>
    request("post", url, { data, ...config }),

  put: (url, data, config) =>
    request("put", url, { data, ...config }),

  patch: (url, data, config) =>
    request("patch", url, { data, ...config }),

  delete: (url, config) =>
    request("delete", url, config),
};

/* =========================================================
   DEFAULT EXPORT (RAW AXIOS)
========================================================= */

export default api;
