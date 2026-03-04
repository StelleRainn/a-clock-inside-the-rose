# A Clock Inside the Rose (ACIR) - 开发计划

**项目目标**: 基于 Spring Boot + Vue 的个人效率中心系统
**核心技术**: Vue 3 (Frontend) + Spring Boot (Backend) + MySQL (Database)
**开发策略**: 前端优先 -> 数据库设计 -> 后端实现 -> 联调

## Phase 1: 前端核心与界面搭建 (Current Focus)
> 目标：完成所有页面的静态展示与交互逻辑，确保用户体验流畅。

- [x] **基础设施搭建**
    - [x] 初始化 Vue 3 + Vite 项目
    - [x] 安装 Element Plus, Axios, Sass, Icons
    - [x] 配置 Vite 代理 (`/api`)
- [x] **路由与布局**
    - [x] 设计 MainLayout (侧边栏 + 顶部栏)
    - [x] 配置 Vue Router
- [ ] **核心模块开发 (交互与逻辑)**
    - [ ] **Pomodoro (番茄钟)**: 实现倒计时逻辑、状态管理 (运行/暂停/重置)、完成通知。
        - *Priority*: High (核心功能)
    - [x] **Tasks (任务管理)**: 实现任务列表展示、添加、删除、状态切换 (使用 Mock 数据 -> 已对接真实 API)。
        - *Priority*: High
    - [ ] **Dashboard (仪表盘)**: 聚合展示今日数据。
    - [ ] **Stats (统计)**: 集成 ECharts/Chart.js 展示图表。
    - [x] **Auth (认证)**: 登录/注册表单验证。

## Phase 1.5: 细节打磨与体验优化 (Next Milestone)
> 目标：在核心功能可用的基础上，提升用户体验和系统稳定性。

- [ ] **Task (任务管理) 进阶**:
    - [ ] **看板视图 (Kanban View)**: 实现类似 Trello 的拖拽任务管理。
    - [ ] **任务详情页**: 支持 Markdown 渲染描述，增加标签 (Tags) 管理。
    - [ ] **筛选与排序**: 支持按优先级、状态、截止时间排序。
- [ ] **Pomodoro (番茄钟) 进阶**:
    - [ ] **任务绑定**: 启动番茄钟时，允许选择关联当前正在进行的任务。
    - [ ] **白噪音 (White Noise)**: 提供背景音效（雨声、咖啡馆等）。
    - [ ] **全屏专注模式**: 提供沉浸式 UI。
- [ ] **Dashboard (仪表盘) 增强**:
    - [ ] **日历视图**: 在日历上展示每日的任务完成情况和专注时长。
    - [ ] **待办事项提醒**: 临近截止日期的任务高亮显示。
- [ ] **Settings (设置)**:
    - [ ] **个人资料**: 修改头像、密码。
    - [ ] **番茄钟偏好**: 自定义默认工作/休息时长。

## Phase 2: 数据库设计与建模 (Completed)
> 目标：设计支撑业务的数据库结构。

- [x] **ER 图设计**: 确定 User, Task, PomodoroRecord, Tag 等实体关系。
- [x] **Schema 实现**: 编写 SQL 建表脚本。
    - `users`: 用户信息
    - `tasks`: 任务详情 (title, status, due_date, priority)
    - `pomodoro_records`: 专注记录 (start_time, duration, linked_task_id)

## Phase 3: 后端开发 (Spring Boot) (Completed)
> 目标：提供 RESTful API，替换前端 Mock 数据。

- [x] **环境配置**: Spring Boot 3.x 初始化, MyBatis/JPA 配置。
- [x] **API 实现**:
    - Auth: `/api/auth/login`, `/api/auth/register`
    - Tasks: `/api/tasks` (CRUD)
    - Pomodoro: `/api/pomodoro` (Save record)
    - Stats: `/api/stats/daily` (Daily Focus & Task Status)
- [ ] **单元测试**: Service 层逻辑验证 (Optional for MVP)。

## Phase 4: 系统联调与优化 (Ongoing)
> 目标：前后端对接，完善系统。

- [x] **接口联调**: 前端 Axios 对接真实后端 API。
- [x] **功能完善**: 错误处理 (404/500)、加载状态、权限控制 (未登录跳转)。
- [ ] **部署准备**: Docker 镜像或常规部署文档。
