# 待改进 & 灵感 (Optimization Log)
> Tracking improvements, ideas, and technical debt.

## Phase 4 成果归档 (Completed in Aesthetic Update)

### 视觉与交互 (Visual & Interaction)
- [x] **UI 重塑**: 全面转向 Glassmorphism 风格，移除侧边栏，采用双 Header 布局。
- [x] **沉浸模式**: 实现 Hero Timer，支持 3s 自动隐藏 UI，支持浏览器全屏模式。
- [x] **智能主题**: 集成 `fast-average-color`，实现背景图智能分析，自动调整文字颜色 (Light/Dark) 以保证可读性。
- [x] **动态背景**: 支持自定义上传背景，支持遮罩浓度调节。
- [x] **Bento Grid**: 首页次级板块采用可定制的网格布局，支持 Widget 增删。

### 功能组件 (Functional Components)
- [x] **Zen Note**: 专注时的临时想法捕捉。
- [x] **Quick Toggles**: 番茄钟上方的快捷模式切换 (Pomodoro/Short/Long)。
- [x] **Task Selector**: 首页集成的任务选择下拉菜单。
- [x] **Calendar View**: 独立的数据回顾页面。

---

## Phase 5 智能化成果归档 (New)

### AI 助手 (ACIR Intelligent)
- [x] **架构设计**: 采用前端 Store 管理状态 + 后端 REST API 持久化存储的架构。
- [x] **会话管理**: 实现了类似 ChatGPT 的左侧会话列表，支持新建、切换、删除会话。
- [x] **上下文注入**: 自动读取 `PomodoroStore` (今日专注时长) 和 `TaskStore` (Top 5 待办)，打造“懂你的”效率顾问。
- [x] **安全性**: 实现了 API Key 的数据库存储方案，用户在 Profile 页面配置 Key，彻底杜绝前端硬编码泄露风险。
- [x] **UI/UX 极致打磨**:
  - 全屏沉浸式布局 (Full-bleed Layout)。
  - Glassmorphism 输入框 + 底部渐变遮罩。
  - 隐藏式滚动条设计。
  - 响应式侧边栏 (Mobile Friendly)。

---

## 待办与灵感 (Backlog for Phase 5+)

### 移动端适配 (Mobile Adaptation)
> 计划于 Phase 5 重点攻克
- [ ] **Bottom Navigation**: 移动端采用底部导航栏代替顶部 Header。
- [ ] **Touch Optimization**: 优化 Widget 的拖拽和点击体验。
- [ ] **Layout**: 强制单列布局，避免挤压。

### 细节体验优化 (UX Polish)
- [ ] **Transition**: 路由切换动画。
- [ ] **Micro-interactions**: 按钮反馈、计时结束特效。
- [ ] **Sound**: 更多白噪音选择，音量渐入渐出。

### 代码与性能 (Code & Performance)
- [ ] **Refactor**: `TimerSection` 和 `HeaderNav` 存在重复的背景色分析代码，应提取到 Store 或 Composable 中。
- [ ] **Assets**: 图片资源压缩与懒加载。
- [ ] **I18n**: 国际化支持 (准备多语言架构)。

### AI 助手与分析 (AI & Analytics)
> 长期规划
- [ ] **智能建议**: 根据专注历史建议最佳专注时长。
- [ ] **周报生成**: AI 生成专注周报摘要。

## 历史优化归档 (Milestone 3.0 已完成)

### 任务与看板优化
- [x] Task 界面下的 Kanban View 实现了拖拽功能，可以切换状态。
- [x] Kanban View 和 List View 支持点击卡片直接编辑。
- [x] List View 支持手动拖拽排序。
- [x] List View 加入分页 UI。
- [x] List View 智能排序：Done 任务自动沉底并按时间排序；禁用 Done 任务拖拽。
- [x] Kanban View 混合排序：默认按 Due Date 排序，支持列内手动拖拽。
- [x] Kanban View 列内滚动：限制高度，优化长列表体验。
- [x] Dashboard "My Tasks" 支持拖拽排序。
- [x] 解决手动排序与字段筛选的冲突（自动禁用/重置）。

### 沉浸式与统计
- [x] **热力图 (Heatmap)**: 类似 GitHub 贡献图，适配深色模式，优化色阶。
- [x] **标签分析 (Rose Chart)**: 统计标签专注时长。
- [x] **Zen Note**: 全屏专注时的防走神便签。
- [x] **精准时长**: 重构时长统计逻辑。
- [x] **深色模式**: 全局适配。

### 番茄钟
- [x] 实现对具体任务开启番茄钟并存入数据库。

### 其他待办 (Backlog for Future)
- [ ] **番茄估算 (Estimation)**: 预估任务需要的番茄钟数量。
- [ ] **快捷键系统**: `N` 新建, `Space` 暂停等。
- [ ] **白噪音混音**: 支持多音轨混音。
- [ ] **首页时长逻辑**: 检查 Approx 时间计算。
- [ ] **子任务Bug**: 偶发报错排查。