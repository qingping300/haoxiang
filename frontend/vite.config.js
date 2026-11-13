import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const backend = env.VITE_LEGACY_TARGET || 'http://127.0.0.1:8081'
  const proxy = { target: backend, changeOrigin: true, secure: false }
  return {
    plugins: [vue()],
    server: {
      host: '127.0.0.1',
      port: 5173,
      proxy: {
        '/legacy': {
          ...proxy,
          rewrite: path => path.replace(/^\/legacy/, '') || '/',
          configure(server) {
            server.on('proxyRes', response => {
              const location = response.headers.location
              if (location?.startsWith('/')) response.headers.location = `/legacy${location}`
            })
          },
        },
        '/upload': proxy,
        '/goods-img': proxy,
      },
    },
  }
})
