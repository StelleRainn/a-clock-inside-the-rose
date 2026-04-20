<template>
  <div class="pomodoro-container" :class="{ 'immersive-mode': isImmersive }">
    <!-- 
    PomodoroTimer: 番茄钟的核心视图组件。
    负责渲染计时器进度条、任务关联选择器，并支持“沉浸模式”(Immersive Mode)。
  -->
    <el-card class="timer-card" :class="{ 'immersive-card': isImmersive }">
      <!-- 非沉浸模式下显示的常规卡片头部：包含 专注/休息 切换以及全屏按钮 -->
      <template #header v-if="!isImmersive">
        <div class="card-header">
          <el-radio-group v-model="sessionMode" @change="handleModeChange">
            <el-radio-button label="work" value="work">Focus</el-radio-button>
            <el-radio-button label="break" value="break">Break</el-radio-button>
          </el-radio-group>
          <el-button link @click="toggleImmersive">
            <el-icon><FullScreen /></el-icon>
          </el-button>
        </div>
      </template>

      <!-- 沉浸模式下的头部：只保留退出按钮 -->
      <div v-if="isImmersive" class="immersive-header">
        <el-button link class="exit-btn" @click="toggleImmersive">
          <el-icon><Close /></el-icon> Exit Focus Mode
        </el-button>
      </div>

      <!-- 核心倒计时展示区：使用 el-progress 生成环形进度条 -->
      <div class="timer-display">
        <el-progress 
          type="dashboard" 
          :percentage="pomodoroStore.progressPercentage" 
          :width="isImmersive ? 300 : 200"
          :stroke-width="isImmersive ? 15 : 6"
          :color="progressColor"
        >
          <template #default>
            <h1 class="timer-text" :class="{ 'immersive-text': isImmersive }">{{ pomodoroStore.formattedTime }}</h1>
          </template>
        </el-progress>
      </div>

      <!-- 动态时长滑块：仅在非沉浸模式且未运行状态下允许用户快捷调整时长 -->
      <div class="slider-container" v-if="!isImmersive">
        <span class="duration-label">Duration (min)</span>
        <el-slider 
          v-model="sliderValue" 
          :min="1" 
          :max="60" 
          :disabled="pomodoroStore.isRunning"
          @input="handleSliderChange"
        />
      </div>

      <!-- 任务选择下拉框：仅在“专注模式”下显示，用于将本次专注时间关联到特定任务 -->
      <div class="task-selector" v-if="!isImmersive && isWorkSession">
        <el-select 
          v-model="selectedTask" 
          placeholder="Select a task to focus on" 
          clearable 
          filterable
          :disabled="pomodoroStore.isRunning"
          @change="handleTaskSelect"
        >
          <el-option
            v-for="task in tasks"
            :key="task.id"
            :label="task.title"
            :value="String(task.id)"
          >
            <span style="float: left">{{ task.title }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">
              <el-tag size="small" :type="getPriorityType(task.priority)">{{ task.priority }}</el-tag>
            </span>
          </el-option>
        </el-select>
      </div>
      
      <!-- 沉浸模式下的当前任务展示：不再允许切换，只做文本提示 -->
      <div v-if="isImmersive && currentTaskTitle" class="immersive-task">
        <p>Focusing on: <strong>{{ currentTaskTitle }}</strong></p>
      </div>

      <!-- 运行状态文字提示 -->
      <div class="status-text">
        <span v-if="pomodoroStore.isRunning">Running...</span>
        <span v-else>Paused</span>
      </div>

      <!-- 播放控制区：根据当前状态显示开始/暂停/重置按钮 -->
      <div class="timer-controls">
        <el-button 
          v-if="!pomodoroStore.isRunning" 
          type="primary" 
          :size="isImmersive ? 'large' : 'large'" 
          :style="isImmersive ? 'transform: scale(1.5)' : ''"
          circle 
          @click="pomodoroStore.startTimer()"
        >
          <el-icon><VideoPlay /></el-icon>
        </el-button>
        <el-button 
          v-else 
          type="warning" 
          :size="isImmersive ? 'large' : 'large'"
          :style="isImmersive ? 'transform: scale(1.5)' : ''" 
          circle 
          @click="pomodoroStore.pauseTimer"
        >
          <el-icon><VideoPause /></el-icon>
        </el-button>
        <el-button 
          size="large" 
          circle 
          @click="pomodoroStore.resetTimer"
          v-if="!isImmersive || !pomodoroStore.isRunning"
        >
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
      
      <!-- 白噪音控制面板：仅在沉浸模式下提供，增强环境音体验 -->
      <div class="noise-controls" v-if="isImmersive">
        <p>White Noise</p>
        <el-radio-group v-model="currentNoise" size="small" @change="playNoise">
          <el-radio-button label="none" value="none">Off</el-radio-button>
          <el-radio-button label="rain" value="rain">Rain</el-radio-button>
          <el-radio-button label="cafe" value="cafe">Cafe</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 禅意笔记 (Zen Note)：允许在专注时快速记录闪念并作为 LOW 优先级任务存入收件箱 -->
      <div v-if="isImmersive" class="zen-note-container">
        <el-input 
          v-model="zenNote" 
          placeholder="Capture a thought (Zen Note)..." 
          class="zen-input"
          @keyup.enter="saveZenNote"
        >
          <template #append>
            <el-button @click="saveZenNote" :icon="Check" />
          </template>
        </el-input>
      </div>

      <!-- 底部今日统计数据概览 -->
      <div class="stats-footer" v-if="!isImmersive">
        <p>Completed Pomodoros Today: {{ pomodoroStore.completedPomodoros }}</p>
      </div>
    </el-card>
    
    <!-- 隐藏的音频元素：承载白噪音资源，通过 ref 操控播放与暂停 -->
    <audio ref="rainAudio" loop src="https://assets.mixkit.co/active_storage/sfx/2496/2496-preview.mp3"></audio>
    <audio ref="cafeAudio" loop src="https://assets.mixkit.co/active_storage/sfx/248/248-preview.mp3"></audio>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted } from 'vue'
