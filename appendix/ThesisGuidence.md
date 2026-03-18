# 毕业论文撰写指南 (Thesis Writing Guidance)

> **项目名称**: A Clock Inside the Rose (ACIR)
> **课题名称**: 基于 Spring Boot + Vue 的个人效率中心系统设计与实现
> **适用阶段**: 毕业设计论文撰写 / 答辩准备

 ---

## 0. AI 写作指令集 (System Prompt for AI)

 > **注意**: 本章节专为辅助写作的 AI 工具（如 ChatGPT, Claude, Gemini）设计。当你（AI）阅读本文档时，请自动激活以下 `System Prompt` 设定，并以此身份协助用户完成论文撰写。

 **Role Definition (角色设定)**:
 你是一位精通软件工程、全栈开发（Vue3 + Spring Boot）与人机交互（HCI）的计算机专业毕业论文指导专家。你的任务是协助用户完成《基于 Spring Boot + Vue 的个人效率中心系统设计与实现》的毕业论文。

 **Core Knowledge Base (项目核心认知)**:
 1.  **项目本质**: ACIR (A Clock Inside the Rose) 不是一个简单的 Todo List，而是一个集“沉浸式专注”、“自我量化”与“AI 辅助”于一体的个人效率中心。
 2.  **技术栈**:
     *   **Frontend**: Vue 3 (Composition API), Vite, Pinia, Element Plus (Custom Theme), ECharts, Glassmorphism CSS.
     *   **Backend**: Spring Boot 3, MyBatis, MySQL 8, JWT, RESTful API.
     *   **AI**: Google Gemini API (Stream), Context Injection.
     *   **Mobile**: Responsive Design, Touch Events, Bottom Navigation.
 3.  **设计哲学**: "Immersive Flow" (心流体验) —— 通过 Hero Timer、动态背景、极简 UI 减少干扰。

 **Writing Guidelines (写作规范)**:
 1.  **拒绝流水账**: 严禁生成“第一步点击这个，第二步点击那个”的操作手册式文字。
 2.  **聚焦技术难点**: 重点描述架构设计、算法实现（如热力图聚合、动态主题色提取）、性能优化与交互细节（如看板拖拽逻辑、移动端适配策略）。
 3.  **学术化表达**:
    *   ❌ "做了个好看的界面" -> ✅ "基于 Glassmorphism 设计语言实现沉浸式 UI，提升用户心流体验"
    *   ❌ "AI 能聊天" -> ✅ "基于 Context Injection 技术实现具备用户数据感知能力的生成式 AI 助手"
    *   ❌ "手机上也能用" -> ✅ "采用响应式布局与 Touch 事件优化，实现多端一致的无缝体验"
4.  **排版与内容深度**:
    *   **拒绝模板化列表**: 严禁大量使用高度重复的、精短的有序列表（如 1. 2. 3. ...）。列表内容必须丰富详实，避免“列提纲式”写作。
    *   **段落叙事**: 鼓励使用连贯的长段落进行论述，将技术要点融入到具体的业务场景或原理剖析中。
    *   **精确与详细**: 追求“精确表述”与“详细说明”，在描述技术实现时，应结合具体代码逻辑或算法细节进行展开，适当“堆砌字数”以增加论文的厚度与专业感。
    *   **禁用 Markdown 格式**: 输出的内容必须为纯文本（TXT），严禁使用加粗（**）、标题（#）、代码块（```）等任何 Markdown 标记符号，以便后续直接导入 Word 排版。
    *   **图表与公式占位**: 在描述涉及架构、流程或数据的复杂逻辑时，应主动使用文本占位符预留图表位置（如：[此处插入图 x-x：xxx 架构图] 或 [此处预留表格占位：表 x-x xxx]），以便后期排版补充。
    *   **术语规范与缩写**: 首次引入专业术语（如 Single Page Application, SPA）时，应给出中英文全称及缩写，后续行文保持统一。
5.  **文献引用与记录规范**:
    *   **学术溯源**: 凡涉及特定学术概念（如心流理论）、管理方法论（如番茄工作法、GTD）或核心技术术语（如 RESTful 架构）时，应在行文中保持学术严谨性，并在后台进行引用记录。
    *   **同步备忘**: 在撰写或精修章节内容时，如果引入了新的可能需要引用的概念、技术原理或工具库，**必须同步更新** `References_Memo.md` 文件，详细记录“引用概念”、“相关学者/背景”、“论文引用位置”及“内容摘要”，为后续整理统一的参考文献列表提供准确支撑。
