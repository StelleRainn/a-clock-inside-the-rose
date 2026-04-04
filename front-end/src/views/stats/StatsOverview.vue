<template>
  <div class="stats-container">
    <!-- Social Share Section -->
    <div class="share-section mb-20">
      <el-button type="primary" :icon="Share" @click="generateShareCard" :loading="generatingCard">
        {{ $t('stats.shareMyProgress') }}
      </el-button>
    </div>

    <!-- Hidden Share Card Template -->
    <div class="share-card-container" ref="shareCardRef">
      <div class="share-card">
        <div class="card-header">
          <div class="app-logo">
            <el-icon :size="40" color="#409eff"><Timer /></el-icon>
            <span>ACIR Focus</span>
          </div>
          <div class="date">{{ new Date().toLocaleDateString() }}</div>
        </div>
        
        <div class="user-info">
          <div class="avatar-placeholder">{{ userStore.user?.username?.charAt(0).toUpperCase() }}</div>
          <div class="user-details">
            <h3>{{ userStore.user?.username }}</h3>
            <span class="level-badge">Lv. {{ userLevel }}</span>
          </div>
        </div>

        <div class="stats-grid">
          <div class="stat-box">
            <span class="value">{{ totalFocusMinutes }}</span>
            <span class="label">{{ $t('stats.focusMins') }}</span>
          </div>
          <div class="stat-box">
            <span class="value">{{ completedTasks }}</span>
            <span class="label">{{ $t('stats.tasksDone') }}</span>
          </div>
          <div class="stat-box">
            <span class="value">{{ streakDays }}</span>
            <span class="label">{{ $t('stats.dayStreak') }}</span>
          </div>
        </div>

        <div class="achievements-preview" v-if="latestAchievement">
          <div class="achievement-pill">
            <el-icon><Medal /></el-icon>
            <span>{{ $t('stats.latestAchievement', { name: latestAchievement.name }) }}</span>
          </div>
        </div>

        <div class="card-footer">
          <p>{{ $t('stats.generatedBy') }}</p>
        </div>
      </div>
    </div>

    <!-- Image Preview Dialog -->
    <el-dialog v-model="shareDialogVisible" :title="$t('stats.yourFocusCard')" width="400px">
      <div class="image-preview">
        <img :src="shareImageUrl" alt="Focus Card" style="width: 100%; border-radius: 8px;" />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <p class="hint-text">{{ $t('stats.rightClickToSave') }}</p>
          <el-button @click="shareDialogVisible = false">{{ $t('tasks.cancel') }}</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Focus Heatmap Section -->
    <el-card class="mb-20">
      <template #header>
        <span>{{ $t('stats.focusHeatmap') }}</span>
      </template>
      <div ref="heatmapChartRef" style="height: 200px;"></div>
    </el-card>

    <!-- Focus Time (Bar) -->
    <el-card class="mb-20">
      <template #header>
        <div class="card-header">
          <span>{{ $t('stats.focusTime') }}</span>
          <div class="header-actions">
            <el-button-group class="mr-3" size="small">
              <el-button @click="handlePrevFocusTime">
                <el-icon><ArrowLeft /></el-icon>
                {{ prevButtonText }}
              </el-button>
              <el-button @click="handleNextFocusTime" :disabled="focusTimeOffset === 0">
                {{ nextButtonText }}
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </el-button-group>
            <el-radio-group v-model="focusTimeRange" size="small" @change="onFocusTimeRangeChange">
              <el-radio-button label="7days">{{ $t('stats.days7') }}</el-radio-button>
              <el-radio-button label="30days">{{ $t('stats.days30') }}</el-radio-button>
              <el-radio-button label="1year">{{ $t('stats.year1') }}</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>
      <div ref="focusChartRef" style="height: 300px;"></div>
    </el-card>

    <!-- Achievement Section -->
    <el-card class="mb-20">
      <template #header>
        <span>{{ $t('stats.achievements') }}</span>
      </template>
      <div class="achievements-grid" v-loading="achievementsLoading">
        <div 
          v-for="achievement in achievements" 
          :key="achievement.id" 
          class="achievement-item"
          :class="{ 'unlocked': achievement.unlocked }"
        >
          <div class="icon-wrapper">
            <el-icon :size="32">
              <component :is="getIcon(achievement.iconName)" />
            </el-icon>
          </div>
          <div class="achievement-info">
            <h4>{{ achievement.name }}</h4>
            <p>{{ achievement.description }}</p>
            <span class="unlock-date" v-if="achievement.unlocked">
              {{ $t('stats.unlockedAt', { date: formatDate(achievement.unlockedAt) }) }}
            </span>
          </div>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="12" :lg="12" class="mb-20">
        <el-card>
          <template #header>
            <span>{{ $t('stats.focusDistributionByTag') }}</span>
          </template>
          <div ref="tagChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card>
          <template #header>
            <span>{{ $t('stats.taskStatusDistribution') }}</span>
          </template>
          <div ref="taskChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import * as echarts from 'echarts'
