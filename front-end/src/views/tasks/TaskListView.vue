<template>
  <div class="task-list-container">
    <!-- 
    TaskListView: 任务列表的核心视图组件。
    支持 List View (带子任务展开) 和 Kanban View (拖拽状态流转) 两种模式的无缝切换。
  -->
    <el-card class="glass-card main-task-card">
      <!-- 头部：标题与新建按钮 -->
      <template #header>
        <div class="card-header">
          <span>{{ $t('tasks.title') }}</span>
          <el-button type="primary" @click="openCreateDialog">{{ $t('tasks.addTask') }}</el-button>
        </div>
      </template>

      <!-- Filter & Sort Bar: 提供强大的本地搜索、标签过滤与多维度排序功能 -->
      <div class="filter-bar">
        <el-input
          v-model="searchQuery"
          :placeholder="$t('tasks.searchPlaceholder')"
          prefix-icon="Search"
          style="width: 250px"
          clearable
        />
        
        <el-select 
          v-model="selectedFilterTags" 
          multiple 
          collapse-tags
          :placeholder="$t('tasks.filterByTags')" 
          style="width: 200px"
          clearable
        >
          <el-option
            v-for="tag in availableTags"
            :key="tag.id"
            :label="tag.name"
            :value="tag.id"
          >
            <span :style="{ color: tag.color }">●</span> {{ tag.name }}
          </el-option>
        </el-select>

        <el-select v-model="sortBy" :placeholder="$t('tasks.sortBy')" style="width: 150px">
          <el-option :label="$t('tasks.sortManual')" value="manual" />
          <el-option :label="$t('tasks.sortCreated')" value="createdAt" />
          <el-option :label="$t('tasks.sortDue')" value="dueDate" />
          <el-option :label="$t('tasks.sortPriority')" value="priority" />
          <el-option :label="$t('tasks.sortStatus')" value="status" />
        </el-select>

        <!-- 仅在非 manual 排序下允许切换升序/降序 -->
        <el-button @click="toggleSortOrder" :icon="sortOrder === 'asc' ? 'SortUp' : 'SortDown'" :disabled="sortBy === 'manual'">
          {{ sortOrder === 'asc' ? $t('tasks.asc') : $t('tasks.desc') }}
        </el-button>

        <el-button @click="resetFilters" icon="RefreshLeft" type="info" plain>{{ $t('tasks.reset') }}</el-button>
      </div>

      <!-- 视图切换 Tabs: List / Kanban -->
      <el-tabs v-model="activeName">
        <!-- 
          1. List View (列表视图) 
          支持分页、表格行内编辑、以及通过 row-click 触发展开子任务。
        -->
        <el-tab-pane :label="$t('tasks.listView')" name="list">
          <el-table 
            :key="tableKey"
            :data="paginatedTasks" 
            style="width: 100%" 
            v-loading="loading" 
            row-key="id"
            @row-click="handleEdit"
            class="draggable-table"
            :row-class-name="tableRowClassName"
          >
            <!-- 子任务展开列 (Expand Column) -->
            <el-table-column type="expand">
              <template #default="props">
                <div class="subtask-container">
                  <div class="subtask-header">
                    <h4>{{ $t('tasks.subtasks') }} ({{ props.row.subtasks ? props.row.subtasks.length : 0 }})</h4>
                    <!-- 阻止冒泡，防止点击时触发外层行的编辑事件 -->
                    <el-button size="small" type="primary" link @click.stop="openAddSubtask(props.row)">{{ $t('tasks.addSubtask') }}</el-button>
                  </div>
                  <div v-if="!props.row.subtasks || props.row.subtasks.length === 0" class="no-subtasks">
                    {{ $t('tasks.noSubtasks') }}
                  </div>
                  <ul v-else class="subtask-list">
                    <li v-for="sub in props.row.subtasks" :key="sub.id" class="subtask-item">
                      <!-- 状态联动：直接向后端发送更新 -->
                      <el-checkbox 
                        v-model="sub.completed" 
                        @change="(val) => handleSubtaskStatusChange(sub, val)"
                        @click.stop
                      >
                        <span :class="{ 'completed-text': sub.completed }">{{ sub.title }}</span>
                      </el-checkbox>
                      <el-button 
                        type="danger" 
                        icon="Delete" 
                        circle 
                        size="small" 
                        link 
                        @click.stop="handleDeleteSubtask(sub.id, props.row)"
                      />
                    </li>
                  </ul>
                </div>
              </template>
            </el-table-column>

            <!-- 标题 -->
            <el-table-column prop="title" :label="$t('tasks.taskName')" min-width="200">
              <template #default="scope">
                <div class="task-info-cell">
                  <!-- 过期/临期视觉高亮 -->
                  <span :class="{'overdue-text': isOverdue(scope.row), 'neardue-text': isNearDue(scope.row)}">
                    {{ scope.row.title }}
                  </span>
                  <el-tooltip v-if="isOverdue(scope.row)" :content="$t('tasks.overdue')" placement="top">
                    <el-icon class="alert-icon overdue"><Warning /></el-icon>
                  </el-tooltip>
                  <el-tooltip v-else-if="isNearDue(scope.row)" :content="$t('tasks.dueSoon')" placement="top">
                    <el-icon class="alert-icon neardue"><WarningFilled /></el-icon>
                  </el-tooltip>
                  
                  <!-- Mobile Meta Info (移动端降级展示：将标签和日期压缩在标题下方) -->
                  <div v-if="isMobile" class="mobile-meta">
                    <el-tag size="small" :type="getPriorityType(scope.row.priority)" effect="plain" class="mr-1">{{ $t(`tasks.${scope.row.priority.toLowerCase()}`) }}</el-tag>
                    <span v-if="scope.row.dueDate" class="mobile-date" :class="{'overdue-text': isOverdue(scope.row)}">
                      {{ formatDateShort(scope.row.dueDate) }}
                    </span>
                  </div>
                </div>
              </template>
            </el-table-column>
            
            <!-- 以下列在移动端 (max-width: 768px) 会被隐藏，避免表格挤压变形 -->
            <!-- 标签 -->
            <el-table-column :label="$t('tasks.tags')" width="200" v-if="!isMobile">
              <template #default="scope">
                <el-tag 
                  v-for="tag in scope.row.tags" 
                  :key="tag.id" 
                  :color="tag.color" 
                  effect="dark"
                  size="small"
                  class="mr-5"
                  style="border: none;"
                >
                  {{ tag.name }}
                </el-tag>
              </template>
            </el-table-column>

            <!-- 优先级 -->
            <el-table-column prop="priority" :label="$t('tasks.priority')" width="120" v-if="!isMobile">
              <template #default="scope">
                <el-tag :type="getPriorityType(scope.row.priority)">{{ $t(`tasks.${scope.row.priority.toLowerCase()}`) }}</el-tag>
              </template>
            </el-table-column>

            <!-- 状态 -->
            <el-table-column prop="status" :label="$t('tasks.status')" width="120" v-if="!isMobile">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>

            <!-- 截止日期 -->
            <el-table-column prop="dueDate" :label="$t('tasks.dueDate')" width="180" v-if="!isMobile">
              <template #default="scope">
                <span :class="{'overdue-text': isOverdue(scope.row), 'neardue-text': isNearDue(scope.row)}">
                  {{ formatDate(scope.row.dueDate) }}
                </span>
              </template>
            </el-table-column>
            
            <!-- 操作 -->
            <el-table-column :label="$t('tasks.actions')" :width="isMobile ? 100 : 160" fixed="right" align="center">
              <template #default="scope">
                <div class="action-buttons">
                  <div class="custom-action-btn edit-btn" @click.stop="handleEdit(scope.row)">
                    <el-icon><EditPen /></el-icon>
                  </div>
                  <div class="custom-action-btn delete-btn" @click.stop="handleDelete(scope.row)">
                    <el-icon><Delete /></el-icon>
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <!-- Pagination 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="filteredTasks.length"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-tab-pane>

        <!-- 
          2. Kanban View (看板视图)
          利用 vuedraggable 实现的经典三列任务流转板。
        -->
        <el-tab-pane :label="$t('tasks.kanbanView')" name="kanban">
          <div class="kanban-board">
            <div class="kanban-column" v-for="status in ['TODO', 'IN_PROGRESS', 'DONE']" :key="status">
              <div class="column-header">
                <h3>{{ formatStatus(status) }}</h3>
                <el-tag :type="getStatusType(status)">{{ kanbanColumns[status].length }}</el-tag>
              </div>
              <!-- draggable: 当任务在列与列之间拖拽时，会触发 @change 并调用 handleKanbanChange 同步状态到后端 -->
              <draggable 
                :list="kanbanColumns[status]" 
                group="tasks" 
                item-key="id"
                @change="(evt) => handleKanbanChange(evt, status)"
                class="draggable-area"
                :disabled="isDragDisabled"
              >
                <template #item="{ element }">
                  <!-- 任务标题 -->
                  <div class="kanban-card" @click="handleEdit(element)" :class="{'overdue-card': isOverdue(element), 'neardue-card': isNearDue(element)}">
                    <div class="card-title">
                      {{ element.title }}
                      <el-icon v-if="isOverdue(element)" class="alert-icon overdue-kanban"><Warning /></el-icon>
                      <el-icon v-else-if="isNearDue(element)" class="alert-icon neardue-kanban"><WarningFilled /></el-icon>
                    </div>
                    
                    <!-- Subtask Progress in Kanban: 以微型进度条形式直观展示子任务完成度 -->
                    <div class="subtask-progress" v-if="element.subtasks && element.subtasks.length > 0">
                      <el-progress 
                        :percentage="calculateProgress(element.subtasks)" 
                        :show-text="false" 
                        :stroke-width="4"
                        :color="calculateProgress(element.subtasks) === 100 ? '#67c23a' : '#409eff'"
                      />
                      <span class="subtask-count">
                        {{ element.subtasks.filter(s => s.completed).length }}/{{ element.subtasks.length }}
                      </span>
                    </div>

                    <!-- 标签 -->
                    <div class="card-tags" v-if="element.tags && element.tags.length">
                      <el-tag 
                        v-for="tag in element.tags" 
                        :key="tag.id" 
                        :color="tag.color" 
                        effect="dark"
                        size="small"
                        class="mr-5"
                        style="border: none; font-size: 10px; height: 18px; padding: 0 5px;"
                      >
                        {{ tag.name }}
                      </el-tag>
                    </div>
                    <!-- 优先级 -->
                    <div class="card-meta">
                      <el-tag size="small" :type="getPriorityType(element.priority)">{{ $t(`tasks.${element.priority.toLowerCase()}`) }}</el-tag>
                      <span class="card-date" v-if="element.dueDate" :class="{'overdue-text': isOverdue(element), 'neardue-text': isNearDue(element)}">
                        {{ formatDateShort(element.dueDate) }}
                      </span>
                    </div>
                  </div>
                </template>
              </draggable>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Task Dialog (新建/编辑任务表单弹窗，逻辑类似前面分析的全局 TaskFormDialog) -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form :model="taskForm" label-width="100px">
        <el-form-item :label="$t('tasks.titleLabel')">
          <el-input v-model="taskForm.title" />
        </el-form-item>
        <el-form-item :label="$t('tasks.descLabel')">
          <el-input v-model="taskForm.description" type="textarea" />
        </el-form-item>
        
        <el-form-item :label="$t('tasks.tags')">
          <el-select
            v-model="selectedTagIds"
            multiple
            filterable
            allow-create
            default-first-option
            :placeholder="$t('tasks.selectOrCreateTags')"
            @change="handleTagChange"
          >
            <el-option
              v-for="tag in availableTags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            >
              <span :style="{ color: tag.color }">●</span> {{ tag.name }}
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('tasks.priorityLabel')">
          <el-select v-model="taskForm.priority">
            <el-option :label="$t('tasks.low')" value="LOW" />
            <el-option :label="$t('tasks.medium')" value="MEDIUM" />
            <el-option :label="$t('tasks.high')" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('tasks.statusLabel')">
          <el-select v-model="taskForm.status">
            <el-option :label="$t('tasks.todo')" value="TODO" />
            <el-option :label="$t('tasks.inProgress')" value="IN_PROGRESS" />
            <el-option :label="$t('tasks.done')" value="DONE" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('tasks.dueDateLabel')">
          <el-date-picker
            v-model="taskForm.dueDate"
            type="datetime"
            :placeholder="$t('tasks.selectDateTime')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ $t('tasks.cancel') }}</el-button>
          <el-button type="primary" @click="submitTask">{{ $t('tasks.confirm') }}</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Subtask Add Dialog (添加子任务的弹窗) -->
    <el-dialog v-model="subtaskDialogVisible" :title="$t('tasks.addSubtask')" width="30%">
      <el-input v-model="newSubtaskTitle" :placeholder="$t('tasks.subtaskTitlePlaceholder')" @keyup.enter="submitSubtask" />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="subtaskDialogVisible = false">{{ $t('tasks.cancel') }}</el-button>
          <el-button type="primary" @click="submitSubtask">{{ $t('tasks.add') }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, reactive, computed, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getTasks, createTask, updateTask, deleteTask, reorderTasks } from '@/api/task'
