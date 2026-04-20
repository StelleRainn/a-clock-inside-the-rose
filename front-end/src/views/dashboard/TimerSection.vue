<template>
  <section 
    class="timer-section" 
    :class="[
      { 'ui-hidden': isUIHidden },
      `hero-theme-${currentHeroTheme}`
    ]"
    :style="heroThemeStyles"
  >
    <!-- 
    TimerSection: 番茄钟的核心展示区，也就是 Dashboard 顶部的“沉浸式大屏”。
    动态类名控制了 UI 的隐藏（沉浸模式）以及文字颜色的深浅（响应背景图）。
  -->
    <!-- Hero Background (全屏背景层) -->
    <div class="hero-bg" :style="bgStyle">
      <!-- 遮罩层，透明度由设置中的 bgOverlayOpacity 控制，用于保证文字对比度 -->
      <div 
        class="bg-overlay"
        :style="{ backgroundColor: `rgba(0,0,0,${pomodoroStore.bgOverlayOpacity})` }"
      ></div>
    </div>

    <div class="timer-content">
      
      <!-- Mode Toggles (专注 / 短休 / 长休 切换按钮) -->
      <!-- 注意：在倒计时运行期间，这些按钮被禁用 (:disabled="pomodoroStore.isRunning") -->
      <div class="mode-toggles">
        <button 
          class="mode-btn" 
          :class="{ active: pomodoroStore.currentMode === 'pomodoro' }"
          :disabled="pomodoroStore.isRunning"
          @click="pomodoroStore.setMode('pomodoro')"
        >
          {{ $t('timer.pomodoro') }}
        </button>
        <button 
          class="mode-btn" 
          :class="{ active: pomodoroStore.currentMode === 'short-break' }"
          :disabled="pomodoroStore.isRunning"
          @click="pomodoroStore.setMode('short-break')"
        >
          {{ $t('timer.shortBreak') }}
        </button>
        <button 
          class="mode-btn" 
          :class="{ active: pomodoroStore.currentMode === 'long-break' }"
          :disabled="pomodoroStore.isRunning"
          @click="pomodoroStore.setMode('long-break')"
        >
          {{ $t('timer.longBreak') }}
        </button>
      </div>

      <!-- Main Timer (核心倒计时数字显示) -->
      <div class="timer-display">
        <h1 class="timer-digits">{{ pomodoroStore.formattedTime }}</h1>
      </div>

      <!-- Current Task (当前关联的任务下拉框) -->
      <!-- 将番茄钟时间计入特定任务的数据流转枢纽 -->
      <div class="current-task">
        <el-dropdown 
          trigger="click" 
          :disabled="pomodoroStore.isRunning"
          @command="handleTaskSelect"
          popper-class="glass-dropdown"
        >
          <div class="task-chip" :class="{ disabled: pomodoroStore.isRunning }">
            <span class="task-icon">⚡</span>
            <span class="task-text">{{ currentTaskTitle || $t('timer.selectTask') }}</span>
            <el-icon v-if="!pomodoroStore.isRunning" class="dropdown-icon"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <!-- 渲染从后端获取的待办任务列表 -->
              <el-dropdown-item 
                v-for="task in pendingTasks" 
                :key="task.id" 
                :command="task.id"
              >
                {{ task.title }}
              </el-dropdown-item>
              <!-- 提供清除选项，允许进行无关联的纯净专注 -->
              <el-dropdown-item divided command="clear">{{ $t('timer.clearSelection') }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <!-- Controls (控制按钮组：开始/暂停、重置、全屏、设置) -->
      <div class="timer-controls">
        <!-- 动态切换 Start / Pause，通过 toggleTimer 触发 -->
        <el-button 
          class="control-btn main-action"
          :class="{ active: pomodoroStore.isRunning }"
          @click="pomodoroStore.toggleTimer()"
        >
          {{ pomodoroStore.isRunning ? $t('timer.pause') : $t('timer.start') }}
        </el-button>
        
        <!-- 重置按钮，仅在非运行状态可用 -->
        <el-button 
          class="control-btn icon-only"
          circle
          @click="pomodoroStore.resetTimer()"
          :disabled="pomodoroStore.isRunning"
        >
          <el-icon :size="20"><Refresh /></el-icon>
        </el-button>

        <!-- 触发浏览器原生全屏 API，并在全屏时激活专注模式隐藏无用 UI -->
        <el-button 
          class="control-btn icon-only"
          circle
          @click="toggleFullscreen"
        >
          <el-icon :size="20"><FullScreen /></el-icon>
        </el-button>

        <el-button 
          class="control-btn icon-only"
          circle
          @click="showSettings = true"
        >
          <el-icon :size="20"><Setting /></el-icon>
        </el-button>
      </div>

      <!-- Zen Note (禅意笔记/灵感捕捉框) -->
      <!-- 允许用户在专注过程中不切出页面的情况下，快速记录闪念 -->
      <div class="zen-note" :class="{ 'is-focused': isZenNoteFocused }">
        <input 
          v-model="zenNote" 
          :placeholder="$t('timer.captureThought')" 
          @keyup.enter="saveZenNote"
          @focus="isZenNoteFocused = true"
          @blur="isZenNoteFocused = false"
          class="zen-input"
        />
      </div>

      <!-- Scroll Indicator (向下滚动的箭头提示，通过向上 emit 事件交由 DashboardView 处理滚动) -->
      <div v-if="!pomodoroStore.isImmersive" class="scroll-indicator" @click="$emit('scroll-down')">
        <el-icon><ArrowDown /></el-icon>
      </div>
    </div>

    <!-- Settings Dialog (通过 append-to-body 挂载到 body 下的全局设置弹窗) -->
    <el-dialog 
      v-model="showSettings" 
      :title="$t('settings.title')" 
      width="800px" 
      top="10vh"
      append-to-body
      class="settings-modal"
      :show-close="false"
    >
      <SettingsDialog @close="showSettings = false" />
    </el-dialog>
  </section>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { usePomodoroStore } from '@/stores/pomodoro'
import { useUserStore } from '@/stores/user'
import { createTask, getTasks } from '@/api/task'
import { Refresh, Setting, ArrowDown, FullScreen } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import SettingsDialog from '@/components/SettingsDialog.vue'
import { FastAverageColor } from 'fast-average-color'

const { t } = useI18n()
const pomodoroStore = usePomodoroStore()
const userStore = useUserStore()
// 初始化提取图片主色调的工具库
const fac = new FastAverageColor()

const isUIHidden = ref(false)
let hideTimer = null
const zenNote = ref('')
const isZenNoteFocused = ref(false)
const showSettings = ref(false)
const tasks = ref([])
const analyzedTheme = ref('light') // 'light' means light text (dark bg), 'dark' means dark text (light bg)

// ==========================================
// 背景图加载与动态样式计算
// ==========================================
const bgStyle = computed(() => {
  if (pomodoroStore.backgroundImage) {
    const url = pomodoroStore.backgroundImage === 'custom' 
      ? pomodoroStore.customBgUrl 
      : `/backgrounds/${pomodoroStore.backgroundImage}`
      
    return {
      backgroundImage: `url(${url})`,
      opacity: 1
    }
  }
  // 如果用户选择了 "Minimal Black"，则直接应用纯黑背景
  return { 
    backgroundColor: '#1a1a1a',
    opacity: 1 
  }
})

// 根据背景图计算应该使用深色文字还是浅色文字
const currentHeroTheme = computed(() => {
  if (pomodoroStore.backgroundImage) {
    if (pomodoroStore.heroTheme === 'auto') {
      return analyzedTheme.value
    }
    return pomodoroStore.heroTheme
  }
  // 纯黑背景必然配浅色文字
  return 'light'
})

// Provide CSS variables for hero section
const heroThemeStyles = computed(() => {
  const isLightText = currentHeroTheme.value === 'light'
  return {
    '--hero-text-color': isLightText ? '#ffffff' : '#303133',
    '--hero-text-shadow': isLightText ? '0 2px 10px rgba(0,0,0,0.3)' : 'none',
    '--hero-bg-blur': isLightText ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.05)',
    '--hero-border-color': isLightText ? 'rgba(255,255,255,0.2)' : 'rgba(0,0,0,0.1)',
    '--hero-control-bg': isLightText ? '#ffffff' : '#303133',
    '--hero-control-text': isLightText ? '#303133' : '#ffffff',
  }
})

