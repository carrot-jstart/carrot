import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import monacoEditorPlugin from "vite-plugin-monaco-editor";

export default defineConfig({
  plugins: [react(), (monacoEditorPlugin as any).default({})],
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:8848",
        changeOrigin: true,
      },
    },
  },
});