import { getTags, createTag } from '@/api/tag'
import { createSubtask, updateSubtask, deleteSubtask } from '@/api/subtask'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import draggable from 'vuedraggable'
import Sortable from 'sortablejs'
import { Warning, WarningFilled } from '@element-plus/icons-vue'

const router = useRouter()
const { t } = useI18n()
const activeName = ref('list')
const tableData = ref([])
const loading = ref(false)
const userStore = useUserStore()
// 响应式降级标志：宽度小于 768px 时，隐藏部分表格列
const isMobile = ref(window.innerWidth < 768)

const handleResize = () => {
  isMobile.value = window.innerWidth < 768
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  fetchTasks()
  fetchTags()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

const dialogTitle = ref(t('tasks.createTask'))
const dialogVisible = ref(false)
const isEditMode = ref(false)
const currentTaskId = ref(null)

// Subtask state
const subtaskDialogVisible = ref(false)
const newSubtaskTitle = ref('')
const currentParentTask = ref(null)

// Tags
const availableTags = ref([])
const selectedTagIds = ref([])

// Filters & Sort (过滤与排序的响应式状态)
const searchQuery = ref('')
const selectedFilterTags = ref([])
const sortBy = ref('manual') // 默认使用 manual (手动拖拽排序)
const sortOrder = ref('asc') // asc (升序) or desc (降序)
const tableKey = ref(0) // 强制重新渲染 ElTable 的 Key 技巧

const taskForm = reactive({
  title: '',
  description: '',
  priority: 'MEDIUM',
  status: 'TODO',
  dueDate: ''
})

// ==========================================
// 1. 数据拉取层 (Data Fetching)
// ==========================================
const fetchTasks = async () => {
  if (!userStore.user || !userStore.user.id) return
  loading.value = true
  try {
    const data = await getTasks(userStore.user.id)
    tableData.value = data || []
  } catch {
    ElMessage.error('Failed to fetch tasks')
  } finally {
    loading.value = false
  }
}

const fetchTags = async () => {
  if (!userStore.user || !userStore.user.id) return
  try {
    const data = await getTags(userStore.user.id)
    availableTags.value = data || []
  } catch (error) {
    console.error('Failed to fetch tags', error)
  }
}

// ==========================================
// 2. 子任务逻辑层 (Subtask Logic)
// 包含独立的增删改逻辑，更新后直接操作前端内存树，避免刷新整个列表
// ==========================================
const openAddSubtask = (task) => {
  currentParentTask.value = task
  newSubtaskTitle.value = ''
  subtaskDialogVisible.value = true
}

const submitSubtask = async () => {
  if (!newSubtaskTitle.value.trim()) return
  try {
    const res = await createSubtask({
      taskId: currentParentTask.value.id,
      title: newSubtaskTitle.value,
      completed: false
    })
    // 乐观更新：直接推入当前父任务的 subtasks 数组中
    if (!currentParentTask.value.subtasks) {
      currentParentTask.value.subtasks = []
    }
    currentParentTask.value.subtasks.push(res)
    ElMessage.success('Subtask added')
    subtaskDialogVisible.value = false
  } catch {
    ElMessage.error('Failed to add subtask')
  }
}

// 子任务状态变更（打勾/取消打勾）
const handleSubtaskStatusChange = async (sub, val) => {
  try {
    await updateSubtask(sub.id, { ...sub, completed: val })
  } catch {
    ElMessage.error('Failed to update subtask')
    sub.completed = !val // 请求失败时，将前端 Checkbox 状态回滚
  }
}

const handleDeleteSubtask = async (subId, parentTask) => {
  try {
    await deleteSubtask(subId)
    // 乐观更新：从父任务数组中滤除该子任务
    parentTask.subtasks = parentTask.subtasks.filter(s => s.id !== subId)
    ElMessage.success('Subtask deleted')
  } catch {
    ElMessage.error('Failed to delete subtask')
  }
}

// 计算子任务进度百分比 (用于看板视图展示)
const calculateProgress = (subtasks) => {
  if (!subtasks || subtasks.length === 0) return 0
  const completed = subtasks.filter(s => s.completed).length
  return Math.round((completed / subtasks.length) * 100)
}

// ==========================================
// 3. 日期警告计算逻辑 (Date Alerts)
// ==========================================
const isOverdue = (task) => {
  if (task.status === 'DONE' || !task.dueDate) return false
  return new Date(task.dueDate) < new Date() // 已过 deadline
}

const isNearDue = (task) => {
  if (task.status === 'DONE' || !task.dueDate) return false
  const due = new Date(task.dueDate)
  const now = new Date()
  const diffHours = (due - now) / (1000 * 60 * 60)
  return diffHours > 0 && diffHours <= 24 // 距离 deadline 仅剩 24 小时以内
}

const currentPage = ref(1)
const pageSize = ref(10)

// ==========================================
// 4. 核心过滤与排序管道 (Computed Pipeline)
// 依次执行：源数据 -> 标题过滤 -> 标签过滤 -> 状态分离 -> 排序 -> 合并
// ==========================================
const filteredTasks = computed(() => {
  let result = [...tableData.value]

  // 1. 过滤：按标题搜索
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(task => task.title.toLowerCase().includes(query))
  }

  // 2. 过滤：按选中的标签多选过滤 (交集逻辑)
  if (selectedFilterTags.value.length > 0) {
    result = result.filter(task => {
      if (!task.tags) return false
      const taskTagIds = task.tags.map(t => t.id)
      return selectedFilterTags.value.some(id => taskTagIds.includes(id))
    })
  }

  // 3. 状态分离：无论如何排序，已完成(DONE)的任务始终被垫底
  const notDone = result.filter(t => t.status !== 'DONE')
  const done = result.filter(t => t.status === 'DONE')

  // 4. 排序逻辑：执行未完成任务的排序
  if (sortBy.value === 'manual') {
    // manual 模式下保持后端返回的原始顺序（基于 position 字段）
  } else {
    sortTasks(notDone)
  }

  // 已完成的任务固定按完成时间（或截止时间）倒序排列
  done.sort((a, b) => {
    const dateA = a.dueDate ? new Date(a.dueDate).getTime() : 0
    const dateB = b.dueDate ? new Date(b.dueDate).getTime() : 0
    return dateB - dateA // Descending
  })

  // 5. 合并并返回最终视图数组
  return [...notDone, ...done]
})

