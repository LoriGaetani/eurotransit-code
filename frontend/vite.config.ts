import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const catalogApiTarget = process.env.VITE_CATALOG_API_TARGET ?? 'http://localhost:8080'
const inventoryApiTarget = process.env.VITE_INVENTORY_API_TARGET ?? 'http://localhost:8081'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api/catalog': {
        target: catalogApiTarget,
        changeOrigin: true,
      },
      '/api/inventory': {
        target: inventoryApiTarget,
        changeOrigin: true,
      },
    },
  },
})
