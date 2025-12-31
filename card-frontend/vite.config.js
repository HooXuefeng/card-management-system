import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// 核心：配置base为/api（匹配后端context-path），解决静态资源路径404
export default defineConfig({
  plugins: [vue()],
  // 路径别名：@指向src目录
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  // 关键：静态部署的基础路径，设置为根路径
  base: '/',
  // 开发服务器配置（本地运行npm run dev时用）
  server: {
    port: 5173, // 前端开发端口（和后端8080区分）
    open: true, // 启动后自动打开浏览器
    proxy: {
      // 代理/api请求到后端，解决跨域
      '/api': {
        target: 'http://localhost:8080', // 后端IP+端口
        changeOrigin: true, // 开启跨域代理
        rewrite: (path) => path.replace(/^\/api/, '/api') // 保留/api前缀
      }
    }
  },
  // 打包配置：确保产物完整
  build: {
    outDir: 'dist', // 打包输出目录
    assetsDir: 'assets', // 静态资源目录
    sourcemap: false // 关闭sourcemap，减小体积
  }
})