// 自定义排序器
const sortTasks = (list) => {
  list.sort((a, b) => {
    let valA, valB
    
    switch (sortBy.value) {
      case 'priority': {
        const pMap = { 'HIGH': 3, 'MEDIUM': 2, 'LOW': 1 }
        valA = pMap[a.priority] || 0
        valB = pMap[b.priority] || 0
        break
      }
      case 'status': {
        const sMap = { 'TODO': 1, 'IN_PROGRESS': 2, 'DONE': 3 }
        valA = sMap[a.status] || 0
        valB = sMap[b.status] || 0
        break
      }
      case 'dueDate':
        // 空日期永远排在最后
        valA = a.dueDate ? new Date(a.dueDate).getTime() : (sortOrder.value === 'asc' ? Infinity : -Infinity)
        valB = b.dueDate ? new Date(b.dueDate).getTime() : (sortOrder.value === 'asc' ? Infinity : -Infinity)
        break
      case 'createdAt':
      default:
        valA = a.createdAt ? new Date(a.createdAt).getTime() : 0
        valB = b.createdAt ? new Date(b.createdAt).getTime() : 0
    }

    if (valA < valB) return sortOrder.value === 'asc' ? -1 : 1
    if (valA > valB) return sortOrder.value === 'asc' ? 1 : -1
    return 0
  })
}

