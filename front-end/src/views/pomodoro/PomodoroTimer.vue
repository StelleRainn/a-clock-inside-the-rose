<template>
  <div class="pomodoro-container">
    <el-card class="timer-card">
      <template #header>
        <div class="card-header">
          <el-radio-group v-model="sessionMode" @change="handleModeChange">
            <el-radio-button label="work" value="work">Focus</el-radio-button>
            <el-radio-button label="break" value="break">Break</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div class="timer-display">
        <el-progress 
          type="dashboard" 
          :percentage="pomodoroStore.progressPercentage" 
          :width="200"
          :color="progressColor"
        >
          <template #default>
            <h1 class="timer-text">{{ pomodoroStore.formattedTime }}</h1>
          </template>
        </el-progress>
      </div>

      <div class="slider-container">
        <span class="duration-label">Duration (min)</span>
        <el-slider 
          v-model="sliderValue" 
          :min="1" 
          :max="60" 
          :disabled="pomodoroStore.isRunning"
          @input="handleSliderChange"
        />
      </div>

      <div class="status-text">
        <span v-if="pomodoroStore.isRunning">Running...</span>
        <span v-else>Paused</span>
      </div>

      <div class="timer-controls">
        <el-button 
          v-if="!pomodoroStore.isRunning" 
          type="primary" 
          size="large" 
          circle 
          @click="pomodoroStore.startTimer()"
        >
          <el-icon><VideoPlay /></el-icon>
        </el-button>
        <el-button 
          v-else 
          type="warning" 
          size="large" 
          circle 
          @click="pomodoroStore.pauseTimer"
        >
          <el-icon><VideoPause /></el-icon>
        </el-button>
        <el-button 
          size="large" 
          circle 
          @click="pomodoroStore.resetTimer"
        >
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
      
      <div class="stats-footer">
        <p>Completed Pomodoros Today: {{ pomodoroStore.completedPomodoros }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted } from 'vue'
import { usePomodoroStore } from '@/stores/pomodoro'

const pomodoroStore = usePomodoroStore()
const sessionMode = ref('work')
const sliderValue = ref(25)

// Sync sessionMode with store state
watch(() => pomodoroStore.isWorkSession, (isWork) => {
  sessionMode.value = isWork ? 'work' : 'break'
  // Update slider value based on current duration setting
  const duration = isWork ? pomodoroStore.workDuration : pomodoroStore.breakDuration
  sliderValue.value = Math.floor(duration / 60)
}, { immediate: true })

const handleModeChange = (val) => {
  pomodoroStore.setMode(val)
}

const handleSliderChange = (val) => {
  pomodoroStore.updateDuration(val * 60)
}

const progressColor = computed(() => {
  return pomodoroStore.isWorkSession ? '#409eff' : '#67c23a'
})

onMounted(() => {
  pomodoroStore.fetchTodayCount()
  // Initialize slider value correctly on mount
  const duration = pomodoroStore.isWorkSession ? pomodoroStore.workDuration : pomodoroStore.breakDuration
  sliderValue.value = Math.floor(duration / 60)
})
</script>

<style scoped>
.pomodoro-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}
.timer-card {
  width: 400px;
  text-align: center;
  padding: 20px;
}
.timer-display {
  margin: 20px 0;
  display: flex;
  justify-content: center;
}
.timer-text {
  font-size: 2.5rem;
  margin: 0;
  color: #303133;
}
.slider-container {
  padding: 0 20px;
  margin-bottom: 20px;
}
.duration-label {
  font-size: 12px;
  color: #909399;
  display: block;
  margin-bottom: 5px;
  text-align: left;
}
.timer-controls {
  margin-top: 20px;
  margin-bottom: 20px;
}
.status-text {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}
.stats-footer {
  margin-top: 20px;
  border-top: 1px solid #ebeef5;
  padding-top: 10px;
  color: #606266;
}
</style>
