import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

// ==========================================
// themeStore: 负责全局黑暗模式 (Dark Mode) 的状态管理与 CSS 变量注入
// ==========================================
export const useThemeStore = defineStore('theme', () => {
  // 核心状态：当前是否为暗色模式
  const isDark = ref(false)

  // 核心执行函数：通过在 HTML 根节点 <html> 上挂载/移除 'dark' 类名
  // 来激活全局的 CSS Dark 变量 (如 Element Plus 的黑暗主题以及 MainLayout 的星空背景)
  const applyTheme = () => {
    if (isDark.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  // 供组件调用的切换开关 (如 HeaderNav 中的主题 Toggle)
  const toggleTheme = () => {
    isDark.value = !isDark.value
  }
  
  // Watch for changes and apply theme automatically
  // 响应式监听：一旦 isDark 发生改变，立即应用新的主题
  watch(isDark, () => {
    applyTheme()
  })

  // Initialize theme
  // 在 App.vue 挂载时调用，确保页面初次加载时主题状态正确
  const initTheme = () => {
    applyTheme()
  }

  return {
    isDark,
    toggleTheme,
    initTheme
  }
}, {
  // 利用 pinia-plugin-persistedstate 插件，自动将 isDark 的状态持久化到 LocalStorage
  persist: true
})
