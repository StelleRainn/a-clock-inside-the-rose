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

## 6. Docker 部署实战总结 (核心概念与命令)

在本次全栈项目的部署实战中，我们使用了 Docker 体系来完成环境隔离与一键部署。以下是对本次实战中涉及的核心技术点和操作的复盘总结。

### 6.1 核心概念解析

**1. 什么是容器 (Container)？**
你可以把容器想象成一个“极度轻量的虚拟机”。传统虚拟机（如 VMware）需要模拟整个硬件和操作系统，而容器只打包了你的应用程序及其依赖（比如 JDK、Node.js、Nginx 等），它直接共享宿主机的系统内核。这使得容器启动极快（秒级），且占用资源极小。

**2. 为什么克隆项目后，用 Docker 命令就能跑起来？**
这得益于 **Dockerfile** 的“环境即代码 (Infrastructure as Code)”特性。
在过去，部署一个项目你需要：安装 Java -> 安装 Node.js -> 安装 MySQL -> 配置 Nginx... 极易出现“在我电脑上能跑，服务器上跑不了”的尴尬。
而现在，我们在 `Dockerfile` 中写明了构建步骤，在 `docker-compose.yml` 中声明了各种服务的依赖。只要服务器装了 Docker，它就能自动去下载所需的环境镜像，按照你写好的剧本，在云端 100% 还原你本地的运行环境。

**3. Docker 与 Docker Compose 的关系是什么？**
- **Docker** 是一切的基础，它负责打包和运行**单个**容器（比如只运行一个 MySQL，或者只运行一个 SpringBoot）。
- **Docker Compose** 是 Docker 的“大管家/交响乐指挥”。对于我们这种**多组件项目**（前端 + 后端 + 数据库），如果纯用 Docker，你需要手动敲三条很长的命令，还要手动配置网络让它们互相通信。而 Docker Compose 允许我们用一个 `docker-compose.yml` 文件把这三个容器统筹起来：定义它们的启动顺序（比如先起数据库再起后端）、配置它们之间的内网别名，最后只需一条 `docker-compose up` 命令就能一键启动整个集群。

### 6.2 常见命令速查与原理解析

在部署和更新时，我们经常用到以下命令组合：

*   `docker-compose build`: **根据 Dockerfile 制造镜像**。它会执行代码打包（如 `mvn package` 或 `pnpm build`），把你的代码和运行环境封装成一个只读的镜像包。
*   `docker-compose up`: **启动容器**。如果发现镜像不存在，它会自动先去 `build`。
*   `-d` (detached): **后台运行**。让容器在后台默默工作，不占用你当前的 SSH 终端，你可以随时安全关闭终端窗口。
*   `--build`: **强制重新构建**。当你修改了代码后，加上这个参数，Docker 会先重新打包镜像，再平滑替换掉旧容器。
*   `--no-cache`: **无缓存构建**。Docker 构建时默认会利用缓存（比如之前下载的依赖）。如果你修改了环境变量，或者怀疑缓存导致了“幽灵 Bug”，加上这个参数会让 Docker 彻底从零开始下载和构建。
*   `docker-compose down`: **停止并销毁容器集群**。它会停止服务，并移除容器实例和内部网络，但**默认不会删除你的持久化数据卷（如数据库数据）**。

### 6.3 进阶实战：代码更新与数据库维护

**Q: `git pull` 后重新 `--build`，Docker 在后台到底做了什么？**
1. Docker 会读取 `Dockerfile`，发现前面的基础镜像层（比如 `FROM node:20`）没变，直接使用缓存。
2. 发现 `COPY . .` 这一层进来的代码发生了变化（因为 `git pull` 了新代码），**从这一层开始，后续的所有缓存失效**。
3. 触发后续的打包命令（如重新执行 `pnpm run build`）。
4. 打包出新的镜像后，Docker 会把旧的前端/后端容器停掉，然后用新镜像启动新容器，完成“无缝升级”。

**Q: 什么样的操作会影响到 ECS 云主机的数据库？**
云主机上的 MySQL 数据库运行在 `acir-mysql` 容器中。由于我们在 `docker-compose.yml` 中配置了数据卷映射（`volumes: - ./mysql-data:/var/lib/mysql`），数据库的真实数据**已经持久化保存在了云主机的 `mysql-data/` 文件夹中**。
*   **安全操作**：`docker-compose down` 或重启服务器，**不会**丢失数据。容器虽然销毁了，但下次启动时，它会重新读取 `mysql-data/` 里的文件，数据原样恢复。
*   **会改变/覆盖数据的操作**：
    1. 使用 Navicat 等客户端直接连接服务器 IP 并执行了 INSERT/UPDATE/DELETE 语句，或者执行了新的 SQL 脚本文件（推荐的数据同步方式）。
    2. **（高危）** 删除了宿主机上的 `mysql-data/` 文件夹。一旦删除，数据将彻底丢失，下次启动容器时，Docker 会认为这是一个全新的数据库，从而去执行你挂载的 `init.sql` 进行初始化。

