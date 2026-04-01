<script setup>
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.initTheme()
})
</script>

<template>
  <RouterView />
</template>

<style>
:root {
  --el-font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

html, body {
  margin: 0;
  padding: 0;
  min-height: 100%;
  font-family: var(--el-font-family);
  background-color: var(--el-bg-color);
  color: var(--el-text-color-primary);
  transition: background-color 0.3s, color 0.3s;
  overflow-x: hidden;
}

#app {
  min-height: 100%;
}

/* Global Glassmorphism Utility */
.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.dark .glass-card {
  background: rgba(30, 30, 30, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
}

/* =========================================
   Global Dialog Glassmorphism Overrides
   ========================================= */
.el-overlay-dialog .el-dialog {
  background: rgba(255, 255, 255, 0.75) !important;
  backdrop-filter: blur(16px) !important;
  -webkit-backdrop-filter: blur(16px) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  border-radius: 16px !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1) !important;
}

.el-dialog__header {
  padding: 20px 24px 15px !important;
  margin-right: 0 !important;
}

.el-dialog__title {
  font-weight: 600;
  letter-spacing: 0.5px;
}

.el-dialog__header,
.el-dialog__body,
.el-dialog__footer {
  background: transparent !important;
}

/* Dark Mode Dialog */
.dark .el-overlay-dialog .el-dialog {
  background: rgba(30, 30, 30, 0.75) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3) !important;
}

/* Input styles inside glass dialogs */
.el-dialog .el-input__wrapper,
.el-dialog .el-textarea__inner,
.el-dialog .el-select .el-input__wrapper {
  background-color: rgba(255, 255, 255, 0.5) !important;
  backdrop-filter: blur(4px);
  border-radius: 8px;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.05) inset !important;
}

.dark .el-dialog .el-input__wrapper,
.dark .el-dialog .el-textarea__inner,
.dark .el-dialog .el-select .el-input__wrapper {
  background-color: rgba(0, 0, 0, 0.3) !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset !important;
  color: var(--el-text-color-primary);
}

.el-dialog .el-input__wrapper.is-focus,
.el-dialog .el-textarea__inner:focus {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
  background-color: rgba(255, 255, 255, 0.8) !important;
}

.dark .el-dialog .el-input__wrapper.is-focus,
.dark .el-dialog .el-textarea__inner:focus {
  background-color: rgba(0, 0, 0, 0.5) !important;
}

/* Scrollbar Styling */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}

.dark ::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
}

.dark ::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style>