// 5. 分页截取
const paginatedTasks = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredTasks.value.slice(start, end)
})

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1 // Reset to first page
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

const tableRowClassName = ({ row }) => {
  return row.status === 'DONE' ? 'done-row' : ''
}


const toggleSortOrder = () => {
  sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
}

// 重置筛选
const resetFilters = () => {
  searchQuery.value = ''
  selectedFilterTags.value = []
  sortBy.value = 'manual'
  sortOrder.value = 'asc'
}

// ==========================================
// 6. List View 的原生拖拽排序 (Sortable.js)
// 只有在 manual 排序且没有应用任何过滤器时才允许拖拽，否则数据大乱
// ==========================================
let sortableInstance = null

const initSortable = () => {
  const table = document.querySelector('.draggable-table .el-table__body-wrapper tbody')
  if (!table) return

  // 销毁旧实例，防止内存泄漏或重复绑定
  if (sortableInstance) {
    sortableInstance.destroy()
    sortableInstance = null
  }

  // 严格拦截：如果正在使用高级排序或搜索过滤，禁止拖拽
  if (sortBy.value !== 'manual' || searchQuery.value || selectedFilterTags.value.length > 0) {
    return
  }

  sortableInstance = Sortable.create(table, {
    handle: '.el-table__row', // 允许拖拽整行
    filter: '.done-row', // 已完成的任务行禁止拖拽
    animation: 150,
    onMove: (evt) => {
      // 阻止将未完成任务拖拽到已完成区域
      if (evt.related.classList.contains('done-row')) {
        return false
      }
      return true
    },
    onEnd: async ({ newIndex, oldIndex }) => {
      if (newIndex === oldIndex) return
      
      const currentList = [...paginatedTasks.value]
      
      // 越界检查防御
      const firstDoneIndex = currentList.findIndex(t => t.status === 'DONE')
      if (firstDoneIndex !== -1 && newIndex >= firstDoneIndex) {
        ElMessage.warning('Cannot drag tasks into Done section')
        tableKey.value++ // 通过更新 key 强制 Vue 重新渲染表格，还原 DOM 拖拽造成的破坏
        return
      }
      
      // 前端内存数组元素置换
      const movedItem = currentList.splice(oldIndex, 1)[0]
      currentList.splice(newIndex, 0, movedItem)

      // 将当前页的顺序映射回全局完整数组中
      const allTasks = [...filteredTasks.value]
      const start = (currentPage.value - 1) * pageSize.value
      allTasks.splice(start, pageSize.value, ...currentList)

      // 提取最新的全局 ID 顺序数组，准备发给后端
      const newOrderIds = allTasks.map(t => t.id)
      
      // 乐观更新：立刻让 UI 生效
      tableData.value = allTasks
      tableKey.value++
      
      try {
        // 调用后端 reorder 接口，执行真正的数据库 Position 更新
        await reorderTasks(newOrderIds)
      } catch {
        ElMessage.error('Failed to update order')
        fetchTasks() // 如果失败，重新拉取后端真实数据回滚 UI
      }
    }
  })
}

