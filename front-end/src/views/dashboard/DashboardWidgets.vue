<template>
  <div class="dashboard-widgets">
    <!-- 
    DashboardWidgets: 负责渲染并管理主页下方的各种数据挂件 (Widgets)。
    支持编辑模式：用户可以自由添加或移除 Widget，实现高度个性化的空间。
  -->
    <!-- Header with Edit Mode Toggle (编辑模式开关头) -->
    <div class="widgets-header">
      <div class="left">
        <h2>{{ $t('widgets.mySpace') }}</h2>
      </div>
      <div class="right">
        <!-- 非编辑状态：显示“Customize”按钮 -->
        <el-button 
          v-if="!isEditMode" 
          type="primary" 
          link 
          @click="isEditMode = true"
        >
          {{ $t('widgets.customize') }}
        </el-button>
        <!-- 编辑状态：显示“Add Card”和“Done”按钮 -->
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

    <!-- Empty State (当用户删除了所有 Widget 时的占位图) -->
    <div v-if="activeWidgets.length === 0" class="empty-state">
      <h3>{{ $t('widgets.emptyTitle') }}</h3>
      <div class="add-widget-card" @click="showAddWidget = true">
        <el-icon :size="24"><Plus /></el-icon>
        <span>{{ $t('widgets.addCard') }}</span>
      </div>
    </div>

    <!-- Grid Layout (核心的瀑布流/网格布局容器) -->
    <div v-else class="widgets-grid">
      <!-- 遍历当前激活的 Widget 列表，动态计算它们所占的列数 (col-1 占一半，col-2 占满宽) -->
      <div 
        v-for="widget in activeWidgets" 
        :key="widget.id"
        class="widget-wrapper"
        :class="[`col-${widget.cols || 1}`, { 'is-editing': isEditMode }]"
      >
        <!-- Remove Button (Edit Mode) (仅在编辑模式下显示的删除按钮) -->
        <div v-if="isEditMode" class="remove-btn" @click="removeWidget(widget.id)">
          <el-icon><Close /></el-icon>
        </div>

        <!-- Dynamic Component (Vue 动态组件，根据 widget.type 渲染对应的 Vue 单文件组件) -->
        <component :is="getComponent(widget.type)" />
      </div>
    </div>

    <!-- Widget Store Drawer (右侧抽屉式“Widget 商店”，用于挑选并添加新挂件) -->
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
import { Plus, Close, Calendar, PieChart, List } from '@element-plus/icons-vue'
// 导入所有拆分出去的 Widget 子组件
import WidgetHeatmap from '@/components/widgets/WidgetHeatmap.vue'
import WidgetFocusRing from '@/components/widgets/WidgetFocusRing.vue'
import WidgetTodoList from '@/components/widgets/WidgetTodoList.vue'

// UI 控制状态
const isEditMode = ref(false)
const showAddWidget = ref(false)

// Default widgets for first-time users (新手默认布局)
const defaultWidgets = [
  { id: 'w1', type: 'focus-ring', cols: 1 }, // 占 1 列 (半宽)
  { id: 'w2', type: 'todo-list', cols: 1 },  // 占 1 列 (半宽)
  { id: 'w3', type: 'heatmap', cols: 2 }     // 占 2 列 (全宽)
]

// 当前激活在页面上的 Widget 列表
const activeWidgets = ref([])

// 供右侧“Widget 商店”渲染的静态数据字典
const availableWidgets = [
  { type: 'focus-ring', icon: PieChart, nameKey: 'widgets.focusRingName', descKey: 'widgets.focusRingDesc', cols: 1 },
  { type: 'todo-list', icon: List, nameKey: 'widgets.todoListName', descKey: 'widgets.todoListDesc', cols: 1 },
  { type: 'heatmap', icon: Calendar, nameKey: 'widgets.heatmapName', descKey: 'widgets.heatmapDesc', cols: 2 }
]

// 核心的映射函数：将 JSON 中的 type 字符串转换为对应的 Vue 组件对象
const getComponent = (type) => {
  const map = {
    'heatmap': WidgetHeatmap,
    'focus-ring': WidgetFocusRing,
    'todo-list': WidgetTodoList
  }
  return map[type]
}

// 每次 activeWidgets 变化时，自动序列化并存储到 localStorage，实现布局记忆
watch(activeWidgets, (newVal) => {
  localStorage.setItem('dashboard_widgets', JSON.stringify(newVal))
}, { deep: true })

// 组件挂载时，优先从 localStorage 恢复用户自定义布局；若无，则加载默认布局
onMounted(() => {
  const saved = localStorage.getItem('dashboard_widgets')
  if (saved) {
    try {
      activeWidgets.value = JSON.parse(saved)
    } catch {
      activeWidgets.value = [...defaultWidgets]
    }
  } else {
    activeWidgets.value = [...defaultWidgets]
  }
})

// 添加 Widget 逻辑
const addWidget = (item) => {
  // 防御：防止重复添加相同类型的 Widget (根据业务需求，这里限定每种类型只能有一个)
  if (activeWidgets.value.some(w => w.type === item.type)) {
    return // Already exists
  }
  
  activeWidgets.value.push({
    id: `w_${Date.now()}`, // 生成唯一 ID
    type: item.type,
    cols: item.cols
  })
  showAddWidget.value = false // 添加成功后自动关闭抽屉
}

// 移除 Widget 逻辑
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
