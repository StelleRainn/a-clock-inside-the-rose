<template>
  <div class="dashboard-widgets">
    <!-- Header with Edit Mode Toggle -->
    <div class="widgets-header">
      <div class="left">
        <h2>{{ $t('widgets.mySpace') }}</h2>
      </div>
      <div class="right">
        <el-button 
          v-if="!isEditMode" 
          type="primary" 
          link 
          @click="isEditMode = true"
        >
          {{ $t('widgets.customize') }}
        </el-button>
        <div v-else class="edit-controls">
          <el-button type="success" size="small" @click="showAddWidget = true">
            <el-icon><Plus /></el-icon> {{ $t('widgets.addCard') }}
          </el-button>
          <el-button type="info" size="small" @click="isEditMode = false">
            {{ $t('widgets.done') }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-if="activeWidgets.length === 0" class="empty-state">
      <h3>{{ $t('widgets.emptyTitle') }}</h3>
      <div class="add-widget-card" @click="showAddWidget = true">
        <el-icon :size="24"><Plus /></el-icon>
        <span>{{ $t('widgets.addCard') }}</span>
      </div>
    </div>

    <!-- Grid Layout -->
    <div v-else class="widgets-grid">
      <div 
        v-for="widget in activeWidgets" 
        :key="widget.id"
        class="widget-wrapper"
        :class="[`col-${widget.cols || 1}`, { 'is-editing': isEditMode }]"
      >
        <!-- Remove Button (Edit Mode) -->
        <div v-if="isEditMode" class="remove-btn" @click="removeWidget(widget.id)">
          <el-icon><Close /></el-icon>
        </div>

        <!-- Dynamic Component -->
        <component :is="getComponent(widget.type)" />
      </div>
    </div>

    <!-- Widget Store Drawer -->
    <el-drawer v-model="showAddWidget" :title="$t('widgets.storeTitle')" direction="rtl" size="300px">
      <div class="widget-store">
        <div 
          v-for="item in availableWidgets" 
          :key="item.type"
          class="store-item"
          @click="addWidget(item)"
        >
          <div class="item-preview">
            <el-icon :size="32"><component :is="item.icon" /></el-icon>
          </div>
          <div class="item-info">
            <h4>{{ $t(item.nameKey) }}</h4>
            <p>{{ $t(item.descKey) }}</p>
          </div>
          <el-button size="small" icon="Plus" circle />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { Plus, Close, Calendar, PieChart, List, ChatDotSquare } from '@element-plus/icons-vue'
import WidgetHeatmap from '@/components/widgets/WidgetHeatmap.vue'
import WidgetFocusRing from '@/components/widgets/WidgetFocusRing.vue'
import WidgetTodoList from '@/components/widgets/WidgetTodoList.vue'

const { t } = useI18n()

const isEditMode = ref(false)
const showAddWidget = ref(false)

// Default widgets for first-time users
const defaultWidgets = [
  { id: 'w1', type: 'focus-ring', cols: 1 },
  { id: 'w2', type: 'todo-list', cols: 1 },
  { id: 'w3', type: 'heatmap', cols: 2 }
]

const activeWidgets = ref([])

// Load from local storage
onMounted(() => {
  const saved = localStorage.getItem('dashboard_widgets')
  if (saved) {
    activeWidgets.value = JSON.parse(saved)
  } else {
    activeWidgets.value = defaultWidgets
  }
})

// Save on change
watch(activeWidgets, (val) => {
  localStorage.setItem('dashboard_widgets', JSON.stringify(val))
}, { deep: true })

const availableWidgets = [
  { 
    type: 'heatmap', 
    nameKey: 'widgets.heatmap', 
    descKey: 'widgets.heatmapDesc', 
    icon: Calendar,
    defaultCols: 2 
  },
  { 
    type: 'focus-ring', 
    nameKey: 'widgets.focusRing', 
    descKey: 'widgets.focusRingDesc', 
    icon: PieChart,
    defaultCols: 1
  },
  { 
    type: 'todo-list', 
    nameKey: 'widgets.todoList', 
    descKey: 'widgets.todoListDesc', 
    icon: List,
    defaultCols: 1
  },
]

const getComponent = (type) => {
  const map = {
    'heatmap': WidgetHeatmap,
    'focus-ring': WidgetFocusRing,
    'todo-list': WidgetTodoList
  }
  return map[type]
}

const addWidget = (item) => {
  activeWidgets.value.push({
    id: Date.now().toString(),
    type: item.type,
    cols: item.defaultCols
  })
  showAddWidget.value = false
}

const removeWidget = (id) => {
  activeWidgets.value = activeWidgets.value.filter(w => w.id !== id)
}
</script>

<style scoped>
.dashboard-widgets {
  padding: 40px 20px;
  min-height: 50vh;
  max-width: 1200px;
  margin: 0 auto;
}

.widgets-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.widgets-header h2 {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0;
  color: var(--el-text-color-primary);
}

.edit-controls {
  display: flex;
  gap: 12px;
}

/* Grid Layout */
.widgets-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr); /* 2 Column layout for desktop */
  gap: 24px;
}

.widget-wrapper {
  position: relative;
  transition: all 0.3s ease;
}

.widget-wrapper.col-2 {
  grid-column: span 2;
}

.widget-wrapper.col-1 {
  grid-column: span 1;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .dashboard-widgets {
    padding: 20px 16px;
    min-height: auto;
  }

  .widgets-header {
    flex-direction: row;
    align-items: center;
    margin-bottom: 16px;
  }
  
  .widgets-header h2 {
    font-size: 1.25rem;
  }
  
  .widgets-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .widget-wrapper.col-2 {
    grid-column: span 1;
  }
}

/* Edit Mode Styles */
.widget-wrapper.is-editing {
  transform: scale(0.98);
  opacity: 0.9;
  cursor: grab;
}

.widget-wrapper.is-editing::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border: 2px dashed var(--el-color-primary);
  border-radius: 16px;
  pointer-events: none;
}

.remove-btn {
  position: absolute;
  top: -10px;
  right: -10px;
  width: 24px;
  height: 24px;
  background: var(--el-color-danger);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--el-text-color-secondary);
}

.add-widget-card {
  margin-top: 24px;
  width: 200px;
  height: 120px;
  border: 2px dashed var(--el-border-color);
  border-radius: 16px;
  display: inline-flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.add-widget-card:hover {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
  background: rgba(var(--el-color-primary-rgb), 0.05);
}

/* Widget Store */
.store-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  border: 1px solid var(--el-border-color-lighter);
  margin-bottom: 12px;
}

.store-item:hover {
  background: var(--el-fill-color-light);
  border-color: var(--el-color-primary-light-5);
}

.item-preview {
  width: 48px;
  height: 48px;
  background: var(--el-fill-color);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary);
}

.item-info {
  flex: 1;
}

.item-info h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
}

.item-info p {
  margin: 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
