import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { savePomodoro, getTodayPomodoroCount } from '@/api/pomodoro'
import { useUserStore } from '@/stores/user'
import { imageDb } from '@/utils/imageDb'
import { ElMessage } from 'element-plus'

// ==========================================
// pomodoroStore: 整个番茄钟的核心状态机，负责倒计时逻辑、设置持久化以及数据流转
// ==========================================
export const usePomodoroStore = defineStore('pomodoro', () => {
  const userStore = useUserStore()

  // 格式化 taskId，防御脏数据（如 'null', null, undefined）
  const normalizeTaskId = (taskId) => {
    if (taskId === null || taskId === undefined || taskId === '' || taskId === 'null') {
      return null
    }
    return String(taskId)
  }

  const parseTaskIdForApi = (taskId) => {
    const normalizedTaskId = normalizeTaskId(taskId)
    if (!normalizedTaskId) return null
    const numericTaskId = Number(normalizedTaskId)
    return Number.isNaN(numericTaskId) ? normalizedTaskId : numericTaskId
  }
  
  // -- Persisted Settings (持久化设置项) --
  // 从 LocalStorage 中读取用户的个性化设置，如果没有则使用默认值
  const pomodoroDuration = ref(parseInt(localStorage.getItem('pomodoro_duration_work')) || 25 * 60)
  const shortBreakDuration = ref(parseInt(localStorage.getItem('pomodoro_duration_short')) || 5 * 60)
  const longBreakDuration = ref(parseInt(localStorage.getItem('pomodoro_duration_long')) || 15 * 60)
  
  // 自动流转设置：是否自动开始休息/下一个番茄钟，以及几个番茄钟后触发长休
  const autoStartBreaks = ref(localStorage.getItem('pomodoro_autoStartBreaks') === 'true')
  const autoStartPomodoros = ref(localStorage.getItem('pomodoro_autoStartPomodoros') === 'true')
  const longBreakInterval = ref(parseInt(localStorage.getItem('pomodoro_longBreakInterval')) || 4)
  
  // Sound Settings (声音设置)
  const soundEnabled = ref(localStorage.getItem('pomodoro_soundEnabled') !== 'false')
  const soundVolume = ref(parseInt(localStorage.getItem('pomodoro_soundVolume')) || 50)
  const selectedSound = ref(localStorage.getItem('pomodoro_selectedSound') || 'bell')
  
  // Background Settings (Global or Hero specific) (背景与主题设置)
  const backgroundImage = ref(localStorage.getItem('pomodoro_backgroundImage') || '')
  const customBgUrl = ref('')
  const heroTheme = ref(localStorage.getItem('pomodoro_heroTheme') || 'auto') // 'auto' | 'light' | 'dark'
  const bgOverlayOpacity = ref(parseFloat(localStorage.getItem('pomodoro_bgOverlayOpacity')) || 0.2)
  
  // 沉浸模式开关 (开启后隐藏导航栏和侧边栏)
  const isImmersive = ref(false)

  // -- State (倒计时与运行状态) --
  const isRunning = ref(false)
  const currentMode = ref(localStorage.getItem('pomodoro_currentMode') || 'pomodoro') // 'pomodoro' | 'short-break' | 'long-break'
  
  // timeLeft: 剩余秒数；endTime: 预计结束的时间戳 (用于处理页面刷新/后台运行的计时弥补)
  const timeLeft = ref(parseInt(localStorage.getItem('pomodoro_timeLeft')) || pomodoroDuration.value)
  const endTime = ref(parseInt(localStorage.getItem('pomodoro_endTime')) || null)
  
  // 统计数据
  const completedPomodoros = ref(0)
  const todayFocusSeconds = ref(0)
  const selectedTaskId = ref(normalizeTaskId(localStorage.getItem('pomodoro_selectedTaskId')))
  
  // 记录距离上一次长休已经完成了几个番茄钟，用于触发长休逻辑
  const pomodorosSinceLongBreak = ref(parseInt(localStorage.getItem('pomodoro_pomodorosSinceLongBreak')) || 0)

  // setInterval 的定时器句柄
  let timerInterval = null

  // -- Computed (派生状态) --
  // 将剩余秒数转换为 MM:SS 格式用于时钟显示
  const formattedTime = computed(() => {
    const minutes = Math.floor(timeLeft.value / 60)
    const seconds = timeLeft.value % 60
    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
  })

  // 获取当前模式下对应的总时长（秒）
  const currentDuration = computed(() => {
    if (currentMode.value === 'short-break') return shortBreakDuration.value
    if (currentMode.value === 'long-break') return longBreakDuration.value
    return pomodoroDuration.value
  })

  // 计算当前倒计时的进度百分比，用于环形图
  const progressPercentage = computed(() => {
    const total = currentDuration.value
    if (total === 0) return 0
    return ((total - timeLeft.value) / total) * 100
  })

  // -- Watchers for persistence (利用 Vue watch 实现响应式持久化) --
  // 每当这些 ref 改变时，自动将新值同步到 localStorage
  watch(pomodoroDuration, (val) => localStorage.setItem('pomodoro_duration_work', val))
  watch(shortBreakDuration, (val) => localStorage.setItem('pomodoro_duration_short', val))
  watch(longBreakDuration, (val) => localStorage.setItem('pomodoro_duration_long', val))
  watch(autoStartBreaks, (val) => localStorage.setItem('pomodoro_autoStartBreaks', val))
  watch(autoStartPomodoros, (val) => localStorage.setItem('pomodoro_autoStartPomodoros', val))
  watch(soundEnabled, (val) => localStorage.setItem('pomodoro_soundEnabled', val))
  watch(soundVolume, (val) => localStorage.setItem('pomodoro_soundVolume', val))
  watch(selectedSound, (val) => localStorage.setItem('pomodoro_selectedSound', val))
  watch(backgroundImage, (val) => {
    localStorage.setItem('pomodoro_backgroundImage', val)
    if (val === 'custom') {
      loadCustomBackground()
    }
  })
  watch(heroTheme, (val) => localStorage.setItem('pomodoro_heroTheme', val))
  watch(bgOverlayOpacity, (val) => localStorage.setItem('pomodoro_bgOverlayOpacity', val))
  watch(currentMode, (val) => localStorage.setItem('pomodoro_currentMode', val))
  watch(pomodorosSinceLongBreak, (val) => localStorage.setItem('pomodoro_pomodorosSinceLongBreak', val))
  watch(selectedTaskId, (val) => {
    const normalizedTaskId = normalizeTaskId(val)
    if (normalizedTaskId) {
      localStorage.setItem('pomodoro_selectedTaskId', normalizedTaskId)
    } else {
      localStorage.removeItem('pomodoro_selectedTaskId')
    }
  })

  // 从 IndexedDB 加载用户上传的自定义背景图并生成预览 URL
  async function loadCustomBackground() {
    try {
      const blob = await imageDb.getImage('custom')
      if (blob) {
        if (customBgUrl.value) URL.revokeObjectURL(customBgUrl.value)
        customBgUrl.value = URL.createObjectURL(blob)
      } else if (backgroundImage.value === 'custom') {
        // Fallback if db is empty but setting says custom
        // 防御：如果设置里写着 custom 但库里没图，回退到默认
        backgroundImage.value = ''
      }
    } catch (e) {
      console.error('Failed to load custom bg', e)
    }
  }

  // Restore logic
  if (backgroundImage.value === 'custom') {
    loadCustomBackground()
  }

  // ==========================================
  // [核心] 刷新/恢复状态补偿机制
  // 如果用户在倒计时中刷新页面，只要 endTime 在未来，就自动恢复倒计时。
  // ==========================================
  if (endTime.value && endTime.value > Date.now()) {
    isRunning.value = true
    startTimer(true)
  } else if (endTime.value && endTime.value <= Date.now()) {
    timeLeft.value = 0
    isRunning.value = false
    localStorage.removeItem('pomodoro_endTime')
    // Ideally we should process completion here too if app was closed
  }

  // -- Actions (动作与核心逻辑) --
  
  // 切换模式 (专注/短休/长休)
  function setMode(mode) {
    pauseTimer()
    currentMode.value = mode
    // Reset time to full duration of that mode
    // 切换模式时重置时间为该模式的总时长
    timeLeft.value = currentDuration.value
    localStorage.setItem('pomodoro_timeLeft', timeLeft.value)
  }

  // 启动/恢复定时器
  function startTimer(resume = false) {
    if (isRunning.value && !resume) return
    
    isRunning.value = true
    
    // 如果是全新启动(非恢复)，则以当前时间加上剩余时间计算出预计结束的绝对时间点 (endTime)
    if (!resume) {
      endTime.value = Date.now() + timeLeft.value * 1000
      localStorage.setItem('pomodoro_endTime', endTime.value)
    }

    if (timerInterval) clearInterval(timerInterval)

    timerInterval = setInterval(() => {
      const now = Date.now()
      // 利用绝对时间 (endTime - now) 计算剩余，防止 JS 线程休眠/阻塞导致的时间漂移
      const remaining = Math.round((endTime.value - now) / 1000)
      
      if (remaining >= 0) {
        timeLeft.value = remaining
        localStorage.setItem('pomodoro_timeLeft', timeLeft.value)
      } else {
        timeLeft.value = 0
        completeSession()
        clearInterval(timerInterval)
      }
    }, 1000)
  }

  // 暂停定时器
  function pauseTimer() {
    isRunning.value = false
    if (timerInterval) {
      clearInterval(timerInterval)
      timerInterval = null
    }
    // 暂停时移除 endTime，因为预计结束时间会因为暂停而延后
    localStorage.removeItem('pomodoro_endTime')
    localStorage.setItem('pomodoro_timeLeft', timeLeft.value)
  }

  // 放弃当前进度，重置倒计时
  function resetTimer() {
    pauseTimer()
    timeLeft.value = currentDuration.value
    localStorage.setItem('pomodoro_timeLeft', timeLeft.value)
  }

  function selectTask(taskId) {
    selectedTaskId.value = normalizeTaskId(taskId)
  }

  // ==========================================
  // [核心] 会话完成与状态机流转 (State Machine)
  // 处理倒计时到 0 后的逻辑：打卡记录、播放声音、判断下一次模式
  // ==========================================
  async function completeSession() {
    pauseTimer()
    localStorage.removeItem('pomodoro_endTime')
    
    // Play sound
    if (soundEnabled.value) {
      playSound()
    }

    if (currentMode.value === 'pomodoro') {
      // Completed a pomodoro
      pomodorosSinceLongBreak.value++
      
      // Save record
      if (userStore.user?.id) {
        try {
          await savePomodoro({
            userId: userStore.user.id,
            taskId: parseTaskIdForApi(selectedTaskId.value),
            durationSeconds: pomodoroDuration.value,
            status: 'COMPLETED'
          })
          fetchTodayCount()
        } catch (e) { console.error(e) }
      } else {
        completedPomodoros.value++
      }

      // Auto-switch logic
      if (pomodorosSinceLongBreak.value >= longBreakInterval.value) {
        setMode('long-break')
        pomodorosSinceLongBreak.value = 0
      } else {
        setMode('short-break')
      }

      if (autoStartBreaks.value) {
        startTimer()
      } else {
        ElMessage.success('Pomodoro finished! Take a break.')
      }

    } else {
      // Break finished
      setMode('pomodoro')
      if (autoStartPomodoros.value) {
        startTimer()
      } else {
        ElMessage.info('Break is over! Ready to focus?')
      }
    }
  }

  function playSound() {
    // Simple implementation - can be improved with AudioContext or preloaded assets
    const audio = new Audio(`/sounds/${selectedSound.value}.mp3`)
    audio.volume = soundVolume.value / 100
    audio.play().catch(e => console.log('Audio play failed', e))
  }

  // Update duration from settings
  function updateSettings(settings) {
    if (settings.pomodoro) pomodoroDuration.value = settings.pomodoro * 60
    if (settings.shortBreak) shortBreakDuration.value = settings.shortBreak * 60
    if (settings.longBreak) longBreakDuration.value = settings.longBreak * 60
    
    // If not running, update current timeLeft to reflect new duration
    if (!isRunning.value) {
      timeLeft.value = currentDuration.value
    }
  }

  function updateDuration(durationSeconds) {
    if (currentMode.value === 'pomodoro') {
      pomodoroDuration.value = durationSeconds
    } else if (currentMode.value === 'short-break') {
      shortBreakDuration.value = durationSeconds
    } else {
      longBreakDuration.value = durationSeconds
    }
    
    if (!isRunning.value) {
      timeLeft.value = currentDuration.value
      localStorage.setItem('pomodoro_timeLeft', timeLeft.value)
    }
  }

  async function fetchTodayCount() {
    if (userStore.user?.id) {
      try {
        const res = await getTodayPomodoroCount(userStore.user.id)
        if (res) {
          completedPomodoros.value = res.count || 0
          todayFocusSeconds.value = res.totalSeconds || 0
        }
      } catch (e) {
        console.error('Failed to fetch today count', e)
      }
    }
  }

  function toggleTimer() {
    if (isRunning.value) {
      pauseTimer()
    } else {
      startTimer()
    }
  }

  return {
    // Settings
    pomodoroDuration,
    shortBreakDuration,
    longBreakDuration,
    autoStartBreaks,
    autoStartPomodoros,
    longBreakInterval,
    soundEnabled,
    soundVolume,
    selectedSound,
    backgroundImage,
    customBgUrl,
    heroTheme,
    bgOverlayOpacity,
    isImmersive,
    
    // State
    isRunning,
    currentMode,
    timeLeft,
    formattedTime,
    progressPercentage,
    completedPomodoros,
    todayFocusSeconds,
    selectedTaskId,
    
    // Actions
    toggleTimer,
    startTimer,
    pauseTimer,
    resetTimer,
    selectTask,
    setMode,
    updateSettings,
    updateDuration,
    fetchTodayCount,
    loadCustomBackground
  }
})
