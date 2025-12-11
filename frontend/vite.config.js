import { defineConfig } from 'vite';

export default defineConfig({
  root: '.', // Changed from 'src' to '.' for root directory
  base: '/',
  publicDir: 'public',
  build: {
    outDir: 'dist',
    emptyOutDir: true,
    sourcemap: true,
  },
  server: {
    port: 5173,
    open: true,
  },
  css: {
    devSourcemap: true,
  },
});