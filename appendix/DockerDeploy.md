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
- [x] **后端打包**: 执行 `mvn clean package`（由 Docker 多阶段构建自动完成）。
- [x] **前端打包**: 执行 `npm run build`（由 Docker 多阶段构建自动完成）。
- [x] **文件上传**: 通过 Git 将代码及配置文件推送到远程仓库，并在服务器上拉取。

### 阶段四：启动与验证 (上线阶段)
- [x] **启动服务**: 在服务器部署目录下执行 `docker-compose up -d --build` 成功。
- [x] **检查日志**: 执行成功，无报错，解决了一次 JDK 版本不匹配问题。
- [x] **联调测试**: 调整安全组放行 80 端口后，通过 HTTP 协议成功访问项目。

---

## 5. 故障排查 (Troubleshooting) 预案

1. **后端连不上数据库**: 检查 `application.yml` 中的 `spring.datasource.url`，在 docker-compose 内部网络中，主机名应为数据库容器名（如 `jdbc:mysql://mysql-container:3306/db_name`）。
2. **前端刷新 404**: 确保 Nginx 配置文件中配置了 `try_files $uri $uri/ /index.html;`。
3. **前端跨域/请求后端失败**: Nginx 需正确配置反向代理 `location /api/ { proxy_pass http://acir-backend:8080; }` (注意 `proxy_pass` 结尾不能有斜杠 `/`，否则会导致 Nginx 吞掉 `/api/` 路径前缀，进而触发后端 SpringBoot 404 错误)。
4. **数据库中文乱码**: 确保 `init.sql` 及 MySQL 容器启动参数设置了 `utf8mb4` 字符集。

## 6. 常见运维问题解答 (FAQ)

### Q1: 本地开发更新后，如何更新到服务器？
**是的，只需简单的三个步骤：**
1. **本地开发并提交**: 在本地完成代码修改，执行 `git add .`、`git commit -m "..."` 和 `git push`。
2. **服务器拉取**: 通过 SSH 登录服务器，进入部署目录（如 `/opt/acir-deploy`），执行 `git pull` 拉取最新代码。
3. **重新构建并启动**: 在部署目录下执行 `docker-compose up -d --build`。Docker 会自动检测有改动的容器，重新打包（因为有缓存机制，这次会非常快），并平滑替换旧容器，实现无缝更新。

### Q2: 成功启动后，SSH 会话（终端窗口）可以关闭吗？
**可以安全关闭！**
因为我们启动 Docker 容器时，使用了 `-d`（detached mode，后台守护模式）参数，即 `docker-compose up -d`。这意味着所有的容器服务都在服务器的后台独立运行，完全脱离了你当前的 SSH 会话。你可以放心关闭电脑的终端窗口、盖上 Mac 屏幕甚至关机，你的项目依然在云端 24 小时运行。

### Q3: 怎么访问云主机上的数据库内容？
你有两种方式来管理云服务器上的 MySQL 数据库：
- **方式一（推荐，直观方便）**: 使用本地的数据库管理工具（如 Navicat, DataGrip, DBeaver）。
  1. 确保阿里云的**安全组**中已经放行了 `3306` 端口。
  2. 在 Navicat 中新建连接：
     - 主机/IP: **你的云服务器公网 IP**
     - 端口: **3306**
     - 用户名: **root**
     - 密码: **3186287129Qq.** （即 `docker-compose.yml` 中设置的密码）
  3. 点击连接即可像管理本地数据库一样查看和修改数据。
- **方式二（命令行操作）**: 如果不想用外部工具，可以直接在服务器终端里进入 MySQL 容器内部操作：
  ```bash
  docker exec -it acir-mysql mysql -uroot -p
  ```
  输入密码后，即可使用原生 SQL 语句查询。

### Q4: 如何在云服务器上同步本地最新导出的数据库 SQL 文件？
你在本地修改并导出了最新的 `_localhost-2026_04_04_11_36_56-dump.sql`。更新到云端有以下两种常见方式：

**方式一（推荐，无损且直观）：通过可视化工具直接运行 SQL**
如果你只是想导入最新的基础数据，且不想重启或删除容器：
1. 像 **Q3** 里那样，使用你本地的 Navicat 连接到云端数据库。
2. 右键点击你的云端 `ACIR` 数据库 -> 选择 **运行 SQL 文件 (Execute SQL File)**。
3. 选择你本地电脑上的 `_localhost-2026_04_04_11_36_56-dump.sql`，点击开始。这会直接覆盖或更新云端的表结构和基础数据。

**方式二（删库重装法，适合部署初期全面重置）：通过 Docker 重新初始化**
如果你希望云端数据库“完全重头开始”（⚠️ **警告**：这会清空云端自上线以来产生的所有新数据！）：
1. 确保本地 `docker-compose.yml` 的初始化脚本路径已指向新文件（我已在本地帮你修改好了，指向了新的 `11_36_56` 那个文件）。
2. 在本地执行 `git add .`、`git commit -m "Update db dump"` 和 `git push`。
3. 登录云服务器，进入目录执行 `git pull` 拉取新代码。
4. 执行以下命令销毁旧的数据库并重新初始化：
   ```bash
   # 1. 停止当前所有容器
   docker-compose down
   
   # 2. 彻底删除旧的数据库数据文件（⚠️ 危险操作：数据将永久丢失！）
   sudo rm -rf mysql-data/
   
   # 3. 重新启动，Docker 会发现没有数据文件，从而自动运行你刚更新的 SQL 初始化脚本
   docker-compose up -d
   ```

### Q5: 每次 git pull 后执行 Docker 构建，卡在前端 `pnpm run build` 很久甚至导致 SSH 断线，怎么办？
**这是云服务器（尤其是 2GB 内存的小型机）非常典型的“内存耗尽 (OOM)”问题。**
现代前端项目打包（Vite/Webpack）会占用极大的内存（经常瞬间飙升到 1.5GB 以上）。当服务器物理内存耗尽时，系统要么会拼命把内存数据写入硬盘（导致系统卡死），要么会直接杀掉你的进程甚至 SSH 连接。

**解决方案一：限制 Node.js 的打包内存（已在本地修复）**
我已经帮你修改了本地的 `front-end/Dockerfile`，在构建命令前加上了限制：
`RUN NODE_OPTIONS="--max-old-space-size=1024" pnpm run build`
这样可以强制 Node.js 在垃圾回收上更勤快，尽量不超出 1GB 内存。

**解决方案二（终极推荐）：为云服务器增加虚拟内存 (Swap)**
如果方案一还是卡，说明服务器连 1GB 的空余内存都挤不出来。你需要给服务器手动分配一块“硬盘空间”当作内存用（即 Swap）。通过 SSH 连上云服务器，依次执行以下命令：
```bash
# 1. 创建一个 2GB 大小的虚拟内存文件（大约需要十几秒）
sudo fallocate -l 2G /swapfile

# 2. 设置正确的权限（只有 root 可以读写）
sudo chmod 600 /swapfile

# 3. 将文件格式化为 swap 格式
sudo mkswap /swapfile

# 4. 启用虚拟内存
sudo swapon /swapfile

# 5. 验证是否成功（如果你看到 Swap 一行有 2.0G，说明成功了！）
free -h

# 6. (可选) 让虚拟内存永久生效，避免重启服务器后失效
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```
增加 Swap 后，虽然打包时间依然可能需要一两分钟（因为硬盘比内存慢），但**绝对不会再出现卡死或断线的问题**了。

---

> **行动锚点**: 本文档为动态更新文档，每完成一个阶段的任务，我们将在此处打钩 `[x]` 并记录相关细节。
