# ACIR Intelligent: AI 助手开发规划

> **核心目标**: 打造 ACIR 的“第二大脑”。不仅是一个聊天窗口，更是能够理解用户专注数据、任务状态的智能效率顾问。

---

## 1. 产品定位 (Product Definition)

*   **功能入口**: 第 5 个一级导航 (Header Nav: `Intelligent`).
*   **核心价值**:
    *   **智能问答**: 通用 LLM 对话能力。
    *   **效率诊断**: (进阶) 读取用户的 Stats/Tasks 数据，分析效率瓶颈。
    *   **情感陪伴**: 在用户疲惫时提供鼓励（结合 Zen Note 的语境）。

---

## 2. UI/UX 设计方案 (Immersive Chat)

参考主流 AI 产品 (ChatGPT/Claude) 的布局，但融入 ACIR 的 **Glassmorphism (毛玻璃)** 风格。

### 2.1 整体布局 (Layout)
采用 **三段式垂直布局**，限制在 `100vh` 内，确保页面不被撑开。

*   **Header (Top)**: 
    *   继承全局 HeaderNav，但在 AI 页面可能需要显示当前模型状态 (e.g., "Gemini 2.0 Flash • Online").
*   **Chat Container (Middle - Flexible)**:
    *   **高度**: `flex-grow: 1` (占据剩余空间)。
    *   **滚动**: `overflow-y: auto` (内部滚动)。
    *   **样式**: 透明或极淡的磨砂背景，让对话气泡悬浮于壁纸之上。
*   **Input Area (Bottom - Fixed)**:
    *   **位置**: 固定在底部，留有 `padding-bottom`。
    *   **样式**: 类似于 macOS Spotlight 或 Raycast 的悬浮输入框。
    *   **交互**: 支持 `Shift + Enter` 换行，`Enter` 发送。

### 2.2 对话气泡 (Bubbles)
*   **User**: 右侧对齐，主色调 (Primary Color) 渐变背景，圆角。
*   **AI**: 左侧对齐，Glassmorphism 背景 (半透明白/黑)，打字机效果 (Streaming effect)。

### 2.3 历史记录 (Sidebar - Optional)
*   桌面端：左侧可折叠的侧边栏，显示 "Recent Chats"。
*   移动端：通过汉堡菜单呼出。

---

## 3. 技术架构 (Technical Architecture)

### 3.1 前端集成 (Frontend First)
鉴于我们已有官方 JavaScript SDK 示例，初期开发将 AI 逻辑集成在前端。

*   **SDK**: `@google/genai`
*   **Store**: `stores/ai.js`
    *   管理 `messages` 数组: `[{ role: 'user', content: '...' }, { role: 'model', content: '...' }]`
    *   管理 `isLoading`, `currentModel`.
*   **Context Injection (核心差异化)**:
    *   在发送请求前，前端自动收集当前用户的上下文（如：今日专注时长、待办任务 Top 3），组装成 System Prompt 的一部分。
    *   *Prompt 示例*: "You are ACIR, a productivity assistant. The user has focused for 25 mins today and has 3 pending tasks..."

### 3.2 API Key 配置 (Security Update)
*   **不再使用** `.env` 存储硬编码 Key (避免泄露)。
*   **用户配置**: 在 `Profile Settings` 页面手动输入 Gemini API Key。
*   **持久化**: Key 将加密存储（或安全存储）在后端数据库 `users` 表中，每次对话时动态加载。
*   **Fallback**: 如果未配置，AI 功能将提示跳转设置。

---

## 4. 开发路线图 (Roadmap)

### Step 1: 基础框架 (Skeleton)
- [ ] 创建 `views/ai/AiAssistantView.vue`。
- [ ] 配置路由 `/intelligent`。
- [ ] 实现 "Chat Container + Input Area" 的 CSS 布局 (确保滚动体验丝滑)。

### Step 2: API 联调 (Connectivity)
- [ ] 安装 `@google/genai` 依赖。
- [ ] 实现基础对话功能（发送 -> 等待 -> 接收）。
- [ ] 处理 Markdown 渲染 (使用 `markdown-it` 或类似库渲染 AI 回复)。

### Step 3: 上下文感知 (Context Awareness)
- [ ] 编写 "Prompt Engineering" 逻辑。
- [ ] 让 AI 能读取 `useTaskStore` 和 `usePomodoroStore` 的数据。

### Step 4: 体验优化 (Polish)
- [ ] 打字机流式输出效果 (Streaming Response)。
- [ ] 历史对话本地存储 (LocalStorage / IndexedDB)。

---

## 附录：API 调用参考 (Reference Code)

### Google Gemini API (JavaScript SDK)

API_KEY: `YOUR_GEMINI_API_KEY` 

```js
import { GoogleGenAI } from "@google/genai";

// 显示提供 API 硬编码
const ai = new GoogleGenAI({ apiKey: "YOUR_API_KEY" });

async function main() {
    const response = await ai.models.generateContent({
        model: "gemini-3-flash-preview",
        contents: "Explain how AI works in a few words",
    });
    console.log(response.text);
}

main();
```

#### Note：

以上提供了硬编码方案。除此之外，还支持读取环境变量：

(以下引自官方原文，文章所提到的环境变量属于本机机器，不是 Web)

>以下示例使用 [`generateContent`](https://ai.google.dev/api/generate-content?hl=zh-cn#method:-models.generatecontent) 方法，通过 Gemini 2.5 Flash 模型向 Gemini API 发送请求。

>如果您将 API 密钥[设置为环境变量](https://ai.google.dev/gemini-api/docs/api-key?hl=zh-cn#set-api-env-var) `GEMINI_API_KEY`，那么在使用 [Gemini API 库](https://ai.google.dev/gemini-api/docs/libraries?hl=zh-cn)时，客户端会自动获取该密钥。否则，您需要在初始化客户端时[将 API 密钥作为实参传递](https://ai.google.dev/gemini-api/docs/api-key?hl=zh-cn#provide-api-key-explicitly)。

>请注意，Gemini API 文档中的所有代码示例都假定您已设置环境变量 `GEMINI_API_KEY`。

```js

import { GoogleGenAI } from "@google/genai";

// The client gets the API key from the environment variable `GEMINI_API_KEY`.
const ai = new GoogleGenAI({});

async function main() {
  const response = await ai.models.generateContent({
    model: "gemini-3-flash-preview",
    contents: "Explain how AI works in a few words",
  });
  console.log(response.text);
}

main();
```

