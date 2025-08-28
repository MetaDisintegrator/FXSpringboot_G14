#!/bin/bash

# 前端 CI/CD 脚本
echo "Starting frontend CI/CD pipeline..."

# 1. 运行测试
echo "Running tests..."
npm run test:run

# 检查测试是否通过
if [ $? -ne 0 ]; then
    echo "Tests failed. Aborting pipeline."
    exit 1
fi

echo "Tests passed successfully!"

# 2. 构建应用
echo "Building application..."
npm run build

# 检查构建是否成功
if [ $? -ne 0 ]; then
    echo "Build failed. Aborting pipeline."
    exit 1
fi

echo "Build completed successfully!"

# 3. 构建 Docker 镜像
echo "Building Docker image..."
docker build -t frontend:latest .

if [ $? -ne 0 ]; then
    echo "Docker build failed. Aborting pipeline."
    exit 1
fi

echo "Docker image built successfully!"

# 4. 部署到 Kubernetes
echo "Deploying to Kubernetes..."
../k8s/deploy-frontend.sh

if [ $? -ne 0 ]; then
    echo "Kubernetes deployment failed."
    exit 1
fi

echo "Frontend CI/CD pipeline completed successfully!"
