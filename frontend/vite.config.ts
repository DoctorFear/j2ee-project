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
    },
  };
});