import { usePomodoroStore } from '@/stores/pomodoro'
import { useUserStore } from '@/stores/user'
import { getTasks, createTask } from '@/api/task'
import { Check, FullScreen, Close, VideoPlay, VideoPause, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const pomodoroStore = usePomodoroStore()
const userStore = useUserStore()
const sessionMode = ref('work')
const sliderValue = ref(25)
const isImmersive = ref(false)
const currentNoise = ref('none')
const rainAudio = ref(null)
const cafeAudio = ref(null)
const tasks = ref([])
const zenNote = ref('')

// 拦截双向绑定，使选中的任务 ID 始终与 pomodoroStore 保持同步
const selectedTask = computed({
  get: () => pomodoroStore.selectedTaskId,
  set: (val) => pomodoroStore.selectTask(val)
})

// 判断当前是否处于“专注”阶段 (非休息阶段)
const isWorkSession = computed(() => pomodoroStore.currentMode === 'pomodoro')

// 监听工作模式的变化，并同步更新视图中的 Slider 和 Radio 按钮
watch(() => isWorkSession.value, (isWork) => {
  sessionMode.value = isWork ? 'work' : 'break'
  // 根据当前模式从 Store 读取对应的默认时长并除以 60 转为分钟显示
  const duration = isWork ? pomodoroStore.pomodoroDuration : (pomodoroStore.currentMode === 'short-break' ? pomodoroStore.shortBreakDuration : pomodoroStore.longBreakDuration)
  sliderValue.value = Math.floor(duration / 60)
}, { immediate: true })

const handleModeChange = (val) => {
  pomodoroStore.setMode(val)
}

const handleSliderChange = (val) => {
  pomodoroStore.updateDuration(val * 60)
}

const handleTaskSelect = (val) => {
  pomodoroStore.selectTask(val)
}

// 切换沉浸模式并调用浏览器原生 API 实现真正的全屏
const toggleImmersive = () => {
  isImmersive.value = !isImmersive.value
  if (isImmersive.value) {
    document.documentElement.requestFullscreen().catch(() => {})
  } else {
    if (document.fullscreenElement) {
      document.exitFullscreen().catch(() => {})
    }
  }
}

// 白噪音播放控制逻辑：切换时先停止所有音频，再播放目标音频
const playNoise = (val) => {
  rainAudio.value?.pause()
  cafeAudio.value?.pause()
  
  if (val === 'rain') {
    rainAudio.value?.play()
  } else if (val === 'cafe') {
    cafeAudio.value?.play()
  }
}

// 保存闪念笔记：作为一条低优先级 TODO 任务发送给后端
const saveZenNote = async () => {
  if (!zenNote.value.trim()) return
  if (!userStore.user || !userStore.user.id) {
    ElMessage.warning('Please login to save notes')
    return
  }

  try {
    await createTask({
      userId: userStore.user.id,
      title: '[Zen Note] ' + zenNote.value,
      status: 'TODO',
      priority: 'LOW',
      description: 'Captured during focus session'
    })
    ElMessage.success('Note saved to Inbox')
    zenNote.value = ''
    fetchUserTasks() // Refresh task list
  } catch {
    ElMessage.error('Failed to save note')
  }
}

// 获取当前用户的任务列表以供下拉框选择
const fetchUserTasks = async () => {
  if (userStore.user && userStore.user.id) {
    try {
      const data = await getTasks(userStore.user.id)
      tasks.value = data.filter(t => t.status !== 'DONE') || []
    } catch {
      // ignore
    }
  }
}

// 根据选中的 Task ID 查找任务名称，用于沉浸模式下的纯文本展示
const currentTaskTitle = computed(() => {
  if (!pomodoroStore.selectedTaskId) return null
  const task = tasks.value.find(t => String(t.id) === pomodoroStore.selectedTaskId)
  return task ? task.title : null
})

// 为任务优先级提供颜色主题映射
const getPriorityType = (priority) => {
  const map = {
    'HIGH': 'danger',
    'MEDIUM': 'warning',
    'LOW': 'info'
  }
  return map[priority] || ''
}

// 专注模式时进度条为蓝色，休息模式时为绿色
const progressColor = computed(() => {
  return isWorkSession.value ? '#409eff' : '#67c23a'
})

onMounted(() => {
  pomodoroStore.fetchTodayCount()
  fetchUserTasks()
  // 组件挂载时初始化一次进度条滑块的时长
  const duration = isWorkSession.value ? pomodoroStore.pomodoroDuration : (pomodoroStore.currentMode === 'short-break' ? pomodoroStore.shortBreakDuration : pomodoroStore.longBreakDuration)
  sliderValue.value = Math.floor(duration / 60)
})
</script>

<style scoped>
.pomodoro-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  transition: all 0.3s ease;
}

