import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi } from '@/api/auth'

// ==========================================
// userStore: 负责全局用户鉴权状态、基础信息的存储与登录/登出流转
// ==========================================
export const useUserStore = defineStore('user', () => {
  // 核心状态：当前登录的用户对象信息。初始化时尝试从本地存储恢复，实现“持久化登录”效果
  const user = ref(JSON.parse(localStorage.getItem('user')) || null)
  
  // 核心状态：JWT Token。初始化时同样尝试从本地存储恢复，用于请求拦截器的鉴权头注入
  const token = ref(localStorage.getItem('token') || '')

  // 供组件调用：手动更新/同步最新的用户基础信息，并持久化到本地
  function setUser(userData) {
    user.value = userData
    localStorage.setItem('user', JSON.stringify(userData))
  }

  // 登录流程控制：接收 Login.vue 传来的表单数据，调用后端 Auth 接口
  async function login(loginForm) {
    try {
      const data = await loginApi(loginForm)
      // data should be the User object returned from backend
      // 如果后端接口正常返回了用户数据，将其更新到全局状态中
      setUser(data)
      // If backend returns a token, set it here. For now, we simulate or just use user data.
      // 如果后端启用了 JWT，这里还需要同时解析并写入 token.value 和 localStorage
      return true // 告诉调用方登录成功
    } catch (error) {
      console.error(error)
      return false // 登录失败，由组件负责抛出错误提示 (如 ElMessage)
    }
  }

  // 登出流程控制：清空内存中的全局变量，并彻底抹除浏览器本地存储中的鉴权数据
  function logout() {
    user.value = null
    token.value = ''
    localStorage.removeItem('user')
    localStorage.removeItem('token')
  }

  return {
    user,
    token,
    login,
    logout,
    setUser
  }
})
