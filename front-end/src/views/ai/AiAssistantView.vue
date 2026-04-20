<template>
  <div class="ai-view">
    <!-- 
    AiAssistantView: 智能体对话视图。
    采用了类似 ChatGPT 的经典双栏布局 (侧边栏会话列表 + 主内容区聊天界面)。
    这里使用了 `fixed` 定位来脱离 MainLayout 的常规流，从而实现全屏沉浸式的聊天体验。
  -->
    <!-- 
      左侧栏 (Sidebar): 历史会话列表 
      在移动端，通过 `isSidebarOpen` 状态配合 CSS 的 `transform: translateX` 实现抽屉式滑入滑出。
    -->
    <div class="chat-sidebar" :class="{ 'sidebar-open': isSidebarOpen }">
      <div class="sidebar-header">
        <el-button type="primary" class="new-chat-btn" @click="handleNewChat">
          <el-icon><Plus /></el-icon> {{ $t('ai.newChat') }}
        </el-button>
        <!-- 移动端专属的关闭抽屉按钮 -->
        <el-button class="mobile-close-btn" circle @click="isSidebarOpen = false">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      
      <!-- 会话列表展示区 -->
      <div class="sessions-list" v-loading="isSessionsLoading">
        <div v-if="sessions.length === 0 && !isSessionsLoading" class="no-sessions">
          {{ $t('ai.noPreviousChats') }}
        </div>
        <div 
          v-for="session in sessions" 
          :key="session.id"
          class="session-item"
          :class="{ 'active': currentSessionId === session.id }"
          @click="selectSession(session.id)"
        >
          <el-icon><ChatDotSquare /></el-icon>
          <!-- session.title 是在 AI 回复第一句话时截取生成的，见 stores/ai.js -->
          <span class="session-title">{{ session.title }}</span>
          
          <!-- 删除会话按钮：使用了 @click.stop 阻止事件冒泡，防止误触发选中会话 -->
          <el-button 
            type="danger" 
            link 
            class="delete-btn" 
            @click.stop="deleteSession(session.id)"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 右侧主聊天区 (Main Chat Area) -->
    <div class="chat-main">
      <!-- 移动端专属的汉堡菜单按钮，用于呼出侧边栏 -->
      <div class="mobile-header">
        <el-button circle @click="isSidebarOpen = true">
          <el-icon><Menu /></el-icon>
        </el-button>
        <span class="mobile-title">{{ $t('ai.intelligent') }}</span>
      </div>

      <!-- 聊天消息滚动容器 -->
      <div class="chat-container" ref="chatContainerRef">
        <!-- 空状态：当是一个全新会话时，展示打招呼信息和快捷指令卡片 -->
        <div v-if="messages.length === 0" class="empty-state">
          <el-icon :size="64" class="ai-icon"><Cpu /></el-icon>
          <h2>{{ $t('ai.intelligent') }}</h2>
          <p>{{ $t('ai.intro') }}</p>
          <div class="suggestions">
            <el-tag 
              v-for="s in suggestions" 
              :key="s.key" 
              effect="plain" 
              class="suggestion-tag"
              @click="sendMessage($t(`ai.${s.key}`))"
            >
              {{ $t(`ai.${s.key}`) }}
            </el-tag>
          </div>
        </div>

        <!-- 消息列表渲染区 -->
        <div v-else class="messages-list">
          <!-- 
            消息气泡包装器：通过判断 msg.role，动态应用 `is-user` 类名。
            `is-user` 会将 flex 方向设置为 row-reverse，从而让用户头像和消息靠右显示。
          -->
          <div 
            v-for="(msg, index) in messages" 
            :key="index" 
            class="message-wrapper"
            :class="{ 'is-user': msg.role === 'user' }"
          >
            <div class="avatar">
              <!-- 用户的真实头像 or AI 的固定图标 -->
              <el-avatar v-if="msg.role === 'user'" :size="36" :src="userAvatar" icon="UserFilled" />
              <div v-else class="ai-avatar">
                <el-icon><Cpu /></el-icon>
              </div>
            </div>
            
            <div class="message-content">
              <!-- 
                气泡实体：
                如果 role === 'model' (AI 返回)，则使用 markdown-it 渲染为 HTML；
                如果是用户输入，则作为普通文本渲染，防止 XSS 注入。
              -->
              <div class="bubble" :class="{ 'user-bubble': msg.role === 'user', 'ai-bubble': msg.role === 'model', 'error-bubble': msg.isError }">
                <div v-if="msg.role === 'model'" v-html="renderMarkdown(msg.content)" class="markdown-body"></div>
                <div v-else>{{ msg.content }}</div>
              </div>
              <div class="timestamp">
                {{ formatTime(msg.timestamp) }}
              </div>
            </div>
          </div>
          
          <!-- 
            打字机 Loading 占位符：
            在发送请求但大模型尚未返回完整数据时，显示三个跳动的小圆点，增强等待体验。
          -->
          <div v-if="isLoading" class="message-wrapper">
            <div class="avatar">
              <div class="ai-avatar"><el-icon><Cpu /></el-icon></div>
            </div>
            <div class="message-content">
              <div class="bubble ai-bubble typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 
        底部输入区 (Input Area) 
        使用了绝对/固定定位在底部，并通过背景渐变(linear-gradient)实现了文字滚入输入框后方的“消失感”。
      -->
      <div class="input-area">
        <div class="input-wrapper">
          <!-- 支持自动长高 (autosize) 的文本域 -->
          <el-input
            v-model="inputMessage"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 4 }"
            :placeholder="$t('ai.inputPlaceholder')"
            resize="none"
            @keydown.enter.prevent="handleEnter"
            :disabled="isLoading"
          >
            <template #prefix>
              <el-icon class="input-icon"><ChatDotRound /></el-icon>
            </template>
          </el-input>
          <!-- 发送按钮：无输入内容或 AI 正在思考时禁用 -->
          <el-button 
            type="primary" 
            circle 
            :icon="Position" 
            :disabled="!inputMessage.trim() || isLoading"
            @click="handleSend"
            class="send-btn"
          />
        </div>
        <div class="disclaimer">
          {{ $t('ai.disclaimer') }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useAiStore } from '@/stores/ai'