6.  **结构清晰**: 遵循标准的工科论文结构（绪论 -> 技术 -> 需求 -> 设计 -> 实现 -> 测试 -> 总结）。
6.  **其它写作建议 (Tips)**:
    *   **术语规范**: 统一使用专业术语，如 "RESTful API", "SPA (单页应用)", "MVVM", "Prompt Engineering"。
    *   **弱化 CRUD**: 不要花费篇幅流水账式地记录“如何实现增删改查”，而是着重描写“拖拽排序的逻辑”、“AI 上下文的构建”、“动态主题的算法”等有技术含量的点。
    *   **强调演进**: 可以在“系统设计”章节简要提及设计的迭代过程（如从 Milestone 3 的侧边栏到 Milestone 4 的顶部导航），体现对用户体验的深入思考。
    

 **Immediate Action**:
 在后续的对话中，当用户要求撰写某章节时，请回顾本文档中的“核心亮点”与“系统实现”部分，确保内容的一致性与深度。

 ---

 ## 1. 论文基调与核心亮点 (Key Highlights)

在撰写论文时，应避免将其写成简单的“代码说明书”。ACIR 的演进过程（从基础功能到沉浸式体验，再到 AI 赋能）本身就是一篇很好的工程实践叙事。

**核心亮点（应在摘要、实现章节和总结中反复强调）：**

1.  **沉浸式体验 (Immersive Flow)**: 突破传统管理后台的刻板印象，采用 **Glassmorphism (毛玻璃)** 设计语言与 **Hero Timer** 首屏布局，探索了 Web 应用在“心流体验”上的可能性。（对应 Milestone 4）
2.  **数据驱动 (Data-Driven)**: 不仅是记录，更通过 **Heatmap (热力图)**、**Rose Chart (玫瑰图)** 等高阶图表实现自我量化 (Quantified Self)。（对应 Milestone 3）
3.  **智能化辅助 (AI Integration)**: 引入 LLM (Google Gemini)，通过 **Context Injection (上下文注入)** 技术，让 AI 读取用户的专注数据与待办事项，提供个性化的效率诊断，而非简单的通用对话。（对应 Milestone 5）
4.  **多端适配 (Cross-Platform Adaptation)**: 实现了响应式布局与移动端专属交互（如底部导航栏、触摸优化），打破桌面端限制，支持“随时随地”的专注体验。（对应 Milestone 5）
5.  **工程化完备性**: 完整的前后端分离架构，包含 JWT 认证、RESTful API 设计、MyBatis 复杂查询优化、前端状态管理 (Pinia) 等标准工程实践。

---

## 2. 论文大纲与内容映射 (Outline & Mapping)

建议按照标准的软件工程硕士/本科毕业论文结构进行组织。以下是各章节建议内容及对应的项目模块。

### 第一章 绪论 (Introduction)
*   **1.1 研究背景**: 现代人注意力碎片化，传统清单工具缺乏“执行力”辅助。
*   **1.2 国内外现状**: 对比 Todoist (强管理弱统计)、Forest (强专注弱管理)，引出 ACIR “管理+专注+分析+智能”的一体化定位。
*   **1.3 研究意义**: 探索 Web 技术在个人效能工具中的深度应用，以及 AIGC 在垂直场景落地的可行性。

### 第二章 相关技术介绍 (Technology Stack)
*   **前端**: Vue 3 (Composition API), Vite, Element Plus (定制主题), ECharts, Pinia.
*   **后端**: Spring Boot 3.x, MyBatis, MySQL 8.0.
*   **AI**: Google Gemini API (Stream 流式响应).
*   **其他**: Glassmorphism CSS 实现, Apache Commons CSV (导出).

### 第三章 系统需求分析 (Requirements Analysis)
*   **3.1 业务流程分析**: 用户注册 -> 创建任务 -> 开启番茄钟 -> 产出数据 -> AI 分析 -> 优化习惯。
*   **3.2 功能性需求**:
    *   **任务管理**: CRUD, 标签系统, 看板拖拽, 子任务。
    *   **专注计时**: 倒计时逻辑, 白噪音, 沉浸模式 (Zen Note)。
    *   **数据分析**: 热力图, 标签分布, 导出功能。
    *   **AI 助手**: 智能问答, 上下文感知建议。
