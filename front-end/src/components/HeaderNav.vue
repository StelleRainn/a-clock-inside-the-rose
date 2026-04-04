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
          <el-icon :size="24" :color="heroLogoColor"><Timer /></el-icon>
          <span class="logo-text">ACIR</span>
        </div>
      </div>
      
      <div class="nav-center">
        <nav class="main-menu">
          <router-link to="/dashboard" class="nav-item" active-class="active">
            <el-icon><Odometer /></el-icon> {{ $t('nav.focusStation') }}
          </router-link>
          <router-link to="/calendar" class="nav-item" active-class="active">
            <el-icon><Calendar /></el-icon> {{ $t('nav.calendar') }}
          </router-link>
          <router-link to="/tasks" class="nav-item" active-class="active">
            <el-icon><List /></el-icon> {{ $t('nav.tasks') }}
          </router-link>
          <router-link to="/stats" class="nav-item" active-class="active">
            <el-icon><TrendCharts /></el-icon> {{ $t('nav.stats') }}
          </router-link>
        </nav>
      </div>

      <div class="nav-right">
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
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-profile">
            <el-avatar :size="32" :src="avatarUrl" class="user-avatar">
              {{ username.charAt(0).toUpperCase() }}
            </el-avatar>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">{{ $t('nav.profile') }}</el-dropdown-item>
              <el-dropdown-item divided command="logout">{{ $t('nav.logout') }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- Fixed Header: Fixed, Glass, System Theme -->
    <header 
      class="header-nav fixed-header" 
      :class="{ 'visible': showFixedHeader && !pomodoroStore.isImmersive }"
    >
      <div class="nav-left">
        <div class="logo">
          <el-icon :size="24" color="#409eff"><Timer /></el-icon>
          <span class="logo-text">ACIR</span>
        </div>
      </div>
      
      <div class="nav-center">
        <nav class="main-menu">
          <router-link to="/dashboard" class="nav-item" active-class="active">
            <el-icon><Odometer /></el-icon> {{ $t('nav.focusStation') }}
          </router-link>
          <router-link to="/calendar" class="nav-item" active-class="active">
            <el-icon><Calendar /></el-icon> {{ $t('nav.calendar') }}
          </router-link>
          <router-link to="/tasks" class="nav-item" active-class="active">
            <el-icon><List /></el-icon> {{ $t('nav.tasks') }}
          </router-link>
          <router-link to="/stats" class="nav-item" active-class="active">
            <el-icon><TrendCharts /></el-icon> {{ $t('nav.stats') }}
          </router-link>
        </nav>
      </div>

      <div class="nav-right">
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
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-profile">
            <el-avatar :size="32" :src="avatarUrl" class="user-avatar">
              {{ username.charAt(0).toUpperCase() }}
            </el-avatar>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">{{ $t('nav.profile') }}</el-dropdown-item>
              <el-dropdown-item divided command="logout">{{ $t('nav.logout') }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
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
import { Moon, Sunny, Timer, Odometer, List, TrendCharts, Plus, Calendar } from '@element-plus/icons-vue'
import { getUserProfile } from '@/api/user'
import { FastAverageColor } from 'fast-average-color'

const props = defineProps({
  transparent: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()
const { t, locale } = useI18n()
const userStore = useUserStore()
const themeStore = useThemeStore()
const pomodoroStore = usePomodoroStore()
const fac = new FastAverageColor()

const isScrolled = ref(false)
const username = computed(() => userStore.user?.username || 'User')
const avatarUrl = ref('')
const analyzedTheme = ref('light')

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

const heroLogoColor = computed(() => isHeroLight.value ? 'white' : '#303133')

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
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.header-nav-container {
  /* Wrapper to hold both headers */
}

.header-nav {
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  z-index: 1000;
  width: 100%;
  box-sizing: border-box;
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
  background: rgba(255, 255, 255, 0.2);
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
  background: rgba(0, 0, 0, 0.05);
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
  background: rgba(var(--el-color-primary-rgb), 0.1);
}
.fixed-header .nav-item.active {
  color: var(--el-color-primary);
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
.user-profile {
  cursor: pointer;
  transition: transform 0.2s;
}
.user-profile:hover {
  transform: scale(1.05);
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
