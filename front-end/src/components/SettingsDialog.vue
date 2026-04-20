<template>
  <div class="settings-dialog-container">
    <!-- 
    设置弹窗主容器：采用左侧导航(sidebar) + 右侧内容(content) 的经典两栏布局。
    在移动端通过媒体查询 (max-width: 768px) 会自适应转变为上下布局（导航栏横向滚动）。
  -->
    <div class="sidebar">
      <div 
        class="menu-item" 
        :class="{ active: activeTab === 'general' }"
        @click="activeTab = 'general'"
      >
        <el-icon><Monitor /></el-icon> {{ $t('settings.general') }}
      </div>
      <div 
        class="menu-item" 
        :class="{ active: activeTab === 'timers' }"
        @click="activeTab = 'timers'"
      >
        <el-icon><Timer /></el-icon> {{ $t('settings.timers') }}
      </div>
      <div 
        class="menu-item" 
        :class="{ active: activeTab === 'sounds' }"
        @click="activeTab = 'sounds'"
      >
        <el-icon><Headset /></el-icon> {{ $t('settings.sounds') }}
      </div>
    </div>
    
    <div class="content">
      <!-- 
        Tab 1: General (常规设置)
        核心功能：背景图选择、自定义背景图上传、Hero Theme 字体反色策略、背景遮罩透明度调节。
      -->
      <div v-if="activeTab === 'general'" class="tab-pane">
        <h3>{{ $t('settings.background') }}</h3>
        <!-- 隐藏的原生文件上传控件，通过点击事件代理触发 -->
        <input 
          type="file" 
          ref="fileInput" 
          accept="image/*" 
          style="display: none" 
          @change="handleFileUpload"
        >
        <div class="bg-grid">
          <!-- 极简纯黑背景选项 -->
          <div 
            class="bg-item" 
            :class="{ active: !tempBg }"
            @click="tempBg = ''"
          >
            <div class="bg-preview default-bg"></div>
            <span>Minimal Black</span>
          </div>
          
          <!-- 自定义上传背景选项：结合 IndexedDB 实现本地持久化 -->
          <div 
            class="bg-item" 
            :class="{ active: tempBg === 'custom' }"
            @click="triggerUpload"
          >
            <div 
              class="bg-preview custom-bg" 
              :style="localCustomUrl ? { backgroundImage: `url(${localCustomUrl})` } : {}"
            >
              <el-icon v-if="!localCustomUrl" class="upload-icon"><Plus /></el-icon>
              
              <div 
                v-if="localCustomUrl" 
                class="delete-btn"
                @click="removeCustomImage"
              >
                <el-icon><Delete /></el-icon>
              </div>
            </div>
            <span>{{ $t('settings.customImage') }}</span>
          </div>

          <!-- 系统预设背景列表渲染 -->
          <div 
            v-for="bg in backgrounds" 
            :key="bg" 
            class="bg-item"
            :class="{ active: tempBg === bg }"
            @click="tempBg = bg"
          >
            <div 
              class="bg-preview" 
              :style="{ backgroundImage: `url(/backgrounds/${bg})` }"
            ></div>
            <span>{{ formatBgName(bg) }}</span>
          </div>
        </div>

        <!-- 仅当选中了图片背景时，才显示主题与透明度设置 -->
        <div v-if="tempBg" class="mt-4">
          <h3>{{ $t('settings.theme') }}</h3>
          <div class="form-col">
            <div class="form-group full-width">
              <label>Hero Text Theme</label>
              <!-- 控制 HeaderNav 文字颜色是深色还是浅色，auto 为借助 fast-average-color 自动分析 -->
              <el-radio-group v-model="heroTheme" size="small">
                <el-radio-button label="auto">{{ $t('settings.auto') }}</el-radio-button>
                <el-radio-button label="light">{{ $t('settings.light') }}</el-radio-button>
                <el-radio-button label="dark">{{ $t('settings.dark') }}</el-radio-button>
              </el-radio-group>
            </div>
            <div class="form-group full-width">
              <div class="flex-between">
                <label>{{ $t('settings.overlayOpacity') }}</label>
                <span>{{ Math.round(bgOverlayOpacity * 100) }}%</span>
              </div>
              <el-slider v-model="bgOverlayOpacity" :min="0" :max="1" :step="0.05" />
            </div>
          </div>
        </div>
      </div>

      <!-- 
        Tab 2: Timers (番茄钟设置)
        核心功能：配置专注时长、短休、长休时长，以及自动流转规则。
      -->
      <div v-if="activeTab === 'timers'" class="tab-pane">
        <h3>{{ $t('settings.durations') }}</h3>
        <div class="form-row">
          <div class="form-group">
            <label>{{ $t('settings.pomodoro') }}</label>
            <el-input-number v-model="workMins" :min="1" :max="60" />
          </div>
          <div class="form-group">
            <label>{{ $t('settings.shortBreak') }}</label>
            <el-input-number v-model="shortBreakMins" :min="1" :max="30" />
          </div>
          <div class="form-group">
            <label>{{ $t('settings.longBreak') }}</label>
            <el-input-number v-model="longBreakMins" :min="1" :max="60" />
          </div>
        </div>
        
        <h3>{{ $t('settings.autoStart') }}</h3>
        <div class="form-col">
          <div class="switch-row">
            <span>{{ $t('settings.autoStartBreaks') }}</span>
            <el-switch v-model="autoStartBreaks" />
          </div>
          <div class="switch-row">
            <span>{{ $t('settings.autoStartPomodoros') }}</span>
            <el-switch v-model="autoStartPomodoros" />
          </div>
          <div class="form-group mt-2">
            <label>{{ $t('settings.longBreakInterval') }}</label>
            <el-input-number v-model="longBreakInterval" :min="1" :max="10" />
          </div>
        </div>
      </div>

      <!-- 
        Tab 3: Sounds (声音设置)
        核心功能：白噪音/提示音的选择、音量控制及全局开关。
      -->
      <div v-if="activeTab === 'sounds'" class="tab-pane">
        <h3>{{ $t('settings.sound') }}</h3>
        <div class="form-col">
          <div class="form-group full-width">
            <label>{{ $t('settings.alertSound') }}</label>
            <el-select v-model="selectedSound">
              <el-option :label="$t('settings.bell')" value="bell" />
              <el-option :label="$t('settings.digital')" value="digital" />
              <el-option :label="$t('settings.chime')" value="chime" />
            </el-select>
          </div>
          
          <div class="form-group full-width mt-2">
            <div class="flex-between">
              <label>{{ $t('settings.volume') }}</label>
              <span>{{ soundVolume }}%</span>
            </div>
            <el-slider v-model="soundVolume" />
          </div>
          
          <div class="switch-row mt-2">
            <span>{{ $t('settings.enableSound') }}</span>
            <el-switch v-model="soundEnabled" />
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <div class="dialog-footer">
    <el-button @click="$emit('close')">Close</el-button>
    <el-button type="primary" @click="saveChanges">Save Changes</el-button>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { usePomodoroStore } from '@/stores/pomodoro'
