<template>
  <div class="header-nav-container">
    <!-- Hero Header: Absolute, Transparent, Hero Theme -->
    <header 
      v-if="transparent && !pomodoroStore.isImmersive"
      class="header-nav hero-header" 
      :class="{ 'hero-light': isHeroLight }"
    >
      <div class="nav-left">
        <div class="logo">
          <img src="@/assets/images/ACIR_logo.png" alt="ACIR" class="logo-img" />
        </div>
      </div>
      
      <div class="nav-center">
        <nav class="main-menu">
          <router-link to="/dashboard" class="nav-item" active-class="active">
          {{ $t('nav.focusStation') }}
          </router-link>
          <router-link to="/calendar" class="nav-item" active-class="active">
          {{ $t('nav.calendar') }}
          </router-link>
          <router-link to="/tasks" class="nav-item" active-class="active">
          {{ $t('nav.tasks') }}
          </router-link>
          <router-link to="/stats" class="nav-item" active-class="active">
          {{ $t('nav.stats') }}
          </router-link>
        </nav>
      </div>

      <div class="nav-right">
        <!-- Bilibili Style User Profile -->
        <div class="bili-avatar-wrapper">
          <div class="avatar-transform-container">
            <el-avatar :size="32" :src="avatarUrl" class="bili-avatar">
              {{ username.charAt(0).toUpperCase() }}
            </el-avatar>
          </div>
          
          <div class="bili-dropdown-card">
            <div class="bili-user-info">
              <div class="bili-nickname">{{ username }}</div>
              <div class="bili-level-row">
                <span class="level-label">Lv. {{ userLevel }}</span>
                <el-progress 
                  :percentage="xpProgress" 
                  :show-text="false" 
                  class="level-progress"
                  :stroke-width="8"
                  color="var(--el-color-primary)"
                ></el-progress>
              </div>
            </div>
            <div class="bili-actions">
              <div class="bili-action-item" @click="handleCommand('profile')">
                <el-icon><User /></el-icon>
                <span>{{ $t('nav.profile') }}</span>
              </div>
              <div class="bili-action-item logout-btn" @click="handleCommand('logout')">
                <el-icon><SwitchButton /></el-icon>
                <span>{{ $t('nav.logout') }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Language Toggle -->
        <el-button
          circle
          plain
          @click="toggleLanguage"
          class="hero-btn lang-toggle"
        >
          {{ currentLang === 'en' ? '中' : 'EN' }}
        </el-button>
        <el-switch
          v-model="themeStore.isDark"
          inline-prompt
          :active-icon="Moon"
          :inactive-icon="Sunny"
          class="theme-toggle"
        />
        <el-button circle plain @click="$emit('add-task')" class="hero-btn">
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>
    </header>

    <!-- Fixed Header: Fixed, Glass, System Theme -->
    <header 
      class="header-nav fixed-header" 
      :class="{ 'visible': showFixedHeader && !pomodoroStore.isImmersive }"
    >
      <div class="nav-left">
        <div class="logo">
          <img src="@/assets/images/ACIR_logo.png" alt="ACIR" class="logo-img" />
        </div>
      </div>
      
      <div class="nav-center">
        <nav class="main-menu">
          <router-link to="/dashboard" class="nav-item" active-class="active">
          {{ $t('nav.focusStation') }}
          </router-link>
          <router-link to="/calendar" class="nav-item" active-class="active">
          {{ $t('nav.calendar') }}
          </router-link>
          <router-link to="/tasks" class="nav-item" active-class="active">
          {{ $t('nav.tasks') }}
          </router-link>
          <router-link to="/stats" class="nav-item" active-class="active">
          {{ $t('nav.stats') }}
          </router-link>
        </nav>
      </div>

      <div class="nav-right">
        <!-- Bilibili Style User Profile -->
        <div class="bili-avatar-wrapper">
          <div class="avatar-transform-container">
            <el-avatar :size="32" :src="avatarUrl" class="bili-avatar">
              {{ username.charAt(0).toUpperCase() }}
            </el-avatar>
          </div>
          
          <div class="bili-dropdown-card">
            <div class="bili-user-info">
              <div class="bili-nickname">{{ username }}</div>
              <div class="bili-level-row">
                <span class="level-label">Lv. {{ userLevel }}</span>
                <el-progress 
                  :percentage="xpProgress" 
                  :show-text="false" 
                  class="level-progress"
                  :stroke-width="8"
                  color="var(--el-color-primary)"
                ></el-progress>
              </div>
            </div>
            <div class="bili-actions">
              <div class="bili-action-item" @click="handleCommand('profile')">
                <el-icon><User /></el-icon>
                <span>{{ $t('nav.profile') }}</span>
              </div>
              <div class="bili-action-item logout-btn" @click="handleCommand('logout')">
                <el-icon><SwitchButton /></el-icon>
                <span>{{ $t('nav.logout') }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Language Toggle -->
        <el-button
          circle
          plain
          @click="toggleLanguage"
          class="lang-toggle"
        >
          {{ currentLang === 'en' ? '中' : 'EN' }}
        </el-button>
        <el-switch
          v-model="themeStore.isDark"
          inline-prompt
          :active-icon="Moon"
          :inactive-icon="Sunny"
          class="theme-toggle"
        />
        <el-button circle plain @click="$emit('add-task')">
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>
    </header>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { usePomodoroStore } from '@/stores/pomodoro'
import { Moon, Sunny, Plus } from '@element-plus/icons-vue'
import { getUserProfile } from '@/api/user'
import { getUserStats } from '@/api/gamification'
import { FastAverageColor } from 'fast-average-color'
import { User, SwitchButton } from '@element-plus/icons-vue'

const props = defineProps({
  transparent: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()
const { locale } = useI18n()
const userStore = useUserStore()
const themeStore = useThemeStore()
const pomodoroStore = usePomodoroStore()
const fac = new FastAverageColor()

const isScrolled = ref(false)
const username = computed(() => userStore.user?.username || 'User')
const avatarUrl = ref('')
const analyzedTheme = ref('light')

// Gamification Data
const userLevel = ref(1)
const currentXp = ref(0)
const nextLevelXp = ref(100)
const xpProgress = computed(() => {
  if (nextLevelXp.value === 0) return 0
  return Math.min(100, Math.round((currentXp.value / nextLevelXp.value) * 100))
})

// Language Toggle Logic
const currentLang = computed(() => locale.value)
const toggleLanguage = () => {
  const newLang = locale.value === 'en' ? 'zh' : 'en'
  locale.value = newLang
  localStorage.setItem('language', newLang)
}

// Hero Theme Logic
const currentHeroTheme = computed(() => {
  if (pomodoroStore.heroTheme === 'auto') {
    return analyzedTheme.value
  }
  return pomodoroStore.heroTheme
})

const isHeroLight = computed(() => {
  if (!pomodoroStore.backgroundImage) {
    return true // Minimal Black -> Light Text
  }
  return currentHeroTheme.value === 'light'
})

const showFixedHeader = computed(() => {
  // If not transparent (e.g. Tasks/Stats page), always show fixed header
  if (!props.transparent) return true
  // If transparent (Dashboard), show fixed header only when scrolled past threshold
  return isScrolled.value
})

// Analyze background image
const analyzeBackground = () => {
  if (!pomodoroStore.backgroundImage) {
    analyzedTheme.value = themeStore.isDark ? 'light' : 'dark'
    return
  }
  const img = new Image()
  if (pomodoroStore.backgroundImage === 'custom') {
    img.src = pomodoroStore.customBgUrl
  } else {
    img.src = `/backgrounds/${pomodoroStore.backgroundImage}`
  }
  img.crossOrigin = "Anonymous"
  img.onload = () => {
    const color = fac.getColor(img)
    analyzedTheme.value = color.isDark ? 'light' : 'dark'
  }
}

watch(() => pomodoroStore.backgroundImage, analyzeBackground, { immediate: true })
watch(() => themeStore.isDark, () => {
  if (!pomodoroStore.backgroundImage) {
     analyzedTheme.value = themeStore.isDark ? 'light' : 'dark'
  }
})

const handleScroll = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0
  // Threshold can be header height (60px) or more
  isScrolled.value = scrollTop > 80
}

const fetchUserProfile = async () => {
  if (!userStore.user || !userStore.user.id) return
  try {
    const res = await getUserProfile(userStore.user.id)
    if (res) {
      avatarUrl.value = res.avatarUrl
    }
  } catch (e) {
    console.error('Failed to fetch user profile', e)
  }
}

const fetchUserStats = async () => {
  if (!userStore.user || !userStore.user.id) return
  try {
    const res = await getUserStats(userStore.user.id)
    if (res) {
      userLevel.value = res.level || 1
      currentXp.value = res.currentXp || 0
      nextLevelXp.value = res.nextLevelXp || 100
    }
  } catch (e) {
    console.error('Failed to fetch user stats', e)
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

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  fetchUserProfile()
  fetchUserStats()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.header-nav {
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  z-index: 1000;
  width: 100%;
  box-sizing: border-box;
  letter-spacing: 2px;
}

/* =========================================
   1. Hero Header (Absolute, Transparent)
   ========================================= */
.hero-header {
  position: absolute;
  top: 0;
  left: 0;
  background: transparent;
  border-bottom: none;
  z-index: 1000; /* Ensure it is above content */
}

/* Hero Light Text Theme */
.hero-header.hero-light .logo-text {
  color: white;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}
.hero-header.hero-light .nav-item {
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}
.hero-header.hero-light .nav-item:hover,
.hero-header.hero-light .nav-item.active {
  color: white;
  transform: translateY(-2px);
  font-size: 17px;
  text-shadow: 0 0 8px rgba(255, 255, 255, 0.8), 0 0 12px rgba(255, 255, 255, 0.4);
}
/* Hero Button adjustments */
.hero-header.hero-light .hero-btn {
  border-color: rgba(255, 255, 255, 0.5);
  color: white;
  background: rgba(255, 255, 255, 0.1);
}
.hero-header.hero-light .hero-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* Hero Dark Text Theme */
.hero-header:not(.hero-light) .logo-text {
  color: #303133;
}
.hero-header:not(.hero-light) .nav-item {
  color: #606266;
}
.hero-header:not(.hero-light) .nav-item:hover,
.hero-header:not(.hero-light) .nav-item.active {
  color: var(--el-color-primary);
  transform: translateY(-2px);
  font-size: 17px;
  text-shadow: 0 0 8px rgba(var(--el-color-primary-rgb), 0.6), 0 0 12px rgba(var(--el-color-primary-rgb), 0.3);
}

/* =========================================
   2. Fixed Header (Fixed, Glass, System)
   ========================================= */
.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.1);
  z-index: 1001; /* Above hero header */
  
  /* Transition for slide in/out */
  transform: translateY(-100%);
  transition: transform 0.3s ease, opacity 0.3s ease;
  opacity: 0;
}

.fixed-header.visible {
  transform: translateY(0);
  opacity: 1;
}

/* Dark mode for fixed header */
.dark .fixed-header {
  background: rgba(0, 0, 0, 0.6);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

/* Fixed Header Typography (System Theme) */
.fixed-header .logo-text {
  color: var(--el-text-color-primary);
}
.fixed-header .nav-item {
  color: var(--el-text-color-secondary);
}
.fixed-header .nav-item:hover {
  color: var(--el-color-primary);
  transform: translateY(-2px);
  font-size: 17px;
  text-shadow: 0 0 8px rgba(var(--el-color-primary-rgb), 0.6), 0 0 12px rgba(var(--el-color-primary-rgb), 0.3);
}
.fixed-header .nav-item.active {
  color: var(--el-color-primary);
  transform: translateY(-2px);
  font-size: 17px;
  text-shadow: 0 0 8px rgba(var(--el-color-primary-rgb), 0.6), 0 0 12px rgba(var(--el-color-primary-rgb), 0.3);
}


/* Common Layout (Duplicated from before) */
.nav-left, .nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 200px;
}
.nav-right {
  justify-content: flex-end;
}
.nav-center {
  flex: 1;
  display: flex;
  justify-content: center;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 18px;
}
.logo-img {
  height: 48px;
  width: auto;
  object-fit: contain;
}
.main-menu {
  display: flex;
  gap: 32px;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  text-decoration: none;
  font-weight: 500;
  font-size: 15px;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.2s;
}
/* Bilibili Avatar Dropdown Styles */
.bili-avatar-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  height: 100%;
  cursor: pointer;
  margin-right: 8px;
}

.avatar-transform-container {
  position: relative;
  z-index: 2000;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  transform-origin: center center;
}

.bili-avatar-wrapper:hover .avatar-transform-container {
  /* Scale up and move left-down */
  transform: scale(2) translate(-10px, 10px);
}

.bili-dropdown-card {
  position: absolute;
  top: 100%;
  left: calc(50% - 20px); /* Center matches the translated avatar (scale(2) translates -20px visually) */
  transform: translateX(-50%) translateY(10px) scale(0.95);
  transform-origin: top center;
  width: 280px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.6);
  padding: 40px 20px 20px; /* Top padding to make room for avatar */
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  z-index: 1999;
  display: flex;
  flex-direction: column;
  align-items: center;
  pointer-events: none; /* Prevent interacting when hidden */
}

/* Hover bridge */
.bili-avatar-wrapper::after {
  content: '';
  position: absolute;
  top: 100%;
  left: -40px;
  right: -40px;
  height: 30px;
}

.bili-avatar-wrapper:hover .bili-dropdown-card {
  opacity: 1;
  visibility: visible;
  transform: translateX(-50%) translateY(0px) scale(1);
  pointer-events: auto;
}

html.dark .bili-dropdown-card {
  background: rgba(30, 30, 30, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.4);
}

.bili-user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  margin-bottom: 16px;
}

