import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import AppProviders from "./core/providers/AppProviders";

import "./index.css";
import "./App.css";

const rootElement = document.getElementById("root");

if (!rootElement) {
  throw new Error("Root element not found");
}

const root = ReactDOM.createRoot(rootElement);

root.render(
  <React.StrictMode>
    <AppProviders>
      <App />
    </AppProviders>
  </React.StrictMode>
);