// Analyze background image
const analyzeBackground = () => {
  if (!pomodoroStore.backgroundImage) {
    // Default to system theme behavior (no special hero class)
    // But here we need a concrete value. If no bg, we usually want system theme.
    // However, our template logic relies on heroThemeStyles.
    // Let's assume if no BG, we follow system.
    // But wait, if no BG, `bgStyle` opacity is 0, so we see main-layout background.
    // Main layout background changes with system theme.
    // So if no BG, we should probably set 'auto' to match system?
    // Actually, if no BG, we might want to just stick to default colors.
    // For now let's default to 'dark' text (light theme assumption) or just let CSS handle it?
    // The requirement is "If background image...".
    // If no background image, we can just not apply special styles?
    return
  }

  const img = new Image()
  if (pomodoroStore.backgroundImage === 'custom') {
    img.src = pomodoroStore.customBgUrl
  } else {
    img.src = `/backgrounds/${pomodoroStore.backgroundImage}`
  }
  img.crossOrigin = "Anonymous"
  img.onload = () => {
    const color = fac.getColor(img)
    // isDark -> background is dark -> we want light text
    analyzedTheme.value = color.isDark ? 'light' : 'dark'
  }
}

watch(() => pomodoroStore.backgroundImage, analyzeBackground, { immediate: true })

