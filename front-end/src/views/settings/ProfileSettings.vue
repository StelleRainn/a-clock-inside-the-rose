<template>
  <div class="settings-container">
    <el-card class="mb-20">
      <template #header>
        <div class="card-header">
          <span>Edit Profile</span>
        </div>
      </template>
      
      <el-form :model="form" label-width="120px" v-loading="loading">
        <el-form-item label="Avatar URL">
          <el-input v-model="form.avatarUrl" placeholder="https://example.com/avatar.png" />
          <div class="avatar-preview" v-if="form.avatarUrl">
            <el-avatar :size="50" :src="form.avatarUrl" />
          </div>
        </el-form-item>

        <el-form-item label="Username">
          <el-input v-model="form.username" disabled />
          <span class="hint">Username cannot be changed</span>
        </el-form-item>

        <el-form-item label="Nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>

        <el-form-item label="Bio">
          <el-input v-model="form.bio" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item label="Gender">
          <el-radio-group v-model="form.gender">
            <el-radio label="MALE">Male</el-radio>
            <el-radio label="FEMALE">Female</el-radio>
            <el-radio label="OTHER">Other</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="Website">
          <el-input v-model="form.website" placeholder="https://your-site.com" />
        </el-form-item>

        <el-divider content-position="left">Security</el-divider>

        <el-form-item label="New Password">
          <el-input 
            v-model="form.password" 
            type="password" 
            show-password 
            placeholder="Enter new password to change" 
          />
          <div class="hint">Leave blank if you don't want to change it.</div>
        </el-form-item>

        <el-divider content-position="left">AI Configuration</el-divider>

        <el-form-item label="Gemini API Key">
          <el-input 
            v-model="form.geminiApiKey" 
            type="password" 
            show-password 
            placeholder="AIzaSy..." 
          />
          <div class="hint">Required for Intelligent Assistant. Stored securely.</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="saveProfile">Save Changes</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Data Export Section -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>Data Export</span>
        </div>
      </template>
      <div class="export-section">
        <p class="export-desc">Download your personal data in CSV format.</p>
        <div class="export-buttons">
          <el-button @click="handleExportTasks" :loading="exportingTasks">
            <el-icon><List /></el-icon> Export Tasks
          </el-button>
          <el-button @click="handleExportPomodoro" :loading="exportingPomodoro">
            <el-icon><Timer /></el-icon> Export Focus Records
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import { getUserProfile, updateUserProfile } from '@/api/user'
import { exportTasks, exportPomodoro } from '@/api/export'
import { ElMessage } from 'element-plus'
import { List, Timer } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const exportingTasks = ref(false)
const exportingPomodoro = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  avatarUrl: '',
  bio: '',
  gender: '',
  website: '',
  password: '',
  geminiApiKey: ''
})

const fetchProfile = async () => {
  if (!userStore.user || !userStore.user.id) return
  loading.value = true
  try {
    const res = await getUserProfile(userStore.user.id)
    if (res) {
      Object.assign(form, res)
    }
  } catch (e) {
    console.error('Fetch profile error:', e)
    ElMessage.error('Failed to load profile')
  } finally {
    loading.value = false
  }
}

const saveProfile = async () => {
  if (!userStore.user || !userStore.user.id) return
  try {
    const updatedUser = await updateUserProfile(userStore.user.id, form)
    console.log('Update response:', updatedUser) // Debug log
    if (updatedUser) {
      userStore.setUser(updatedUser)
      ElMessage.success('Profile updated successfully')
      form.password = '' // Clear password field after save
    } else {
      console.warn('Updated user is empty')
      // Even if empty, maybe success?
    }
  } catch (e) {
    console.error('Update profile error:', e)
    ElMessage.error('Failed to update profile: ' + (e.message || 'Unknown error'))
  }
}

const downloadFile = (data, filename) => {
  const url = window.URL.createObjectURL(new Blob([data]))
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const handleExportTasks = async () => {
  if (!userStore.user || !userStore.user.id) return
  exportingTasks.value = true
  try {
    const data = await exportTasks(userStore.user.id)
    downloadFile(data, 'my_tasks.csv')
    ElMessage.success('Tasks exported successfully')
  } catch (e) {
    console.error(e)
    ElMessage.error('Failed to export tasks')
  } finally {
    exportingTasks.value = false
  }
}

const handleExportPomodoro = async () => {
  if (!userStore.user || !userStore.user.id) return
  exportingPomodoro.value = true
  try {
    const data = await exportPomodoro(userStore.user.id)
    downloadFile(data, 'my_focus_records.csv')
    ElMessage.success('Focus records exported successfully')
  } catch (e) {
    console.error(e)
    ElMessage.error('Failed to export records')
  } finally {
    exportingPomodoro.value = false
  }
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.settings-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.hint {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}
.avatar-preview {
  margin-top: 10px;
}
.mb-20 {
  margin-bottom: 20px;
}
.export-desc {
  color: #606266;
  margin-bottom: 20px;
}
.export-buttons {
  display: flex;
  gap: 15px;
}
</style>