// 监听各种状态，在视图切换回 List 或取消过滤时重新初始化拖拽引擎
watch([sortBy, searchQuery, selectedFilterTags, activeName, loading, tableKey], () => {
  nextTick(() => {
    if (activeName.value === 'list' && !loading.value) {
      initSortable()
    } else if (sortableInstance) {
      sortableInstance.destroy()
      sortableInstance = null
    }
  })
})

// ==========================================
// 7. Kanban View 逻辑 (看板模式)
// ==========================================
const kanbanColumns = reactive({
  'TODO': [],
  'IN_PROGRESS': [],
  'DONE': []
})

// 保持过滤后的任务数组与看板三列数据的实时同步
watch(filteredTasks, (tasks) => {
  const groups = {
    'TODO': [],
    'IN_PROGRESS': [],
    'DONE': []
  }
  
  tasks.forEach(t => {
    if (groups[t.status]) groups[t.status].push(t)
  })
  
  // 对看板内的每一列进行二次排序 (位置 > 截止日期 > 优先级)
  Object.keys(groups).forEach(status => {
    groups[status].sort((a, b) => {
      // 1. Position (Asc)
      if (a.position !== b.position) return a.position - b.position
      
      // 2. Due Date (Asc, null last)
      const dateA = a.dueDate ? new Date(a.dueDate).getTime() : Infinity
      const dateB = b.dueDate ? new Date(b.dueDate).getTime() : Infinity
      if (dateA !== dateB) return dateA - dateB
      
      // 3. Priority (High to Low)
      const pMap = { 'HIGH': 3, 'MEDIUM': 2, 'LOW': 1 }
      return (pMap[b.priority] || 0) - (pMap[a.priority] || 0)
    })
  })
  
  kanbanColumns['TODO'] = groups['TODO']
  kanbanColumns['IN_PROGRESS'] = groups['IN_PROGRESS']
  kanbanColumns['DONE'] = groups['DONE']
}, { deep: true, immediate: true })

