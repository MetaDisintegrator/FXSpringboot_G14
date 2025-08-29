#!/bin/bash

# 定义变量
FRONTEND_DIR="frontend"
IMAGE_TAG=$(date +%Y%m%d%H%M%S)
MAIN_NODE="root@192.168.184.131"
WORKER_NODES=("worker1" "worker2")
YAML_PATH="~/fx/yml/backend.yml"
YAML_FRONTEND_PATH="~/fx/yml/frontend.yml"

# 函数：错误处理（仅提示不退出）
handle_error() {
    echo "Error on line $1"
    read -p "按任意键退出..." -n 1
    exit 1
}

trap 'handle_error $LINENO' ERR

# 1. 构建Java后端
echo "========== 构建Java后端 =========="
mvn clean package -DskipTests || { echo "Maven构建失败"; exit 1; }
docker build -t fx-backend:"$IMAGE_TAG" .


# 2. 保存镜像并清理本地
echo "========== 保存镜像并清理本地 =========="
docker save -o ./app.tar fx-backend:"$IMAGE_TAG"
docker rmi fx-backend:"$IMAGE_TAG"

# 3. 分发镜像到所有节点（通过主节点中转）
echo "========== 分发镜像到所有节点 =========="
scp ./app.tar $MAIN_NODE:~/fx/

worker_nodes_str="${WORKER_NODES[*]}"

ssh $MAIN_NODE << EOF
    nodes=($worker_nodes_str)
    for node in "\${nodes[@]}"; do
        echo "正在处理节点: \$node"
        ssh \$node "mkdir -p ~/fx/"
        scp ~/fx/app.tar \$node:~/fx/ || echo "镜像分发到 \$node 失败"
    done
EOF

# 4. 加载镜像到工作节点
echo "========== 加载镜像到工作节点 =========="
ssh $MAIN_NODE << EOF
    nodes=($worker_nodes_str)
    for node in "\${nodes[@]}"; do
        echo "正在加载到节点: \$node"
        ssh \$node "docker load -i ~/fx/app.tar" || echo "节点 \$node 镜像加载失败"
    done
EOF

# 5. 更新K8s部署
echo "========== 更新K8s部署 =========="
ssh  $MAIN_NODE << EOF
    # kubectl delete -f $YAML_PATH || echo "删除旧部署失败"
    sed -i 's|image: fx-backend:.*|image: fx-backend:$IMAGE_TAG|g' $YAML_PATH || echo "YAML文件修改失败"
    kubectl apply -f $YAML_PATH || echo "新部署应用失败"
EOF

# 6. 清理所有节点的旧镜像
echo "========== 清理所有节点的旧镜像 =========="
ssh  $MAIN_NODE << EOF
    nodes=($worker_nodes_str)
    for node in "\${nodes[@]}"; do
        echo "正在清理节点: \$node"
        ssh \$node << 'INNER_EOF'
            docker rm -f $(docker ps -aq -f "ancestor=fx-backend") 2>/dev/null
            docker images fx-backend --format "{{.ID}}" | tail -n +4 | xargs -r docker rmi -f 2>/dev/null || echo "镜像清理失败"
            docker image prune -f || echo "悬空镜像清理失败"
INNER_EOF
    done
EOF

echo "========== 构建前端镜像 =========="
docker build -t frontend:"$IMAGE_TAG" "$FRONTEND_DIR"

echo "========== 保存镜像并清理本地 =========="
docker save -o ./app_frontend.tar frontend:"$IMAGE_TAG"
docker rmi frontend:"$IMAGE_TAG"

echo "========== 分发镜像到所有节点 =========="
scp ./app_frontend.tar $MAIN_NODE:~/fx/

ssh $MAIN_NODE << EOF
    nodes=($worker_nodes_str)
    for node in "\${nodes[@]}"; do
        echo "正在处理节点: \$node"
        ssh \$node "mkdir -p ~/fx/"
        scp ~/fx/app_frontend.tar \$node:~/fx/ || echo "镜像分发到 \$node 失败"
    done
EOF

echo "========== 加载镜像到工作节点 =========="
ssh $MAIN_NODE << EOF
    nodes=($worker_nodes_str)
    for node in "\${nodes[@]}"; do
        echo "正在加载到节点: \$node"
        ssh \$node "docker load -i ~/fx/app_frontend.tar" || echo "节点 \$node 镜像加载失败"
    done
EOF

echo "========== 更新K8s部署 =========="
ssh $MAIN_NODE << EOF
    sed -i 's|image: frontend:.*|image: frontend:$IMAGE_TAG|g' $YAML_FRONTEND_PATH
    kubectl apply -f $YAML_FRONTEND_PATH || echo "新部署应用失败"
EOF

echo "========== 清理所有节点的旧镜像 =========="
ssh $MAIN_NODE << EOF
    nodes=($worker_nodes_str)
    for node in "\${nodes[@]}"; do
        echo "正在清理节点: \$node"
        ssh \$node << 'INNER_EOF'
            docker rm -f $(docker ps -aq -f "ancestor=frontend") 2>/dev/null
            docker images frontend --format "{{.ID}}" | tail -n +4 | xargs -r docker rmi -f 2>/dev/null || echo "镜像清理失败"
            docker image prune -f || echo "悬空镜像清理失败"
INNER_EOF
    done
EOF

echo "========== 部署结束 =========="
echo "新镜像标签: $IMAGE_TAG"

read -p "按任意键退出..." -n 1