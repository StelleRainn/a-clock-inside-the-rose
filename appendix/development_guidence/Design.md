# ACIR Design 4.0: "Immersive Flow" (沉浸·心流)

> **设计代号**: Phase 4 - Aesthetic & Experience
> **核心理念**: 从“管理工具”转型为“专注空间”。用户打开应用是为了*开始*，而不仅仅是*规划*。

---

## 1. 设计愿景 (Vision)

我们决定推翻原有的“管理后台式”布局（侧边栏 + 仪表盘），拥抱一种更现代、更感性、更具沉浸感的设计语言。

*   **沉浸 (Immersive)**: 界面应当服务于“专注”这一核心行为，而非堆砌数据。
*   **极简 (Minimalist)**: 移除视觉噪点，使用留白和呼吸感来引导用户视线。
*   **灵动 (Fluid)**: 就像 Apple 官网一样，页面应当是流动的，配合丝滑的过渡和微交互。

---

## 2. 核心布局重构 (Layout Redesign)

### 2.1 导航栏变革 (Navigation)
*   **Current**: 固定侧边栏 (Aside Sidebar) —— 典型的 SaaS/Admin 后台风格，效率高但略显僵硬。
*   **New**: **顶部悬浮导航 (Header Navigation)**。
    *   **样式**: 采用双 Header 策略。
        *   **Hero Header**: 绝对定位，背景透明，文字颜色智能适配背景图深浅（或默认 Minimal Black）。
        *   **Fixed Header**: 固定定位，毛玻璃效果，响应系统深/浅色模式，滚动过 Hero 区域后滑出。
    *   **行为**: 智能切换，无缝过渡。
    *   **内容**:
        *   Left: Logo (ACIR 极简图标).
        *   Center: 核心模块 (Focus Station / Calendar / Tasks / Stats).
        *   Right: 全局功能 (Add Task, Profile, Settings, Theme Toggle).

### 2.2 背景与氛围 (Atmosphere)
*   引入 **动态背景 (Dynamic Backgrounds)** 系统。
    *   **设置**: 在 General 选项中可配置【调整背景图片】。
    *   **范围**: 背景图片仅对 **首页 Pomodoro 的 100vh Hero 区域** 有效。
    *   **默认主题**: 若未选择背景，默认采用 **Minimal Black** 主题（深灰色背景 + 白色文字），不随系统深浅色切换，保持专注氛围。
    *   **过渡**: 首页 Hero 区域的背景图在滚动到次级板块 (Dashboard Widgets) 时，自然上移淡出，平滑过渡到应用主题色。
    *   **其他页面**: Tasks, Stats 等其他路由页面保持纯净的深/浅模式背景，不显示壁纸。

---

## 3. 页面设计方案 (Page Specifics)

### 3.1 首页：番茄钟优先 (Focus Station)
这是本次改版最大的变化。首页不再是 Dashboard，而是 **Focus Station**。

*   **板块一：Hero Section (100vh)**
    *   **布局**: 占据首屏 100% 高度。
    *   **核心元素**: 
        *   **快捷模式切换**: 计时器上方增加三个快捷胶囊按钮 (Pomodoro, Short Break, Long Break)，点击即切换模式并重置时长。
        *   **任务选择器**: 支持下拉选择当前专注的任务，运行时锁定。
        *   中央巨大的 **计时器 (Timer)**。字体采用超细或超粗的现代无衬线体 (Inter / SF Pro Display)。
        *   极简控制按钮 (Start / Pause / Stop / Fullscreen)。
        *   **Zen Note**: 底部居中一个半透明的输入框，随时捕捉想法。
    *   **交互**: 鼠标移动时 UI 显现，静止 3 秒后 UI 淡出，极致沉浸。
    *   **全屏沉浸**: 支持 Full Screen 模式，隐藏所有 Header 和 Widgets，仅保留计时器。

*   **板块二：Customizable Widgets (Below the fold)**
    *   **理念**: 从“被动展示”转变为“主动定义”。
    *   **布局**: 采用 **Responsive Bento Grid (响应式便当盒)** 布局。
    *   **交互**: 
        *   支持点击 "Customize" 进入编辑模式。
        *   支持添加、移除 Widget。
        *   状态本地持久化。
    *   **组件库**:
        *   Calendar Heatmap (迷你热力图)
        *   Today's Focus (环形进度)
        *   Todo List (迷你任务列表)
        *   *Planned*: Quote of the Day, Quick Note, Music Player.

### 3.2 日历与回顾 (Calendar)
*   **定位**: 数据回顾与规划中心。
*   **内容**:
    *   Activity Heatmap (完整热力图)。
    *   History List (专注记录)。
    *   Weekly/Monthly Report (占位中)。

### 3.3 任务中心 (Tasks)
由于取消了侧边栏，任务界面将获得更宽阔的横向空间。

