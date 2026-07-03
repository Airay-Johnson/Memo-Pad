# ---- 阶段1：构建前端 ----
FROM node:22-alpine AS frontend
WORKDIR /app/vue
COPY vue/package*.json ./
RUN npm ci
COPY vue/ ./
RUN npm run build

# ---- 阶段2：构建后端 ----
FROM maven:3.9-eclipse-temurin-17 AS backend
WORKDIR /app/SpringBoot
COPY SpringBoot/pom.xml ./
RUN mvn dependency:go-offline -q
COPY SpringBoot/src ./src
RUN mvn clean package -DskipTests -q

# ---- 阶段3：运行镜像 ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# 复制后端 JAR
COPY --from=backend /app/SpringBoot/target/memopad-0.0.1-SNAPSHOT.jar app.jar

# 复制前端静态文件（可选：用 Nginx 托管）
COPY --from=frontend /app/vue/dist /usr/share/nginx/html

# 复制 SQL 初始化脚本
COPY sql/ /app/sql/

EXPOSE 9999 80

# 启动后端
ENTRYPOINT ["java", "-jar", "app.jar"]
