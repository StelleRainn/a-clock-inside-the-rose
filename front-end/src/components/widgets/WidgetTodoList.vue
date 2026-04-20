<template>
  <el-card class="widget-card glass-card">
    <!-- 
    WidgetTodoList 核心组件
    定位：Dashboard 上的迷你任务列表，方便用户在专注前快速浏览和勾选近期任务。
    视觉：统一的毛玻璃卡片风格，提供直接跳转到完整任务页面的入口。
  -->
    <template #header>
      <div class="card-header">
        <span>{{ $t('widgets.todoList') }}</span>
        <el-button link @click="$router.push('/tasks')">{{ $t('widgets.viewAll') }}</el-button>
      </div>
    </template>
    <div class="task-list">
      <!-- 遍历计算属性 recentTasks，只展示未完成的 Top 5 任务 -->
      <div v-for="task in recentTasks" :key="task.id" class="task-item">
        <!-- 任务复选框，直接触发状态切换 -->
        <el-checkbox v-model="task.done" @change="toggleTask(task)" />
        <span class="task-title" :class="{ done: task.status === 'DONE' }">{{ task.title }}</span>
        <el-tag size="small" :type="getPriorityType(task.priority)">{{ task.priority }}</el-tag>
      </div>
      <!-- 空状态提示：引导用户创建任务，向上抛出 add-task 事件由 MainLayout 接管弹窗 -->
      <div v-if="recentTasks.length === 0" class="empty-state">
        {{ $t('widgets.noPendingTasks') }} <el-button link type="primary" @click="$emit('add-task')">{{ $t('widgets.addOne') }}</el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getTasks, updateTask } from '@/api/task'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const tasks = ref([])

// 衍生数据流：过滤掉已完成的任务，并且只截取前 5 条显示在 Dashboard 面板上
const recentTasks = computed(() => {
  return tasks.value
    .filter(t => t.status !== 'DONE')
    .slice(0, 5)
})

// 拉取当前用户的所有任务数据
const fetchData = async () => {
  if (!userStore.user?.id) return
  try {
    const res = await getTasks(userStore.user.id)
    tasks.value = res || []
  } catch (e) {
    console.error('Failed to fetch tasks', e)
  }
}

// 任务状态切换与乐观更新机制 (Optimistic Update)
const toggleTask = async (task) => {
  const newStatus = task.status === 'DONE' ? 'TODO' : 'DONE'
  // 1. 先在前端直接修改状态，让 UI 瞬间响应（复选框打勾、文字划线）
  task.status = newStatus
  
  try {
    // 2. 异步发起网络请求同步给后端
    await updateTask(task.id, { ...task, status: newStatus, userId: userStore.user.id })
    ElMessage.success('Task updated')
  } catch {
    // 3. 如果网络请求失败，将前端状态回滚，并给出错误提示
    task.status = newStatus === 'DONE' ? 'TODO' : 'DONE'
    ElMessage.error('Failed to update task')
  }
}

// 根据任务优先级映射 Element Plus 的标签颜色主题
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
