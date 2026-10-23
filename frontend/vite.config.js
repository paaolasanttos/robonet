import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "node:path";
import { fileURLToPath } from "node:url";

const frontendDir = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  plugins: [react()],
  envDir: path.resolve(frontendDir, ".."),
  server: {
    host: true,
    port: 5173,
  },
});