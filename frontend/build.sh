#!/bin/bash

# 构建前端 Docker 镜像
echo "Building frontend Docker image..."
docker build -t frontend:latest .

# 如果需要推送到镜像仓库，可以取消下面的注释
# docker tag frontend:latest your-registry/frontend:latest
# docker push your-registry/frontend:latest

echo "Build completed!"
