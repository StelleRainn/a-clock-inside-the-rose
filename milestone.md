# A Clock Inside the Rose (ACIR) - 里程碑报告 (Milestone 1)

**版本号**: v0.1.0-alpha
**完成日期**: 2026-03-05
**主要成就**: 完成核心业务闭环（用户认证 -> 任务管理 -> 番茄钟专注 -> 数据统计），前后端基础架构搭建完毕。

---

## 🚀 核心功能交付 (Delivered Features)

### 1. 用户认证模块 (Authentication)
- [x] **后端**:
    - 基于 MySQL 的用户表设计 (`users`)。
    - 实现了 `/api/auth/register` (注册) 和 `/api/auth/login` (登录) 接口。
- [x] **前端**:
    - 登录/注册页面 (`LoginView.vue`)，支持表单验证。
    - 基于 Pinia 的 `UserStore`，管理用户登录状态和 Token（目前模拟）。
    - 解决了路由代理 404 问题，确保前后端通信顺畅。

### 2. 任务管理模块 (Task Management)
- [x] **后端**:
    - 任务实体 (`Task`) 与数据库表 (`tasks`) 设计。
    - 完整的 CRUD 接口 (`/api/tasks`)，支持按用户 ID 查询。
- [x] **前端**:
    - 任务列表视图 (`TaskListView.vue`)，支持展示任务状态、优先级（颜色标签）。
    - 任务创建/编辑弹窗，支持设置标题、描述、优先级、状态和截止时间。
    - 删除了未登录创建任务导致的空指针异常，增强了健壮性。

### 3. 番茄钟专注模块 (Pomodoro Timer)
- [x] **后端**:
    - 专注记录实体 (`PomodoroRecord`) 与数据库表 (`pomodoro_records`) 设计。
    - 记录保存接口 (`POST /api/pomodoro`) 与今日统计接口 (`GET /api/pomodoro/today-count`)。
- [x] **前端**:
    - **倒计时逻辑**: 基于 Pinia 的 `PomodoroStore`，支持开始/暂停/重置。
    - **状态持久化**: 利用 `localStorage` 实现防刷新机制，页面刷新后倒计时自动恢复。
    - **自定义时长**: 引入滑块组件 (`el-slider`)，允许用户在 1-60 分钟内自由调整专注时长。
    - **数据同步**: 倒计时结束后自动调用后端接口保存记录，并实时更新“今日完成数”。

### 4. 数据统计模块 (Statistics)
- [x] **后端**:
    - 聚合查询接口：每日专注时长 (`/api/stats/focus-daily`) 与 任务状态分布 (`/api/stats/task-status`)。
- [x] **前端**:
    - 集成 **ECharts** 图表库。
    - **专注趋势图**: 柱状图展示过去 7 天的每日专注时长。
    - **任务分布图**: 饼图展示 To Do / In Progress / Done 的任务占比。

### 5. 仪表盘 (Dashboard)
- [x] **首页聚合**:
    - 展示“我的任务”列表（最新 5 条）。
    - 展示“今日专注”统计数据。
    - 顶部栏显示当前登录用户名，支持注销。

---

## 🛠 技术架构 (Technical Architecture)

- **前端**: Vue 3 + Vite + Element Plus + Pinia + Axios + ECharts + Sass
- **后端**: Spring Boot 3.2.3 + MyBatis 3.0.3 + MySQL 8.0
- **数据库**: 关系型数据库，包含 `users`, `tasks`, `pomodoro_records` 三张核心表。

---

## 📝 测试验证 (Verification)

1.  **认证**: 成功注册新用户，登录后自动跳转 Dashboard。
2.  **任务**: 成功创建、编辑、删除任务，数据持久化到 MySQL。
3.  **番茄钟**:
    -   倒计时功能正常，工作/休息模式切换顺畅。
    -   刷新浏览器后，倒计时能准确恢复。
    -   完成后数据库新增记录，统计数字实时 +1。
4.  **统计**: ECharts 图表能正确渲染后端返回的 JSON 数据。

---

## 🔮 未来展望 (Next Steps)
> 详见 `plans.md` 的 Phase 2 规划。重点在于增强模块间的联动性（如：任务与番茄钟绑定）以及提升用户体验（如：看板视图、更丰富的图表）。