const currentTaskTitle = computed(() => {
  if (!pomodoroStore.selectedTaskId) return null
  const task = tasks.value.find(t => String(t.id) === pomodoroStore.selectedTaskId)
  return task ? task.title : null
})

const pendingTasks = computed(() => {
  return tasks.value.filter(t => t.status !== 'DONE')
})

const handleTaskSelect = (command) => {
  if (command === 'clear') {
    pomodoroStore.selectTask(null)
  } else {
    pomodoroStore.selectTask(command)
  }
}

const handleMouseMove = () => {
  isUIHidden.value = false
  clearTimeout(hideTimer)
  hideTimer = setTimeout(() => {
    if (pomodoroStore.isRunning) {
      isUIHidden.value = true
    }
  }, 3000)
}

const saveZenNote = async () => {
  if (!zenNote.value.trim()) return
  try {
    await createTask({
      userId: userStore.user.id,
      title: '[Zen Note] ' + zenNote.value,
      status: 'TODO',
      priority: 'LOW'
    })
    ElMessage.success(t('timer.noteCaptured'))
    zenNote.value = ''
  } catch {
    ElMessage.error(t('timer.noteFailed'))
  }
}

const fetchTasks = async () => {
  if (!userStore.user?.id) return
  const data = await getTasks(userStore.user.id)
  tasks.value = data || []
}

const toggleFullscreen = async () => {
  if (!document.fullscreenElement) {
    try {
      await document.documentElement.requestFullscreen()
      pomodoroStore.isImmersive = true
    } catch (e) {
      console.error(e)
    }
  } else {
    if (document.exitFullscreen) {
      await document.exitFullscreen()
      pomodoroStore.isImmersive = false
    }
  }
}

const handleFullscreenChange = () => {
  pomodoroStore.isImmersive = !!document.fullscreenElement
}

onMounted(() => {
  window.addEventListener('mousemove', handleMouseMove)
  window.addEventListener('fullscreenchange', handleFullscreenChange)
  fetchTasks()
})

onUnmounted(() => {
  window.removeEventListener('mousemove', handleMouseMove)
  window.removeEventListener('fullscreenchange', handleFullscreenChange)
  clearTimeout(hideTimer)
})
</script>