import { Monitor, Timer, Headset, Plus, Delete } from '@element-plus/icons-vue'
import { imageDb } from '@/utils/imageDb'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['close'])
const pomodoroStore = usePomodoroStore()

// UI 控制状态
const activeTab = ref('general')

// ==========================================
// 自定义背景图片上传逻辑 (借助 IndexedDB)
// 核心难点：浏览器不能直接保存大文件到 LocalStorage，故采用 IndexedDB (imageDb) 进行本地化持久存储。
// ==========================================
const fileInput = ref(null)
const pendingFile = ref(null) // 暂存用户选中的文件，点击 Save 后才入库
const localCustomUrl = ref(pomodoroStore.customBgUrl) // 生成的 Blob URL，用于即时预览
const isCustomDeleted = ref(false)

const handleFileUpload = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 限制图片大小为 10MB
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('Image size should be less than 10MB')
    return
  }

  // 释放旧的内存 URL，防止内存泄漏
  if (pendingFile.value && localCustomUrl.value) {
    URL.revokeObjectURL(localCustomUrl.value)
  }

  pendingFile.value = file
  // 创建本地预览 URL (Blob URL)
  localCustomUrl.value = URL.createObjectURL(file)
  tempBg.value = 'custom'
  isCustomDeleted.value = false
}

