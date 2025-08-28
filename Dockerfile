# 使用轻量级JDK17基础镜像（仅包含JRE，适合生产环境）
FROM openjdk:17

# 将构建的JAR包复制到镜像中（假设JAR包位于target目录）
COPY target/*.jar fx-app.jar

# 暴露应用端口（根据实际端口修改）
EXPOSE 80

# 启动命令
ENTRYPOINT ["java", "-jar", "fx-app.jar"]