<template>
  <div class="task-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ $t('tasks.title') }}</span>
          <el-button type="primary" @click="openCreateDialog">{{ $t('tasks.addTask') }}</el-button>
        </div>
      </template>

      <!-- Filter & Sort Bar -->
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

        <el-button @click="toggleSortOrder" :icon="sortOrder === 'asc' ? 'SortUp' : 'SortDown'" :disabled="sortBy === 'manual'">
          {{ sortOrder === 'asc' ? $t('tasks.asc') : $t('tasks.desc') }}
        </el-button>

        <el-button @click="resetFilters" icon="RefreshLeft" type="info" plain>{{ $t('tasks.reset') }}</el-button>
      </div>

      <el-tabs v-model="activeName">
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
            <el-table-column type="expand">
              <template #default="props">
                <div class="subtask-container">
                  <div class="subtask-header">
                    <h4>{{ $t('tasks.subtasks') }} ({{ props.row.subtasks ? props.row.subtasks.length : 0 }})</h4>
                    <el-button size="small" type="primary" link @click="openAddSubtask(props.row)">{{ $t('tasks.addSubtask') }}</el-button>
                  </div>
                  <div v-if="!props.row.subtasks || props.row.subtasks.length === 0" class="no-subtasks">
                    {{ $t('tasks.noSubtasks') }}
                  </div>
                  <ul v-else class="subtask-list">
                    <li v-for="sub in props.row.subtasks" :key="sub.id" class="subtask-item">
                      <el-checkbox 
                        v-model="sub.completed" 
                        @change="(val) => handleSubtaskStatusChange(sub, val)"
                      >
                        <span :class="{ 'completed-text': sub.completed }">{{ sub.title }}</span>
                      </el-checkbox>
                      <el-button 
                        type="danger" 
                        icon="Delete" 
                        circle 
                        size="small" 
                        link 
                        @click="handleDeleteSubtask(sub.id, props.row)"
                      />
                    </li>
                  </ul>
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="title" :label="$t('tasks.taskName')" min-width="200">
              <template #default="scope">
                <div class="task-info-cell">
                  <span :class="{'overdue-text': isOverdue(scope.row), 'neardue-text': isNearDue(scope.row)}">
                    {{ scope.row.title }}
                  </span>
                  <el-tooltip v-if="isOverdue(scope.row)" :content="$t('tasks.overdue')" placement="top">
                    <el-icon class="alert-icon overdue"><Warning /></el-icon>
                  </el-tooltip>
                  <el-tooltip v-else-if="isNearDue(scope.row)" :content="$t('tasks.dueSoon')" placement="top">
                    <el-icon class="alert-icon neardue"><WarningFilled /></el-icon>
                  </el-tooltip>
                  
                  <!-- Mobile Meta Info -->
                  <div v-if="isMobile" class="mobile-meta">
                    <el-tag size="small" :type="getPriorityType(scope.row.priority)" effect="plain" class="mr-1">{{ $t(`tasks.${scope.row.priority.toLowerCase()}`) }}</el-tag>
                    <span v-if="scope.row.dueDate" class="mobile-date" :class="{'overdue-text': isOverdue(scope.row)}">
                      {{ formatDateShort(scope.row.dueDate) }}
                    </span>
                  </div>
                </div>
              </template>
            </el-table-column>
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
            <el-table-column prop="priority" :label="$t('tasks.priority')" width="120" v-if="!isMobile">
              <template #default="scope">
                <el-tag :type="getPriorityType(scope.row.priority)">{{ $t(`tasks.${scope.row.priority.toLowerCase()}`) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" :label="$t('tasks.status')" width="120" v-if="!isMobile">
               <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="dueDate" :label="$t('tasks.dueDate')" width="180" v-if="!isMobile">
               <template #default="scope">
                 <span :class="{'overdue-text': isOverdue(scope.row), 'neardue-text': isNearDue(scope.row)}">
                   {{ formatDate(scope.row.dueDate) }}
                 </span>
               </template>
            </el-table-column>
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

          <!-- Pagination -->
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
        <el-tab-pane :label="$t('tasks.kanbanView')" name="kanban">
          <div class="kanban-board">
            <div class="kanban-column" v-for="status in ['TODO', 'IN_PROGRESS', 'DONE']" :key="status">
              <div class="column-header">
                <h3>{{ formatStatus(status) }}</h3>
                <el-tag :type="getStatusType(status)">{{ kanbanColumns[status].length }}</el-tag>
              </div>
              <draggable 
                :list="kanbanColumns[status]" 
                group="tasks" 
                item-key="id"
                @change="(evt) => handleKanbanChange(evt, status)"
                class="draggable-area"
                :disabled="isDragDisabled"
              >
                <template #item="{ element }">
                  <div class="kanban-card" @click="handleEdit(element)" :class="{'overdue-card': isOverdue(element), 'neardue-card': isNearDue(element)}">
                    <div class="card-title">
                      {{ element.title }}
                      <el-icon v-if="isOverdue(element)" class="alert-icon overdue-kanban"><Warning /></el-icon>
                      <el-icon v-else-if="isNearDue(element)" class="alert-icon neardue-kanban"><WarningFilled /></el-icon>
                    </div>
                    
                    <!-- Subtask Progress in Kanban -->
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

    <!-- Task Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form :model="taskForm" label-width="100px">
        <el-form-item :label="$t('tasks.titleLabel')">
          <el-input v-model="taskForm.title" />
        </el-form-item>
        <el-form-item :label="$t('tasks.descLabel')">
          <el-input v-model="taskForm.description" type="textarea" />
        </el-form-item>
        
        <!-- Tag Selection -->
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

    <!-- Subtask Add Dialog -->
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

// Filters & Sort
const searchQuery = ref('')
const selectedFilterTags = ref([])
const sortBy = ref('manual') // Default manual sort
const sortOrder = ref('asc') // asc or desc
const tableKey = ref(0) // Key to force re-render table

const taskForm = reactive({
  title: '',
  description: '',
  priority: 'MEDIUM',
  status: 'TODO',
  dueDate: ''
})

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

// Subtask Logic
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
    // Update local state
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

const handleSubtaskStatusChange = async (sub, val) => {
  try {
    await updateSubtask(sub.id, { ...sub, completed: val })
  } catch {
    ElMessage.error('Failed to update subtask')
    sub.completed = !val // Revert on error
  }
}

const handleDeleteSubtask = async (subId, parentTask) => {
  try {
    await deleteSubtask(subId)
    parentTask.subtasks = parentTask.subtasks.filter(s => s.id !== subId)
    ElMessage.success('Subtask deleted')
  } catch {
    ElMessage.error('Failed to delete subtask')
  }
}

const calculateProgress = (subtasks) => {
  if (!subtasks || subtasks.length === 0) return 0
  const completed = subtasks.filter(s => s.completed).length
  return Math.round((completed / subtasks.length) * 100)
}

// Due Date Alerts
const isOverdue = (task) => {
  if (task.status === 'DONE' || !task.dueDate) return false
  return new Date(task.dueDate) < new Date()
}

const isNearDue = (task) => {
  if (task.status === 'DONE' || !task.dueDate) return false
  const due = new Date(task.dueDate)
  const now = new Date()
  const diffHours = (due - now) / (1000 * 60 * 60)
  return diffHours > 0 && diffHours <= 24 // Within 24 hours
}

const currentPage = ref(1)
const pageSize = ref(10)

// Computed Filtered Tasks
const filteredTasks = computed(() => {
  let result = [...tableData.value]

  // 1. Filter by Title
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(task => task.title.toLowerCase().includes(query))
  }

  // 2. Filter by Tags
  if (selectedFilterTags.value.length > 0) {
    result = result.filter(task => {
      if (!task.tags) return false
      const taskTagIds = task.tags.map(t => t.id)
      return selectedFilterTags.value.some(id => taskTagIds.includes(id))
    })
  }

  // 3. Sort
  // Separate Done and Not Done
  const notDone = result.filter(t => t.status !== 'DONE')
  const done = result.filter(t => t.status === 'DONE')

  // Sort Not Done
  if (sortBy.value === 'manual') {
    // Keep backend order (position)
    // But we need to ensure they are sorted by position just in case
    // Assuming backend returns sorted by position
  } else {
    sortTasks(notDone)
  }

  // Sort Done (Always by Date desc or user pref?)
  // User req: "In Done tasks, sort by date"
  // Let's sort done tasks by Due Date descending (most recent due first) or Created At
  done.sort((a, b) => {
    const dateA = a.dueDate ? new Date(a.dueDate).getTime() : 0
    const dateB = b.dueDate ? new Date(b.dueDate).getTime() : 0
    return dateB - dateA // Descending
  })

  // Merge: Not Done + Done (Done at bottom)
  return [...notDone, ...done]
})

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

const resetFilters = () => {
  searchQuery.value = ''
  selectedFilterTags.value = []
  sortBy.value = 'manual'
  sortOrder.value = 'asc'
}

// Drag & Drop for List View
let sortableInstance = null

const initSortable = () => {
  const table = document.querySelector('.draggable-table .el-table__body-wrapper tbody')
  if (!table) return

  // Destroy previous instance if exists
  if (sortableInstance) {
    sortableInstance.destroy()
    sortableInstance = null
  }

  // Only enable if manual sort and no filters
  if (sortBy.value !== 'manual' || searchQuery.value || selectedFilterTags.value.length > 0) {
    return
  }

  sortableInstance = Sortable.create(table, {
    handle: '.el-table__row', // Drag whole row
    filter: '.done-row', // Disable dragging for done rows
    animation: 150,
    onMove: (evt) => {
      // Prevent dragging INTO a done row (or swapping with it)
      if (evt.related.classList.contains('done-row')) {
        return false
      }
      return true
    },
    onEnd: async ({ newIndex, oldIndex }) => {
      if (newIndex === oldIndex) return
      
      const currentList = [...paginatedTasks.value]
      
      // Check if moving into Done section
      const firstDoneIndex = currentList.findIndex(t => t.status === 'DONE')
      
      if (firstDoneIndex !== -1 && newIndex >= firstDoneIndex) {
        ElMessage.warning('Cannot drag tasks into Done section')
        // Force update to revert DOM
        tableKey.value++
        return
      }
      
      // Modify current list directly
      const movedItem = currentList.splice(oldIndex, 1)[0]
      currentList.splice(newIndex, 0, movedItem)

      // Replace the current page's slice in the fully filtered array
      const allTasks = [...filteredTasks.value]
      const start = (currentPage.value - 1) * pageSize.value
      allTasks.splice(start, pageSize.value, ...currentList)

      // Get new order of IDs from the ALL tasks to ensure position integrity
      const newOrderIds = allTasks.map(t => t.id)
      
      // Update local state and force re-render
      tableData.value = allTasks
      tableKey.value++
      
      try {
        await reorderTasks(newOrderIds)
      } catch {
        ElMessage.error('Failed to update order')
        fetchTasks() // Revert
      }
    }
  })
}

// Watchers to re-init or destroy sortable
// watch is already imported at top

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

// Original onMounted removed, handled at top


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

// Kanban Logic
const kanbanColumns = reactive({
  'TODO': [],
  'IN_PROGRESS': [],
  'DONE': []
})

// Sync Filtered Tasks to Kanban Columns
watch(filteredTasks, (tasks) => {
  // We only sync if NOT dragging (to avoid jitter), but here we rebuild columns
  // To preserve drag state, draggable handles the array mutation.
  // But if filters change, we MUST rebuild.
  // A simple strategy: Always rebuild on filter/data change.
  
  const groups = {
    'TODO': [],
    'IN_PROGRESS': [],
    'DONE': []
  }
  
  tasks.forEach(t => {
    if (groups[t.status]) groups[t.status].push(t)
  })
  
  // Sort each group
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

const handleKanbanChange = async (evt, status) => {
  // Handle Move (Reorder in same column)
  if (evt.moved) {
    const newOrderIds = kanbanColumns[status].map(t => t.id)
    try {
      await reorderTasks(newOrderIds)
      // ElMessage.success('Order updated')
    } catch {
      ElMessage.error('Failed to update order')
    }
  }
  
  // Handle Add (Move from another column)
  if (evt.added) {
    const task = evt.added.element
    
    // 1. Update Status
    task.status = status
    
    // 2. Update Position (Send full list order of new column)
    const newOrderIds = kanbanColumns[status].map(t => t.id)
    
    try {
      // We can do both in parallel or sequential
      // Update status first
      await updateTask(task.id, { ...task, status: status, userId: userStore.user.id })
      // Then reorder
      await reorderTasks(newOrderIds)
    } catch {
      ElMessage.error('Failed to update task')
      fetchTasks() // Revert
    }
  }
}

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
  background-color: var(--el-bg-color-page);
  border-radius: 8px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 220px); /* Limit height */
}
.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding: 0 5px;
}
.column-header h3 {
  margin: 0;
  font-size: 16px;
  color: var(--el-text-color-primary);
}
.draggable-area {
  flex: 1;
  min-height: 100px;
  overflow-y: auto; /* Enable scrolling */
  padding-right: 5px; /* Space for scrollbar */
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
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: box-shadow 0.2s, background-color 0.3s;
}
.kanban-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
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
