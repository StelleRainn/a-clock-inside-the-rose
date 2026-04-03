# A Clock Inside The Rose - 全栈项目 Docker 部署指南与行动记录

## 1. 部署架构概览

本项目采用 **Vue3 (前端) + SpringBoot (后端) + MySQL (数据库)** 的全栈架构。
为保证环境的一致性与部署的便捷性，我们选用 **Docker + Docker Compose** 进行容器化编排部署。

### 架构拓扑
- **前端容器 (Nginx)**: 托管 Vue3 编译后的静态文件 `dist`，并代理转发前端的 API 请求到后端容器。
- **后端容器 (Java 17/21)**: 运行 SpringBoot 打包后的 `jar` 文件，提供 RESTful API 服务。
- **数据库容器 (MySQL 8.x)**: 运行 MySQL 数据库，挂载本地数据卷以保证数据持久化。

---

## 2. 部署前置要求

- [ ] **云服务器准备**: 一台拥有公网 IP 的 Linux 云服务器（推荐 Ubuntu 20.04/22.04 或 CentOS 7/8，至少 2C4G）。
- [ ] **环境安装**: 服务器已安装 `Docker` 和 `Docker Compose`。
- [ ] **域名准备 (可选)**: 若需通过域名访问并配置 HTTPS，需提前准备已备案/解析的域名及 SSL 证书。

---

## 3. 核心配置文件清单

我们需要在项目根目录下创建/整理以下配置文件（用于本地构建及服务器部署）：

- [ ] `docker-compose.yml`: 统筹编排前端、后端、数据库三个容器。
- [ ] `frontend/Dockerfile`: 用于构建前端 Nginx 镜像。
- [ ] `frontend/nginx.conf`: Nginx 配置文件，处理前端路由及后端 API 反向代理。
- [ ] `backend/Dockerfile`: 用于构建 SpringBoot 后端镜像。
- [ ] `db/init.sql`: 数据库初始化脚本（建表、插入初始数据），MySQL 容器初次启动时会自动执行。

---

## 4. 详细实施步骤与行动记录

### 阶段一：本地 Docker 配置文件编写 (准备阶段)
- [x] **编写数据库相关配置**: 确认最新的数据库结构及基础数据为 `_localhost-2026_04_02_23_15_46-dump.sql`。
- [x] **编写后端 Dockerfile**: 编写使用 Maven 构建，依赖缓存优化，基于 JRE 17 运行的 Dockerfile。
- [x] **调整后端数据库连接配置**: 将 `application.yaml` 中的 MySQL 连接 URL 中的 localhost 和密码提取为环境变量 `${DB_HOST:localhost}` 和 `${DB_PASSWORD:3186287129Qq.}`，以便在 Docker 环境和本地开发中自适应切换。
- [x] **编写前端 Dockerfile 及 Nginx 配置**: 使用 multi-stage build (pnpm install -> Nginx)，并在 nginx.conf 中配置 Vue3 History 模式的刷新问题，且设置了反向代理将 `/api/` 转发给 `acir-backend:8080` 容器。
- [x] **编写 `docker-compose.yml`**: 统筹三大容器组件（前端、后端、数据库），配置内网别名和 MySQL 数据卷挂载，使得启动更加顺滑。

### 阶段二：服务器环境初始化 (基建阶段)
- [x] **连接服务器**: 使用 SSH 登录云服务器。
- [x] **安装 Docker**: 执行 Docker 官方安装脚本。
- [x] **安装 Docker Compose**: 下载对应系统的 Compose 插件。
- [x] **开放防火墙/安全组**: 在云厂商控制台开放 HTTP(80)、HTTPS(443) 等必要端口。

### 阶段三：项目打包与上传 (构建阶段)
- [ ] **后端打包**: 执行 `mvn clean package`（或 gradle）生成 `jar` 包。
- [ ] **前端打包**: 执行 `npm run build` 生成 `dist` 静态资源目录。
- [ ] **文件上传**: 将打包好的产物、`Dockerfile`、`nginx.conf`、`init.sql` 及 `docker-compose.yml` 上传至服务器的指定目录（如 `/opt/acir-deploy/`）。

### 阶段四：启动与验证 (上线阶段)
- [ ] **启动服务**: 在服务器部署目录下执行 `docker-compose up -d --build`。
- [ ] **检查日志**: 执行 `docker-compose logs -f` 查看服务启动情况，确保无报错（尤其是后端连库是否成功）。
- [ ] **联调测试**: 通过公网 IP（或域名）访问项目，测试登录、数据加载、CRUD 等核心业务链路是否畅通。

---

## 5. 故障排查 (Troubleshooting) 预案

1. **后端连不上数据库**: 检查 `application.yml` 中的 `spring.datasource.url`，在 docker-compose 内部网络中，主机名应为数据库容器名（如 `jdbc:mysql://mysql-container:3306/db_name`）。
2. **前端刷新 404**: 确保 Nginx 配置文件中配置了 `try_files $uri $uri/ /index.html;`。
3. **前端跨域/请求后端失败**: Nginx 需正确配置反向代理 `location /api/ { proxy_pass http://backend-container:8080/; }`。
4. **数据库中文乱码**: 确保 `init.sql` 及 MySQL 容器启动参数设置了 `utf8mb4` 字符集。

---

> **行动锚点**: 本文档为动态更新文档，每完成一个阶段的任务，我们将在此处打钩 `[x]` 并记录相关细节。
