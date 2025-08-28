# 前端容器化部署指南

## 前置条件

1. 已安装 Docker
2. 已安装 Kubernetes 和 kubectl
3. Docker Desktop 已启用 Kubernetes 支持

## 部署步骤

### 方法一：使用 CI/CD 脚本（推荐）

```bash
# 在前端目录下执行
./ci-cd.sh
```

此脚本将自动执行以下步骤：
1. 运行测试
2. 构建应用
3. 构建 Docker 镜像
4. 部署到 Kubernetes

### 方法二：手动执行

#### 1. 运行测试
```bash
npm run test:run
```

#### 2. 构建应用
```bash
npm run build
```

#### 3. 构建 Docker 镜像
```bash
./build.sh
```

#### 4. 部署到 Kubernetes
```bash
../k8s/deploy-frontend.sh
```

## 验证部署

部署完成后，可以使用以下命令检查前端服务状态：

```bash
# 获取服务信息
kubectl get service frontend-service -n fx-g14

# 获取部署状态
kubectl get deployment frontend -n fx-g14

# 查看 Pod 日志
kubectl logs -f deployment/frontend -n fx-g14
```
```bash
#端口转发
kubectl port-forward -n fx-g14 service/frontend-service 8080:80
```

访问前端应用：
在浏览器中访问 `http://localhost:8080` 或通过 Kubernetes 获取的 NodePort 地址。

## 清理资源

如需删除已部署的前端应用：

```bash
kubectl delete deployment frontend -n fx-g14
kubectl delete service frontend-service -n fx-g14
```

## 输出结果
```bash
# 检查一下容器的具体位置和状态：
yurunhao@yurunhaodeMacBook-Air k8s % kubectl get pods -n fx-g14 -o wide

NAME                      READY   STATUS    RESTARTS   AGE     IP         NODE             NOMINATED NODE   READINESS GATES
frontend-6cfd7c46-6jlh9   1/1     Running   0          2m34s   10.1.0.8   docker-desktop   <none>           <none>
#frontend Pod 详细信息
yurunhao@yurunhaodeMacBook-Air k8s % kubectl get pods -n fx-g14

NAME                      READY   STATUS    RESTARTS   AGE
frontend-6cfd7c46-6jlh9   1/1     Running   0          6m39s

#本地 Docker 镜像：
yurunhao@yurunhaodeMacBook-Air k8s % docker images | grep frontend

frontend                                  latest                                                                        0587fc3926f9   21 minutes ago   95.9MB
```
## 容器化部署的流程是：

1. 您构建了一个 Docker 镜像：docker build -t frontend:latest ./frontend
2. 这个镜像被部署到 Kubernetes 集群中，创建了一个 Pod
3. Pod 中的容器正在运行您的 Nginx 服务器，提供前端应用服务