**Q: 为什么我重新构建后，云主机数据库里新增的测试数据丢失了（“回退”到了初始 SQL 的状态）？是 `git pull` 或 `--no-cache` 导致的吗？**
**都不是。**
1. `git pull` 只负责更新代码（如 Java/Vue 源码）。`mysql-data/` 目录属于运行时产生的挂载数据，通常在 `.gitignore` 中被忽略，Git **绝对不会**去覆盖或删除它。
2. `--no-cache` 只是让 Docker 在构建 `acir-frontend` 和 `acir-backend` 镜像时不使用旧缓存（比如重新拉取 npm 包），它**完全不会**删除任何挂载的外部数据卷。
**真正的元凶：** 出现“数据回退”的唯一原因，是你在更新 SQL 文件时，手动执行了 `sudo rm -rf mysql-data/`（即我们之前讨论同步数据时的“删库重装法”）。
一旦 `mysql-data/` 被清空，当你再次执行 `docker-compose up` 时，MySQL 容器会发现自己的 `/var/lib/mysql` 是空的，它就会触发**“初次启动初始化”**机制，自动导入 `docker-compose.yml` 中挂载的那个最新 `.sql` 脚本。这就导致你在这之前直接在云端手动添加的测试数据全部被抹除，数据库完美“回退”到了该本地 `.sql` 文件的状态。

**Q: 如果我想对云主机 ECS 上的容器进行“全面删除重装”，应该怎么操作？**
如果你遇到了难以排查的缓存问题，或者想要彻底重置项目环境（包括清空数据库、删除所有旧镜像和容器），可以按照以下步骤执行“推倒重来”操作：

1. **停止并删除所有相关的容器与网络**
   在项目根目录下执行：
   ```bash
   docker-compose down
   ```
2. **（可选，高危！）删除数据库持久化数据**
   如果你**确定**要清空云端数据库（例如，想重新导入最新的 `init.sql`），请删除数据卷目录：
   ```bash
   sudo rm -rf mysql-data/
   ```
   *注意：这一步是不可逆的，如果你想保留数据，千万不要执行此命令！*
3. **清理 Docker 系统中悬空/未使用的资源（深度清理）**
   这一步会清理掉没有被容器使用的镜像、网络和构建缓存，腾出磁盘空间：
   ```bash
   docker system prune -a --volumes
   ```
   *执行后会提示你确认，输入 `y` 即可。这会将你之前打包好的前端/后端镜像彻底删除。*
4. **重新拉取代码并构建**
   确认代码是最新的后，重新执行彻底的无缓存构建与启动：
   ```bash
   git pull
   docker-compose build --no-cache
   docker-compose up -d
   ```
完成以上 4 步，你的 ECS 服务器就仿佛回到了第一次部署这个项目时的纯净状态！

### 6.4 企业级架构进阶：本地 M1 交叉编译与云端拉取 (CI/CD 雏形)

**痛点**：在 2C2G 的低配云主机上直接执行 `pnpm build` 或 `mvn package`，不仅速度极慢，还极易因为内存耗尽（OOM）导致机器卡死、SSH 断线。
**破局点**：利用 Docker "Build once, run everywhere" 的特性，将**重体力劳动（源码编译打包）**交给性能强劲的 Mac 本地完成，将打好的镜像直接推送到阿里云镜像仓库，云主机只负责**轻松的运行（拉取镜像并启动）**。


**核心配置调整 (`docker-compose.yml`)**：
为需要自己构建的容器（前端和后端）指定 `image` 名称（关联阿里云镜像仓库地址），并**务必添加 `platform: linux/amd64`** 配置。
*注意：Mac M 系列是 ARM64 架构，而云主机通常是 AMD64 (x86_64) 架构。`platform` 参数会强制开启交叉编译，让 Mac 打包出云主机能原生运行的镜像包，避免出现 `exec format error`。*

**终极丝滑部署/更新工作流（两步走）：**

**第一步：在 Mac 本地（构建与推送）**

_建议在 macOS Terminal 中进行，避免网络问题！_

1. 登录阿里云镜像仓库（首次需输入控制台设置的密码）：
   ```bash
    docker login --username=时雨蔷薇 crpi-wwifswyqz8bp8ddb.cn-wulanchabu.personal.cr.aliyuncs.com
   ```
2. 执行交叉编译构建（M1 芯片会自动模拟 AMD64 环境进行打包）：
   ```bash
   docker-compose build
   ```
3. 推送构建好的成品镜像包到阿里云仓库：
   ```bash
   docker-compose push
   ```

**第二步：在 ECS 云主机上（拉取与运行）**
*(前提：确保服务器上的 `docker-compose.yml` 已经通过 `git pull` 更新到了带有 image 和 platform 配置的最新版本)*
1. 登录阿里云镜像仓库（首次需登录）：
   ```bash
    docker login --username=时雨蔷薇 crpi-wwifswyqz8bp8ddb.cn-wulanchabu.personal.cr.aliyuncs.com
   ```
2. 拉取刚刚在 Mac 上做好的镜像（这会直接下载编译好的环境和代码，跳过任何耗时的 build 阶段）：
   ```bash
   docker-compose pull
   ```
3. 瞬间启动容器集群：
   ```bash
   docker-compose up -d
   ```
*至此，你完美避开了低配云主机 CPU/内存不足的物理限制。以后每次修改代码，只需要本地 `build -> push`，云端 `pull -> up -d` 即可实现光速发版！*

---

> **行动锚点**: 本文档为动态更新文档，每完成一个阶段的任务，我们将在此处打钩 `[x]` 并记录相关细节。
