<template>
  <el-card class="widget-card glass-card">
    <div class="focus-stat-container">
      <div class="ring-chart">
        <el-progress type="dashboard" :percentage="dailyGoalPercentage" :color="colors">
          <template #default>
            <span class="percentage-value">{{ Math.round(pomodoroStore.todayFocusSeconds / 60) }}m</span>
            <span class="percentage-label">{{ $t('widgets.focused') }}</span>
          </template>
        </el-progress>
      </div>
      <div class="stat-details">
        <h3>{{ $t('widgets.todaysFocus') }}</h3>
        <p>{{ pomodoroStore.completedPomodoros }} {{ $t('widgets.pomodorosCompleted') }}</p>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { usePomodoroStore } from '@/stores/pomodoro'

const { t } = useI18n()
const pomodoroStore = usePomodoroStore()
const dailyGoalMinutes = 120 // Example goal

const colors = [
  { color: '#f56c6c', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#5cb87a', percentage: 60 },
  { color: '#1989fa', percentage: 80 },
  { color: '#6f7ad3', percentage: 100 },
]

const dailyGoalPercentage = computed(() => {
  const minutes = pomodoroStore.todayFocusSeconds / 60
  return Math.min((minutes / dailyGoalMinutes) * 100, 100)
})

onMounted(() => {
  pomodoroStore.fetchTodayCount()
})
</script>

<style scoped>
.widget-card {
  border: none;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(12px);
  border-radius: 16px;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.07);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.dark .widget-card {
  background: rgba(30, 30, 30, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.focus-stat-container {
  display: flex;
  align-items: center;
  gap: 24px;
}

.percentage-value {
  display: block;
  font-size: 24px;
  font-weight: bold;
}

.percentage-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.stat-details h3 {
  margin: 0 0 8px 0;
  font-size: 1.2rem;
}

.stat-details p {
  margin: 0;
  color: var(--el-text-color-secondary);
}
</style>