// 看板拖拽事件核心处理：列内排序 (Reorder) 与跨列移动 (Change Status)
const handleKanbanChange = async (evt, status) => {
  // 场景 A: 同一列内的上下拖拽改变顺序
  if (evt.moved) {
    const newOrderIds = kanbanColumns[status].map(t => t.id)
    try {
      await reorderTasks(newOrderIds)
    } catch {
      ElMessage.error('Failed to update order')
    }
  }
  
  // 场景 B: 跨列拖拽 (比如从 TODO 拖入 IN_PROGRESS)
  if (evt.added) {
    const task = evt.added.element
    
    // 1. 乐观更新：立刻修改内存中该任务的状态为目标列状态
    task.status = status
    
    // 2. 提取目标列全新的 ID 顺序数组
    const newOrderIds = kanbanColumns[status].map(t => t.id)
    
    try {
      // 并发或顺序执行：先更新状态，再更新目标列的顺序
      await updateTask(task.id, { ...task, status: status, userId: userStore.user.id })
      await reorderTasks(newOrderIds)
    } catch {
      ElMessage.error('Failed to update task')
      fetchTasks() // 回滚
    }
  }
}

// 辅助方法：标签动态创建、表单重置等 (代码略...)
const handleTagChange = async (val) => {
  // Check if new tag created
  const newTagNames = val.filter(v => typeof v === 'string')
  if (newTagNames.length > 0) {
    for (const name of newTagNames) {
      try {
        const newTag = await createTag({
          userId: userStore.user.id,
          name: name,
          color: getRandomColor()
        })
        availableTags.value.push(newTag)
        // Replace string ID with real ID in selection
        const index = selectedTagIds.value.indexOf(name)
        if (index !== -1) {
          selectedTagIds.value[index] = newTag.id
        }
      } catch {
        ElMessage.error('Failed to create tag: ' + name)
      }
    }
  }
}

