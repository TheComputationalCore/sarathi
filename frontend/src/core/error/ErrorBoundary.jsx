import React from "react";

export default class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    if (process.env.NODE_ENV === "development") {
      console.error("ErrorBoundary caught:", error, errorInfo);
    }
  }

  handleReload = () => {
    window.location.reload();
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center bg-black text-white px-6">
          <div className="max-w-xl text-center space-y-6">
            <h1 className="text-4xl font-serif text-brand-gold">
              Something went wrong
            </h1>
            <p className="text-gray-400">
              The civilizational thread momentarily broke.
            </p>
            <button
              onClick={this.handleReload}
              className="px-6 py-3 bg-brand-gold text-black rounded-lg"
            >
              Reload Application
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}