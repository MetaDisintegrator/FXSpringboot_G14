# 使用官方 Node.js 18 镜像作为基础镜像
FROM node:18-alpine AS build

# 设置工作目录
WORKDIR /app

# 复制 package.json 和 package-lock.json
COPY package*.json ./

# 安装依赖
RUN npm ci

# 复制项目源代码
COPY . .

# 构建应用
RUN npm run build

# 使用 nginx 部署构建后的应用
FROM nginx:alpine

# 将构建产物复制到 nginx 的默认目录
COPY --from=build /app/dist /usr/share/nginx/html

# 复制自定义 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
