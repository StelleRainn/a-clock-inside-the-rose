<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical-demo"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>Dashboard</span>
        </el-menu-item>
        <el-menu-item index="/tasks">
          <el-icon><List /></el-icon>
          <span>Tasks</span>
        </el-menu-item>
        <el-menu-item index="/pomodoro">
          <el-icon><Timer /></el-icon>
          <span>Pomodoro</span>
        </el-menu-item>
        <el-menu-item index="/stats">
          <el-icon><TrendCharts /></el-icon>
          <span>Statistics</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>A Clock Inside the Rose</h2>
          <div class="right-panel">
            <!-- Dark Mode Toggle -->
            <el-switch
              v-model="themeStore.isDark"
              inline-prompt
              :active-icon="Moon"
              :inactive-icon="Sunny"
              style="margin-right: 16px"
            />

            <!-- User Level Display -->
            <div v-if="userLevel" class="level-container">
              <div class="level-info">
                <span class="level-text" :style="{ color: levelColor }">Lv. {{ userLevel }}</span>
                <span class="xp-text">{{ currentXp }} / {{ nextLevelXp }}</span>
              </div>
              <div class="xp-bar-bg">
                <div 
                  class="xp-bar-fill" 
                  :style="{ width: xpPercentage + '%', backgroundColor: levelColor }"
                ></div>
              </div>
            </div>
            
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link">
                <el-avatar :size="32" :src="avatarUrl" class="user-avatar">
                  {{ username.charAt(0).toUpperCase() }}
                </el-avatar>
                <span class="username-text">{{ nickname || username }}</span>
                <el-icon class="el-icon--right">
                  <arrow-down />
                </el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">Profile Settings</el-dropdown-item>
                  <el-dropdown-item divided command="logout">Logout</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { Moon, Sunny } from '@element-plus/icons-vue'
import { getUserStats } from '@/api/gamification'
import { getUserProfile } from '@/api/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const activeMenu = computed(() => route.path)
const username = computed(() => userStore.user?.username || 'User')

// Profile Data
const nickname = ref('')
const avatarUrl = ref('')

const userLevel = ref(null)
const currentXp = ref(0)
const nextLevelXp = ref(100)
let pollInterval = null

const xpPercentage = computed(() => {
  if (!nextLevelXp.value) return 0
  return Math.min((currentXp.value / nextLevelXp.value) * 100, 100)
})

const levelColor = computed(() => {
  const level = userLevel.value || 1
  if (level >= 50) return '#ff4949' // Red
  if (level >= 30) return '#f7ba2a' // Orange
  if (level >= 20) return '#a0cfff' // Light Blue
  if (level >= 10) return '#67c23a' // Green
  return '#409eff' // Blue
})

const fetchUserLevel = async () => {
  if (!userStore.user || !userStore.user.id) return
  try {
    const res = await getUserStats(userStore.user.id)
    if (res) {
      userLevel.value = res.level
      currentXp.value = res.currentXp
      nextLevelXp.value = res.nextLevelXp
    }
  } catch (e) {
    console.error('Failed to fetch user level', e)
  }
}

const fetchUserProfile = async () => {
  if (!userStore.user || !userStore.user.id) return
  try {
    const res = await getUserProfile(userStore.user.id)
    if (res) {
      nickname.value = res.nickname
      avatarUrl.value = res.avatarUrl
    }
  } catch (e) {
    console.error('Failed to fetch user profile', e)
  }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

// Watch for route changes to refresh profile if user navigates back from settings
watch(() => route.path, (newPath) => {
  if (newPath !== '/profile') {
    fetchUserProfile()
  }
})

onMounted(() => {
  fetchUserLevel()
  fetchUserProfile()
  pollInterval = setInterval(fetchUserLevel, 10000) 
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.el-aside {
  background-color: var(--el-bg-color-page);
  border-right: 1px solid var(--el-border-color-light);
  transition: background-color 0.3s, border-color 0.3s;
}
.el-header {
  background-color: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: background-color 0.3s, border-color 0.3s;
}
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.right-panel {
  display: flex;
  align-items: center;
  gap: 25px;
}
.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}
.username-text {
  font-weight: 500;
  color: var(--el-text-color-regular);
}

/* Level Badge Styles */
.level-container {
  display: flex;
  flex-direction: column;
  width: 120px;
}
.level-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  margin-bottom: 4px;
  font-weight: bold;
}
.xp-text {
  color: var(--el-text-color-secondary);
  font-size: 10px;
  font-weight: normal;
}
.xp-bar-bg {
  width: 100%;
  height: 6px;
  background-color: var(--el-border-color-lighter);
  border-radius: 3px;
  overflow: hidden;
}
.xp-bar-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease, background-color 0.3s ease;
}
</style>
