<template>
  <footer class="global-footer">
    <div class="footer-content">
      <span class="text-part">{{ $t('footer.designedBy') }}</span>
      <span class="separator" v-if="$t('footer.separator1')">{{ $t('footer.separator1') }}</span>
      <a href="https://www.gdufs.edu.cn/" target="_blank" rel="noopener noreferrer" class="footer-link">{{ $t('footer.gdufs') }}</a>
      <span class="separator">{{ $t('footer.separator2') }}</span>
      <a href="https://github.com/StelleRainn" target="_blank" rel="noopener noreferrer" class="footer-link">{{ $t('footer.knowMore') }}</a>
      <span class="separator">{{ $t('footer.separator2') }}</span>
      <a href="https://stellerainn.vercel.app/home" target="_blank" rel="noopener noreferrer" class="footer-link">{{ $t('footer.personalWebsite') }}</a>
    </div>
  </footer>
</template>

<script setup>
// The component just needs the template for i18n
</script>

<style scoped>
.global-footer {
  width: 100%;
  padding: 20px 0;
  text-align: center;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  background-color: transparent;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 40px;
}

.footer-content {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: center;
  gap: 4px;
}

.text-part {
  color: var(--el-text-color-regular);
}

.separator {
  color: var(--el-text-color-placeholder);
  margin: 0 4px;
}

.footer-link {
  color: var(--el-text-color-regular);
  text-decoration: none;
  transition: color 0.3s ease;
  position: relative;
}

.footer-link:hover {
  color: var(--el-color-primary);
}

.footer-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 1px;
  background-color: var(--el-color-primary);
  transition: width 0.3s ease;
}

.footer-link:hover::after {
  width: 100%;
}

@media (max-width: 768px) {
  .global-footer {
    font-size: 12px;
    padding: 15px 0 20px;
    margin-top: 20px;
  }
  
  .footer-content {
    line-height: 1.8;
  }
}
</style>
