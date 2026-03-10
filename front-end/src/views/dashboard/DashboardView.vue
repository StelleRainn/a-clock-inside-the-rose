<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <h1>Dashboard</h1>
      <span class="date-display">{{ currentDate }}</span>
    </div>
    
    <el-row :gutter="20">
      <!-- Left Column: Tasks & Calendar -->
      <el-col :span="16">
        <!-- Calendar View -->
        <el-card class="calendar-card">
          <el-calendar v-model="calendarDate">
            <template #date-cell="{ data }">
              <el-popover
                placement="top"
                :width="250"
                trigger="hover"
                :show-after="200"
              >
                <template #reference>
                  <div class="calendar-cell" :class="{ 'is-selected': data.isSelected }">
                    <div class="day-number">{{ data.day.split('-').slice(2).join('') }}</div>
                    <div class="day-content">
                      <div v-if="getTasksForDate(data.day).length > 0" class="task-dot-container">
                        <span class="task-dot"></span>
                      </div>
                      <div v-if="getFocusTimeForDate(data.day) > 0" class="focus-badge">
                        {{ Math.round(getFocusTimeForDate(data.day) / 60) }}m
                      </div>
                      <!-- Streak Flame Icon -->
                      <div v-if="isStreakDay(data.day)" class="streak-icon">
                        <el-icon color="#f56c6c"><Opportunity /></el-icon>
                      </div>
                    </div>
                  </div>
                </template>
                
                <!-- Popover Content -->
                <div class="popover-content">
                  <h4 class="popover-date">{{ formatDateFull(data.day) }}</h4>
                  
                  <div class="popover-section">
                    <div class="section-title">Tasks</div>
                    <ul v-if="getTasksForDate(data.day).length > 0" class="popover-list">
                      <li v-for="task in getTasksForDate(data.day).slice(0, 5)" :key="task.id">
                        <el-tag size="small" :type="getStatusType(task.status)" effect="plain" class="mini-tag">
                          {{ task.status === 'DONE' ? '✓' : '○' }}
                        </el-tag>
                        <span class="task-title" :class="{ 'done': task.status === 'DONE' }">{{ task.title }}</span>
                      </li>
                      <li v-if="getTasksForDate(data.day).length > 5" class="more-text">
                        + {{ getTasksForDate(data.day).length - 5 }} more
                      </li>
                    </ul>
                    <div v-else class="empty-state">No tasks due this day.</div>
                  </div>

                  <div class="popover-section">
                    <div class="section-title">Focus</div>
                    <div v-if="getFocusTimeForDate(data.day) > 0">
                      <span class="focus-time">{{ Math.round(getFocusTimeForDate(data.day) / 60) }} minutes</span>
                    </div>
                    <div v-else class="empty-state">No focus records.</div>
                  </div>
                </div>
              </el-popover>
            </template>
          </el-calendar>
        </el-card>

        <!-- Recent Tasks -->
        <el-card class="box-card mt-20">
          <template #header>
            <div class="card-header">
              <span>Recent Tasks (Drag to Reorder)</span>
              <el-button text @click="router.push('/tasks')">View All</el-button>
            </div>
          </template>
          <div v-if="recentTasks.length === 0" class="empty-text">No tasks found.</div>
          <draggable 
            v-model="recentTasks" 
            item-key="id"
            @end="handleDragEnd"
            class="task-list-draggable"
          >
            <template #item="{ element }">
              <div class="text item task-item clickable-task" @click="handleEdit(element)">
                <div class="task-info">
                  <span :class="{ 'completed': element.status === 'DONE' }">{{ element.title }}</span>
                  <span v-if="element.dueDate" class="task-due">{{ formatDateShort(element.dueDate) }}</span>
                </div>
                <el-tag size="small" :type="getStatusType(element.status)">{{ element.status }}</el-tag>
              </div>
            </template>
          </draggable>
        </el-card>
      </el-col>

      <!-- Right Column: Stats & Quick Actions -->
      <el-col :span="8">
        <!-- Streak Card -->
        <el-card class="box-card mb-20 streak-card" v-if="streakDays > 0">
          <div class="streak-content">
            <div class="streak-icon-large">
              <el-icon><Opportunity /></el-icon>
            </div>
            <div class="streak-text">
              <span class="streak-number">{{ streakDays }}</span>
              <span class="streak-label">Day Streak!</span>
            </div>
          </div>
        </el-card>

        <!-- Pomodoro Stats -->
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>Today's Focus</span>
            </div>
          </template>
          <div class="stat-item">
            <h3>Completed Pomodoros</h3>
            <p class="stat-number">{{ pomodoroStore.completedPomodoros }}</p>
            <p class="stat-subtitle">
              {{ Math.round(pomodoroStore.todayFocusSeconds / 60) }} minutes focused today
            </p>
          </div>
          <div class="action-buttons">
            <el-button type="primary" class="w-100" @click="router.push('/pomodoro')">Start Focus</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <TaskFormDialog
      v-model:visible="dialogVisible"
      :is-edit="isEditMode"
      :initial-data="currentTaskData"
      :available-tags="availableTags"
      @submit="handleTaskSubmit"
      @tag-created="handleTagCreated"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getTasks, updateTask, createTask, reorderTasks } from '@/api/task'
