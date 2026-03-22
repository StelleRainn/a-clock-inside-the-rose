# A Clock Inside the Rose (ACIR) - 开发计划

**项目目标**: 基于 Spring Boot + Vue 的个人效率中心系统
**核心技术**: Vue 3 (Frontend) + Spring Boot (Backend) + MySQL (Database)
**开发策略**: 前端优先 -> 数据库设计 -> 后端实现 -> 联调

## Milestone 1: 前端核心与界面搭建 (Completed)
> 目标：完成所有页面的静态展示与交互逻辑，确保用户体验流畅。

- [x] **基础设施搭建**
    - [x] 初始化 Vue 3 + Vite 项目
    - [x] 安装 Element Plus, Axios, Sass, Icons
    - [x] 配置 Vite 代理 (`/api`)
- [x] **路由与布局**
    - [x] 设计 MainLayout (侧边栏 + 顶部栏)
    - [x] 配置 Vue Router
- [x] **核心模块开发 (交互与逻辑)**
    - [x] **Pomodoro (番茄钟)**: 实现倒计时逻辑、状态管理 (运行/暂停/重置)、完成通知。
    - [x] **Tasks (任务管理)**: 实现任务列表展示、添加、删除、状态切换。
    - [x] **Dashboard (仪表盘)**: 聚合展示今日数据。
    - [x] **Stats (统计)**: 集成 ECharts/Chart.js 展示图表。
    - [x] **Auth (认证)**: 登录/注册表单验证。

## Milestone 1.5: 细节打磨与体验优化 (Completed)
> 目标：在核心功能可用的基础上，提升用户体验和系统稳定性。

- [x] **Task (任务管理) 进阶**:
    - [x] **看板视图 (Kanban View)**: 实现类似 Trello 的拖拽任务管理。
    - [x] **任务详情页**: 支持 Markdown 渲染描述，增加标签 (Tags) 管理。
    - [x] **筛选与排序**: 支持按优先级、状态、截止时间排序。
- [x] **Pomodoro (番茄钟) 进阶**:
    - [x] **任务绑定**: 启动番茄钟时，允许选择关联当前正在进行的任务。
    - [x] **白噪音 (White Noise)**: 提供背景音效（雨声、咖啡馆等）。
    - [x] **全屏专注模式**: 提供沉浸式 UI。
- [x] **Dashboard (仪表盘) 增强**:
    - [x] **日历视图**: 在日历上展示每日的任务完成情况和专注时长。

## Milestone 2: 游戏化与个性化 (Completed)
> 目标：趣味性内容更新，打造吸引力。

- [x] **Gamification (游戏化系统)**:
    - [x] **等级系统 (Leveling)**: 根据专注时长计算 XP，提升用户等级。
    - [x] **成就徽章 (Achievements)**: 达成特定条件解锁徽章。
    - [x] **连胜纪录 (Streaks)**: 追踪连续登录/专注天数。
- [x] **Personalization (个性化)**:
    - [x] **用户资料**: 上传头像、修改昵称、个性签名。
- [x] **Social (社交分享)**:
    - [x] **专注卡片 (Focus Card)**: 生成精美的“今日总结”图片。

## Milestone 3: 深度与专业化 (Completed)
> 目标：兼具深度与广度、探索更多功能，面向专业化、硬核功能

- [x] **Task Management Advanced (任务管理进阶)**:
    - [x] **标签系统 (Tags)**: 支持多色标签、创建与筛选。
    - [x] **高级筛选 (Filtering)**: 支持按标题搜索、按标签筛选。
    - [x] **智能排序 (Sorting)**: 支持按优先级、状态、时间排序（升/降序）。
    - [x] **子任务 (Subtasks)**: 无限层级子任务，支持进度条展示。
    - [x] **交互升级**: 拖拽排序（含防误触）、分页系统、看板列内滚动。
- [x] **Data Export (数据导出)**: 
    - [x] 支持导出专注记录 (CSV)。
    - [x] 支持导出任务清单 (CSV)。
- [x] **Focus Analytics Pro (高阶统计)**:
  - [x] **Heatmap (热力图)**: 类似 GitHub 贡献图的专注热力展示。
  - [x] **Tag Analysis (标签分析)**: 基于标签的时间分布统计。
- [x] **Immersive Suite (沉浸体验增强)**:
  - [x] **Zen Note (防走神便签)**: 专注时的快速记录板。
  - [x] **Dark Mode (深色模式)**: 全局适配 Element Plus 深色主题。

## Milestone 4: 美术迭代 (Completed)
> 目标：大幅优化页面设计，打造 "Immersive Flow" 体验。

- [x] **UI 重塑 (Redesign)**:
  - [x] **Glassmorphism**: 全面引入毛玻璃风格。
  - [x] **Navigation**: 移除侧边栏，转向顶部双 Header 导航。
  - [x] **Dynamic Backgrounds**: 支持动态背景与智能主题色适配。
- [x] **Focus Station (New Home)**:
  - [x] **Hero Timer**: 首页首屏即为沉浸式番茄钟。
  - [x] **Bento Grid**: 次级板块采用网格布局，展示热力图与任务概览。
- [x] **UX Enhancements**:
  - [x] **Quick Toggles**: 快速切换专注模式。
  - [x] **Task Selector**: 首页集成任务选择器。
  - [x] **Calendar View**: 独立的数据回顾页面。

## Milestone 5: 智能化与全平台 (In Progress)
> 目标：引入 AI 能力，实现移动端适配，完善系统部署。

- [x] **AI Assistant (AI 助手)**:
  - [x] **LLM Integration**: 接入 Google Gemini 2.0 Flash 模型，支持 Markdown 渲染与流式对话。
  - [x] **Context Awareness**: 自动注入用户今日专注数据与 Top 待办任务，实现个性化建议。
  - [x] **History Management**: 完整的会话管理（新建/删除/切换），支持历史记录持久化存储 (MySQL)。
  - [x] **Security**: API Key 跟随账户配置（加密存储于数据库），移除前端硬编码。
  - [x] **Immersive UI**: 全屏 Glassmorphism 设计，类 ChatGPT 的极简交互体验。
- [x] **Mobile Adaptation (移动端适配)**:
  - [x] **Responsive Layout**: 适配手机屏幕尺寸 (Bottom Navigation + Single Column)。
  - [x] **Touch Optimization**: 优化触摸交互体验 (Header/AI 布局优化)。
- [ ] **System Polish (系统完善)**:
  - [ ] **Sound Mixer**: 多音轨白噪音混音。
  - [ ] **Shortcuts**: 全局键盘快捷键。
  - [ ] **I18n**: 多语言支持。
- [ ] **Deployment (部署)**:
  - [ ] Docker 容器化。
  - [ ] 生产环境部署文档。
