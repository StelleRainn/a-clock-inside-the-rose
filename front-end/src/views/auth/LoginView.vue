<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>{{ $t('auth.login') }}</h2>
      </template>
      <el-form :model="form" label-width="120px">
        <el-form-item :label="$t('auth.username')">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item :label="$t('auth.password')">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSubmit">{{ $t('auth.login') }}</el-button>
          <el-button @click="onRegister">{{ $t('auth.register') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const { t } = useI18n()
const userStore = useUserStore()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const onSubmit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning(t('auth.enterUsernamePassword'))
    return
  }
  
  loading.value = true
  try {
    const success = await userStore.login(form)
    if (success) {
      ElMessage.success(t('auth.loginSuccess'))
      router.push('/dashboard')
    } else {
      ElMessage.error(t('auth.loginFailed'))
    }
  } catch {
    ElMessage.error(t('auth.errorOccurred'))
  } finally {
    loading.value = false
  }
}

const onRegister = async () => {
   if (!form.username || !form.password) {
    ElMessage.warning(t('auth.enterToRegister'))
    return
  }
  
  try {
    await register(form)
    ElMessage.success(t('auth.registerSuccess'))
  } catch (e) {
    ElMessage.error(t('auth.registerFailed') + e.message)
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
