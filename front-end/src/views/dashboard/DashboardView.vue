<template>
  <div class="dashboard-view" ref="dashboardRef">
    <!-- 
    DashboardView: 整个系统的主仪表盘容器
    负责组合顶部的沉浸式倒计时区域 (TimerSection) 和下部的挂件数据区 (DashboardWidgets)
  -->
    <!-- 监听 TimerSection 抛出的 scroll-down 事件，触发平滑滚动到数据区 -->
    <TimerSection @scroll-down="scrollToWidgets" />
    
    <!-- 当番茄钟处于“沉浸模式” (isImmersive) 时，自动隐藏下方的数据挂件区以减少干扰 -->
    <DashboardWidgets v-if="!pomodoroStore.isImmersive" ref="widgetsRef" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import TimerSection from './TimerSection.vue'
import DashboardWidgets from './DashboardWidgets.vue'
import { usePomodoroStore } from '@/stores/pomodoro'

const pomodoroStore = usePomodoroStore()
const dashboardRef = ref(null)
const widgetsRef = ref(null)

// 利用原生 DOM 的 scrollIntoView 方法实现向下滚动的平滑锚点跳转
const scrollToWidgets = () => {
  if (widgetsRef.value && widgetsRef.value.$el) {
    widgetsRef.value.$el.scrollIntoView({ behavior: 'smooth' })
  }
}
</script>

<style scoped>
.dashboard-view {
  width: 100%;
}
</style>