<style>
/* Global Glass Dropdown Style for el-dropdown appended to body */
.glass-dropdown {
  border-radius: 16px !important;
  background: rgba(255, 255, 255, 0.7) !important;
  backdrop-filter: blur(20px) !important;
  -webkit-backdrop-filter: blur(10px) !important;
  border: 1px solid var(--el-border-color-lighter) !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1) !important;
  padding: 8px 0 !important;
}

html.dark .glass-dropdown {
  background: rgba(30, 30, 30, 0.7) !important;
}

.glass-dropdown .el-dropdown-menu {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}

.glass-dropdown .el-dropdown-menu__item {
  padding: 10px 20px !important;
  font-size: 14px !important;
  transition: all 0.2s ease !important;
  color: var(--el-text-color-primary) !important;
  background: transparent !important;
}

.glass-dropdown .el-dropdown-menu__item:hover {
  background: rgba(0, 0, 0, 0.05) !important;
  color: var(--el-color-primary) !important;
}

html.dark .glass-dropdown .el-dropdown-menu__item:hover {
  background: rgba(255, 255, 255, 0.1) !important;
}

.glass-dropdown .el-dropdown-menu__item--divided {
  margin-top: 6px !important;
  border-top: 1px solid var(--el-border-color-lighter) !important;
}

.glass-dropdown .el-popper__arrow::before {
  background: rgba(255, 255, 255, 0.7) !important;
  border: 1px solid var(--el-border-color-lighter) !important;
  backdrop-filter: blur(20px) !important;
}

html.dark .glass-dropdown .el-popper__arrow::before {
  background: rgba(30, 30, 30, 0.7) !important;
}
</style>

<style scoped>
.timer-section {
  height: 100vh;
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
  transition: all 0.5s ease;
  overflow: hidden;
  padding-top: 60px;
  
  /* Default variables (will be overridden by inline styles) */
  --hero-text-color: var(--el-text-color-primary);
  --hero-text-shadow: none;
  --hero-bg-blur: rgba(255, 255, 255, 0.15);
  --hero-border-color: rgba(255, 255, 255, 0.1);
  --hero-control-bg: var(--el-bg-color);
  --hero-control-text: var(--el-text-color-primary);
}

/* Dynamic Background */
.hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  z-index: 0;
  transition: opacity 0.5s ease;
}

.bg-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  /* background-color set via inline style */
  transition: background-color 0.3s;
}

/* UI Hiding Logic */
.ui-hidden .mode-toggles,
.ui-hidden .timer-controls,
.ui-hidden .zen-note:not(.is-focused),
.ui-hidden .scroll-indicator,
.ui-hidden .current-task {
  opacity: 0;
  pointer-events: none;
  transform: translateY(10px);
}

.timer-content {
  text-align: center;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 800px;
}

/* Mode Toggles */
.mode-toggles {
  display: flex;
  gap: 12px;
  margin-bottom: 2rem;
  background: var(--hero-bg-blur);
  padding: 6px;
  border-radius: 50px;
  backdrop-filter: blur(10px);
  border: 1px solid var(--hero-border-color);
  transition: all 0.5s ease;
}

.mode-btn {
  background: transparent;
  border: none;
  color: var(--hero-text-color);
  padding: 8px 20px;
  border-radius: 20px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: var(--el-font-family);
  font-size: 14px;
  text-shadow: var(--hero-text-shadow);
}

.mode-btn:hover:not(:disabled) {
  background: rgba(128, 128, 128, 0.2);
}

.mode-btn.active {
  background: var(--el-color-primary);
  color: white;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  text-shadow: none;
}

.mode-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Timer Display */
.timer-display {
  margin: 1rem 0;
}

.timer-digits {
  font-size: 12rem;
  font-weight: 700;
  margin: 0;
  line-height: 1;
  font-family: 'Inter', sans-serif;
  font-variant-numeric: tabular-nums;
  letter-spacing: -5px;
  color: var(--hero-text-color);
  text-shadow: var(--hero-text-shadow);
  transition: color 0.3s;
}

