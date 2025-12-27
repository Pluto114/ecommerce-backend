# 1. 构建阶段：使用 Maven 镜像编译项目
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# 开始编译，跳过测试以节省时间
RUN mvn clean package -DskipTests

# 2. 运行阶段：使用轻量级 JRE 运行项目
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# 从构建阶段把生成的 jar 包复制过来，重命名为 app.jar
COPY --from=build /app/target/*.jar app.jar

# 暴露端口 (虽然 Render 会自动探测，但写上是好习惯)
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]