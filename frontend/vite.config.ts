import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// Em dev o navegador fala só com o Vite (:5173); /api é repassado ao Spring (:8080).
// Mesma origem => o cookie de sessão e o cookie XSRF-TOKEN funcionam sem CORS.
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
