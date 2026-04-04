<template>
  <div class="calendar-view">
    <div class="page-header">
      <h1>{{ $t('calendar.title') }}</h1>
      <p class="subtitle">{{ $t('calendar.subtitle') }}</p>
    </div>

    <el-row :gutter="24">
      <el-col :span="16" :xs="24">
        <WidgetHeatmap class="mb-4" />
        <!-- Placeholder for Weekly Report -->
        <el-card class="glass-card mb-4">
          <template #header><h3>{{ $t('calendar.weeklyReport') }}</h3></template>
          <div class="placeholder-content">{{ $t('calendar.weeklyAnalyticsComingSoon') }}</div>
        </el-card>
      </el-col>
      
      <el-col :span="8" :xs="24">
        <WidgetFocusRing class="mb-4" />
        <WidgetTodoList />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import WidgetHeatmap from '@/components/widgets/WidgetHeatmap.vue'
import WidgetFocusRing from '@/components/widgets/WidgetFocusRing.vue'
import WidgetTodoList from '@/components/widgets/WidgetTodoList.vue'
</script>

<style scoped>
.calendar-view {
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 2rem;
  margin-bottom: 8px;
}

.subtitle {
  color: var(--el-text-color-secondary);
}

.mb-4 {
  margin-bottom: 24px;
}

.glass-card {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
}

.dark .glass-card {
  background: rgba(30, 30, 30, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.placeholder-content {
  padding: 40px;
  text-align: center;
  color: var(--el-text-color-secondary);
}

@media (max-width: 768px) {
  .calendar-view {
    padding: 20px 16px;
  }
  .page-header {
    margin-bottom: 24px;
  }
  .page-header h1 {
    font-size: 1.5rem;
  }
}
</style>
