import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(false)

  const applyTheme = () => {
    if (isDark.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  const toggleTheme = () => {
    isDark.value = !isDark.value
  }
  
  // Watch for changes and apply theme automatically
  watch(isDark, () => {
    applyTheme()
  })

  // Initialize theme
  const initTheme = () => {
    applyTheme()
  }

  return {
    isDark,
    toggleTheme,
    initTheme
  }
}, {
  persist: true
})
