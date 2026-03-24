# 待改进 & 灵感 (Optimization Log)

> Tracking improvements, ideas, and technical debt.

## 待办与灵感 

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

### 其他待办 (Backlog for Future)
- [ ] **番茄估算 (Estimation)**: 预估任务需要的番茄钟数量。
- [ ] **快捷键系统**: `N` 新建, `Space` 暂停等。
- [ ] **白噪音混音**: 支持多音轨混音。
- [ ] **首页时长逻辑**: 检查 Approx 时间计算。
- [ ] **子任务Bug**: 偶发报错排查。
- [ ] **支持用户密码修改**

---

## 历史优化归档 

### 定稿至终稿期
- [x] **任务表单校验增强**: 修复了可以提交全空格空任务标题的 Bug，在前端提交前增加了 `.trim()` 校验。
- [x] **时区偏移修复**: 修复了在添加/编辑任务时，选择的截止时间 (Due Date) 传到后端后会产生 8 小时早退的问题。现在前端在发送前会将 Date 对象转换为基于本地时区的正确 ISO 字符串，消除了时区转换偏差。

#### 视觉与交互重塑 (Visual & Interaction Revamp)
- [x] **全局对话框毛玻璃化 (Glassmorphism Dialogs)**: 贯彻极简与沉浸式设计理念，重写了 Element Plus `el-dialog` 的全局样式。移除了原有的直角纯色边框，全面应用带有 `backdrop-filter` 的半透明玻璃拟态效果，并增加了大圆角 (`16px`) 和柔和的阴影。同时对对话框内部的输入框 (`el-input`, `el-select` 等) 进行了自适应半透明处理，完美兼容深/浅色模式，提升了整体 UI 的高级感与灵动性。


### 项目初稿阶段

#### 安全与鉴权优化 (Security & Auth)
- [x] **密码加密存储**: 引入 BCrypt 算法，在用户注册和修改密码时进行强哈希加密存储，彻底消除明文密码隐患。
- [x] **敏感信息脱敏**: 在后端登录接口和获取用户信息接口中，强制将 User 实体类的 password 字段设为 null，并在实体类使用 `@ToString.Exclude` 防止哈希密文传输至前端或在日志中泄露。
- [x] **平滑升级机制**: 登录接口实现向下兼容，若检测到用户密码仍为明文，在验证成功后会自动在后台将其静默升级为 BCrypt 哈希密文。
- [x] **API Key 加密方案**: 针对用户配置的 Gemini API Key，在存入数据库前使用 AES 对称加密 (`EncryptionUtil`)，并在读取时自动解密，既保证了数据库被拖库时的安全性，又确保了前端调用的可用性。

#### 体验与交互优化 (UX & Interaction)
- [x] **任务弹窗修复**: 修复了全局添加任务弹窗（Header 触发）缺少标签数据的 Bug，通过在弹窗内直接请求标签数据解耦了组件；修复了 Task 列表页面由于变量未声明导致无法呼出添加/编辑弹窗的错误。
- [x] **上下文注入精准度提升**: 修复了 AI 助手无法准确获取用户当日专注时长的问题。在发送请求前强制同步调用后端接口更新状态，并修正了 Store 中变量名的引用错误（由 `todayTotalMinutes` 修正为 `todayFocusSeconds / 60`），确保 AI 获取到的 System Context 始终是最新的真实数据。
- [x] **上下文信息维度扩充**: 为解决 AI 无法回答任务截止日期等细节的问题，重构了 `taskContext` 的构建逻辑。现在向 AI 注入的未完成任务信息不仅包含标题和优先级，还扩展了 **截止日期 (Due Date)** 和 **状态 (Status)**。对任务进行了智能分类与截断（紧急任务优先、高优任务次之）。此外，并行聚合了游戏化数据（连胜天数、等级）和近期趋势（昨日专注时长），并在 Prompt 中显式注入了当前的系统精确时间 (`Current Local Time`)，彻底消除了大模型的时间幻觉，极大地增强了 AI 的上帝视角能力。

### Milestone 5 智能化与全平台成果归档

