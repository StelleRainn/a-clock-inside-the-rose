# 构建阶段：使用 Maven 官方镜像打包 SpringBoot 项目 (使用 JDK 21)
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# 设置工作目录
WORKDIR /build

# 仅拷贝 pom.xml 以利用 Docker 缓存机制下载依赖
COPY pom.xml .
# 预先下载依赖，加快后续构建速度
RUN mvn dependency:go-offline -B

# 拷贝项目源代码
COPY src ./src

# 执行打包命令，跳过测试
RUN mvn clean package -DskipTests

# 运行阶段：使用轻量级的 JRE 21 镜像运行应用
FROM eclipse-temurin:21-jre-alpine

# 设置工作目录
WORKDIR /app

# 将构建阶段生成的 jar 包复制到运行环境
COPY --from=builder /build/target/*.jar app.jar

# 设置时区环境变量（使用亚洲/上海）
ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata \
    && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime \
    && echo $TZ > /etc/timezone

# 暴露后端应用端口
EXPOSE 8080

# 启动 SpringBoot 应用
ENTRYPOINT ["java", "-jar", "app.jar"]