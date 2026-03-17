# 毕业论文大纲：基于 Spring Boot + Vue 的个人效率中心系统设计与实现
## (A Clock Inside the Rose - ACIR)

## 摘要 (Abstract)
- **研究背景**：现代人注意力碎片化与现有工具的局限性。
- **核心工作**：设计并实现集“管理+专注+分析+智能”于一体的个人效率中心（ACIR）。
- **技术栈**：Spring Boot 3 + Vue 3 + MySQL 8。
- **创新亮点**：
  - **沉浸式体验 (Immersive Flow)**：引入 Glassmorphism 与 Hero Timer 首屏布局。
  - **数据驱动量化**：基于 GitHub 风格热力图与玫瑰图的多维数据分析。
  - **AI 上下文注入**：基于 Google Gemini API 的个性化效率智能诊断。
  - **多端响应式**：移动端触控优化与自适应 Bento Grid 布局。
- **结论**：验证了前后端分离架构在效率工具中的高效实践，并成功探索了 AIGC 在垂直场景中的应用。

## 第一章 绪论 (Introduction)
### 1.1 研究背景
- 现代人注意力碎片化问题及信息过载现状。
- 传统的纸质计划或简单的备忘录工具（缺乏统计、无专注引导）难以满足需求。
### 1.2 国内外研究现状
- **传统任务管理工具**（如 Todoist、Trello）：强管理、弱统计，缺乏专注过程的引导。
- **纯专注类工具**（如 Forest）：强专注、弱管理，缺乏长期的任务规划体系。
- **引出 ACIR 定位**：“任务管理 + 沉浸专注 + 深度统计 + AI 分析”的四位一体化个人效率中心。
### 1.3 研究目标与意义
- **工程实践意义**：验证 Spring Boot + Vue 前后端分离架构在中小型全栈应用中的可行性。
- **探索意义**：探索 Web 技术在人机交互（HCI）“心流体验”上的可能性，以及大语言模型（LLM）通过上下文注入在垂直效率场景落地的可行性。
### 1.4 论文结构安排
- 简述各章节内容分布。

## 第二章 相关技术介绍 (Technology Stack)
### 2.1 前端开发技术
- **Vue 3 (Composition API) & Vite**：构建高性能单页应用 (SPA)。
- **Pinia**：复杂状态管理（如番茄钟状态、用户上下文、AI 会话）。
- **Element Plus & ECharts**：定制化 UI 组件库与复杂数据可视化。
- **Glassmorphism CSS**：实现现代毛玻璃沉浸式 UI。
### 2.2 后端开发技术
- **Spring Boot 3.x**：快速构建 RESTful API 与业务逻辑处理。
- **MyBatis & MySQL 8**：持久层框架与关系型数据库设计（支持复杂聚合查询）。
- **JWT (JSON Web Token)**：实现无状态的用户身份认证。
### 2.3 AI 与其他技术
- **Google Gemini API**：基于流式响应 (Streaming) 的生成式 AI 交互。
- **Apache Commons CSV**：实现规范化的用户数据导出。

## 第三章 系统需求分析 (Requirements Analysis)
### 3.1 业务流程分析
- 核心闭环：用户注册/登录 -> 规划任务 (看板/列表) -> 开启番茄钟 (进入心流) -> 产出专注数据 -> AI 读取数据并提供诊断 -> 优化个人习惯。
### 3.2 功能性需求
- **任务深度管理**：CRUD、多维标签系统、子任务拆解、交互式看板 (Kanban) 拖拽。
- **沉浸式专注计时**：自定义番茄钟时长、全屏专注、防走神便签 (Zen Note)。
- **多维数据分析**：GitHub 风格年度热力图、标签时间分布（玫瑰图）、数据导出。
- **智能 AI 助手**：具备系统状态感知能力（Context Injection）的效率问答与诊断。
### 3.3 非功能性需求
- **交互与体验**：全站深/浅色模式无缝切换，动态背景智能适配。
- **响应式与多端适配**：PC 端双 Header 导航至移动端 Bottom TabBar 的无缝降级，Bento Grid 自适应。
- **性能与安全**：用户密码加密，API Key 安全存储策略，大列表客户端分页。