const triggerUpload = () => {
  if (!localCustomUrl.value) {
    fileInput.value.click()
  } else {
    // 如果已经有图片了，点击该块仅作为选中操作
    tempBg.value = 'custom'
  }
}

const removeCustomImage = (e) => {
  e.stopPropagation() // 阻止事件冒泡，防止触发选中
  if (localCustomUrl.value && pendingFile.value) {
    URL.revokeObjectURL(localCustomUrl.value)
  }
  localCustomUrl.value = null
  pendingFile.value = null
  isCustomDeleted.value = true
  if (tempBg.value === 'custom') {
    tempBg.value = ''
  }
}

// 组件卸载时务必清理内存中的 Blob URL
onUnmounted(() => {
  if (pendingFile.value && localCustomUrl.value) {
    URL.revokeObjectURL(localCustomUrl.value)
  }
})

// ==========================================
// 乐观 UI 状态绑定 (Optimistic UI)
// 为什么不直接绑定 store？为了实现“点击 Save 才生效，点击 Close 放弃修改”的体验。
// 这里使用本地 ref 拷贝 store 中的初始值。
// ==========================================
const workMins = ref(Math.floor(pomodoroStore.pomodoroDuration / 60))
const shortBreakMins = ref(Math.floor(pomodoroStore.shortBreakDuration / 60))
const longBreakMins = ref(Math.floor(pomodoroStore.longBreakDuration / 60))

const autoStartBreaks = ref(pomodoroStore.autoStartBreaks)
const autoStartPomodoros = ref(pomodoroStore.autoStartPomodoros)
const longBreakInterval = ref(pomodoroStore.longBreakInterval)

const soundEnabled = ref(pomodoroStore.soundEnabled)
const soundVolume = ref(pomodoroStore.soundVolume)
const selectedSound = ref(pomodoroStore.selectedSound)

const tempBg = ref(pomodoroStore.backgroundImage)
const heroTheme = ref(pomodoroStore.heroTheme)
const bgOverlayOpacity = ref(pomodoroStore.bgOverlayOpacity)

const backgrounds = ['day_1.jpg', 'night_1.jpg']

// 格式化背景图名称，例如 "day_1.jpg" -> "Day 1"
const formatBgName = (name) => {
  return name.replace('.jpg', '').replace('_', ' ').replace(/\b\w/g, l => l.toUpperCase())
}

// ==========================================
// 保存修改核心逻辑
// ==========================================
const saveChanges = async () => {
  
  // 1. 处理自定义图片的删除（如果用户点击了垃圾桶）
  if (isCustomDeleted.value) {
    try {
      await imageDb.deleteImage('custom') // 从 IndexedDB 中抹除
      pomodoroStore.customBgUrl = '' 
      await pomodoroStore.loadCustomBackground() 
    } catch {
      ElMessage.error('Failed to remove custom background')
    }
  }

  // 2. 处理新上传的自定义图片
  if (tempBg.value === 'custom' && pendingFile.value) {
    try {
      await imageDb.saveImage('custom', pendingFile.value) // 存入 IndexedDB
      await pomodoroStore.loadCustomBackground()
    } catch {
      ElMessage.error('Failed to save custom background')
    }
  }

  // 3. 将本地的 ref 状态同步回 Pinia Store
  pomodoroStore.pomodoroDuration = workMins.value * 60
  pomodoroStore.shortBreakDuration = shortBreakMins.value * 60
  pomodoroStore.longBreakDuration = longBreakMins.value * 60
  
  pomodoroStore.autoStartBreaks = autoStartBreaks.value
  pomodoroStore.autoStartPomodoros = autoStartPomodoros.value
  pomodoroStore.longBreakInterval = longBreakInterval.value
  
  pomodoroStore.soundEnabled = soundEnabled.value
  pomodoroStore.soundVolume = soundVolume.value
  pomodoroStore.selectedSound = selectedSound.value
  
  pomodoroStore.backgroundImage = tempBg.value
  pomodoroStore.heroTheme = heroTheme.value
  pomodoroStore.bgOverlayOpacity = bgOverlayOpacity.value
  
  // 4. 如果当前番茄钟没有在运行，立即重置倒计时以应用新的时长设置
  if (!pomodoroStore.isRunning) {
    pomodoroStore.updateSettings({
      pomodoro: workMins.value,
      shortBreak: shortBreakMins.value,
      longBreak: longBreakMins.value
    })
  }
  
  // 通知父组件关闭弹窗
  emit('close')
}
</script>

