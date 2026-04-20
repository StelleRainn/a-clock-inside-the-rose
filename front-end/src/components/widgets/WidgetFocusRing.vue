<template>
  <el-card class="widget-card glass-card">
    <!-- 
    WidgetFocusRing 核心组件
    定位：Dashboard 上的专注环形进度条组件，用于直观展示今日专注总时长与目标达成率。
    视觉：采用统一的 glass-card 毛玻璃质感类名。
  -->
    <div class="focus-stat-container">
      <div class="ring-chart">
        <!-- 
          el-progress type="dashboard" 生成仪表盘式半环形图
          动态绑定 percentage 和分段颜色 colors，提供视觉上的阶段激励感。
        -->
        <el-progress type="dashboard" :percentage="dailyGoalPercentage" :color="colors">
          <template #default>
            <!-- 将全局 store 中的专注秒数转换为分钟展示 -->
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
import { usePomodoroStore } from '@/stores/pomodoro'

const pomodoroStore = usePomodoroStore()

// 预设每日目标为 120 分钟 (2小时)。后续可以考虑将其抽离到用户的个性化设置中
const dailyGoalMinutes = 120

// 仪表盘颜色分段：随着完成度的提高，颜色从红(警告)过渡到蓝/紫(完美)，增强游戏化反馈
const colors = [
  { color: '#f56c6c', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#5cb87a', percentage: 60 },
  { color: '#1989fa', percentage: 80 },
  { color: '#6f7ad3', percentage: 100 },
]

// 计算当前完成的百分比，上限封顶为 100%
const dailyGoalPercentage = computed(() => {
  const minutes = pomodoroStore.todayFocusSeconds / 60
  return Math.min((minutes / dailyGoalMinutes) * 100, 100)
})

// 组件挂载时，主动拉取今日的专注数据统计
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