import html2canvas from 'html2canvas'
import { getDailyFocusStats, getTaskStatusStats, getTagFocusStats } from '@/api/stats'
import { getUserAchievements, getUserStats } from '@/api/gamification'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { Medal, Trophy, Calendar, Finished, Share, Timer, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const { t } = useI18n()
const userStore = useUserStore()
const themeStore = useThemeStore()
const focusChartRef = ref(null)
const taskChartRef = ref(null)
const heatmapChartRef = ref(null)
const tagChartRef = ref(null)
let focusChart = null
let taskChart = null
let heatmapChart = null
let tagChart = null

const focusTimeRange = ref('7days')
const focusTimeOffset = ref(0)
const allDailyData = ref([])

const prevButtonText = computed(() => {
  if (focusTimeRange.value === '7days') return t('stats.previous7Days')
  if (focusTimeRange.value === '30days') return t('stats.previous30Days')
  return t('stats.previous1Year')
})

const nextButtonText = computed(() => {
  if (focusTimeRange.value === '7days') return t('stats.next7Days')
  if (focusTimeRange.value === '30days') return t('stats.next30Days')
  return t('stats.next1Year')
})

const achievements = ref([])
const achievementsLoading = ref(false)
const userStats = ref({})

// Share Card Refs
const shareCardRef = ref(null)
const shareDialogVisible = ref(false)
const shareImageUrl = ref('')
const generatingCard = ref(false)

const userLevel = computed(() => userStats.value.level || 1)
const totalFocusMinutes = computed(() => userStats.value.totalFocusMinutes || 0)
const streakDays = computed(() => userStats.value.streakDays || 0)
const completedTasks = ref(0) // Need to fetch this separately or estimate
const latestAchievement = computed(() => {
  return achievements.value.filter(a => a.unlocked).sort((a, b) => new Date(b.unlockedAt) - new Date(a.unlockedAt))[0]
})

// Icon mapping
const getIcon = (name) => {
  const map = {
    'medal': Medal,
    'trophy': Trophy,
    'calendar': Calendar,
    'finished': Finished
  }
  return map[name] || Medal
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString()
}

const fetchAllData = async () => {
  if (!userStore.user || !userStore.user.id) return
  
  achievementsLoading.value = true
  try {
    const [achievementsData, statsData, taskStats] = await Promise.all([
      getUserAchievements(userStore.user.id),
      getUserStats(userStore.user.id),
      getTaskStatusStats(userStore.user.id)
    ])
    
    achievements.value = achievementsData || []
    userStats.value = statsData || {}
    
    // Calculate completed tasks from stats
    const doneTaskStat = taskStats.find(s => s.status === 'DONE')
    completedTasks.value = doneTaskStat ? doneTaskStat.count : 0
    
  } catch (e) {
    console.error(e)
  } finally {
    achievementsLoading.value = false
  }
}

const generateShareCard = async () => {
  if (!shareCardRef.value) return
  generatingCard.value = true
  
  try {
    const element = shareCardRef.value.querySelector('.share-card')
    const canvas = await html2canvas(element, {
      scale: 2, // High resolution
      backgroundColor: null,
      logging: false
    })
    
    shareImageUrl.value = canvas.toDataURL('image/png')
    shareDialogVisible.value = true
  } catch (e) {
    console.error('Failed to generate image', e)
  } finally {
    generatingCard.value = false
  }
}

const updateChartsTheme = () => {
  const isDark = themeStore.isDark
  const textColor = isDark ? '#ccc' : '#333'
  const borderColor = isDark ? '#1a1a1a' : '#fff'

  // Update Heatmap
  if (heatmapChart) {
    // Colors
    const lightColorsDeep = ['#ebedf0', '#40c463', '#30a14e', '#216e39', '#0e4429']
    const finalDarkColors = ['#2d2d2d', '#238636', '#2ea043', '#3fb950', '#a2d9a7']
    const colors = isDark ? finalDarkColors : lightColorsDeep
    const splitLineColor = isDark ? '#333' : '#ccc'
    const calBorderColor = isDark ? '#1a1a1a' : '#ffffff'

    heatmapChart.setOption({
      visualMap: {
        inRange: { color: colors },
        textStyle: { color: textColor }
      },
      calendar: {
        itemStyle: { borderColor: calBorderColor },
        splitLine: { lineStyle: { color: splitLineColor } },
        dayLabel: { color: textColor },
        monthLabel: { color: textColor }
      },
      series: {
        itemStyle: { borderColor: calBorderColor }
      }
    })
  }

  // Update Task Chart
  if (taskChart) {
    taskChart.setOption({
      legend: { textStyle: { color: textColor } },
      series: [{ itemStyle: { borderColor: borderColor } }]
    })
  }

  // Update Tag Chart
  if (tagChart) {
    tagChart.setOption({
      legend: { textStyle: { color: textColor } },
      // Tag Chart doesn't have borders usually, but we can check
    })
  }
  
  // Update Focus Chart (Bar)
  if (focusChart) {
      focusChart.setOption({
          xAxis: {
              axisLabel: { color: textColor },
              nameTextStyle: { color: textColor }
          },
          yAxis: {
              axisLabel: { color: textColor },
              nameTextStyle: { color: textColor }
          },
          legend: { textStyle: { color: textColor } }
      })
  }
}

// Watch theme changes
watch(() => themeStore.isDark, () => {
  updateChartsTheme()
})

const handlePrevFocusTime = () => {
  focusTimeOffset.value++
  updateFocusChart()
}

const handleNextFocusTime = () => {
  if (focusTimeOffset.value > 0) {
    focusTimeOffset.value--
    updateFocusChart()
  }
}

const onFocusTimeRangeChange = () => {
  focusTimeOffset.value = 0
  updateFocusChart()
}

const updateFocusChart = () => {
  if (!focusChart) return
  
  const range = focusTimeRange.value
  const offset = focusTimeOffset.value
  let dates = []
  let durations = []
  
  const endDate = new Date()
  endDate.setHours(0, 0, 0, 0)

  if (range === '7days') {
     endDate.setDate(endDate.getDate() - (offset * 7))
  } else if (range === '30days') {
     endDate.setDate(endDate.getDate() - (offset * 30))
  } else if (range === '1year') {
     endDate.setMonth(endDate.getMonth() - (offset * 12))
  }
  
  const dateMap = new Map()
  if (allDailyData.value && allDailyData.value.length > 0) {
    allDailyData.value.forEach(item => {
      dateMap.set(item.date, Math.round(item.totalSeconds / 60))
    })
  }

  if (range === '7days' || range === '30days') {
    const days = range === '7days' ? 7 : 30
    for (let i = days - 1; i >= 0; i--) {
      const d = new Date(endDate)
      d.setDate(endDate.getDate() - i)
      
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const dateStr = `${year}-${month}-${day}`
      
      // We can format x-axis label nicer (e.g. 'MM-DD')
      dates.push(`${month}-${day}`)
      durations.push(dateMap.get(dateStr) || 0)
    }
  } else if (range === '1year') {
    const monthMap = new Map()
    if (allDailyData.value && allDailyData.value.length > 0) {
      allDailyData.value.forEach(item => {
        const monthStr = item.date.substring(0, 7) // 'YYYY-MM'
        monthMap.set(monthStr, (monthMap.get(monthStr) || 0) + Math.round(item.totalSeconds / 60))
      })
    }
    
    for (let i = 11; i >= 0; i--) {
      const d = new Date(endDate.getFullYear(), endDate.getMonth() - i, 1)
      // Safely get YYYY-MM based on local time
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const monthStr = `${year}-${month}`
      
      const monthLabel = d.toLocaleDateString(undefined, { year: 'numeric', month: 'short' })
      dates.push(monthLabel)
      durations.push(monthMap.get(monthStr) || 0)
    }
  }
  
    focusChart.setOption({
      xAxis: {
        type: 'category',
        data: dates,
        axisTick: { alignWithLabel: true }
      },
      series: [
        {
          name: t('stats.focusTimeLabel'),
          type: 'bar',
          barWidth: '60%',
          data: durations,
          itemStyle: { color: '#409eff' }
        }
      ]
    })
}

const initCharts = async () => {
  if (!userStore.user || !userStore.user.id) return

  // 1. Fetch & Render Focus Chart
  if (focusChartRef.value) {
    focusChart = echarts.init(focusChartRef.value)
    focusChart.showLoading()
    
    try {
      const dailyData = await getDailyFocusStats(userStore.user.id)
      allDailyData.value = dailyData
      
      focusChart.hideLoading()
      focusChart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
        },
        legend: {
          bottom: '0%', // explicitly position legend at the very bottom
          data: [t('stats.focusTimeLabel')]
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%', // Increased bottom margin to prevent overlap with legend
          containLabel: true
        },
        xAxis: {
          type: 'category',
          axisTick: { alignWithLabel: true }
        },
        yAxis: {
          type: 'value',
          name: 'Minutes'
        }
      })
      
      updateFocusChart()

      // 3. Render Heatmap (Using same daily data)
      if (heatmapChartRef.value) {
        heatmapChart = echarts.init(heatmapChartRef.value)
        const currentYear = new Date().getFullYear()
        
        // Generate full year date range to ensure all days are interactive
        const startDate = new Date(currentYear, 0, 1)
        const endDate = new Date(currentYear, 11, 31)
        const dateMap = new Map()
        
        // Fill map with actual data
        dailyData.forEach(item => {
          dateMap.set(item.date, Math.round(item.totalSeconds / 60))
        })
        
        const heatmapData = []
        for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
          const dateStr = d.toISOString().split('T')[0]
          const value = dateMap.get(dateStr) || 0
          heatmapData.push([dateStr, value])
        }

        heatmapChart.setOption({
          tooltip: {
            position: 'top',
            formatter: (params) => {
              if (params.value[1] === 0) {
                 return t('stats.noFocusRecords', { date: params.value[0] })
              }
              return t('stats.minsFocus', { date: params.value[0], mins: params.value[1] })
            }
          },
          visualMap: {
            min: 0,
            max: 120, // Max 2 hours for deep color
            calculable: false,
            orient: 'horizontal',
            left: 'center',
            bottom: '0%',
            // Initial colors will be set by updateChartsTheme
          },
          calendar: {
            top: 30,
            left: 30,
            right: 30,
            cellSize: ['auto', 13],
            range: currentYear,
            yearLabel: { show: false }
          },
          series: {
            type: 'heatmap',
            coordinateSystem: 'calendar',
            data: heatmapData
          }
        })
        
        // Apply theme styles immediately
        updateChartsTheme()
      }

    } catch (e) {
      console.error(e)
      focusChart.hideLoading()
    }
  }

  // 2. Fetch & Render Task Chart
  if (taskChartRef.value) {
    taskChart = echarts.init(taskChartRef.value)
    taskChart.showLoading()
    
    try {
      const statusData = await getTaskStatusStats(userStore.user.id)
      const pieData = statusData.map(item => ({
        name: item.status,
        value: item.count
      }))

      taskChart.hideLoading()
      const isDark = themeStore.isDark
      
      taskChart.setOption({
        tooltip: {
          trigger: 'item'
        },
        legend: {
          type: 'scroll',
          orient: 'vertical',
          left: '5%',
          top: 'middle',
          textStyle: { color: isDark ? '#ccc' : '#333' }
        },
        series: [
          {
            name: t('stats.taskStatus'),
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['65%', '50%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: isDark ? '#1a1a1a' : '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 20,
                fontWeight: 'bold'
              }
            },
            labelLine: { show: false },
            data: pieData
          }
        ]
      })
    } catch (e) {
      console.error(e)
      taskChart.hideLoading()
    }
  }

  // 4. Fetch & Render Tag Chart
  if (tagChartRef.value) {
    tagChart = echarts.init(tagChartRef.value)
    tagChart.showLoading()

    try {
      const tagData = await getTagFocusStats(userStore.user.id)
      const pieData = tagData.map(item => ({
        name: item.tagName,
        value: Math.round(item.totalSeconds / 60), // Convert to minutes
        itemStyle: { color: item.tagColor }
      }))

      tagChart.hideLoading()
      const isDark = themeStore.isDark
      
      tagChart.setOption({
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} mins ({d}%)'
        },
        legend: {
          type: 'scroll',
          orient: 'vertical',
          left: '5%',
          top: 'middle',
          textStyle: { color: isDark ? '#ccc' : '#333' }
        },
        series: [
          {
            name: t('stats.focusByTag'),
            type: 'pie',
            radius: [20, 90],
            center: ['65%', '50%'],
            roseType: 'radius',
            itemStyle: {
              borderRadius: 5
            },
            data: pieData
          }
        ]
      })
    } catch (e) {
      console.error(e)
      tagChart.hideLoading()
    }
  }
}