import { useUserStore } from '@/stores/user'
import { Cpu, Position, ChatDotRound, Plus, ChatDotSquare, Delete, Menu, Close } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import MarkdownIt from 'markdown-it'
import { ElMessageBox, ElMessage } from 'element-plus'

// 初始化
const { t } = useI18n()
const aiStore = useAiStore()
const userStore = useUserStore()

// 实例化 Markdown-it，配置 linkify 让文本中的 URL 自动转为超链接
const md = new MarkdownIt({
  html: false, // 禁用 HTML 标签渲染，防止 XSS
  linkify: true,
  breaks: true // 将 \n 转换为 <br>
})

const chatContainerRef = ref(null)
const inputMessage = ref('')
const isSidebarOpen = ref(false)

// 派生状态映射 (与 Pinia Store 联动)
const messages = computed(() => aiStore.messages)
const sessions = computed(() => aiStore.sessions)
const currentSessionId = computed(() => aiStore.currentSessionId)
const isSessionsLoading = computed(() => aiStore.isSessionsLoading)
const isLoading = computed(() => aiStore.isLoading)
const userAvatar = computed(() => userStore.user?.avatarUrl || '')

// 预设的快捷指令，在 i18n 语言包中定义
const suggestions = [
  { key: 'suggAnalyze' },
  { key: 'suggPrioritize' },
  { key: 'suggTired' },
  { key: 'suggSummarize' }
]

// ==========================================
// 辅助与工具函数
// ==========================================
const renderMarkdown = (text) => {
  return md.render(text || '')
}

const formatTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

// 自动滚动到底部机制
// 必须包裹在 nextTick 中，确保 Vue 已经完成了最新的 DOM 渲染后再获取 scrollHeight
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  })
}

// 监听消息数组的长度变化，只要有新消息进来，立刻触发滚动到底部
watch(() => aiStore.messages.length, scrollToBottom)


// ==========================================
// 核心交互逻辑
// ==========================================
const handleNewChat = () => {
  aiStore.startNewSession()
  // 移动端体验优化：新建对话后自动收起侧边栏抽屉
  if (window.innerWidth <= 768) {
    isSidebarOpen.value = false
  }
}

const selectSession = async (id) => {
  await aiStore.selectSession(id)
  if (window.innerWidth <= 768) {
    isSidebarOpen.value = false
  }
}

const deleteSession = (id) => {
  ElMessageBox.confirm(t('ai.deleteChatConfirm'), t('tasks.deleteConfirmTitle'), {
    confirmButtonText: t('tasks.ok'),
    cancelButtonText: t('tasks.cancel'),
    type: 'warning'
  }).then(() => {
    aiStore.deleteSession(id)
    ElMessage.success(t('ai.chatDeleted'))
  }).catch(() => {})
}