<style scoped>
.settings-dialog-container {
  display: flex;
  height: 480px;
  border-top: 1px solid var(--el-border-color-lighter);
  border-bottom: 1px solid var(--el-border-color-lighter);
  margin: -10px -24px 0 -24px; /* Offset dialog padding */
}

.sidebar {
  width: 220px;
  border-right: 1px solid var(--el-border-color-lighter);
  padding: 20px 0;
  background-color: transparent;
  display: flex;
  flex-direction: column;
}

.menu-item {
  padding: 16px 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--el-text-color-regular);
  transition: all 0.2s;
  font-weight: 500;
  font-size: 15px;
}

.menu-item:hover {
  background-color: var(--el-fill-color-light);
}

.menu-item.active {
  background-color: transparent;
  color: var(--el-color-primary);
  border-right: 3px solid var(--el-color-primary);
}

.content {
  flex: 1;
  padding: 40px 50px;
  overflow-y: auto;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/Edge */
}

.content::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

.tab-pane h3 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.bg-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 20px;
}

.bg-item {
  cursor: pointer;
  text-align: center;
}

.bg-preview {
  width: 100%;
  height: 90px;
  border-radius: 8px;
  background-size: cover;
  background-position: center;
  border: 3px solid transparent;
  margin-bottom: 8px;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.bg-preview.default-bg {
  background: #1a1a1a;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.bg-preview.custom-bg {
  border: 1px dashed var(--el-border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--el-fill-color-light);
  position: relative;
}

.delete-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  background: rgba(0,0,0,0.6);
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  z-index: 10;
}

.delete-btn:hover {
  background: var(--el-color-danger);
  transform: scale(1.1);
}

.upload-icon {
  font-size: 24px;
  color: var(--el-text-color-secondary);
}

.bg-item.active .bg-preview {
  border-color: var(--el-color-primary);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(var(--el-color-primary-rgb), 0.3);
}

.form-row {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
  margin-bottom: 40px;
}

.form-col {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.form-group.full-width {
  width: 100%;
}

.form-group label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

.switch-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.mt-2 {
  margin-top: 16px;
}

.mt-4 {
  margin-top: 32px;
}

.dialog-footer {
  padding: 20px 24px 0;
  text-align: right;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .settings-dialog-container {
    flex-direction: column;
    height: auto;
    min-height: 400px;
    max-height: 65vh;
    margin: -10px -20px 0 -20px;
  }

  .sidebar {
    width: 100%;
    flex-direction: row;
    border-right: none;
    border-bottom: 1px solid var(--el-border-color-lighter);
    padding: 10px 16px;
    overflow-x: auto;
    white-space: nowrap;
    scrollbar-width: none;
  }
  
  .sidebar::-webkit-scrollbar {
    display: none;
  }

  .menu-item {
    padding: 10px 16px;
    border-right: none !important;
    border-bottom: 3px solid transparent;
    font-size: 14px;
  }

  .menu-item.active {
    border-bottom: 3px solid var(--el-color-primary);
  }

  .content {
    padding: 20px 16px;
  }

  .bg-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 10px;
  }

  .bg-preview {
    height: 70px;
  }

  .form-row {
    flex-direction: column;
    gap: 15px;
    margin-bottom: 25px;
  }

  .dialog-footer {
    padding: 15px 16px 0;
    justify-content: center;
  }
  
  .dialog-footer .el-button {
    flex: 1;
  }
}
</style>
