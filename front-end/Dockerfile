# 使用 Node.js 官方轻量级镜像作为构建环境
FROM node:20-alpine AS build-stage

# 设置工作目录
WORKDIR /app

# 先复制 package.json 和 lock 文件，利用 Docker 缓存层加速 npm install
COPY package.json pnpm-lock.yaml ./

# 全局安装 pnpm 并安装项目依赖
RUN npm install -g pnpm && pnpm install

# 复制项目源代码并执行构建
COPY . .
# 限制 Node.js 内存使用（如 1024MB），防止在小内存云服务器上构建时触发 OOM 导致死机或断开连接
RUN NODE_OPTIONS="--max-old-space-size=1024" pnpm run build

# 使用 Nginx 官方镜像作为运行环境
FROM nginx:alpine AS production-stage

# 将构建产物从 build-stage 复制到 Nginx 的默认静态文件目录
COPY --from=build-stage /app/dist /usr/share/nginx/html

# 将自定义的 Nginx 配置文件覆盖默认配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露 80 端口
EXPOSE 80

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]
