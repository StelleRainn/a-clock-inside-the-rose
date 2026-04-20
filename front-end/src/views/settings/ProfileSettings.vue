<template>
  <div class="settings-container">
    <!-- 
    ProfileSettings: 用户个人中心与数据管理页面。
    包含“个人信息展示/编辑双模式切换”以及“用户数据导出”两大核心功能。
  -->
    <el-card class="mb-20">
      <template #header>
        <div class="card-header">
          <!-- 动态标题：根据 isEditing 状态自适应显示 -->
          <span>{{ isEditing ? $t('profile.editProfile') : $t('profile.profile') }}</span>
        </div>
      </template>
      
      <!-- 
        1. 纯展示模式 (Display Mode)
        仅当 isEditing 为 false 时渲染，提供类似名片的视觉体验。
      -->
      <div v-if="!isEditing" class="profile-display" v-loading="loading">
        <!-- 头部：头像与简介 -->
        <div class="profile-header-display">
          <el-avatar :size="200" :src="form.avatarUrl" />
          <!-- 如果没有设置昵称，降级显示用户名 -->
          <h2 class="profile-nickname">{{ form.nickname || form.username }}</h2>
          <p class="profile-bio">{{ form.bio || $t('profile.emptyBio') }}</p>
          <!-- 点击进入编辑模式 -->
          <el-button type="primary" round @click="isEditing = true" class="edit-btn">{{ $t('profile.editProfile') }}</el-button>
        </div>
        
        <!-- 详情列表：使用图标+键值对的方式展示详细信息 -->
        <div class="profile-details-list">
          <div class="detail-item">
            <el-icon><User /></el-icon>
            <span class="detail-label">{{ $t('profile.username') }}:</span>
            <span class="detail-value">{{ form.username }}</span>
          </div>
          <div class="detail-item">
            <!-- 动态图标：根据性别字段显示不同图标 -->
            <el-icon v-if="form.gender === 'MALE'"><Male /></el-icon>
            <el-icon v-else-if="form.gender === 'FEMALE'"><Female /></el-icon>
            <el-icon v-else><User /></el-icon>
            <span class="detail-label">{{ $t('profile.gender') }}:</span>
            <span class="detail-value">
              {{ form.gender === 'MALE' ? $t('profile.male') : form.gender === 'FEMALE' ? $t('profile.female') : form.gender === 'OTHER' ? $t('profile.other') : $t('profile.unknown') }}
            </span>
          </div>
          <div class="detail-item">
            <el-icon><Link /></el-icon>
            <span class="detail-label">{{ $t('profile.website') }}:</span>
            <span class="detail-value">
              <a v-if="form.website" :href="form.website" target="_blank" class="profile-link">{{ form.website }}</a>
              <span v-else class="text-muted">{{ $t('profile.notProvided') }}</span>
            </span>
          </div>
          <!-- 敏感信息状态展示：仅显示是否配置，不显示具体 Key -->
          <div class="detail-item">
            <el-icon><Key /></el-icon>
            <span class="detail-label">{{ $t('profile.apiKey') }}:</span>
            <span class="detail-value">
              <el-tag v-if="form.geminiApiKey" type="success" size="small" round>{{ $t('profile.configured') }}</el-tag>
              <el-tag v-else type="info" size="small" round>{{ $t('profile.notConfigured') }}</el-tag>
            </span>
          </div>
        </div>
      </div>

      <!-- 
        2. 表单编辑模式 (Edit Mode)
        通过 v-model 绑定 reactive 状态 form，支持全量字段更新。
      -->
      <el-form v-else :model="form" label-width="120px" v-loading="loading">
        <el-form-item :label="$t('profile.avatarUrl')">
          <el-input v-model="form.avatarUrl" placeholder="https://example.com/avatar.png" />
          <div class="avatar-preview" v-if="form.avatarUrl">
            <el-avatar :size="50" :src="form.avatarUrl" />
          </div>
        </el-form-item>

        <el-form-item :label="$t('profile.username')">
          <!-- 用户名作为系统唯一标识，禁止修改 -->
          <el-input v-model="form.username" disabled />
          <span class="hint">{{ $t('profile.usernameHint') }}</span>
        </el-form-item>

        <el-form-item :label="$t('profile.nickname')">
          <el-input v-model="form.nickname" />
        </el-form-item>

        <el-form-item :label="$t('profile.bio')">
          <el-input v-model="form.bio" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item :label="$t('profile.gender')">
          <el-radio-group v-model="form.gender">
            <el-radio label="MALE">{{ $t('profile.male') }}</el-radio>
            <el-radio label="FEMALE">{{ $t('profile.female') }}</el-radio>
            <el-radio label="OTHER">{{ $t('profile.other') }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item :label="$t('profile.website')">
          <el-input v-model="form.website" placeholder="https://your-site.com" />
        </el-form-item>

        <!-- 安全区域：密码修改 -->
        <el-divider content-position="left">{{ $t('profile.security') }}</el-divider>

        <el-form-item :label="$t('profile.newPassword')">
          <!-- 只有输入了值才会触发后端的密码修改逻辑 -->
          <el-input 
            v-model="form.password" 
            type="password" 
            show-password 
            placeholder="Enter new password to change" 
          />
          <div class="hint">{{ $t('profile.passwordHint') }}</div>
        </el-form-item>

        <!-- AI 助手配置区 -->
        <el-divider content-position="left">{{ $t('profile.aiConfig') }}</el-divider>

        <el-form-item :label="$t('profile.geminiApiKey')">
          <el-input 
            v-model="form.geminiApiKey" 
            type="password" 
            show-password 
            placeholder="AIzaSy..." 
          />
          <div class="hint">{{ $t('profile.geminiHint') }}</div>
        </el-form-item>

        <!-- 表单动作 -->
        <el-form-item>
          <el-button type="primary" @click="saveProfile">{{ $t('profile.saveChanges') }}</el-button>
          <el-button @click="isEditing = false">{{ $t('profile.cancel') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 
      3. 数据导出区 (Data Export Section)
      为了保护用户数据主权，提供全量任务与番茄钟数据的 CSV 导出功能。
    -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ $t('profile.dataExport') }}</span>
        </div>
      </template>
      <div class="export-section">
        <p class="export-desc">{{ $t('profile.exportDesc') }}</p>
        <div class="export-buttons">
          <el-button @click="handleExportTasks" :loading="exportingTasks">
            <el-icon><List /></el-icon> {{ $t('profile.exportTasks') }}
          </el-button>
          <el-button @click="handleExportPomodoro" :loading="exportingPomodoro">
            <el-icon><Timer /></el-icon> {{ $t('profile.exportFocusRecords') }}
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { getUserProfile, updateUserProfile } from '@/api/user'
import { exportTasks, exportPomodoro } from '@/api/export'
import { ElMessage } from 'element-plus'
import { List, Timer, User, Male, Female, Link, Key } from '@element-plus/icons-vue'

const { t } = useI18n()
const userStore = useUserStore()

// UI 控制状态
const loading = ref(false)
const exportingTasks = ref(false)
const exportingPomodoro = ref(false)
const isEditing = ref(false) // 核心开关：控制名片展示与表单编辑的切换

// 响应式表单数据容器
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

// ==========================================
// 数据拉取与保存逻辑
// ==========================================
const fetchProfile = async () => {
  if (!userStore.user || !userStore.user.id) return
  loading.value = true
  try {
    const res = await getUserProfile(userStore.user.id)
    if (res) {
      // 提取后端返回的数据并合并入本地响应式 form 中
      Object.assign(form, res)
    }
  } catch (e) {
    console.error('Fetch profile error:', e)
    ElMessage.error(t('profile.loadFailed'))
  } finally {
    loading.value = false
  }
}

const saveProfile = async () => {
  if (!userStore.user || !userStore.user.id) return
  try {
    // 全量更新用户信息
    const updatedUser = await updateUserProfile(userStore.user.id, form)
    if (updatedUser) {
      // 同步更新 Pinia Store 中的全局用户信息（比如头部导航栏的头像需要立即响应）
      userStore.setUser(updatedUser)
      ElMessage.success(t('profile.updateSuccess'))
      form.password = '' // 安全起见：保存成功后清空前端的明文密码字段
      isEditing.value = false // 自动退出编辑模式
    }
  } catch (e) {
    console.error('Update profile error:', e)
    ElMessage.error(t('profile.updateFailed') + (e.message || 'Unknown error'))
  }
}

// ==========================================
// 数据导出核心逻辑 (Blob URL 生成机制)
// ==========================================
// 将后端返回的 CSV 文本数据流转化为前端可下载的文件
const downloadFile = (data, filename) => {
  // 1. 利用 Blob 对象将纯文本封装为二进制文件对象
  // 2. createObjectURL 为这个对象生成一个临时的浏览器内部 URL (blob:http://...)
  const url = window.URL.createObjectURL(new Blob([data]))
  
  // 3. 创建一个隐藏的 <a> 标签，模拟用户点击下载
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click() // 触发下载
  
  // 4. 清理 DOM 节点
  document.body.removeChild(link)
}

const handleExportTasks = async () => {
  if (!userStore.user || !userStore.user.id) return
  exportingTasks.value = true
  try {
    const data = await exportTasks(userStore.user.id) // 返回 CSV 字符串
    downloadFile(data, 'my_tasks.csv')
    ElMessage.success(t('profile.tasksExported'))
  } catch (e) {
    console.error(e)
    ElMessage.error(t('profile.exportTasksFailed'))
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
    ElMessage.success(t('profile.recordsExported'))
  } catch (e) {
    console.error(e)
    ElMessage.error(t('profile.exportRecordsFailed'))
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
/* Glassmorphism styling for cards */
:deep(.el-card) {
  border-radius: 16px;
  background: var(--el-bg-color-overlay);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid var(--el-border-color-lighter);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
}
:deep(.el-card__header) {
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.card-header {
  font-weight: 600;
  font-size: 16px;
}
.hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-left: 10px;
}
.avatar-preview {
  margin-top: 10px;
}
.mb-20 {
  margin-bottom: 20px;
}
.export-desc {
  color: var(--el-text-color-regular);
  margin-bottom: 20px;
}
.export-buttons {
  display: flex;
  gap: 15px;
}
.profile-display {
  padding: 10px;
}
.profile-header-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40px;
}
.profile-nickname {
  margin: 20px 0 8px;
  font-size: 28px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  letter-spacing: 0.5px;
}
.profile-bio {
  margin: 0 0 24px;
  font-size: 15px;
  color: var(--el-text-color-regular);
  text-align: center;
  max-width: 80%;
  line-height: 1.5;
}
.edit-btn {
  padding: 10px 30px;
  font-weight: 600;
}
.profile-details-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0 20px;
}
.detail-item {
  display: flex;
  align-items: center;
  font-size: 15px;
  color: var(--el-text-color-regular);
}
.detail-item .el-icon {
  font-size: 18px;
  margin-right: 12px;
  color: var(--el-text-color-secondary);
}
.detail-label {
  width: 100px;
  color: var(--el-text-color-secondary);
}
.detail-value {
  color: var(--el-text-color-primary);
  font-weight: 500;
}
.profile-link {
  color: var(--el-color-primary);
  text-decoration: none;
  transition: opacity 0.2s;
}
.profile-link:hover {
  text-decoration: underline;
  opacity: 0.8;
}
.text-muted {
  color: var(--el-text-color-placeholder);
  font-style: italic;
}
</style>
