<template>
  <div class="settings-dialog-container">
    <div class="sidebar">
      <div 
        class="menu-item" 
        :class="{ active: activeTab === 'general' }"
        @click="activeTab = 'general'"
      >
        <el-icon><Monitor /></el-icon> General
      </div>
      <div 
        class="menu-item" 
        :class="{ active: activeTab === 'timers' }"
        @click="activeTab = 'timers'"
      >
        <el-icon><Timer /></el-icon> Timers
      </div>
      <div 
        class="menu-item" 
        :class="{ active: activeTab === 'sounds' }"
        @click="activeTab = 'sounds'"
      >
        <el-icon><Headset /></el-icon> Sounds
      </div>
    </div>
    
    <div class="content">
      <!-- General: Background Image -->
      <div v-if="activeTab === 'general'" class="tab-pane">
        <h3>Background</h3>
        <input 
          type="file" 
          ref="fileInput" 
          accept="image/*" 
          style="display: none" 
          @change="handleFileUpload"
        >
        <div class="bg-grid">
          <div 
            class="bg-item" 
            :class="{ active: !tempBg }"
            @click="tempBg = ''"
          >
            <div class="bg-preview default-bg"></div>
            <span>Default</span>
          </div>
          
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
            <span>Custom</span>
          </div>

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

        <div v-if="tempBg" class="mt-4">
          <h3>Appearance</h3>
          <div class="form-col">
            <div class="form-group full-width">
              <label>Hero Text Theme</label>
              <el-radio-group v-model="heroTheme" size="small">
                <el-radio-button label="auto">Auto</el-radio-button>
                <el-radio-button label="light">Light Text</el-radio-button>
                <el-radio-button label="dark">Dark Text</el-radio-button>
              </el-radio-group>
            </div>
            <div class="form-group full-width">
              <div class="flex-between">
                <label>Background Overlay Opacity</label>
                <span>{{ Math.round(bgOverlayOpacity * 100) }}%</span>
              </div>
              <el-slider v-model="bgOverlayOpacity" :min="0" :max="1" :step="0.05" />
            </div>
          </div>
        </div>
      </div>

      <!-- Timers: Duration & Auto-start -->
      <div v-if="activeTab === 'timers'" class="tab-pane">
        <h3>Timer Durations (minutes)</h3>
        <div class="form-row">
          <div class="form-group">
            <label>Pomodoro</label>
            <el-input-number v-model="workMins" :min="1" :max="60" />
          </div>
          <div class="form-group">
            <label>Short Break</label>
            <el-input-number v-model="shortBreakMins" :min="1" :max="30" />
          </div>
          <div class="form-group">
            <label>Long Break</label>
            <el-input-number v-model="longBreakMins" :min="1" :max="60" />
          </div>
        </div>
        
        <h3>Sequence</h3>
        <div class="form-col">
          <div class="switch-row">
            <span>Auto-start Breaks</span>
            <el-switch v-model="autoStartBreaks" />
          </div>
          <div class="switch-row">
            <span>Auto-start Pomodoros</span>
            <el-switch v-model="autoStartPomodoros" />
          </div>
          <div class="form-group mt-2">
            <label>Long Break interval</label>
            <el-input-number v-model="longBreakInterval" :min="1" :max="10" />
          </div>
        </div>
      </div>

      <!-- Sounds: Volume & Selection -->
      <div v-if="activeTab === 'sounds'" class="tab-pane">
        <h3>Sound Settings</h3>
        <div class="form-col">
           <div class="form-group full-width">
            <label>Alert Sound</label>
            <el-select v-model="selectedSound">
              <el-option label="Bell" value="bell" />
              <el-option label="Alarm" value="alarm" />
              <el-option label="Digital" value="digital" />
            </el-select>
          </div>
          
          <div class="form-group full-width mt-2">
            <div class="flex-between">
              <label>Volume</label>
              <span>{{ soundVolume }}%</span>
            </div>
            <el-slider v-model="soundVolume" />
          </div>
          
          <div class="switch-row mt-2">
            <span>Play sound when timer finishes</span>
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
const activeTab = ref('general')
const fileInput = ref(null)
const pendingFile = ref(null)
const localCustomUrl = ref(pomodoroStore.customBgUrl)
const isCustomDeleted = ref(false)

const handleFileUpload = (event) => {
  const file = event.target.files[0]
  if (!file) return

  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('Image size should be less than 10MB')
    return
  }

  if (pendingFile.value && localCustomUrl.value) {
    URL.revokeObjectURL(localCustomUrl.value)
  }

  pendingFile.value = file
  localCustomUrl.value = URL.createObjectURL(file)
  tempBg.value = 'custom'
  isCustomDeleted.value = false
}

const triggerUpload = () => {
  if (!localCustomUrl.value) {
    fileInput.value.click()
  } else {
    // If image exists, clicking just selects it
    tempBg.value = 'custom'
  }
}

const removeCustomImage = (e) => {
  e.stopPropagation()
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

onUnmounted(() => {
  if (pendingFile.value && localCustomUrl.value) {
    URL.revokeObjectURL(localCustomUrl.value)
  }
})

// Local state for optimistic UI
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

const formatBgName = (name) => {
  return name.replace('.jpg', '').replace('_', ' ').replace(/\b\w/g, l => l.toUpperCase())
}

const saveChanges = async () => {
  // Commit changes to store
  
  // Handle deletion
  if (isCustomDeleted.value) {
    try {
      await imageDb.deleteImage('custom')
      pomodoroStore.customBgUrl = '' // clear store URL
      await pomodoroStore.loadCustomBackground() // Reload to ensure sync
    } catch {
      ElMessage.error('Failed to remove custom background')
    }
  }

  // Handle new upload
  if (tempBg.value === 'custom' && pendingFile.value) {
    try {
      await imageDb.saveImage('custom', pendingFile.value)
      await pomodoroStore.loadCustomBackground()
    } catch {
      ElMessage.error('Failed to save custom background')
    }
  }

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
  
  // If timer is not running, reset it to reflect new settings
  if (!pomodoroStore.isRunning) {
    pomodoroStore.updateSettings({
      pomodoro: workMins.value,
      shortBreak: shortBreakMins.value,
      longBreak: longBreakMins.value
    })
  }
  
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
</style>