const resizeCharts = () => {
  focusChart?.resize()
  taskChart?.resize()
  heatmapChart?.resize()
  tagChart?.resize()
}

onMounted(() => {
  initCharts()
  fetchAllData()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  focusChart?.dispose()
  taskChart?.dispose()
  heatmapChart?.dispose()
  tagChart?.dispose()
})
</script>

<style scoped>
.stats-container {
  padding: 20px;
}
.mb-20 {
  margin-bottom: 20px;
}
.share-section {
  display: flex;
  justify-content: flex-end;
}
.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}
.achievement-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-radius: 8px;
  background-color: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  opacity: 0.6;
  filter: grayscale(100%);
  transition: all 0.3s ease;
}
.achievement-item.unlocked {
  opacity: 1;
  filter: grayscale(0%);
  background-color: var(--el-color-success-light-9);
  border-color: var(--el-color-success-light-8);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.icon-wrapper {
  margin-right: 15px;
  color: var(--el-color-success);
  background: var(--el-bg-color);
  padding: 10px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.achievement-info h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: var(--el-text-color-primary);
}
.achievement-info p {
  margin: 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.unlock-date {
  display: block;
  margin-top: 5px;
  font-size: 10px;
  color: var(--el-color-success);
  font-weight: bold;
}
.hint-text {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-right: 10px;
}

/* Share Card Styles (Hidden until generated) */
.share-card-container {
  position: absolute;
  left: -9999px;
  top: -9999px;
}
.share-card {
  width: 400px;
  height: 600px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 30px;
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-shadow: 0 20px 60px rgba(0,0,0,0.1);
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.app-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: bold;
  font-size: 18px;
  color: #303133;
}
.date {
  color: #606266;
  font-size: 14px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
}
.avatar-placeholder {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  font-weight: bold;
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.3);
}
.user-details h3 {
  margin: 0 0 5px 0;
  font-size: 24px;
  color: #303133;
}
.level-badge {
  background-color: #e6a23c;
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: bold;
}
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin: 30px 0;
}
.stat-box {
  background: rgba(255, 255, 255, 0.8);
  padding: 20px;
  border-radius: 16px;
  text-align: center;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.stat-box:last-child {
  grid-column: span 2;
}
.stat-box .value {
  font-size: 32px;
  font-weight: 800;
  color: #409eff;
}
.stat-box .label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}
.achievements-preview {
  margin-top: auto;
  text-align: center;
}
.achievement-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: white;
  padding: 8px 16px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  color: #67c23a;
  font-weight: bold;
}
.card-footer {
  text-align: center;
  font-size: 12px;
  color: #909399;
  margin-top: 30px;
  border-top: 1px solid rgba(0,0,0,0.05);
  padding-top: 15px;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .stats-container {
    padding: 10px;
  }
  
  .share-section {
    justify-content: center;
    width: 100%;
  }
  
  .share-section .el-button {
    width: 100%;
  }
  
  .header-actions {
    flex-direction: column;
    align-items: flex-end;
    gap: 10px;
  }
  .mr-3 {
    margin-right: 0;
  }
}
.header-actions {
  display: flex;
  align-items: center;
}
.mr-3 {
  margin-right: 12px;
}
</style>
