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
    proxy: {
      '^/api': {
        target: 'http://192.168.184.131:30001/api', // 保留后端需要的 /api 前缀
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '') // 仅移除前端请求的 /api 前缀
      }
    }
  }
});

// src/api/register.js
const request = axios.create({
  baseURL: '/api',  // 所有请求自动添加 /api 前缀
  withCredentials: true  // 保持会话
});
