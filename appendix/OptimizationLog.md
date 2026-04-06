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
- [x] **I18n**: 国际化支持 (准备多语言架构)。

### AI 助手与分析 (AI & Analytics)
> 长期规划
- [ ] **智能建议**: 根据专注历史建议最佳专注时长。
- [ ] **周报生成**: AI 生成专注周报摘要。

### 其他待办 (Backlog for Future)
- [ ] **番茄估算 (Estimation)**: 预估任务需要的番茄钟数量。
- [ ] **快捷键系统**: `N` 新建, `Space` 暂停等。
- [ ] **白噪音混音**: 支持多音轨混音。
- [x] **首页时长逻辑**: 检查 Approx 时间计算。
- [x] **子任务Bug**: 偶发报错排查（已修复列表拖拽后子任务位置错乱问题）。
- [x] **支持用户密码修改**

---

## 历史优化归档 

### 终稿至答辩
- [x] **品牌标识替换与全局底部版权栏 (Brand Logo & Global Footer)**: 移除了原有的纯文本 Logo，在深浅色与沉浸模式下全局应用了全新的透明背景品牌标识图 (`ACIR_logo.png`)。同时为系统引入了 `GlobalFooter` 全局底部版权组件，支持中英双语切换，包含开发者签名与相关外链（学校官网、GitHub、个人总站），并在 `MainLayout` 中实现了优雅的自适应流式排版，避免了与核心内容的遮挡。
- [x] **任务列表操作列极简重构 (Task List Actions Minimalist Redesign)**: 彻底移除了 `/tasks` 列表右侧操作栏原有的 Element UI 默认块状按钮与文字，采用了完全手写的极简镂空圆形图标按钮 (`EditPen` & `Delete`)。新设计赋予了按钮轻量级的色彩边框与细腻的悬浮态光晕及上浮反馈，使复杂的任务列表界面变得更加通透、现代化，进一步强化了全站的极简设计语言。
- [x] **任务列表拖拽与子任务状态修复 (Task List Drag & Subtask Sync Fix)**: 彻底修复了 `/tasks` 页面 List View 中底层数据状态与 DOM 渲染脱节的三个严重 Bug。引入 `:key="tableKey"` 强制重新渲染 `el-table`，解决了手动拖拽后子任务列表在错误位置展开的 DOM 错位问题；重写了基于当前分页数据 (`paginatedTasks`) 的排序截取逻辑，确保了拖拽后的顺序能够正确持久化到后端数据库；修补了 `Sortable` 实例销毁不彻底的问题（销毁后正确置 `null`），实现了不同排序模式与手动拖拽间的丝滑无缝切换。
- [x] **个人主页与下拉组件毛玻璃视觉升级 (Profile & Dropdown Glassmorphism UI Polish)**: 对 `/profile` 个人资料页进行了深度体验优化，重构为“默认展示、点击编辑”的双态模式。默认状态下头像居中放大，并对敏感信息（密码、API 密钥）进行了智能脱敏与占位显示，增强了信息展示的层次感与安全性。同时，针对全局的下拉菜单（如 `/dashboard` 任务选择下拉框）统一引入了毛玻璃（Glassmorphism）与大圆角（16px）设计语言。通过将背景色调整为带 Alpha 通道的半透明色（`rgba`），并结合 `backdrop-filter: blur(20px)`，完美实现了通透的高级模糊质感，且全面兼容了深色模式（Dark Mode），使得全局 UI 风格更加统一沉浸。
- [x] **全局国际化与个人资料页汉化 (Global I18n & Profile Localization)**: 全面引入 Vue-i18n 进行多语言架构重构，将全站所有硬编码文本替换为可切换的中/英文变量。特别补全了 `/profile` 个人资料页面的多语言词条，涵盖用户信息、安全设置、AI配置及数据导出等模块，并在业务逻辑中全面支持了相关提示弹窗的国际化。
- [x] **全局排版与字体间距优化 (Global Typography Polish)**: 调整了全局 CSS 样式，将字间距 (`letter-spacing`) 统一增加至 `2px`，并适当缩小了字号，显著提升了界面的呼吸感与文字可读性。
- [x] **统计页图表排版重构 (Stats Charts Layout Optimization)**: 针对 `/stats` 页面增加字间距后导致的横向空间局促问题，深度优化了“标签专注分布”（玫瑰图）与“任务状态分布”（占比图）的排版。将原本的横向图例改为纵向排列 (`orient: 'vertical'`) 并靠左垂直居中，同时将图表主体整体向右偏移 (`center: ['65%', '50%']`)，实现了优雅的左右分栏视觉效果，完美适配了新的字体排版。
- [x] **番茄钟选中任务持久化与状态修复 (Timer Task Persistence)**: 修复了在首页 (`/dashboard`) 或番茄钟页面 (`/pomodoro`) 选中专注任务后，页面刷新会导致选中状态丢失、进而导致专注记录的任务 ID 存为 null 的问题。在 `pomodoro.js` Store 中引入了 `localStorage` 持久化 `selectedTaskId` 并在初始化时自动恢复；同时深度重构了 `PomodoroTimer.vue` 的响应式逻辑，将原先断层的 `ref` 替换为带有 `get/set` 的 `computed` 属性，并补全了缺失的 `updateDuration` 等方法。随后进一步修复了云端环境下任务 ID 类型不一致（浏览器恢复后为数字、任务列表返回为字符串）导致的匹配失败问题，统一改为以字符串形式持久化与比对，并在提交后端时再安全转换，从而保证本地与生产环境下都能稳定恢复选中任务。
- [x] **修复 Nginx 代理导致后端 404 的问题 (Nginx Reverse Proxy 404 Fix)**: 修复了公网部署时前端请求 `/api/auth/register` 等接口报 404 Not Found 的问题。原因在于 Nginx 的 `nginx.conf` 中 `proxy_pass http://acir-backend:8080/;` 带有尾部斜杠，导致 Nginx 在转发时截断了 `/api/` 前缀，使请求变成了 `/auth/register`，从而无法匹配后端 SpringBoot 控制器的 `@RequestMapping("/api/...")` 路由。通过移除 `proxy_pass` 尾部的斜杠解决了此问题。
- [x] **增加修改密码功能 (Change Password)**: 在用户信息页面 (`/profile`) 增加了修改密码的功能，用户可直接输入新密码进行修改。后端复用了原有的强哈希 (BCrypt) 加密逻辑，保障账户安全。
- [x] **首页禅定模式交互优化 (Zen Note Fade Logic)**: 修复了在首页（Focus Station）启动专注倒计时后，如果用户正在使用底部输入框 (Zen Note) 捕捉灵感（处于聚焦输入状态），3 秒无鼠标移动也会导致输入框错误淡出的问题。现在，只要输入框处于 `focus` 状态，即使触发了全局的 UI 淡出逻辑，该输入框也会保持可见，确保用户的文字录入过程不被打断，提升了极简体验下的可用性。
- [x] **设置弹窗 (Settings Dialog) UI优化**: 全面升级了首页 Settings 弹窗的视觉体验。增加了弹窗的整体宽度（从 `600px` 增至 `800px`），调整了合适的高度（`480px`），并将其整体位置上移（`top: 10vh`），大幅提升了内容留白与沉浸感。同时精准调整了弹窗内容的负边距补偿（`margin-top: -10px`），修复了弹窗标题（Settings）被内部容器遮挡及下沉的问题，并优化了 Timers 选项卡内的表单布局，彻底解决了之前宽度不足导致的横向滚动问题。细节方面，移除了左侧边栏多余的灰色背景底色，并隐藏了右侧内容区的默认滚动条，使整体弹窗的毛玻璃通透感更加纯粹。
- [x] **图表排版与图例优化 (Charts Layout)**: 针对 `/stats` 页面底部的 Focus Distribution (玫瑰图) 和 Task Status (环形图)，将其图例 (`legend`) 修改为可滚动模式 (`type: 'scroll'`)，以支持未来可能增加的多个 Tag。同时，将图表主体的位置下移 (`center: ['50%', '55%']`)，并略微缩小了玫瑰图的最大半径，从而彻底解决了图例与图表主体、标签文字重叠的问题，提升了视觉层次感。
- [x] **专注时长图表分页功能 (Focus Time Chart Pagination)**: 进一步优化了 Focus Time Chart，新增了基于时间维度的“上一页/下一页”切换功能。用户可以按照选定的时间范围（7天、30天、1年）动态向前或向后浏览历史数据，步幅与当前视图完美匹配。同时对按钮样式进行了响应式适配，确保在移动端设备上也能良好展示。
- [x] **专注时长图表优化 (Focus Time Chart)**: 修复了专注时长柱状图默认展示所有历史记录、导致横坐标拥挤的问题。现引入了时间范围筛选器（7天、30天、1年），并补全了没有记录的日期数据（填 0），保证横坐标的时间连续性。同时调整了图表的 `grid.bottom` 边距，解决了图例 (Legend) 与横坐标文字重叠的问题。
- [x] **统计页热力图配色修复**: 修复了 `/stats` 页面年度热力图在初次路由进入或刷新时，未正确应用深浅色模式配色（呈现默认蓝紫色）的问题。将未定义的 `updateHeatmapOptions` 修正为调用 `updateChartsTheme()`，确保图表在数据加载完成后立即同步当前的主题状态，从而正确渲染绿色系配色。

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