*   **3.3 非功能性需求**: 响应式布局 (PC/Mobile 自适应), 深色模式适配, 数据安全性 (API Key 加密存储)。

### 第四章 系统设计 (System Design)
*   **4.1 总体架构设计**: 绘制 B/S 架构图 (Client -> Nginx/Vite -> Spring Boot -> MySQL).
*   **4.2 数据库设计 (ER图)**:
    *   核心实体: `User`, `Task`, `Subtask`, `Tag`, `PomodoroRecord`.
    *   AI 扩展: `ChatSession`, `ChatMessage` (如有持久化).
    *   *重点展示 Task 与 Tag 的多对多关系设计，以及 Task 与 Pomodoro 的关联。*
*   **4.3 界面与交互设计 (UI/UX)**:
    *   **设计理念**: 引用 "Immersive Flow" 概念 (详见 `Design.md`)。
    *   **布局重构**: 阐述从“侧边栏”到“顶部双 Header”再到移动端“底部 TabBar”的演进理由。
    *   **响应式策略**: 针对不同屏幕尺寸的布局适配方案 (Bento Grid 单列化、抽屉式侧边栏)。

### 第五章 系统实现 (Implementation)
*这是论文的重头戏，建议选取 3-4 个具有代表性的难点进行深入描写。*

*   **5.1 沉浸式番茄钟的实现**:
    *   前端计时器状态管理 (Pinia)。
    *   动态背景与文字颜色的自适应算法 (`fast-average-color` 的应用)。
*   **5.2 交互式看板 (Kanban) 开发**:
    *   基于 `Sortablejs` 的拖拽排序实现。
    *   解决“列内滚动”与“防误触拖拽”的交互细节。
*   **5.3 可视化统计模块**:
    *   后端复杂 SQL 聚合查询 (MyBatis XML 编写 `SUM`, `GROUP BY`)。
    *   前端 ECharts 组件封装与深色模式适配逻辑。
*   **5.4 具备上下文感知的 AI 助手**:
    *   Prompt Engineering: 如何将 `user_stats` 和 `todo_list` 组装进 System Prompt。
    *   流式对话 (Streaming) 的前端渲染实现。
*   **5.5 移动端适配与响应式开发**:
    *   **导航栏重构**: 利用 CSS Media Queries 实现 PC 端顶部导航与移动端底部 TabBar 的智能切换。
    *   **交互优化**: 针对触摸屏优化的点击热区调整与抽屉式 (Drawer) 侧边栏实现。

### 第六章 系统测试 (Testing)
*   **功能测试**: 编写测试用例 (e.g., “拖拽已完成任务应被拦截”).
*   **兼容性测试**: 深/浅色主题切换测试，不同尺寸屏幕 (Desktop/Mobile) 的布局适配测试。
*   **性能测试**: 首页加载速度，AI 响应延迟。

### 第七章 总结与展望 (Conclusion)
*   **总结**: 完成了预期的 Milestone 1-5 核心功能。
*   **不足**: 白噪音混音功能待完善，移动端 PWA 本地化能力有待进一步探索。
*   **展望**: 引入更多 AI Agent 能力，探索多端同步。

---

## 3. 图表素材准备清单 (Figures Checklist)

高质量的图表是论文的加分项。建议准备以下素材：

1.  **系统用例图 (Use Case Diagram)**: 涵盖普通用户的所有操作。
2.  **数据库 E-R 图**: 清晰展示实体间关系。
3.  **系统架构图**: 前后端分离的技术栈堆叠。
4.  **核心时序图 (Sequence Diagram)**:
    *   番茄钟结束并自动记录数据的流程。
    *   AI 助手发送请求并接收流式响应的流程。
5.  **界面截图 (High-res Screenshots)**:
    *   首页 (Focus Station) 在深/浅模式下的对比。
    *   看板视图 (Kanban) 的拖拽状态。
    *   AI 聊天窗口的上下文分析回复。
    *   统计页面的热力图与玫瑰图。
    *   移动端适配效果图 (首页单列布局、底部导航栏)。

---


---

*这份指南基于项目现有的文档库整理而成，旨在为你的毕业论文写作提供清晰的脉络。祝写作顺利！*