const getRandomColor = () => {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#a0cfff', '#b3e19d']
  return colors[Math.floor(Math.random() * colors.length)]
}

const openCreateDialog = () => {
  if (!userStore.user) {
    ElMessage.warning('Please login first')
    router.push('/login')
    return
  }
  isEditMode.value = false
  dialogTitle.value = t('tasks.createTask')
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEditMode.value = true
  dialogTitle.value = t('tasks.editTask')
  currentTaskId.value = row.id
  taskForm.title = row.title
  taskForm.description = row.description
  taskForm.priority = row.priority
  taskForm.status = row.status
  taskForm.dueDate = row.dueDate
  // Set selected tags
  selectedTagIds.value = row.tags ? row.tags.map(t => t.id) : []
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    t('tasks.deleteConfirmMsg'),
    t('tasks.deleteConfirmTitle'),
    {
      confirmButtonText: t('tasks.ok'),
      cancelButtonText: t('tasks.cancel'),
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await deleteTask(row.id)
        ElMessage.success(t('tasks.deleteSuccess'))
        fetchTasks()
      } catch {
        ElMessage.error(t('tasks.deleteFailed'))
      }
    })
    .catch(() => {})
}

const submitTask = async () => {
  if (!userStore.user || !userStore.user.id) {
    ElMessage.error('User not logged in')
    router.push('/login')
    return
  }

  if (!taskForm.title || !taskForm.title.trim()) {
    ElMessage.warning(t('tasks.titleRequired'))
    return
  }
  
  // Construct payload with tags
  const tagsPayload = selectedTagIds.value.map(id => ({ id }))
  
  // Handle timezone offset issue for dueDate
  let formattedDueDate = taskForm.dueDate
  if (formattedDueDate instanceof Date) {
    const tzOffset = formattedDueDate.getTimezoneOffset() * 60000;
    formattedDueDate = new Date(formattedDueDate.getTime() - tzOffset).toISOString().slice(0, 19);
  }
  
  const payload = {
    ...taskForm,
    dueDate: formattedDueDate,
    userId: userStore.user.id,
    tags: tagsPayload
  }

  try {
    if (isEditMode.value) {
      await updateTask(currentTaskId.value, payload)
      ElMessage.success(t('tasks.taskUpdated'))
    } else {
      await createTask(payload)
      ElMessage.success(t('tasks.taskCreated'))
    }
    dialogVisible.value = false
    fetchTasks()
  } catch {
    ElMessage.error(t('tasks.operationFailed'))
  }
}

const resetForm = () => {
  taskForm.title = ''
  taskForm.description = ''
  taskForm.priority = 'MEDIUM'
  taskForm.status = 'TODO'
  taskForm.dueDate = ''
  selectedTagIds.value = []
}

const getPriorityType = (priority) => {
  const map = {
    'HIGH': 'danger',
    'MEDIUM': 'warning',
    'LOW': 'info'
  }
  return map[priority] || ''
}