.pomodoro-container.immersive-mode {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: #1a1a1a;
  z-index: 9999;
}

.timer-card {
  width: 400px;
  text-align: center;
  padding: 20px;
  transition: all 0.3s ease;
}

.timer-card.immersive-card {
  width: 100%;
  height: 100%;
  background: transparent;
  border: none;
  box-shadow: none;
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.timer-display {
  margin: 20px 0;
  display: flex;
  justify-content: center;
}

.timer-text {
  font-size: 2.5rem;
  margin: 0;
  color: var(--el-text-color-primary);
}

.immersive-text {
  font-size: 4rem;
  color: white;
}

.slider-container {
  padding: 0 20px;
  margin-bottom: 20px;
}

.duration-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  display: block;
  margin-bottom: 5px;
  text-align: left;
}

.task-selector {
  margin-bottom: 20px;
  padding: 0 20px;
}

.timer-controls {
  margin-top: 20px;
  margin-bottom: 20px;
}

.status-text {
  color: var(--el-text-color-secondary);
  font-size: 14px;
  margin-bottom: 10px;
}

.stats-footer {
  margin-top: 20px;
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 10px;
  color: var(--el-text-color-regular);
}

.immersive-header {
  position: absolute;
  top: 20px;
  right: 20px;
}

.immersive-task {
  margin-bottom: 20px;
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.9);
}

.exit-btn {
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
}

.exit-btn:hover {
  color: white;
}

.noise-controls {
  margin-top: 40px;
  color: rgba(255, 255, 255, 0.8);
}

.noise-controls p {
  margin-bottom: 10px;
  font-size: 14px;
}

.zen-note-container {
  margin-top: 40px;
  width: 300px;
}

.zen-input :deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.1);
  box-shadow: none;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.zen-input :deep(.el-input__inner) {
  color: white;
}

.zen-input :deep(.el-input-group__append) {
  background-color: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-left: none;
  color: white;
}
</style>
