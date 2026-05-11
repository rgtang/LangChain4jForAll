# 使用一个较新且轻量的 JDK 镜像作为基础环境，确保 glibc 版本满足 onnxruntime 的要求
FROM openjdk:17-jdk-slim

# 设置工作目录，容器启动后会自动进入此目录
WORKDIR /app

# 将你项目打好的 JAR 包复制到镜像中，并重命名为 app.jar
# 注意：target/ 目录下的 JAR 包名称请根据你的实际情况确认
COPY target/java-ai-agent-1.0.0.jar app.jar

# 云托管默认使用 80 端口，所以我们让应用监听 80 端口
EXPOSE 80

# 容器启动时执行的命令，用于运行你的 Spring Boot 应用
ENTRYPOINT ["java", "-jar", "app.jar"]