// 拦截文本域的回车事件
const handleEnter = (e) => {
  // 如果按下了 Shift，放行原生的换行行为；如果只有 Enter，则拦截并执行发送
  if (e.shiftKey) return 
  handleSend()
}

// 发送消息的主入口
const handleSend = async () => {
  const text = inputMessage.value
  if (!text.trim()) return
  
  inputMessage.value = '' // 先清空输入框，提供即时的交互反馈
  // 调用 store 发起网络请求并流式渲染
  await aiStore.sendMessage(text)
}

// 用于快捷指令点击
const sendMessage = (text) => {
  inputMessage.value = text
  handleSend()
}

onMounted(() => {
  aiStore.loadSessions()
  scrollToBottom()
})

</script>

<style scoped>
.ai-view {
  height: 100vh; /* Full viewport height */
  width: 100vw;  /* Full viewport width */
  position: fixed; /* Fix to viewport to override MainLayout padding */
  top: 0;
  left: 0;
  display: flex;
  overflow: hidden;
  background: transparent; /* Remove background */
  z-index: 10; /* Ensure it's above background but below header if needed */
}

/* Sidebar Styles */
.chat-sidebar {
  width: 260px;
  background: rgba(255, 255, 255, 0.5); /* Lighter glass */
  backdrop-filter: blur(20px);
  border-right: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  transition: transform 0.3s ease;
  z-index: 20;
  padding-top: 80px; /* Clear the global header */
}
.dark .chat-sidebar {
  background: rgba(20, 20, 20, 0.5);
  border-right: 1px solid rgba(255, 255, 255, 0.05);
}

.sidebar-header {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.new-chat-btn {
  width: 100%;
  justify-content: flex-start;
  border-radius: 8px;
}
.mobile-close-btn {
  display: none;
}

.sessions-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 10px 20px;
}
.no-sessions {
  text-align: center;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  margin-top: 20px;
}
.session-item {
  display: flex;
  align-items: center;
  padding: 12px 10px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 4px;
  color: var(--el-text-color-regular);
  transition: all 0.2s;
  position: relative;
}
.session-item:hover {
  background: rgba(0, 0, 0, 0.05);
}
.dark .session-item:hover {
  background: rgba(255, 255, 255, 0.05);
}
.session-item.active {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-weight: 500;
}
.dark .session-item.active {
  background: rgba(var(--el-color-primary-rgb), 0.2);
}
.session-title {
  margin-left: 10px;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
}
.delete-btn {
  opacity: 0;
  padding: 4px;
}
.session-item:hover .delete-btn {
  opacity: 1;
}

/* Main Chat Area */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  min-width: 0; /* Important for flex child truncating */
  padding-top: 80px; /* Clear the global header */
  background: transparent;
  align-items: center; /* Center the chat container horizontally */
  width: calc(100vw - 260px); /* Fill remaining width minus sidebar */
}

.mobile-header {
  display: none;
  padding: 10px 20px;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  align-items: center;
  gap: 15px;
  width: 100%;
}
.dark .mobile-header {
  border-bottom: 1px solid rgba(255,255,255,0.05);
}
.mobile-title {
  font-weight: bold;
  font-size: 16px;
}

/* Chat Container */
.chat-container {
  flex: 1;
  width: 100%;
  max-width: 800px; /* Constrain the width like ChatGPT */
  overflow-y: auto;
  padding: 20px;
  padding-bottom: 120px; /* Space for input */
  
  /* Hide scrollbar for cleaner look */
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.chat-container::-webkit-scrollbar {
  display: none;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--el-text-color-secondary);
  text-align: center;
}
.ai-icon {
  margin-bottom: 20px;
  color: var(--el-color-primary);
  background: rgba(var(--el-color-primary-rgb), 0.1);
  padding: 20px;
  border-radius: 50%;
  box-shadow: 0 0 20px rgba(var(--el-color-primary-rgb), 0.2);
}
.suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  margin-top: 30px;
  max-width: 600px;
}
.suggestion-tag {
  cursor: pointer;
  transition: all 0.2s;
  padding: 8px 16px;
  height: auto;
}
.suggestion-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  background-color: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary-light-5);
}

