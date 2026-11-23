import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// In development (`npm run dev`) requests to /api are proxied to the Spring Boot backend,
// so the browser sees one origin and no CORS preflight is needed.
// In Docker the same job is done by nginx (see nginx.conf).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_API_PROXY_TARGET || 'http://localhost:3000',
        changeOrigin: true,
      },
    },
  },
});
