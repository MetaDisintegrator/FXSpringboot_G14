# 使用轻量级JDK17基础镜像
FROM openjdk:17

# 将构建的JAR包复制到镜像中
COPY target/*.jar fx-app.jar

# 复制配置文件到镜像中
COPY src/main/resources/application-local.yml ./config/application-local.yml

# 暴露应用端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "fx-app.jar", "--spring.profiles.active=local", "--spring.config.import=optional:file:./config/"]