/* Messages */
.message-wrapper {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  animation: fadeIn 0.3s ease;
}
.message-wrapper.is-user {
  flex-direction: row-reverse;
}

.avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
}
.ai-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}
.is-user .message-content {
  align-items: flex-end;
}

.bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 15px;
  line-height: 1.6;
  position: relative;
  word-wrap: break-word;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.ai-bubble {
  background: var(--el-bg-color); /* More solid background like ChatGPT */
  border-top-left-radius: 2px;
  color: var(--el-text-color-primary);
  border: 1px solid var(--el-border-color-lighter);
}
.dark .ai-bubble {
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-darker);
}

.user-bubble {
  background: var(--el-color-primary);
  color: white;
  border-top-right-radius: 2px;
}
.error-bubble {
  background: var(--el-color-danger-light-9);
  color: var(--el-color-danger);
  border: 1px solid var(--el-color-danger-light-5);
}

.timestamp {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  margin-top: 4px;
  padding: 0 4px;
}

/* Markdown Styles */
:deep(.markdown-body) {
  font-size: 15px;
}
:deep(.markdown-body p) {
  margin-bottom: 10px;
}
:deep(.markdown-body p:last-child) {
  margin-bottom: 0;
}
:deep(.markdown-body ul), :deep(.markdown-body ol) {
  padding-left: 20px;
  margin-bottom: 10px;
}
:deep(.markdown-body code) {
  background: rgba(0,0,0,0.1);
  padding: 2px 4px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 0.9em;
}
.dark :deep(.markdown-body code) {
  background: rgba(255,255,255,0.1);
}

/* Input Area */
.input-area {
  position: fixed;
  bottom: 0;
  left: 260px; /* Offset by sidebar width */
  width: calc(100vw - 260px);
  max-width: none;
  padding: 0 0 20px;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  /* Gradient background mask to hide scrolling content */
  background: linear-gradient(to bottom, transparent, var(--el-bg-color) 40%);
  padding-top: 40px; /* Space for gradient fade */
}
.dark .input-area {
  background: linear-gradient(to bottom, transparent, var(--el-bg-color-overlay) 40%);
}

.input-wrapper {
  position: relative;
  width: 100%;
  max-width: 800px;
  background: rgba(255, 255, 255, 0.65); /* Restored glassmorphism */
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: flex-end;
  padding: 8px;
  transition: all 0.3s;
}
.dark .input-wrapper {
  background: rgba(40, 40, 40, 0.65);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.input-wrapper:focus-within {
  box-shadow: 0 12px 40px rgba(var(--el-color-primary-rgb), 0.15);
  border-color: var(--el-color-primary-light-5);
}

:deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: transparent !important;
  padding: 0 10px !important;
}
:deep(.el-textarea__inner) {
  box-shadow: none !important;
  background: transparent !important;
  border: none !important;
  padding: 8px 0 !important;
  max-height: 150px;
  color: var(--el-text-color-primary);
}

.send-btn {
  margin-left: 8px;
  flex-shrink: 0;
  margin-bottom: 4px; /* Align with bottom */
}

.disclaimer {
  text-align: center;
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  margin-top: 8px;
}

/* Typing Indicator */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 16px 20px;
}
.typing-indicator span {
  width: 6px;
  height: 6px;
  background: currentColor;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
  opacity: 0.6;
}
.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@media (max-width: 768px) {
  .ai-view {
    border-radius: 0;
  }
  .chat-sidebar {
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    transform: translateX(-100%);
    box-shadow: 2px 0 10px rgba(0,0,0,0.1);
    background: rgba(255, 255, 255, 0.9); /* More opaque for mobile */
    z-index: 100;
  }
  .dark .chat-sidebar {
    background: rgba(20, 20, 20, 0.95);
  }
  
  .chat-sidebar.sidebar-open {
    transform: translateX(0);
  }
  
  .chat-main {
    width: 100%; /* Full width on mobile */
    padding-top: 0;
  }
  
  .input-area {
    left: 0;
    width: 100%;
    padding-bottom: 80px; /* Space for MobileNavBar */
  }
  
  .input-wrapper {
    max-width: 90%;
    border-radius: 20px;
  }
  
  .mobile-close-btn {
    display: block;
  }
  .mobile-header {
    display: flex;
    padding-top: 60px; /* Header height space */
    margin-top: 10px;
  }
  .session-item .delete-btn {
    opacity: 1; /* Always show on mobile */
  }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
