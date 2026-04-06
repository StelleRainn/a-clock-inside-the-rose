<template>
  <div class="main-layout" :class="{ 'is-dashboard': isDashboard }">
    <!-- Dynamic Background -->
    <div class="background-layer"></div>
    
    <!-- Top Navigation (Desktop) -->
    <HeaderNav 
      class="desktop-nav"
      @add-task="showAddTaskDialog" 
      :transparent="isDashboard"
    />
    
    <!-- Main Content -->
    <main class="content-wrapper">
      <router-view v-slot="{ Component }">
        <transition name="fade-transform" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
      <GlobalFooter />
    </main>

    <!-- Bottom Navigation (Mobile) -->
    <MobileNavBar 
      class="mobile-nav"
      @add-task="showAddTaskDialog"
    />

    <!-- Global Task Dialog -->
    <TaskFormDialog
      v-model:visible="dialogVisible"
      :is-edit="false"
      @submit="handleTaskSubmit"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import HeaderNav from '@/components/HeaderNav.vue'
import MobileNavBar from '@/components/MobileNavBar.vue'
import TaskFormDialog from '@/components/TaskFormDialog.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
import { createTask } from '@/api/task'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const dialogVisible = ref(false)

const isDashboard = computed(() => {
  return route.name === 'dashboard' || route.path === '/dashboard'
})

const showAddTaskDialog = () => {
  dialogVisible.value = true
}

const handleTaskSubmit = async (payload) => {
  if (!userStore.user || !userStore.user.id) return
  try {
    await createTask({ ...payload, userId: userStore.user.id })
    ElMessage.success('Task created successfully')
    dialogVisible.value = false
    // Ideally we should trigger a refresh in the active view, 
    // but for now this global add is a "fire and forget" or we can use an event bus/store action
  } catch {
    ElMessage.error('Failed to create task')
  }
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

.background-layer {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: -1;
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); /* Default gradient */
  transition: background 1s ease;
}

.dark .background-layer {
  background: linear-gradient(135deg, #0f2027 0%, #203a43 50%, #2c5364 100%);
}

.content-wrapper {
  padding-top: 80px; /* Default for non-dashboard pages */
  min-height: 100vh;
  box-sizing: border-box;
  transition: padding-top 0.3s;
}

/* On dashboard, remove top padding so content (and background) starts at top */
.main-layout.is-dashboard .content-wrapper {
  padding-top: 0;
}

/* Page Transition */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* Mobile Adaptation */
.mobile-nav {
  display: none;
}

@media (max-width: 768px) {
  /* We no longer hide desktop-nav (HeaderNav) entirely, 
     instead we let HeaderNav component handle its responsive state (hiding menu links) 
  */
  .desktop-nav {
    /* display: none !important;  <-- REMOVED */
  }
  
  .mobile-nav {
    display: flex !important;
  }
  
  /* Adjust content wrapper for mobile */
  .content-wrapper {
    /* padding-top: 20px; <-- Changed logic below */
    padding-bottom: 80px; /* Space for bottom nav */
  }

  /* 
     If we show HeaderNav on mobile:
     - It takes 60px height.
     - So padding-top should be ~60px for non-dashboard pages.
  */
  .content-wrapper {
    padding-top: 60px;
  }
  
  /* Dashboard still needs 0 top padding for hero effect */
  .main-layout.is-dashboard .content-wrapper {
    padding-top: 0;
  }
}
</style>