const getStatusType = (status) => {
  const map = {
    'DONE': 'success',
    'IN_PROGRESS': 'primary',
    'TODO': 'info'
  }
  return map[status] || ''
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

const formatDateShort = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth()+1}/${date.getDate()}`
}

const formatStatus = (status) => {
  const map = {
    'TODO': t('tasks.todo'),
    'IN_PROGRESS': t('tasks.inProgress'),
    'DONE': t('tasks.done')
  }
  return map[status] || status
}

const isDragDisabled = computed(() => {
  return searchQuery.value || selectedFilterTags.value.length > 0
})

// Old helpers removal (getTasksByStatus, handleDragChange)
// ... (replaced by above)

onMounted(() => {
  fetchTasks()
  fetchTags()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.filter-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  align-items: center;
}
.kanban-board {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  padding-bottom: 20px;
  min-height: 400px;
}
.kanban-column {
  flex: 1;
  min-width: 250px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  padding: 15px;
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 220px); /* Limit height */
  box-shadow: inset 0 2px 4px rgba(255, 255, 255, 0.5), 0 4px 12px rgba(0, 0, 0, 0.05);
}

html.dark .kanban-column {
  background: rgba(30, 30, 30, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.05);
  box-shadow: inset 0 2px 4px rgba(255, 255, 255, 0.05), 0 4px 12px rgba(0, 0, 0, 0.2);
}

.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 0 5px;
}
.column-header h3 {
  margin: 0;
  font-size: 16px;
  color: var(--el-text-color-primary);
  font-weight: 600;
}
.draggable-area {
  flex: 1;
  min-height: 100px;
  overflow-y: auto; /* Enable scrolling */
  padding-right: 5px; /* Space for scrollbar */
  padding-top: 10px; /* Ensure top margin for the first card */
}

/* Custom Scrollbar for draggable area */
.draggable-area::-webkit-scrollbar {
  width: 6px;
}
.draggable-area::-webkit-scrollbar-thumb {
  background-color: var(--el-border-color);
  border-radius: 3px;
}
.draggable-area::-webkit-scrollbar-track {
  background-color: transparent;
}

.kanban-card {
  background-color: var(--el-bg-color);
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

html.dark .kanban-card {
  border: 1px solid rgba(255, 255, 255, 0.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.kanban-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

html.dark .kanban-card:hover {
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.3);
}

.card-title {
  font-weight: 500;
  margin-bottom: 8px;
  color: var(--el-text-color-primary);
  display: flex;
  align-items: center;
  gap: 5px;
}
.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-date {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.card-tags {
  margin-bottom: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.mr-5 {
  margin-right: 5px;
}

/* Subtask Styles */
.subtask-container {
  padding: 10px 20px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
}
.subtask-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.subtask-header h4 {
  margin: 0;
  font-size: 14px;
  color: var(--el-text-color-regular);
}
.subtask-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.subtask-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 5px 0;
  border-bottom: 1px dashed var(--el-border-color-lighter);
}
.subtask-item:last-child {
  border-bottom: none;
}
.completed-text {
  text-decoration: line-through;
  color: var(--el-text-color-placeholder);
}
.no-subtasks {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-style: italic;
}

/* Kanban Subtask Progress */
.subtask-progress {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.subtask-progress .el-progress {
  flex: 1;
}
.subtask-count {
  font-size: 10px;
  color: var(--el-text-color-secondary);
}

/* Alert Styles */
.overdue-text {
  color: #f56c6c;
  font-weight: bold;
}
.neardue-text {
  color: #e6a23c;
  font-weight: bold;
}
.alert-icon {
  margin-left: 5px;
  vertical-align: middle;
}
.alert-icon.overdue {
  color: #f56c6c;
}
.alert-icon.neardue {
  color: #e6a23c;
}
/* Kanban Card Alert Borders */
.kanban-card.overdue-card {
  border-left: 3px solid #f56c6c;
}
.kanban-card.neardue-card {
  border-left: 3px solid #e6a23c;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* Done Row Style for Drag Disable */
:deep(.el-table .done-row) {
  opacity: 0.7;
  background-color: var(--el-fill-color-lighter);
}

/* Mobile Task List Meta */
.task-info-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.mobile-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  font-size: 12px;
}

.mobile-date {
  color: var(--el-text-color-secondary);
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.custom-action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
  background: transparent;
  font-size: 16px;
}

.custom-action-btn.edit-btn {
  border: 1px solid var(--el-color-primary-light-5);
  color: var(--el-color-primary);
}

.custom-action-btn.edit-btn:hover {
  background: var(--el-color-primary-light-9);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.custom-action-btn.delete-btn {
  border: 1px solid var(--el-color-danger-light-5);
  color: var(--el-color-danger);
}

.custom-action-btn.delete-btn:hover {
  background: var(--el-color-danger-light-9);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.2);
}

/* Dark mode overrides for action buttons */
html.dark .custom-action-btn.edit-btn {
  border-color: rgba(64, 158, 255, 0.4);
}
html.dark .custom-action-btn.edit-btn:hover {
  background: rgba(64, 158, 255, 0.1);
}
html.dark .custom-action-btn.delete-btn {
  border-color: rgba(245, 108, 108, 0.4);
}
html.dark .custom-action-btn.delete-btn:hover {
  background: rgba(245, 108, 108, 0.1);
}

.mr-1 {
  margin-right: 4px;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  /* Filter Bar */
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .filter-bar .el-input,
  .filter-bar .el-select,
  .filter-bar .el-button {
    width: 100% !important;
  }
  
  /* Kanban View */
  .kanban-board {
    padding-bottom: 10px;
    scroll-snap-type: x mandatory;
  }
  
  .kanban-column {
    min-width: 85vw; /* Almost full width */
    scroll-snap-align: center;
    margin-right: 10px;
  }
  
  .kanban-column:last-child {
    margin-right: 0;
  }
}
</style>
