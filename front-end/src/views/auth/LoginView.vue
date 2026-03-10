<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>Login</h2>
      </template>
      <el-form :model="form" label-width="120px">
        <el-form-item label="Username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="Password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSubmit">Login</el-button>
          <el-button @click="onRegister">Register</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const onSubmit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('Please enter username and password')
    return
  }
  
  loading.value = true
  try {
    const success = await userStore.login(form)
    if (success) {
      ElMessage.success('Login successful')
      router.push('/dashboard')
    } else {
      ElMessage.error('Login failed')
    }
  } catch {
    ElMessage.error('An error occurred')
  } finally {
    loading.value = false
  }
}

const onRegister = async () => {
   if (!form.username || !form.password) {
    ElMessage.warning('Please enter username and password to register')
    return
  }
  
  try {
    await register(form)
    ElMessage.success('Registration successful! Please login.')
  } catch (e) {
    ElMessage.error('Registration failed: ' + e.message)
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: var(--el-bg-color-page);
}
.login-card {
  width: 400px;
}
</style>
