import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

const keepFrontendOrigin = (proxy) => {
  proxy.on('proxyRes', (proxyRes) => {
    const location = proxyRes.headers.location
    if (location?.startsWith('http://localhost:8080')) {
      proxyRes.headers.location = location.replace('http://localhost:8080', 'http://localhost:3000')
    }
  })
}

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    host: 'localhost',
    port: 3000,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      },
      '/oauth2': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        configure: keepFrontendOrigin
      },
      '/auth/login': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        configure: keepFrontendOrigin,
        rewrite: (path) => path.replace('/auth/login', '/login')
      },
      '/logout': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      },
      '/userinfo': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