#### 移动端适配 (Mobile Adaptation)
- [x] **导航重构**: 引入 Bottom TabBar (底部导航栏)，在移动端替代顶部菜单，提升单手操作体验。
- [x] **Header 适配**: 移动端顶部保留精简版 Header (Logo Icon + Profile + Theme)，解决 AI 页面遮挡问题，保持沉浸感。
- [x] **响应式布局**: 
  - **Focus Station**: Bento Grid 在移动端自动切换为单列流式布局。
  - **Task List**: 列表视图自动隐藏次要信息列 (Created At等)，优化移动端展示密度。
  - **AI Chat**: 侧边栏调整为抽屉模式 (Drawer)，聊天窗口自适应宽度。
- [x] **交互优化**: 增大移动端按钮点击热区，优化触摸反馈。

#### AI 助手 (ACIR Intelligent)
- [x] **架构设计**: 采用前端 Store 管理状态 + 后端 REST API 持久化存储的架构。
- [x] **会话管理**: 实现了类似 ChatGPT 的左侧会话列表，支持新建、切换、删除会话。
- [x] **上下文注入**: 自动读取 `PomodoroStore` (今日专注时长) 和 `TaskStore` (Top 5 待办)，打造“懂你的”效率顾问。
- [x] **安全性**: 实现了 API Key 的数据库存储方案，用户在 Profile 页面配置 Key，彻底杜绝前端硬编码泄露风险。
- [x] **UI/UX 极致打磨**:
  - 全屏沉浸式布局 (Full-bleed Layout)。
  - Glassmorphism 输入框 + 底部渐变遮罩。
  - 隐藏式滚动条设计。
  - 响应式侧边栏 (Mobile Friendly)。

### Phase 4 成果归档 (Completed in Aesthetic Update)

#### 视觉与交互 (Visual & Interaction)
- [x] **UI 重塑**: 全面转向 Glassmorphism 风格，移除侧边栏，采用双 Header 布局。
- [x] **沉浸模式**: 实现 Hero Timer，支持 3s 自动隐藏 UI，支持浏览器全屏模式。
- [x] **智能主题**: 集成 `fast-average-color`，实现背景图智能分析，自动调整文字颜色 (Light/Dark) 以保证可读性。
- [x] **动态背景**: 支持自定义上传背景，支持遮罩浓度调节。
- [x] **Bento Grid**: 首页次级板块采用可定制的网格布局，支持 Widget 增删。

#### 功能组件 (Functional Components)
- [x] **Zen Note**: 专注时的临时想法捕捉。
- [x] **Quick Toggles**: 番茄钟上方的快捷模式切换 (Pomodoro/Short/Long)。
- [x] **Task Selector**: 首页集成的任务选择下拉菜单。
- [x] **Calendar View**: 独立的数据回顾页面。

### Milestone 3 及更早 

#### 任务与看板优化
- [x] Task 界面下的 Kanban View 实现了拖拽功能，可以切换状态。
- [x] Kanban View 和 List View 支持点击卡片直接编辑。
- [x] List View 支持手动拖拽排序。
- [x] List View 加入分页 UI。
- [x] List View 智能排序：Done 任务自动沉底并按时间排序；禁用 Done 任务拖拽。
- [x] Kanban View 混合排序：默认按 Due Date 排序，支持列内手动拖拽。
- [x] Kanban View 列内滚动：限制高度，优化长列表体验。
- [x] Dashboard "My Tasks" 支持拖拽排序。
- [x] 解决手动排序与字段筛选的冲突（自动禁用/重置）。

#### 沉浸式与统计
- [x] **热力图 (Heatmap)**: 类似 GitHub 贡献图，适配深色模式，优化色阶。
- [x] **标签分析 (Rose Chart)**: 统计标签专注时长。
- [x] **Zen Note**: 全屏专注时的防走神便签。
- [x] **精准时长**: 重构时长统计逻辑。
- [x] **深色模式**: 全局适配。

#### 番茄钟
- [x] 实现对具体任务开启番茄钟并存入数据库。

