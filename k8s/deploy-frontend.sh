#!/bin/bash

# 设置 Kubernetes 命令空间
NAMESPACE="fx-g14"

# 检查命名空间是否存在
if ! kubectl get namespace $NAMESPACE > /dev/null 2>&1; then
    echo "Creating namespace $NAMESPACE..."
    kubectl apply -f namespace.yaml
fi

# 部署前端
echo "Deploying frontend to Kubernetes..."
kubectl apply -f frontend-deployment.yaml

# 等待部署完成
echo "Waiting for deployment to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/frontend -n $NAMESPACE

# 获取前端服务信息
echo "Frontend service information:"
kubectl get service frontend-service -n $NAMESPACE

# 获取前端部署状态
echo "Frontend deployment status:"
kubectl get deployment frontend -n $NAMESPACE

echo "Frontend deployment completed!"