*   **容器化**: 内容包裹在居中的半透明容器中 (Container)，而非铺满全屏，保持阅读舒适度。
*   **List View**: 
    *   行高增加，更像 iOS 的 Reminders。
    *   Hover 效果增强（光标跟随或高亮）。
*   **Kanban View**:
    *   横向滚动体验优化。
    *   卡片设计更轻量，减少边框，利用阴影和背景色区分层级。

### 3.4 统计分析 (Stats)
*   **可视化升级**: 图表背景透明化，直接融合进整体的大背景中。
*   **Rose Chart & Heatmap**: 调整配色以适应新的全局背景（深色/高对比度）。

---

## 4. 全局设置 (Settings)
重构设置弹窗，采用 **Sidebar + Content** 布局。

*   **General**: 
    *   **背景图片**: 选择本地预设壁纸 (Day/Night) 或上传自定义图片。
    *   **外观控制**: 手动/自动 Hero 文字颜色，背景遮罩透明度调节。
*   **Timers**: 
    *   自定义 Pomodoro, Short Break, Long Break 的时长 (分钟)。
    *   自动开始休息/番茄钟的开关。
*   **Sounds**: 
    *   选择提示音 (Bell, Alarm, etc.)。
    *   音量调节滑块。
*   **Account**: 用户信息管理。

---

## 5. 实施路线图 (Implementation Roadmap)

### Phase 4: Aesthetic Optimization (Completed)
- [x] **架构调整**: 移除侧边栏，实现双 Header 导航系统 (Hero Header + Fixed Glass Header)。
- [x] **首页重构**: 转型为 Focus Station，包含 Hero Timer 和 Customizable Widgets (Bento Grid)。
- [x] **视觉升级**: 全局 Glassmorphism，引入 Inter 字体，Minimal Black 默认主题。
- [x] **沉浸体验**: 实现自动 UI 淡出 (3s无操作) 和全屏沉浸模式 (Fullscreen API)。
- [x] **智能主题**: 引入 `fast-average-color` 实现背景图智能文字变色，支持遮罩透明度调节。
- [x] **组件化**: 实现 Widget Store 和 Edit Mode，支持 Heatmap, Focus Ring, Todo List。
- [x] **数据中心**: 建立独立的 Calendar 页面用于数据回顾。

### Phase 5: The Second UI Revolution (Next Steps)
> **目标**: 在 Phase 4 确立的视觉框架下，丰富内容生态，打磨交互细节，实现“好用”到“爱用”的跨越。

#### 5.1 Widget 生态扩展 (Widget Ecosystem)
- [ ] **Quick Note (速记便签)**: 
    - 区别于 Zen Note，这是一个持久化的小组件，用于记录临时想法。
    - 支持 Markdown 语法。
- [ ] **Quote of the Day (每日名言)**:
    - 每日更新一句激励名言。
    - 支持自定义语录库。
- [ ] **Lo-Fi Player (白噪音/音乐)**:
    - 集成简单的白噪音播放器 (Rain, Forest, Cafe)。
    - (可选) Spotify/Apple Music 嵌入控制。
- [ ] **Countdown (倒数日)**:
    - 重要日期倒计时 (e.g., "Project Deadline", "Exams")。
- [ ] **Project Tracker (项目追踪)**:
    - 关联特定 Project 的进度条。

#### 5.2 交互与动画 (Interaction & Motion)
- [ ] **视图过渡 (View Transitions)**: 路由切换时的平滑过渡 (Fade / Slide)。
- [ ] **微交互 (Micro-interactions)**:
    - 按钮点击的涟漪效果。
    - Widget 添加/删除/拖拽的物理动画 (使用 VueTransition 或 AutoAnimate)。
    - 番茄钟完成时的全屏庆祝动画 (Confetti)。
- [ ] **手势操作 (Gestures)**: 移动端左滑返回，下滑刷新。

#### 5.3 移动端深度适配 (Mobile First)
- [ ] **移动端导航**: 在 Mobile 视图下隐藏顶部 Header，使用底部 TabBar。
- [ ] **Grid 响应式优化**: 手机端 Widget 强制单列或双列紧凑布局。
- [ ] **触控优化**: 增大点击热区。

#### 5.4 数据可视化增强 (Data Viz)
- [ ] **Calendar History**: 完整的专注流水记录 (Timeline View)。
- [ ] **Weekly Report**: 生成周报卡片，支持分享/下载图片。

#### 5.5 技术债与性能 (Tech & Perf)
- [ ] **Theme Logic Refactor**: 将 `TimerSection` 和 `HeaderNav` 中的背景色分析逻辑 (`fast-average-color`) 统一移至 Pinia Store (`pomodoro.js`)，避免重复计算。
- [ ] **Asset Optimization**: 背景图片懒加载与压缩。
- [ ] **Type Safety**: 补充 Widget 系统的 Props 定义和类型检查。

---

*此文档作为 Phase 5 开发的参考手册。*