.bili-nickname {
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 10px;
  letter-spacing: 1px;
}

.bili-level-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  width: 100%;
}

.level-label {
  font-size: 14px;
  font-weight: 800;
  color: var(--el-color-primary);
  font-style: italic;
}

.level-progress {
  width: 120px;
}
.level-progress :deep(.el-progress-bar__outer) {
  background-color: var(--el-color-primary-light-8);
}

.bili-actions {
  display: flex;
  width: 100%;
  gap: 12px;
}

.bili-action-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 8px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--el-text-color-regular);
  background: rgba(0, 0, 0, 0.03);
}

html.dark .bili-action-item {
  background: rgba(255, 255, 255, 0.05);
}

.bili-action-item:hover {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  transform: translateY(-2px);
}

html.dark .bili-action-item:hover {
  background: rgba(var(--el-color-primary-rgb), 0.2);
}

.bili-action-item.logout-btn:hover {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.bili-action-item .el-icon {
  font-size: 22px;
}

.bili-action-item span {
  font-size: 13px;
  font-weight: 500;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .header-nav {
    padding: 0 16px;
  }
  
  .nav-center {
    display: none; /* Hide main menu links on mobile */
  }
  
  .nav-left {
    width: auto;
  }
  
  .nav-right {
    width: auto;
    flex: 1;
  }
  
  /* Hide logo text on mobile */
  .logo-text {
    display: none; 
  }

  /* Make sure nav-right is aligned properly */
  .nav-right {
    width: auto;
    flex: 1;
    justify-content: flex-end;
  }

  /* Hide the "Add Task" circle button specifically, but NOT the theme toggle */
  /* The theme toggle is an el-switch, not el-button */
  /* The add button is el-button circle plain */
  
  .nav-right .el-button.is-circle {
    display: none;
  }
}
</style>
