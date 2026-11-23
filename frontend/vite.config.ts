// Cấu hình máy chủ và build frontend
import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import { fileURLToPath, URL } from "node:url";
import { normalizeApiBaseUrl } from "./src/lib/api-base-url";

export default defineConfig(({ mode }) => {
  const variables = loadEnv(mode, process.cwd(), "VITE_");

  normalizeApiBaseUrl(process.env.VITE_API_BASE_URL ?? variables.VITE_API_BASE_URL);

  return {
    plugins: [react(), tailwindcss()],
    resolve: {
      alias: { "@": fileURLToPath(new URL("./src", import.meta.url)) },
    },
    server: {
      port: 5173,
      proxy: {
        "/auth": {
          target: "http://localhost:8080",
          changeOrigin: true,
        },
        "/user": {
          target: "http://localhost:8080",
          changeOrigin: true,
        },
        "/bookings": {
          target: "http://localhost:8080",
          changeOrigin: true,
        },
        "/tickets": {
          target: "http://localhost:8080",
          changeOrigin: true,
        },
        "/trips": {
          target: "http://localhost:8080",
          changeOrigin: true,
        },
        "/stations": {
          target: "http://localhost:8080",
          changeOrigin: true,
        },
        "/routes": {
          target: "http://localhost:8080",
          changeOrigin: true,
        },
        "/route-stops": {
          target: "http://localhost:8080",
          changeOrigin: true,
        },
        "/vehicles": {
          target: "http://localhost:8080",
          changeOrigin: true,
        },
        "/drivers": {
          target: "http://localhost:8080",
          changeOrigin: true,
        },
        "/trip-drivers": {
          target: "http://localhost:8080",
          changeOrigin: true,
        },
        "/trip-statistics": {
          target: "http://localhost:8080",
          changeOrigin: true,
        },
        "/api": {
          target: "http://localhost:8080",
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ""),
        },
      },
    },
  };
});