import { getDailyFocusStats } from '@/api/stats'
import { getUserStats } from '@/api/gamification'
import { getTags } from '@/api/tag'
import { useUserStore } from '@/stores/user'
import { usePomodoroStore } from '@/stores/pomodoro'
import { Opportunity } from '@element-plus/icons-vue'
import TaskFormDialog from '@/components/TaskFormDialog.vue'
import draggable from 'vuedraggable'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const pomodoroStore = usePomodoroStore()
const tasks = ref([])
const availableTags = ref([])
const dailyFocusData = ref([])
const calendarDate = ref(new Date())
const streakDays = ref(0)

// Dialog State
const dialogVisible = ref(false)
const isEditMode = ref(false)
const currentTaskData = ref({})

const currentDate = computed(() => {
  return new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })
})

const fetchTasks = async () => {
  if (!userStore.user || !userStore.user.id) return
  try {
    const data = await getTasks(userStore.user.id)
    tasks.value = data || []
  } catch (error) {
    console.error(error)
  }
}

const fetchFocusStats = async () => {
  if (!userStore.user || !userStore.user.id) return
  try {
    const data = await getDailyFocusStats(userStore.user.id)
    dailyFocusData.value = data || []
  } catch (error) {
    console.error(error)
  }
}

const fetchUserStreak = async () => {
  if (!userStore.user || !userStore.user.id) return
  try {
    const stats = await getUserStats(userStore.user.id)
    streakDays.value = stats.streakDays || 0
  } catch (error) {
    console.error(error)
  }
}

const fetchTags = async () => {
  if (!userStore.user || !userStore.user.id) return
  try {
    const data = await getTags(userStore.user.id)
    availableTags.value = data || []
  } catch (error) {
    console.error(error)
  }
}

const handleEdit = (task) => {
  isEditMode.value = true
  currentTaskData.value = { ...task } // Clone
  dialogVisible.value = true
}

const handleTaskSubmit = async (payload) => {
  try {
    if (isEditMode.value) {
      await updateTask(currentTaskData.value.id, { ...payload, userId: userStore.user.id })
      ElMessage.success('Task updated')
    } else {
      await createTask({ ...payload, userId: userStore.user.id })
      ElMessage.success('Task created')
    }
    dialogVisible.value = false
    fetchTasks()
  } catch {
    ElMessage.error('Operation failed')
  }
}

const handleTagCreated = (newTag) => {
  availableTags.value.push(newTag)
}

const handleDragEnd = async () => {
  // tasks array is already updated by vuedraggable (if used with v-model or list prop?)
  // Wait, I will use v-model="tasks" on draggable.
  // But Dashboard usually shows top 5.
  // If I want to allow reordering, I should probably show more or limit draggable?
  // User said: "In Dash Board My Tasks... implement drag-and-drop sort".
  // If I drag item at index 0 to index 1, I need to update all positions.
  
  // Since I am fetching all tasks and slicing in template, I should use `tasks` in draggable
  // but slice it?
  // draggable works on an array.
  // If I slice, I get a new array. Dragging on it won't affect `tasks`.
  // So I should have a `recentTasks` ref that is a subset?
  // No, if I want to persist changes, I need to update `tasks`.
  
  // Solution: Use `tasks` but css limit height? Or just show all?
  // "Recent Tasks" implies subset.
  // If user reorders the subset, does it mean they are reordering the top of the list?
  // Yes.
  // So I can copy top 5 to `recentTasks`, drag, then merge back?
  // Or just use `tasks` and limit `item` slot?
  // Draggable renders what is in the list.
  
  // Let's create `displayTasks` computed? No, `v-model` needs writable.
  // Let's create `recentTasks` ref, watch `tasks` to populate it.
  // On drag end, we update `tasks` positions based on `recentTasks` + rest of `tasks`.
  // Actually, simplest is to just let the Dashboard show the top 5, and if they drag, we assume they are reordering those 5 relative to each other.
  // So:
  // 1. `recentTasks` = tasks.value.slice(0, 5)
  // 2. Drag `recentTasks`.
  // 3. On end, update backend with `recentTasks` IDs + `tasks.value.slice(5)` IDs?
  //    Wait, `tasks` from backend is ordered by position.
  //    So `recentTasks` are the first 5.
  //    If I reorder them, the new order of all tasks is `newRecentTasks` + `tasks[5...]`.
  //    So I can construct the full ID list and send to backend.
  
  const topIds = recentTasks.value.map(t => t.id)
  const restIds = tasks.value.slice(recentTasks.value.length).map(t => t.id)
  const allIds = [...topIds, ...restIds]
  
  try {
    await reorderTasks(allIds)
    // Refresh to get correct order and positions
    fetchTasks()
  } catch {
    ElMessage.error('Reorder failed')
  }
}

