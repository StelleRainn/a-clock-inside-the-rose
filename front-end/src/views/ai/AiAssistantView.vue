<template>
  <div class="ai-view">
    <!-- Sidebar for Chat History -->
    <div class="chat-sidebar" :class="{ 'sidebar-open': isSidebarOpen }">
      <div class="sidebar-header">
        <el-button type="primary" class="new-chat-btn" @click="handleNewChat">
          <el-icon><Plus /></el-icon> New Chat
        </el-button>
        <el-button class="mobile-close-btn" circle @click="isSidebarOpen = false">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      
      <div class="sessions-list" v-loading="isSessionsLoading">
        <div v-if="sessions.length === 0 && !isSessionsLoading" class="no-sessions">
          No previous chats
        </div>
        <div 
          v-for="session in sessions" 
          :key="session.id"
          class="session-item"
          :class="{ 'active': currentSessionId === session.id }"
          @click="selectSession(session.id)"
        >
          <el-icon><ChatDotSquare /></el-icon>
          <span class="session-title">{{ session.title }}</span>
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

    <!-- Main Chat Area -->
    <div class="chat-main">
      <div class="mobile-header">
        <el-button circle @click="isSidebarOpen = true">
          <el-icon><Menu /></el-icon>
        </el-button>
        <span class="mobile-title">ACIR Intelligent</span>
      </div>

      <!-- Chat Container -->
      <div class="chat-container" ref="chatContainerRef">
      <div v-if="messages.length === 0" class="empty-state">
        <el-icon :size="64" class="ai-icon"><Cpu /></el-icon>
        <h2>ACIR Intelligent</h2>
        <p>I am your personal productivity assistant. How can I help you today?</p>
        <div class="suggestions">
          <el-tag 
            v-for="s in suggestions" 
            :key="s" 
            effect="plain" 
            class="suggestion-tag"
            @click="sendMessage(s)"
          >
            {{ s }}
          </el-tag>
        </div>
      </div>

      <div v-else class="messages-list">
        <div 
          v-for="(msg, index) in messages" 
          :key="index" 
          class="message-wrapper"
          :class="{ 'is-user': msg.role === 'user' }"
        >
          <div class="avatar">
            <el-avatar v-if="msg.role === 'user'" :size="36" :src="userAvatar" icon="UserFilled" />
            <div v-else class="ai-avatar">
              <el-icon><Cpu /></el-icon>
            </div>
          </div>
          
          <div class="message-content">
            <div class="bubble" :class="{ 'user-bubble': msg.role === 'user', 'ai-bubble': msg.role === 'model', 'error-bubble': msg.isError }">
              <!-- Render Markdown for AI, Plain text for User -->
              <div v-if="msg.role === 'model'" v-html="renderMarkdown(msg.content)" class="markdown-body"></div>
              <div v-else>{{ msg.content }}</div>
            </div>
            <div class="timestamp">
              {{ formatTime(msg.timestamp) }}
            </div>
          </div>
        </div>
        
        <!-- Loading Indicator -->
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

    <!-- Input Area -->
    <div class="input-area">
      <div class="input-wrapper">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :autosize="{ minRows: 1, maxRows: 4 }"
          placeholder="Message ACIR..."
          resize="none"
          @keydown.enter.prevent="handleEnter"
          :disabled="isLoading"
        >
          <template #prefix>
            <el-icon class="input-icon"><ChatDotRound /></el-icon>
          </template>
        </el-input>
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
        AI can make mistakes. Please verify important information.
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
import MarkdownIt from 'markdown-it'
import { ElMessageBox, ElMessage } from 'element-plus'

// Initialize
const aiStore = useAiStore()
const userStore = useUserStore()
const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true
})

const chatContainerRef = ref(null)
const inputMessage = ref('')
const isSidebarOpen = ref(false)

// Computed
const messages = computed(() => aiStore.messages)
const sessions = computed(() => aiStore.sessions)
const currentSessionId = computed(() => aiStore.currentSessionId)
const isSessionsLoading = computed(() => aiStore.isSessionsLoading)
const isLoading = computed(() => aiStore.isLoading)
const userAvatar = computed(() => userStore.user?.avatarUrl || '')

const suggestions = [
  "Analyze my focus efficiency today",
  "Help me prioritize my tasks",
  "I'm feeling tired, what should I do?",
  "Summarize my recent achievements"
]

// Methods
const renderMarkdown = (text) => {
  return md.render(text || '')
}

const formatTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const handleNewChat = () => {
  aiStore.startNewSession()
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
  ElMessageBox.confirm('Are you sure you want to delete this chat?', 'Warning', {
    confirmButtonText: 'OK',
    cancelButtonText: 'Cancel',
    type: 'warning'
  }).then(() => {
    aiStore.deleteSession(id)
    ElMessage.success('Chat deleted')
  }).catch(() => {})
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  })
}

const handleSend = async () => {
  const text = inputMessage.value
  if (!text.trim()) return
  
  inputMessage.value = ''
  await aiStore.sendMessage(text)
}

const sendMessage = (text) => {
  inputMessage.value = text
  handleSend()
}

const handleEnter = (e) => {
  if (e.shiftKey) return // Allow new line
  handleSend()
}

// Watchers
watch(() => aiStore.messages.length, scrollToBottom)

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
  }
  .chat-sidebar.sidebar-open {
    transform: translateX(0);
  }
  .mobile-close-btn {
    display: block;
  }
  .mobile-header {
    display: flex;
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
