# ACIR 代码完全详解

## 前端部分 src

### layout

#### MainLayout.vue

`MainLayout.vue` 是整个前端应用的骨架（Skeleton），负责组织全局公共组件（如导航栏、背景、全局弹窗）并承载各个子页面的路由出口（`router-view`）。

##### 1. Template 解析

```vue
<template>
  <div class="main-layout" :class="{ 'is-dashboard': isDashboard }">
    <!-- 动态背景层 -->
    <div class="background-layer"></div>
    
    <!-- 顶部导航栏 (桌面端) -->
    <HeaderNav 
      class="desktop-nav"
      @add-task="showAddTaskDialog" 
      :transparent="isDashboard"
    />
    
    <!-- 主体内容区 -->
    <main class="content-wrapper">
      <router-view v-slot="{ Component }">
        <transition name="fade-transform" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 底部导航栏 (移动端) -->
    <MobileNavBar 
      class="mobile-nav"
      @add-task="showAddTaskDialog"
    />

    <!-- 全局任务创建弹窗 -->
    <TaskFormDialog
      v-model:visible="dialogVisible"
      :is-edit="false"
      @submit="handleTaskSubmit"
    />
  </div>
</template>
```

**深度解读：**
- **动态类名绑定**：`<div class="main-layout" :class="{ 'is-dashboard': isDashboard }">` 使得布局容器能够根据当前路由动态切换样式。对于主页（dashboard），这种设计使得导航栏可以变为透明，移除内边距，实现沉浸式大图背景效果。
- **独立背景层**：`<div class="background-layer"></div>` 将背景与内容分离。这有助于背景在进行平滑主题切换（例如昼夜交替的渐变色过渡）时不影响上层 DOM 树的重绘，提升性能与视觉体验。
- **响应式导航架构**：同时引入了 `HeaderNav` 和 `MobileNavBar`。在不同尺寸下由 CSS 控制显隐（桌面端显示完整的 HeaderNav，移动端则同时显示精简的 HeaderNav 和底部的 MobileNavBar）。通过 `@add-task` 事件监听，统一调用父组件的方法拉起新建任务弹窗。
- **路由过渡动画**：利用 Vue 的 `<transition>` 配合 `router-view` 实现页面切换时的淡入淡出与轻微位移效果（`fade-transform`），提升了 SPA（单页应用）的流畅体验。

##### 2. Script 解析

```vue
<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import HeaderNav from '@/components/HeaderNav.vue'
import MobileNavBar from '@/components/MobileNavBar.vue'
import TaskFormDialog from '@/components/TaskFormDialog.vue'
import { createTask } from '@/api/task'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const dialogVisible = ref(false)

// 计算属性：判断当前是否是 Dashboard 页面
const isDashboard = computed(() => {
  return route.name === 'dashboard' || route.path === '/dashboard'
})

// 控制任务表单弹窗的显示
const showAddTaskDialog = () => {
  dialogVisible.value = true
}

// 提交任务的异步处理
const handleTaskSubmit = async (payload) => {
  if (!userStore.user || !userStore.user.id) return
  try {
    // 注入当前用户ID后发起请求
    await createTask({ ...payload, userId: userStore.user.id })
    ElMessage.success('Task created successfully')
    dialogVisible.value = false
    // 理想情况下应该触发一个全局事件或 store action 来刷新列表
  } catch {
    ElMessage.error('Failed to create task')
  }
}
</script>
```

**深度解读：**
- **状态计算与响应式流**：`isDashboard` 依赖于 `useRoute()`。由于它是 computed 属性，每当路由发生跳转，它会自动重新计算，进而联动模板中的 CSS 类名和向子组件 `HeaderNav` 传递的 `transparent` 属性。
- **全局任务中心**：将 `TaskFormDialog` 放在 Layout 层级是一个非常实用的“全局弹窗”设计。这意味着用户可以在应用的**任何页面**（通过顶部或底部导航栏的“+”按钮）快速记录灵感或创建任务，而无需跳转离开当前正在工作的页面。
- **业务逻辑拦截**：`handleTaskSubmit` 函数在提交前拦截检查了用户登录状态（`userStore.user.id`）。随后通过对象展开符 `...payload` 注入了从 Store 获取的 `userId`，再将其传递给后端 API，解耦了弹窗组件与用户鉴权逻辑。

**关联 API 实现：`createTask` (`src/api/task.js`)**
```javascript
import request from '@/utils/request'

export function createTask(data) {
  return request({
    url: '/tasks',
    method: 'post',
    data
  })
}
```
*注：该 API 极其简洁，它利用了基于 Axios 封装的 `request` 实例，将拼装好的任务数据通过 POST 方式发送至后端的 `/tasks` 接口。所有的 Token 注入、统一错误处理均在 `request` 拦截器中完成。*

