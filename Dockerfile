# 构建阶段
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# 复制 pom.xml 文件
COPY pom.xml .
COPY knowledge-base-server/pom.xml knowledge-base-server/

# 下载依赖
RUN mvn dependency:go-offline -B

# 复制源代码
COPY . .

# 构建项目
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 安装 curl 用于健康检查
RUN apk add --no-cache curl

# 从构建阶段复制 jar 包
COPY --from=builder /app/knowledge-base-server/target/*.jar app.jar

# 创建必要的目录
RUN mkdir -p /app/log /app/.tmp/knowledge-base/doc /app/models

# 暴露端口
EXPOSE 8000

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8000/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=docker", \
  "-jar", "app.jar"]
