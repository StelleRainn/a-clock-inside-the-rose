import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { savePomodoro, getTodayPomodoroCount } from '@/api/pomodoro'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

export const usePomodoroStore = defineStore('pomodoro', () => {
  const userStore = useUserStore()
  
  // Configurable settings
  const workDuration = ref(parseInt(localStorage.getItem('pomodoro_workDuration')) || 25 * 60)
  const breakDuration = ref(parseInt(localStorage.getItem('pomodoro_breakDuration')) || 5 * 60)
  
  // State
  const isRunning = ref(false)
  const isWorkSession = ref(localStorage.getItem('pomodoro_isWorkSession') !== 'false') // Default true
  const completedPomodoros = ref(0)
  const endTime = ref(parseInt(localStorage.getItem('pomodoro_endTime')) || null)
  const savedTimeLeft = ref(parseInt(localStorage.getItem('pomodoro_timeLeft')) || workDuration.value)
  
  // Initialize timeLeft based on persistence
  const timeLeft = ref(savedTimeLeft.value)

  let timerInterval = null

  // Computed
  const formattedTime = computed(() => {
    const minutes = Math.floor(timeLeft.value / 60)
    const seconds = timeLeft.value % 60
    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
  })

  const progressPercentage = computed(() => {
    const total = isWorkSession.value ? workDuration.value : breakDuration.value
    return ((total - timeLeft.value) / total) * 100
  })

  // Watchers for persistence
  watch(workDuration, (val) => localStorage.setItem('pomodoro_workDuration', val))
  watch(breakDuration, (val) => localStorage.setItem('pomodoro_breakDuration', val))
  watch(isWorkSession, (val) => localStorage.setItem('pomodoro_isWorkSession', val))
  
  // Restore timer state on load
  if (endTime.value && endTime.value > Date.now()) {
    isRunning.value = true
    startTimer(true) // Resume
  } else if (endTime.value && endTime.value <= Date.now()) {
    // Timer expired while away
    timeLeft.value = 0
    isRunning.value = false
    localStorage.removeItem('pomodoro_endTime')
    // Optionally handle completion here, but for safety let's just reset or show 0
    // completeSession() // This might be risky on reload, user might not notice. 
    // Let's just leave it at 0 and let user click 'reset' or something, or auto-complete?
    // User requirement: "continuous countdown". If it finished, it finished.
    // Let's reset to next state to be clean? Or stay at 0.
    // Let's stay at 0 to show it finished.
  }

  // Actions
  function startTimer(resume = false) {
    if (isRunning.value && !resume) return
    
    isRunning.value = true
    
    if (!resume) {
      // New start
      endTime.value = Date.now() + timeLeft.value * 1000
      localStorage.setItem('pomodoro_endTime', endTime.value)
    }

    if (timerInterval) clearInterval(timerInterval)

    timerInterval = setInterval(() => {
      const now = Date.now()
      if (endTime.value && now < endTime.value) {
        timeLeft.value = Math.round((endTime.value - now) / 1000)
        localStorage.setItem('pomodoro_timeLeft', timeLeft.value)
      } else {
        timeLeft.value = 0
        completeSession()
      }
    }, 1000)
  }

  function pauseTimer() {
    isRunning.value = false
    if (timerInterval) {
      clearInterval(timerInterval)
      timerInterval = null
    }
    localStorage.removeItem('pomodoro_endTime')
    localStorage.setItem('pomodoro_timeLeft', timeLeft.value)
  }

  function resetTimer() {
    pauseTimer()
    timeLeft.value = isWorkSession.value ? workDuration.value : breakDuration.value
    localStorage.setItem('pomodoro_timeLeft', timeLeft.value)
    localStorage.removeItem('pomodoro_endTime')
  }

  function toggleTimer() {
    if (isRunning.value) {
      pauseTimer()
    } else {
      startTimer()
    }
  }

  async function completeSession() {
    pauseTimer()
    localStorage.removeItem('pomodoro_endTime')
    
    // Play sound or notification here (future enhancement)
    
    if (isWorkSession.value) {
      // It was a work session, so save it
      if (userStore.user && userStore.user.id) {
        try {
          await savePomodoro({
            userId: userStore.user.id,
            durationSeconds: workDuration.value,
            status: 'COMPLETED'
          })
          ElMessage.success('Focus session completed!')
          fetchTodayCount() // Refresh count
        } catch (e) {
          console.error('Failed to save pomodoro record', e)
        }
      } else {
        completedPomodoros.value++ // Fallback if no user
      }

      // Auto-switch to break? Or wait for user? Let's wait for user for now, but flip the mode
      isWorkSession.value = false
      timeLeft.value = breakDuration.value
    } else {
      // Break over, back to work
      isWorkSession.value = true
      timeLeft.value = workDuration.value
      ElMessage.info('Break is over! Time to focus.')
    }
    localStorage.setItem('pomodoro_timeLeft', timeLeft.value)
  }

  function setMode(mode) {
    pauseTimer()
    isWorkSession.value = mode === 'work'
    timeLeft.value = isWorkSession.value ? workDuration.value : breakDuration.value
    localStorage.setItem('pomodoro_timeLeft', timeLeft.value)
  }
  
  function updateDuration(seconds) {
    if (isRunning.value) return
    if (isWorkSession.value) {
      workDuration.value = seconds
    } else {
      breakDuration.value = seconds
    }
    // Update current timeLeft immediately if not running
    timeLeft.value = seconds
    localStorage.setItem('pomodoro_timeLeft', seconds)
  }

  async function fetchTodayCount() {
    if (userStore.user && userStore.user.id) {
      try {
        const count = await getTodayPomodoroCount(userStore.user.id)
        completedPomodoros.value = count
      } catch (e) {
        console.error(e)
      }
    }
  }

  return {
    workDuration,
    breakDuration,
    timeLeft,
    isRunning,
    isWorkSession,
    completedPomodoros,
    formattedTime,
    progressPercentage,
    startTimer,
    pauseTimer,
    resetTimer,
    toggleTimer,
    setMode,
    fetchTodayCount,
    updateDuration
  }
})