##### 3. CSS 核心设计概述

`MainLayout` 的 `<style scoped>` 虽然不长，但包含了三个非常关键的全局视觉和布局规范：

1. **昼夜交替的背景过渡 (Dark Mode Integration)**:
   ```css
   .background-layer {
     background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
     transition: background 1s ease;
   }
   .dark .background-layer {
     background: linear-gradient(135deg, #0f2027 0%, #203a43 50%, #2c5364 100%);
   }
   ```
   *概述*：配合系统/用户的亮暗主题，通过根节点（或 body 层）的 `.dark` 类名控制。通过 `transition: background 1s ease` 提供了长达一秒的平滑渐变切换体验，非常符合本项目的艺术感。

2. **Dashboard 沉浸式布局适配**:
   ```css
   .content-wrapper { padding-top: 80px; transition: padding-top 0.3s; }
   .main-layout.is-dashboard .content-wrapper { padding-top: 0; }
   ```
   *概述*：普通页面顶部有 `80px` 的 `padding-top`，为固定的顶部导航栏留白，避免内容被遮挡；而 Dashboard 页面为了实现全屏数据大屏的视觉冲击力，去除了该留白，使页面内容（如顶部大图或时间组件）直接顶至屏幕最上方。

3. **双端适配策略 (Mobile Adaptation)**:
   ```css
   @media (max-width: 768px) {
     .mobile-nav { display: flex !important; }
     .content-wrapper { padding-bottom: 80px; padding-top: 60px; }
     .main-layout.is-dashboard .content-wrapper { padding-top: 0; }
   }
   ```
   *概述*：响应式断点设定在常见的平板/手机分界线 `768px`。在移动端下：
   - 激活底部的 `MobileNavBar`，提供更适合大拇指操作的移动端导航。
   - 为 `content-wrapper` 增加了 `80px` 的 `padding-bottom`，防止页面底部内容（如列表最后一条）被底部导航栏遮挡。
   - 保留了 Dashboard 特殊状态下的“零顶部间距”逻辑，确保移动端的主页同样具备沉浸感。

### components

#### HeaderNav.vue

`HeaderNav.vue` 是全局顶部导航栏组件。其最核心的架构亮点是采用了**“双 Header 策略”**（Hero Header 与 Fixed Header 交替），并结合 `FastAverageColor` 实现了基于背景图的智能字体变色，从而在提供沉浸式体验的同时保证了极佳的交互丝滑度。

##### 1. Template 解析（双 Header 架构）

```vue
<template>
  <div class="header-nav-container">
    <!-- 1. Hero Header: 绝对定位, 透明背景, 沉浸式主题 -->
    <header 
      v-if="transparent && !pomodoroStore.isImmersive"
      class="header-nav hero-header" 
      :class="{ 'hero-light': isHeroLight }"
    >
      <!-- Logo、菜单与右侧操作区 (代码略) -->
    </header>

    <!-- 2. Fixed Header: 固定定位, 毛玻璃效果, 系统级主题 -->
    <header 
      class="header-nav fixed-header" 
      :class="{ 'visible': showFixedHeader && !pomodoroStore.isImmersive }"
    >
      <!-- 结构与 Hero Header 完全一致，但应用不同的 CSS 类 (代码略) -->
    </header>
  </div>
</template>
```

**深度解读：**
- **为什么使用“双 Header 策略”？**：在沉浸式大屏（Dashboard）中，导航栏初始状态需要完全透明（Hero Header），且文字颜色需根据背景图明暗自动反转；而当用户向下滚动页面时，需要一个带有毛玻璃背景、固定在顶部的导航栏（Fixed Header）滑出。如果只用一个 Header 并通过 JS 频繁切换类名，在复杂背景下容易导致重绘卡顿和过渡生硬。双 Header 策略通过两个独立的 DOM 节点，一个固定在顶部作为背景，另一个在滚动时通过 CSS `transform` 丝滑滑入，实现了完美的视觉过渡。
- **沉浸式模式拦截**：通过 `!pomodoroStore.isImmersive` 统一控制。当番茄钟进入专注（沉浸）模式时，两个 Header 都会被隐藏，为用户提供无干扰的专注环境。

##### 2. Script 解析（智能变色与滚动监听）