const recentTasks = ref([])

// Watch tasks to update recentTasks
import { watch } from 'vue'
watch(tasks, (val) => {
  // Only update if not dragging? 
  // If we fetch tasks, we should update recent.
  // We can just take top 5.
  // But if we are dragging, we don't want to overwrite.
  // `draggable` updates `recentTasks` in real time.
  // `fetchTasks` is called on mount or after update.
  // So it's safe to overwrite.
  recentTasks.value = val.slice(0, 5)
})

const getTasksForDate = (dateStr) => {
  return tasks.value.filter(task => {
    if (!task.dueDate) return false
    const taskDate = new Date(task.dueDate).toISOString().split('T')[0]
    return taskDate === dateStr
  })
}

const getFocusTimeForDate = (dateStr) => {
  const stat = dailyFocusData.value.find(d => d.date === dateStr)
  return stat ? stat.totalSeconds : 0
}

const isStreakDay = (dateStr) => {
  // Simple visualization: Mark days with focus time as part of streak history
  // For a true "streak" highlight, we'd need more complex backend logic returning specific streak dates
  // For now, let's just show the flame if there was focus time > 0
  return getFocusTimeForDate(dateStr) > 0
}

const getStatusType = (status) => {
  const map = {
    'DONE': 'success',
    'IN_PROGRESS': 'primary',
    'TODO': 'info'
  }
  return map[status] || ''
}

const formatDateShort = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth()+1}/${date.getDate()}`
}

const formatDateFull = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })
}

onMounted(() => {
  fetchTasks()
  fetchTags()
  fetchFocusStats()
  fetchUserStreak()
  pomodoroStore.fetchTodayCount()
})
</script>

<style scoped>
.dashboard-container {
  padding-bottom: 20px;
}
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.date-display {
  color: var(--el-text-color-secondary);
  font-size: 1.1rem;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.clickable-task {
  cursor: pointer;
  transition: background-color 0.2s;
}
.clickable-task:hover {
  background-color: var(--el-fill-color-light);
}
.task-item:last-child {
  border-bottom: none;
}
.task-info {
  display: flex;
  flex-direction: column;
}
.task-due {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.completed {
  text-decoration: line-through;
  color: var(--el-text-color-placeholder);
}
.empty-text {
  color: var(--el-text-color-secondary);
  text-align: center;
  padding: 20px 0;
}
.stat-item {
  text-align: center;
  padding: 20px 0;
}
.stat-number {
  font-size: 48px;
  font-weight: bold;
  color: var(--el-color-primary);
  margin: 10px 0;
}
.stat-subtitle {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}
.action-buttons {
  margin-top: 20px;
}
.w-100 {
  width: 100%;
}
.mt-20 {
  margin-top: 20px;
}
.mb-20 {
  margin-bottom: 20px;
}

/* Calendar Styles */
.calendar-card :deep(.el-calendar-table .el-calendar-day) {
  height: 80px;
  padding: 5px;
}
.calendar-cell {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  cursor: default;
}
.day-number {
  font-weight: bold;
}
.day-content {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}
.task-dot {
  width: 6px;
  height: 6px;
  background-color: var(--el-color-danger);
  border-radius: 50%;
  display: inline-block;
}
.focus-badge {
  background-color: var(--el-color-success-light-9);
  color: var(--el-color-success);
  font-size: 10px;
  padding: 2px 4px;
  border-radius: 4px;
}
.streak-icon {
  margin-top: 2px;
}

/* Streak Card Styles */
.streak-card {
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 99%, #fad0c4 100%);
  color: white;
  border: none;
}
.streak-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}
.streak-icon-large {
  font-size: 40px;
  animation: flame 1.5s infinite ease-in-out;
}
.streak-text {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.streak-number {
  font-size: 32px;
  font-weight: bold;
  line-height: 1;
}
.streak-label {
  font-size: 14px;
  opacity: 0.9;
}

@keyframes flame {
  0% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.1); opacity: 1; }
  100% { transform: scale(1); opacity: 0.8; }
}

/* Popover Content Styles */
.popover-date {
  margin: 0 0 10px 0;
  color: var(--el-text-color-primary);
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding-bottom: 5px;
}
.popover-section {
  margin-bottom: 10px;
}
.section-title {
  font-size: 12px;
  font-weight: bold;
  color: var(--el-text-color-secondary);
  margin-bottom: 5px;
}
.popover-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.popover-list li {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 4px;
  font-size: 12px;
}
.mini-tag {
  height: 16px;
  padding: 0 2px;
  line-height: 14px;
}
.task-title {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px;
}
.task-title.done {
  text-decoration: line-through;
  color: var(--el-text-color-placeholder);
}
.more-text {
  color: var(--el-text-color-secondary);
  font-style: italic;
  font-size: 10px;
  padding-left: 5px;
}
.empty-state {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  font-style: italic;
}
.focus-time {
  font-weight: bold;
  color: var(--el-color-success);
}
</style>
