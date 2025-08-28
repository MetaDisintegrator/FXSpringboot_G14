// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import axios from "axios";
import path from 'node:path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@': path.resolve(__dirname, 'src') }
  },
  server: {
    host: true,
    proxy: {
      '^/api': {  // 匹配所有以 /api 开头的请求
        target: 'http://localhost:8080',  // 后端地址
        changeOrigin: true,
        rewrite: (path) => path // 不需要重写路径（前后端路径一致）
      }
    }
  }
});

// src/api/register.js
const request = axios.create({
  baseURL: '/api',  // 所有请求自动添加 /api 前缀
  withCredentials: true  // 保持会话
});
