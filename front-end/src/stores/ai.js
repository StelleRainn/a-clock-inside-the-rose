import { defineStore } from 'pinia'
import { ref } from 'vue'
import { GoogleGenAI } from "@google/genai"
import { useUserStore } from './user'
import { usePomodoroStore } from './pomodoro'
import { getTasks } from '@/api/task'
import { getDailyFocusStats } from '@/api/stats'
import { getUserStats } from '@/api/gamification'
import { getChatSessions, createChatSession, getChatMessages, saveChatMessage, deleteChatSession } from '@/api/chat'

// ==========================================
// aiStore: 负责管理与 Google Gemini AI 的对话状态与上下文聚合
// ==========================================
export const useAiStore = defineStore('ai', () => {
  const userStore = useUserStore()
  const pomodoroStore = usePomodoroStore()
  
  // 聊天会话列表与当前活跃会话
  const sessions = ref([])
  const currentSessionId = ref(null)
  
  // 当前会话的消息记录
  const messages = ref([])
  
  // UI 骨架屏 / Loading 状态
  const isLoading = ref(false)
  const isSessionsLoading = ref(false)
  
  // 指定默认使用的 Gemini 模型版本
  const currentModel = ref('gemini-3-flash-preview') 

  // Load all sessions for current user
  // 加载当前用户的所有历史聊天会话
  const loadSessions = async () => {
    if (!userStore.user?.id) return
    isSessionsLoading.value = true
    try {
      const data = await getChatSessions(userStore.user.id)
      sessions.value = data || []
    } catch (e) {
      console.error('Failed to load chat sessions', e)
    } finally {
      isSessionsLoading.value = false
    }
  }

  // Load a specific session's messages
  // 切换并加载指定会话的历史消息记录
  const selectSession = async (sessionId) => {
    currentSessionId.value = sessionId
    messages.value = []
    isLoading.value = true
    try {
      const data = await getChatMessages(sessionId)
      messages.value = (data || []).map(msg => ({
        id: msg.id,
        role: msg.role,
        content: msg.content,
        timestamp: new Date(msg.createdAt)
      }))
    } catch (e) {
      console.error('Failed to load messages', e)
    } finally {
      isLoading.value = false
    }
  }

  // Start a new chat session
  // 初始化一个全新的、空白的聊天会话
  const startNewSession = () => {
    currentSessionId.value = null
    messages.value = []
  }

  // 删除指定的聊天会话，如果删除的是当前会话，则自动重置为空白会话
  const deleteSession = async (id) => {
    try {
      await deleteChatSession(id)
      sessions.value = sessions.value.filter(s => s.id !== id)
      if (currentSessionId.value === id) {
        startNewSession()
      }
    } catch (e) {
      console.error('Failed to delete session', e)
    }
  }

  const clearMessages = () => {
    messages.value = []
  }

  // ==========================================
  // [核心] 生成系统级上下文 (System Prompt Context)
  // 这是本应用 AI 助手拥有“记忆”和“洞察力”的灵魂。它在后台偷偷聚合了用户的各种数据，喂给大模型。
  // ==========================================
  const generateSystemContext = async () => {
    const userName = userStore.user?.username || 'User'
    const localTime = new Date().toLocaleString()
    
    // Ensure we fetch the latest pomodoro count before generating context
    // 确保在生成上下文前，拉取到用户今日最新的专注数据
    await pomodoroStore.fetchTodayCount()
    
    const focusTime = Math.round(pomodoroStore.todayFocusSeconds / 60) || 0
    const currentMode = pomodoroStore.currentMode || 'Focus'
    
    let taskContext = "No specific tasks found."
    let gameContext = ""
    let trendContext = ""
    
    try {
      if (userStore.user?.id) {
        // Parallel requests for enriched context
        // 并行请求：获取所有任务、每日统计数据、游戏化(连续打卡)数据，极大减少等待时间
        const [tasks, focusStats, userStats] = await Promise.all([
          getTasks(userStore.user.id),
          getDailyFocusStats(userStore.user.id),
          getUserStats(userStore.user.id)
        ])
        
        // 1. Task Panorama (任务全景分析)
        if (tasks && tasks.length > 0) {
          const notDone = tasks.filter(t => t.status !== 'DONE')
          
          // Urgent Tasks: Nearest due date first
          // 紧急任务：挑选有截止日期的，并按时间最近排序，最多喂给 AI 5个
          const urgentTasks = notDone
            .filter(t => t.dueDate)
            .sort((a, b) => new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime())
            .slice(0, 5)
            
          const urgentIds = new Set(urgentTasks.map(t => t.id))
          
          // High Priority Tasks: Not in urgent, sorted by priority
          // 高优任务：排除已在“紧急任务”中的，纯按优先级(HIGH/MEDIUM/LOW)排序，最多喂给 AI 3个
          const pMap = { 'HIGH': 3, 'MEDIUM': 2, 'LOW': 1 }
          const highPriTasks = notDone
            .filter(t => !urgentIds.has(t.id))
            .sort((a, b) => (pMap[b.priority] || 0) - (pMap[a.priority] || 0))
            .slice(0, 3)

          // 刚完成的任务：按更新时间倒序，最多喂 3 个，让 AI 知道用户最近在干嘛，方便夸奖
          const doneTasks = tasks
            .filter(t => t.status === 'DONE')
            .sort((a, b) => new Date(b.updatedAt || 0).getTime() - new Date(a.updatedAt || 0).getTime())
            .slice(0, 3)
          
          const formatTask = (t) => `- [${t.priority}] ${t.title} ${t.dueDate ? `(Due: ${new Date(t.dueDate).toLocaleString()})` : ''} - ${t.status}`
          
          // 拼接最终的任务维度 Prompt
          taskContext = `
[Urgent Tasks (Upcoming Deadlines)]
${urgentTasks.length > 0 ? urgentTasks.map(formatTask).join('\n') : 'None'}

[High Priority Tasks]
${highPriTasks.length > 0 ? highPriTasks.map(formatTask).join('\n') : 'None'}

[Recently Completed]
${doneTasks.length > 0 ? doneTasks.map(t => `- ${t.title}`).join('\n') : 'None'}
          `
        }

        // 2. Gamification & Streak (游戏化与连续打卡上下文)
        if (userStats) {
          gameContext = `- Streak: ${userStats.streakDays || 0} days 🔥 (Level ${userStats.level || 1})`
        }

        // 3. Trends (Yesterday's focus) (昨日数据对比上下文)
        if (focusStats && focusStats.length > 0) {
          const yesterday = new Date()
          yesterday.setDate(yesterday.getDate() - 1)
          const yesterdayStr = yesterday.toISOString().split('T')[0]
          
          const yesterdayStat = focusStats.find(s => s.date === yesterdayStr)
          const yesterdayMins = yesterdayStat ? Math.round(yesterdayStat.totalSeconds / 60) : 0
          trendContext = `- Yesterday Focus: ${yesterdayMins} mins`
        }
      }
    } catch (e) {
      console.error("Failed to fetch enriched context", e)
    }

    // 最终组装给 AI 的 System Instruction
    return `
System: You are ACIR, an intelligent productivity assistant.
User: ${userName}
Current Local Time: ${localTime}

[Productivity Status]
- Today Focus: ${focusTime} mins
${trendContext}
${gameContext}
- Current Mode: ${currentMode}

${taskContext}

Guidelines:
1. Act as a strict but empathetic productivity coach.
2. If the user asks about deadlines or times, you MUST use the 'Current Local Time' provided above to calculate the exact remaining time.
3. Praise the user if they maintain a good streak or focus time.
4. If focus time > 120 mins, strictly advise taking a break.
5. Keep answers concise, use Markdown.
    `
  }

  const sendMessage = async (userText) => {
    if (!userText.trim()) return

    // 1. Ensure we have a session (如果没有会话，则自动创建)
    if (!currentSessionId.value) {
      if (!userStore.user?.id) {
        messages.value.push({ role: 'model', content: 'Please login first.', isError: true, timestamp: new Date() })
        return
      }
      try {
        // Generate a short title from the first message
        // 截取用户第一句话的前20个字作为聊天记录的标题
        const title = userText.length > 20 ? userText.substring(0, 20) + '...' : userText
        const newSession = await createChatSession({ userId: userStore.user.id, title })
        currentSessionId.value = newSession.id
        sessions.value.unshift(newSession) // Add to top of list
      } catch (e) {
        console.error('Failed to create session', e)
        return
      }
    }

    // 2. Save user message to backend
    try {
      await saveChatMessage(currentSessionId.value, { role: 'user', content: userText })
    } catch (e) {
      console.error('Failed to save user message', e)
    }

    // 3. Add User Message to State
    messages.value.push({
      role: 'user',
      content: userText,
      timestamp: new Date()
    })

    console.log('User Store Data:', userStore.user) // Debug
    const apiKey = userStore.user?.geminiApiKey || userStore.user?.gemini_api_key || import.meta.env.VITE_GEMINI_API_KEY
    if (!apiKey) {
      messages.value.push({
        role: 'model',
        content: "Please configure your Gemini API Key in **Profile Settings** first.",
        isError: true,
        timestamp: new Date()
      })
      return
    }

    const ai = new GoogleGenAI({ apiKey })

    isLoading.value = true

    try {
      // 2. Prepare Context
      const context = await generateSystemContext()
      
      // 3. Construct Prompt with History
      // We will send a structured prompt to the model
      // Note: For best results with chat, we should use the chat API if available, 
      // but 'contents' can be a list of messages.
      // The new SDK documentation suggests 'contents' can be a string or array.
      // Let's try to send the conversation history as a text block for now to ensure compatibility.
      
      let fullPrompt = `${context}\n\n--- Conversation History ---\n`
      
      messages.value.forEach(msg => {
        const role = msg.role === 'user' ? 'User' : 'ACIR'
        fullPrompt += `${role}: ${msg.content}\n`
      })
      
      // The last message is already in messages.value, so it's included above.
      // We need to prompt the model to answer.
      fullPrompt += `ACIR: `

      // 4. Call API
      const response = await ai.models.generateContent({
        model: currentModel.value,
        contents: fullPrompt,
        config: {
            temperature: 0.7
        }
      })
      
      // In @google/genai SDK, response.text is a property, not a function
      const responseText = response.text || "No response text"

      // Save AI message to backend
      try {
        await saveChatMessage(currentSessionId.value, { role: 'model', content: responseText })
      } catch (e) {
        console.error('Failed to save AI message', e)
      }

      // 5. Add AI Response to State
      messages.value.push({
        role: 'model',
        content: responseText,
        timestamp: new Date()
      })

    } catch (error) {
      console.error('AI Request Failed:', error)
      messages.value.push({
        role: 'model',
        content: "Sorry, I encountered an error. Please check your connection or API key.",
        isError: true,
        timestamp: new Date()
      })
    } finally {
      isLoading.value = false
    }
  }

  return {
    sessions,
    currentSessionId,
    messages,
    isLoading,
    isSessionsLoading,
    currentModel,
    loadSessions,
    selectSession,
    startNewSession,
    deleteSession,
    sendMessage,
    clearMessages
  }
})
