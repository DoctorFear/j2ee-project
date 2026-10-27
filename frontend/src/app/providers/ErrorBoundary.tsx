import { Component, type ErrorInfo, type ReactNode } from "react";
import { ApplicationErrorPage } from "../pages/ApplicationErrorPage";

export class ErrorBoundary extends Component<{ children: ReactNode }, { failed: boolean }> {
  state = { failed: false };

  static getDerivedStateFromError() {
    return { failed: true };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    if (import.meta.env.DEV) console.error(error, info.componentStack);
  }

  render() {
    if (this.state.failed) return <ApplicationErrorPage />;
    return this.props.children;
  }
}
