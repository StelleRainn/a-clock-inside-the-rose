import { createI18n } from 'vue-i18n'
import en from './en'
import zh from './zh'

// 1. 注册语言包字典
const messages = {
  en,
  zh
}

// 2. 初始化时，从本地存储中读取用户上次选择的语言，默认回退到英文 (en)
const savedLanguage = localStorage.getItem('language') || 'en'

// 3. 创建 i18n 实例
const i18n = createI18n({
  legacy: false, // 强制使用 Vue 3 的 Composition API 模式 (支持 useI18n 钩子)
  locale: savedLanguage, // 设置当前语言
  fallbackLocale: 'en',  // 如果当前语言包中缺失某个 key，回退到英文查找
  messages,
})

export default i18n