```vue
<script setup>
// ... 导入省略 ...
import { FastAverageColor } from 'fast-average-color'

const props = defineProps({ transparent: { type: Boolean, default: false } })
const isScrolled = ref(false)
const fac = new FastAverageColor()
const analyzedTheme = ref('light')

// 1. 滚动监听逻辑：控制 Fixed Header 的显隐
const showFixedHeader = computed(() => {
  // 非透明模式（如任务列表页），始终显示固定的毛玻璃 Header
  if (!props.transparent) return true
  // 透明模式（Dashboard），仅在滚动超过阈值时显示
  return isScrolled.value
})

const handleScroll = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0
  isScrolled.value = scrollTop > 80 // 超过 80px 触发 Fixed Header 滑入
}

// 2. 智能背景色分析逻辑：控制 Hero Header 的文字颜色
const analyzeBackground = () => {
  if (!pomodoroStore.backgroundImage) {
    analyzedTheme.value = themeStore.isDark ? 'light' : 'dark'
    return
  }
  const img = new Image()
  img.src = pomodoroStore.backgroundImage === 'custom' 
    ? pomodoroStore.customBgUrl 
    : `/backgrounds/${pomodoroStore.backgroundImage}`
  img.crossOrigin = "Anonymous"
  
  img.onload = () => {
    // 利用 fast-average-color 提取图片主色调，判断明暗
    const color = fac.getColor(img)
    analyzedTheme.value = color.isDark ? 'light' : 'dark'
  }
}
</script>
```

**深度解读：**
- **`FastAverageColor` 算法介入**：Dashboard 允许用户自定义背景图，这意味着背景可能是极光（暗色）也可能是雪山（亮色）。通过 `fac.getColor(img)` 提取主色调的 `isDark` 属性，动态计算出 `isHeroLight`，从而让 Hero Header 的文字在暗色背景下变白，在亮色背景下变黑，保证了极致的可读性。
- **响应式显隐控制**：`showFixedHeader` 结合了 `props.transparent` 和 `isScrolled`。这种设计让组件高度复用：在 Dashboard 页它是“滚动渐显”的，而在 Calendar 或 Tasks 页它是“常驻固定”的。

##### 3. CSS 核心设计概述

`HeaderNav` 的动画与质感主要由以下 CSS 规则赋予：

1. **Hero Header 的绝对定位与透明化**:
   ```css
   .hero-header {
     position: absolute;
     top: 0; left: 0;
     background: transparent;
   }
   /* 基于智能分析的字体反色 */
   .hero-header.hero-light .nav-item { color: rgba(255, 255, 255, 0.9); }
   .hero-header:not(.hero-light) .nav-item { color: #606266; }
   ```
2. **Fixed Header 的毛玻璃与滑入动画**:
   ```css
   .fixed-header {
     position: fixed;
     background: rgba(255, 255, 255, 0.8);
     backdrop-filter: blur(10px); /* 核心毛玻璃滤镜 */
     transform: translateY(-100%); /* 初始状态隐藏在屏幕上方 */
     transition: transform 0.3s ease, opacity 0.3s ease;
     opacity: 0;
   }
   .fixed-header.visible {
     transform: translateY(0); /* 滚动后丝滑滑入 */
     opacity: 1;
   }
   ```
3. **移动端精简策略**:
   ```css
   @media (max-width: 768px) {
     .nav-center { display: none; } /* 隐藏中间的文字菜单 */
     .logo-text { display: none; } /* 隐藏 Logo 文字，仅留图标 */
     .nav-right .el-button.is-circle { display: none; } /* 隐藏快捷创建按钮 */
   }
   ```
   *概述*：移动端空间有限，组件通过媒体查询大胆做减法。隐藏了主菜单（由上一节提到的 `MobileNavBar` 接管），隐藏了“新建任务”按钮（移动端底部已有巨大的 `+` 号），仅保留了最核心的深浅色切换、多语言切换和用户头像。

#### TaskFormDialog.vue

`TaskFormDialog.vue` 是整个应用中极其关键的**全局数据入口组件**。正如我们在 `MainLayout` 中看到的，它被挂载在全局层级。这个组件不仅承担了基础的表单校验，更重要的是它处理了两个在现代 Web 开发中常见的痛点：**标签的动态创建（多对多关系预处理）**以及**前端时区偏移（Timezone Offset）处理**。

##### 1. Template 解析（支持动态创建的 Select）

```vue
<template>
  <el-dialog :model-value="visible" :title="isEdit ? 'Edit Task' : 'Create Task'" ...>
    <el-form :model="form" label-width="100px">
      <!-- 基础字段 (Title, Description, Priority, Status 等) 省略 -->
      
      <!-- 标签选择与动态创建 -->
      <el-form-item label="Tags">
        <el-select
          v-model="selectedTagIds"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="Select or create tags"
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

      <el-form-item label="Due Date">
        <el-date-picker v-model="form.dueDate" type="datetime" />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="$emit('update:visible', false)">Cancel</el-button>
      <el-button type="primary" @click="handleSubmit">Confirm</el-button>
    </template>
  </el-dialog>
</template>
```