/* Current Task */
.current-task {
  margin-bottom: 3rem;
  transition: opacity 0.5s ease;
  height: 40px;
}

.task-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: var(--hero-bg-blur);
  border: 1px solid var(--hero-border-color);
  border-radius: 12px;
  backdrop-filter: blur(5px);
  color: var(--hero-text-color);
  cursor: pointer;
  transition: all 0.2s;
  text-shadow: var(--hero-text-shadow);
}

.task-chip:hover:not(.disabled) {
  background: rgba(128, 128, 128, 0.2);
}

.task-chip.disabled {
  opacity: 0.7;
  cursor: default;
}

.dropdown-icon {
  font-size: 12px;
  margin-left: 4px;
}

/* Controls */
.timer-controls {
  display: flex;
  gap: 1.5rem;
  justify-content: center;
  align-items: center;
  transition: opacity 0.5s ease, transform 0.5s ease;
}

.control-btn {
  background: var(--hero-bg-blur);
  backdrop-filter: blur(5px);
  border: 1px solid var(--hero-border-color);
  color: var(--hero-text-color);
  transition: all 0.2s;
}

.control-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  background: rgba(128, 128, 128, 0.2);
}

.control-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.control-btn.main-action {
  height: 64px;
  padding: 0 40px;
  border-radius: 32px;
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 1px;
  text-transform: uppercase;
  background: var(--hero-control-bg);
  color: var(--hero-control-text);
  border: none;
}

.control-btn.main-action.active {
  background: var(--el-fill-color-darker);
}

.control-btn.icon-only {
  width: 50px;
  height: 50px;
  font-size: 20px;
}

/* Zen Note */
.zen-note {
  margin-top: 4rem;
  width: 360px;
  transition: opacity 0.5s ease, transform 0.5s ease;
}

.zen-input {
  width: 100%;
  background: transparent;
  border: none;
  border-bottom: 2px solid var(--hero-border-color);
  padding: 12px;
  text-align: center;
  color: var(--hero-text-color);
  outline: none;
  font-size: 1.2rem;
  transition: border-color 0.3s;
  font-family: 'Inter', sans-serif;
  text-shadow: var(--hero-text-shadow);
}

.zen-input:focus {
  border-bottom-color: var(--el-color-primary);
}

.zen-input::placeholder {
  color: var(--hero-text-color);
  opacity: 0.7;
  font-style: italic;
}

/* Scroll Indicator */
.scroll-indicator {
  position: absolute;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  cursor: pointer;
  animation: bounce 2s infinite;
  font-size: 2rem;
  color: var(--hero-text-color);
  transition: opacity 0.5s ease;
  opacity: 0.8;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {transform: translateY(0) translateX(-50%);}
  40% {transform: translateY(-10px) translateX(-50%);}
  60% {transform: translateY(-5px) translateX(-50%);}
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .timer-section {
    padding-top: 20px; /* Reduce top padding */
  }

  .timer-digits {
    font-size: 5rem; /* Smaller font for mobile */
    letter-spacing: -2px;
  }
  
  .mode-toggles {
    transform: scale(0.9);
    margin-bottom: 1rem;
    width: 95%;
    justify-content: center;
    flex-wrap: nowrap; /* Keep on one line if possible, or scroll */
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }
  
  .mode-btn {
    padding: 6px 12px;
    font-size: 12px;
    white-space: nowrap;
  }
  
  .timer-controls {
    flex-wrap: wrap;
    gap: 1rem;
    width: 90%;
    justify-content: center;
  }
  
  /* Make Start/Pause button full width on mobile for easier access */
  .control-btn.main-action {
    width: 80%;
    margin-bottom: 0;
    font-size: 20px;
    height: 56px;
  }
  
  .zen-note {
    width: 90%;
    max-width: 360px;
    margin-top: 2rem;
  }
  
  .current-task {
    max-width: 90%;
    margin-bottom: 2rem;
  }
  
  .task-chip {
    max-width: 100%;
  }
  
  .task-text {
    max-width: 180px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