## 第四章 系统设计 (System Design)
### 4.1 总体架构设计
- **B/S 架构图**：Client (Browser/Mobile) -> Nginx/Vite -> Spring Boot (Controller-Service-DAO) -> MySQL。
### 4.2 数据库设计 (E-R图)
- **核心实体**：`User`, `Task`, `Subtask`, `Tag`, `PomodoroRecord`。
- **复杂关联设计**：重点展示 Task 与 Tag 的多对多关系 (`task_tags`)，以及 Task 与 Pomodoro 记录的一对多关系。
### 4.3 界面与交互设计 (UI/UX)
- **设计理念 ("Immersive Flow")**：从传统的“管理后台”范式转型为“专注空间”，强调留白与视觉引导。
- **布局演进与重构**：从早期的固定侧边栏 (Aside Sidebar) 演进为顶部的双 Header (Hero Header + Fixed Glass Header)。
- **响应式策略**：移动端 (Mobile First) 的 Bottom TabBar 设计，及大屏 Bento Grid 向小屏单列流式布局的降级策略。

## 第五章 系统实现 (Implementation)
*(本章聚焦工程技术难点与核心算法)*
### 5.1 沉浸式番茄钟与动态主题实现
- **状态流转引擎**：基于 Pinia 的前端计时器状态机（Pomodoro / Short Break / Long Break 切换）。
- **动态背景与色彩自适应**：利用 `fast-average-color` 算法提取背景图主色调，智能计算 Hero Timer 的文字对比度与遮罩透明度。
### 5.2 交互式任务看板 (Kanban) 开发
- **拖拽排序核心逻辑**：基于 `Sortablejs` 的列表与看板拖拽实现，解决手动排序与自动筛选（如按时间排序）的冲突逻辑。
- **交互细节优化**：防误触机制（禁止拖入已完成区域）及列内独立滚动 (Scrollable Columns) 限制。
### 5.3 数据可视化与聚合统计模块
- **高阶图表渲染**：ECharts 在深色模式下的响应式重绘机制。
- **热力图数据处理**：后端 MyBatis 编写复杂 SQL（`SUM`, `GROUP BY` 时间戳函数）精确聚合用户的每日专注秒数，适配前端按活跃度渲染色阶。
### 5.4 具备上下文感知能力的 AI 助手
- **Prompt Engineering 与 Context Injection**：如何在请求前自动收集 `user_stats` 与 `todo_list` 数据，动态组装进 System Prompt。
- **流式对话渲染**：前端基于 `@google/genai` SDK 实现 Streaming 响应，结合 Markdown 渲染打造打字机气泡体验。
### 5.5 移动端适配与跨端体验打磨
- **导航系统智能切换**：利用 CSS Media Queries 配合 Vue 组件按需加载，实现 PC 端顶部导航与移动端底部 TabBar (`MobileNavBar`) 的切换。
- **触控优化**：扩大热区，调整移动端单列布局策略，并解决移动端视口 (Safe Area) 与软键盘遮挡问题。

## 第六章 系统测试 (Testing)
### 6.1 测试环境与工具
- 测试软硬件环境说明。
### 6.2 功能测试
- 核心用例：任务创建与标签绑定、拖拽逻辑越界测试、番茄钟跨状态计时的准确性、AI 上下文读取准确性测试。
### 6.3 兼容性与响应式测试
- 跨浏览器兼容性。
- 移动端不同分辨率下的 Bento Grid 断点测试，深浅色主题切换测试。
### 6.4 性能测试
- 首屏加载性能分析，复杂看板视图渲染帧率，大批量数据下导出功能的响应耗时。

## 第七章 总结与展望 (Conclusion)
### 7.1 工作总结
- 成功构建并落地 ACIR 个人效率中心，实现了从“任务管理”到“心流引导”再到“数据量化与AI赋能”的完整闭环。
### 7.2 存在的不足
- 离线支持不足（PWA 本地化能力待完善）。
- 白噪音混音等进阶体验仍有优化空间。
### 7.3 未来展望
- 探索多端数据实时同步（WebSocket / SSE）。
- 引入更强大的 AI Agent 能力（如 AI 自动拆解复杂任务，自动排期）。

## 参考文献 (References)
*(按标准格式列出文献)*

## 致谢 (Acknowledgements)
*(致谢导师、同学及开源社区)*
