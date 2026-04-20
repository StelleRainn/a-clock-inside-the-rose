<template>
  <el-card class="widget-card glass-card">
    <!-- 
    WidgetHeatmap 核心组件
    定位：Dashboard 上的活动热力图日历，展示用户每日的任务安排与专注记录。
    视觉：使用 Element Plus 的 el-calendar 深度定制，结合 el-popover 实现悬浮详情。
  -->
    <template #header>
      <div class="card-header">
        <span>{{ $t('widgets.activityHeatmap') }}</span>
        <!-- 连续专注天数（Streak）徽章，增加游戏化体验 -->
        <span class="streak-badge" v-if="streakDays > 0">
          <el-icon color="#f56c6c"><Opportunity /></el-icon> {{ streakDays }} {{ $t('widgets.dayStreak') }}
        </span>
      </div>
    </template>
    
    <el-calendar v-model="calendarDate">
      <!-- 
        自定义日历单元格 (date-cell 插槽)
        通过 data.day (格式如 '2024-05-01') 进行数据匹配。
      -->
      <template #date-cell="{ data }">
        <el-popover
          placement="top"
          :width="240"
          trigger="hover"
          popper-class="heatmap-popover"
        >
          <template #reference>
            <div class="calendar-cell" :class="{ 'has-focus': getFocusTimeForDate(data.day) > 0 }">
              <!-- 仅显示日期数字，去掉年月 -->
              <span class="day-num">{{ data.day.split('-').slice(2).join('') }}</span>
              <div class="indicators">
                <!-- 黄色圆点代表当天有任务安排 -->
                <span v-if="getTasksForDate(data.day).length > 0" class="dot task-dot"></span>
                <!-- 绿色圆点代表当天有专注记录 -->
                <span v-if="getFocusTimeForDate(data.day) > 0" class="dot focus-dot"></span>
              </div>
            </div>
          </template>
          
          <!-- Popover 悬浮窗内容：详细展示当天的专注时长和前 3 个任务 -->
          <div class="popover-content">
            <h4 class="popover-date">{{ formatDate(data.day) }}</h4>
            
            <div v-if="getFocusTimeForDate(data.day) > 0" class="popover-section">
              <div class="section-title">
                <el-icon><Timer /></el-icon> Focus Time
              </div>
              <div class="focus-time">
                {{ formatDuration(getFocusTimeForDate(data.day)) }}
              </div>
            </div>

            <div v-if="getTasksForDate(data.day).length > 0" class="popover-section">
              <div class="section-title">
                <el-icon><List /></el-icon> Tasks
              </div>
              <ul class="popover-task-list">
                <!-- 限制最多显示 3 个任务，防止弹窗过长 -->
                <li v-for="task in getTasksForDate(data.day).slice(0, 3)" :key="task.id" class="popover-task-item">
                  <span class="task-status-dot" :class="{ done: task.status === 'DONE' }"></span>
                  <span class="task-text">{{ task.title }}</span>
                </li>
                <li v-if="getTasksForDate(data.day).length > 3" class="more-tasks">
                  +{{ getTasksForDate(data.day).length - 3 }} more
                </li>
              </ul>
            </div>

            <!-- 无数据时的空状态占位 -->
            <div v-if="getFocusTimeForDate(data.day) === 0 && getTasksForDate(data.day).length === 0" class="empty-state">
              <span class="empty-text">{{ $t('widgets.noActivity') }}</span>
            </div>
          </div>
        </el-popover>
      </template>
    </el-calendar>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getTasks } from '@/api/task'
import { getDailyFocusStats } from '@/api/stats'
import { getUserStats } from '@/api/gamification'
import { Opportunity, Timer, List } from '@element-plus/icons-vue'

const userStore = useUserStore()
const calendarDate = ref(new Date())
const tasks = ref([])
const dailyFocusData = ref([])
const streakDays = ref(0)

// 格式化日期为友好易读的字符串 (如 "Fri, May 1, 2024")
const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleDateString(undefined, {
    weekday: 'short',
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

// 将秒数格式化为 "Xh Ym" 或 "Ym"
const formatDuration = (seconds) => {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  if (h > 0) return `${h}h ${m}m`
  return `${m}m`
}

// 核心数据拉取：并行请求 任务列表、每日专注统计、用户游戏化数据(Streak)
const fetchData = async () => {
  if (!userStore.user?.id) return
  try {
    const [tasksRes, focusRes, statsRes] = await Promise.all([
      getTasks(userStore.user.id),
      getDailyFocusStats(userStore.user.id),
      getUserStats(userStore.user.id)
    ])
    tasks.value = tasksRes || []
    dailyFocusData.value = focusRes || []
    streakDays.value = statsRes?.streakDays || 0
  } catch (e) {
    console.error('Failed to fetch heatmap data', e)
  }
}

// 从每日专注统计中查找特定日期的专注总秒数
const getFocusTimeForDate = (dateStr) => {
  const stat = dailyFocusData.value.find(d => d.date === dateStr)
  return stat ? stat.totalSeconds : 0
}

// 过滤出截止日期为特定日期的任务
const getTasksForDate = (dateStr) => {
  return tasks.value.filter(task => {
    if (!task.dueDate) return false
    const taskDate = new Date(task.dueDate).toISOString().split('T')[0]
    return taskDate === dateStr
  })
}

onMounted(() => {
  fetchData()
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 1.1rem;
}

.calendar-cell {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.has-focus {
  background-color: rgba(var(--el-color-primary-rgb), 0.1);
  border-radius: 4px;
}

.indicators {
  display: flex;
  gap: 4px;
  margin-top: 4px;
}

.dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
}

.task-dot { background-color: var(--el-color-warning); }
.focus-dot { background-color: var(--el-color-success); }
</style>

<style>
.heatmap-popover {
  padding: 16px !important;
  border-radius: 12px !important;
  border: 1px solid var(--el-border-color-light) !important;
  background: var(--el-bg-color-overlay) !important;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
}

.popover-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.popover-date {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding-bottom: 8px;
}

.popover-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

.focus-time {
  font-size: 18px;
  font-weight: bold;
  color: var(--el-color-primary);
  padding-left: 0;
  line-height: 1.2;
}

.popover-task-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.popover-task-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.task-status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--el-color-warning);
  flex-shrink: 0;
}

.task-status-dot.done {
  background-color: var(--el-color-success);
}

.task-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
}

.more-tasks {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  padding-left: 14px;
}

.empty-state {
  text-align: center;
  padding: 8px 0;
  color: var(--el-text-color-placeholder);
  font-style: italic;
  font-size: 13px;
}
</style>