**深度解读：**
- **复用性设计**：通过传入 `isEdit` 和 `initialData` props，同一个弹窗组件同时满足了“新建任务”和“编辑任务”两种场景，极大地减少了代码冗余。
- **强大的 Tag 选择器**：`el-select` 配置了 `multiple filterable allow-create` 属性。这意味着用户不仅可以从下拉列表中多选已有标签（通过小圆点颜色预览 `tag.color`），还可以在输入框中直接输入新文字。如果文字不匹配已有标签，按下回车即可触发“创建新标签”的流程。

##### 2. Script 解析（标签拦截与时区修正）

```vue
<script setup>
import { ref, reactive, watch } from 'vue'
import { getTags, createTag } from '@/api/tag'
// ...

// 1. 动态标签拦截与创建逻辑
const handleTagChange = async (val) => {
  // `val` 包含了所有选中的项。如果是新建的标签，它的值会是一个纯字符串（而非数字/UUID）
  const newTagNames = val.filter(v => typeof v === 'string')
  if (newTagNames.length > 0) {
    for (const name of newTagNames) {
      try {
        // 调用后端 API，静默创建新标签
        const newTag = await createTag({
          userId: userStore.user.id,
          name: name,
          color: getRandomColor() // 随机赋予一个颜色
        })
        emit('tag-created', newTag)
        // 关键：将刚选中的“字符串名称”替换为后端返回的“真实 Tag ID”
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

// 2. 提交数据封装与时区偏移处理
const handleSubmit = () => {
  if (!form.title || !form.title.trim()) {
    ElMessage.warning('Title is required')
    return
  }
  
  // 将扁平的 ID 数组组装成后端需要的对象数组结构
  const tagsPayload = selectedTagIds.value.map(id => ({ id }))
  
  // 核心：处理 dueDate 的时区问题
  let formattedDueDate = form.dueDate
  if (formattedDueDate instanceof Date) {
    // 避免 JS Date 的 toISOString() 默认转为 UTC+0 的问题
    const tzOffset = formattedDueDate.getTimezoneOffset() * 60000;
    formattedDueDate = new Date(formattedDueDate.getTime() - tzOffset).toISOString().slice(0, 19);
  }
  
  const payload = {
    ...form,
    dueDate: formattedDueDate,
    tags: tagsPayload
  }
  
  // 向上抛出 payload，由父组件（如 MainLayout）接管具体的网络请求
  emit('submit', payload)
}
</script>
```

**深度解读：**
- **标签的“前置静默创建”模式**：在任务提交前，当用户在下拉框中敲下回车新建标签时，`handleTagChange` 会立即拦截这个操作。它通过判断选中值是否为字符串来识别新标签，然后**立刻发起请求在后端生成该标签**，获取到真实 ID 后，再替换掉数组里的字符串。这种设计确保了最终点击“Confirm”时，提交给后端的 `tagsPayload` 始终是干净的 `[{id: 1}, {id: 2}]` 格式，简化了后端保存任务时的级联逻辑。
- **时区安全防御（Timezone Offset）**：当我们在 Element Plus 的 `el-date-picker` 中选择 "2024-05-01 12:00:00"（北京时间 UTC+8）时，它生成的是一个本地 Date 对象。如果直接调用 `Date.toISOString()`，它会返回 `"2024-05-01T04:00:00.000Z"`。后端如果是按字符串接收并存入 MySQL `DATETIME` 字段，时间就会莫名其妙少 8 个小时。
  组件中的这段代码 `formattedDueDate.getTime() - tzOffset` 巧妙地做了一个逆向偏移补偿。它先把本地时间加上 8 小时变成 "2024-05-01 20:00:00" 的时间戳，此时再调 `toISOString()`，截取前 19 位后得到的就是 `"2024-05-01T12:00:00"`，完美保证了前后端时间字符串的绝对一致。
- **职责边界清晰**：组件内部完成了所有复杂的数据清洗（标签创建、时间格式化、ID数组组装），但**绝不越权调用** `createTask` API，而是将组装好的完美 `payload` 通过 `emit('submit', payload)` 交给父组件处理，体现了极高的组件封装素养。

**关联 API 实现：`getTags`, `createTag` (`src/api/tag.js`)**
```javascript
import request from '@/utils/request'

export function getTags(userId) {
  return request({
    url: '/tags',
    method: 'get',
    params: { userId }
  })
}

export function createTag(data) {
  return request({
    url: '/tags',
    method: 'post',
    data
  })
}
```
*注：API 依然保持了极简风格，这得益于之前解析中提到的全局 Axios 拦截器的统一处理。*



## 后端部分