<template>
  <el-card class="widget-card glass-card">
    <template #header>
      <div class="card-header">
        <span>{{ $t('widgets.todoList') }}</span>
        <el-button link @click="$router.push('/tasks')">{{ $t('widgets.viewAll') }}</el-button>
      </div>
    </template>
    <div class="task-list">
      <div v-for="task in recentTasks" :key="task.id" class="task-item">
        <el-checkbox v-model="task.done" @change="toggleTask(task)" />
        <span class="task-title" :class="{ done: task.status === 'DONE' }">{{ task.title }}</span>
        <el-tag size="small" :type="getPriorityType(task.priority)">{{ task.priority }}</el-tag>
      </div>
      <div v-if="recentTasks.length === 0" class="empty-state">
        {{ $t('widgets.noPendingTasks') }} <el-button link type="primary" @click="$emit('add-task')">{{ $t('widgets.addOne') }}</el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { getTasks, updateTask } from '@/api/task'
import { ElMessage } from 'element-plus'

const { t } = useI18n()
const userStore = useUserStore()
const tasks = ref([])

const recentTasks = computed(() => {
  return tasks.value
    .filter(t => t.status !== 'DONE')
    .slice(0, 5)
})

const fetchData = async () => {
  if (!userStore.user?.id) return
  try {
    const res = await getTasks(userStore.user.id)
    tasks.value = res || []
  } catch (e) {
    console.error('Failed to fetch tasks', e)
  }
}

const toggleTask = async (task) => {
  const newStatus = task.status === 'DONE' ? 'TODO' : 'DONE'
  task.status = newStatus
  
  try {
    await updateTask(task.id, { ...task, status: newStatus, userId: userStore.user.id })
    ElMessage.success('Task updated')
  } catch {
    task.status = newStatus === 'DONE' ? 'TODO' : 'DONE'
    ElMessage.error('Failed to update task')
  }
}

const getPriorityType = (p) => {
  const map = { HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' }
  return map[p] || ''
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

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.3);
  transition: background 0.2s;
}

.dark .task-item {
  background: rgba(255, 255, 255, 0.05);
}

.task-item:hover {
  background: rgba(255, 255, 255, 0.5);
}

.task-title {
  flex: 1;
  font-weight: 500;
}

.task-title.done {
  text-decoration: line-through;
  opacity: 0.6;
}

.empty-state {
  text-align: center;
  color: var(--el-text-color-secondary);
  padding: 20px;
}
</style>
