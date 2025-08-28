#!/bin/bash

# 定义变量
FRONTEND_DIR="frontend"
IMAGE_TAG=$(date +%Y%m%d%H%M%S)
MAIN_NODE="root@192.168.184.131"        # 主节点使用主机名
WORKER_NODES=("worker1" "worker2")
YAML_PATH="~/fx/yml/backend.yml"

# 函数：错误处理
handle_error() {
    echo "Error on line $1"
    read -p "按任意键退出..." -n 1
    exit 1
}

trap 'handle_error $LINENO' ERR

# 1. 构建Java后端
echo "========== 构建Java后端 =========="
mvn clean package
docker build -t fx-backend:"$IMAGE_TAG" .

# 2. 保存镜像并清理本地
echo "========== 保存镜像并清理本地 =========="
docker save -o ./app.tar fx-backend:"$IMAGE_TAG"
docker rmi fx-backend:"$IMAGE_TAG"

# 3. 分发镜像到所有节点（通过主节点中转）
echo "========== 分发镜像到所有节点 =========="
scp ./app.tar $MAIN_NODE:~/fx/

# 将WORKER_NODES数组转换为空格分隔的字符串
worker_nodes_str="${WORKER_NODES[*]}"

ssh $MAIN_NODE << EOF
    # 将字符串转换回数组
    nodes=($worker_nodes_str)
    # 在主节点上分发镜像
    for node in "\${nodes[@]}"; do
        echo "正在处理节点: \$node"
        # 确保目标目录存在
        ssh \$node "mkdir -p ~/fx/"
        scp ~/fx/app.tar \$node:~/fx/
    done
EOF

# 4. 加载镜像到所有节点
echo "========== 加载镜像到所有节点 =========="
ssh $MAIN_NODE << EOF
    nodes=($worker_nodes_str)
    for node in "\${nodes[@]}"; do
        echo "正在加载到节点: \$node"
        ssh \$node "docker load -i ~/fx/app.tar"
    done
EOF

# 5. 更新K8s部署
echo "========== 更新K8s部署 =========="
ssh $MAIN_NODE << EOF
    kubectl delete -f $YAML_PATH
    sed -i 's|image: fx-backend:.*|image: fx-backend:$IMAGE_TAG|g' $YAML_PATH
    kubectl apply -f $YAML_PATH
EOF

# 6. 清理所有节点的旧镜像
echo "========== 清理所有节点的旧镜像 =========="
ssh $MAIN_NODE << EOF
    nodes=($worker_nodes_str)
    for node in "\${nodes[@]}"; do
        echo "正在清理节点: \$node"
        ssh \$node << 'INNER_EOF'
            # 删除所有相关容器
            docker rm -f \$(docker ps -aq -f "ancestor=fx-backend") 2>/dev/null || true

            # 保留最新3个镜像
            docker images fx-backend --format "{{.ID}}" | tail -n +4 | xargs -r docker rmi -f

            # 清理悬空镜像
            docker image prune -f
INNER_EOF
    done
EOF

echo "========== 部署完成 =========="
echo "新镜像标签: $IMAGE_TAG"
echo "YAML文件已更新: $YAML_PATH"

read -p "按任意键退出..." -n 1