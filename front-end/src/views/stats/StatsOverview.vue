<template>
  <div class="stats-container">
    <!-- 
    StatsOverview: 数据统计与成就展示中心。
    整合了 ECharts 图表（热力图、柱状图、饼图）和 HTML2Canvas（生成分享卡片）。
  -->
    <!-- 顶部动作区：生成并分享个人专属专注卡片 -->
    <div class="share-section mb-20">
      <el-button type="primary" :icon="Share" @click="generateShareCard" :loading="generatingCard">
        {{ $t('stats.shareMyProgress') }}
      </el-button>
    </div>

    <!-- 
      0. 卡片分享
      隐藏的分享卡片模板 (Hidden Share Card Template)
      技巧：使用绝对定位将其移出可视区域 (-9999px)，但在 DOM 中依然存在，
      以便 html2canvas 能够读取它的完整结构并截图。
    -->
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

        <!-- 核心统计数据网格 -->
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

    <!-- 图片预览弹窗：展示 html2canvas 生成的 Base64 图片，供用户长按/右键保存 -->
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

    <!-- 1. 专注日历热力图 (GitHub Contribution Style) -->
    <el-card class="glass-card mb-20">
      <template #header>
        <span>{{ $t('stats.focusHeatmap') }}</span>
      </template>
      <div ref="heatmapChartRef" style="height: 200px;"></div>
    </el-card>

    <!-- 2. 专注时间趋势图 (Bar Chart) -->
    <el-card class="glass-card mb-20">
      <template #header>
        <div class="card-header">
          <span>{{ $t('stats.focusTime') }}</span>
          <div class="header-actions">
            <!-- 时间翻页器：支持按周、按月、按年前后翻阅数据 -->
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
            <!-- 时间粒度切换器 -->
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

    <!-- 3. 游戏化成就系统 (Achievements) -->
    <el-card class="glass-card mb-20">
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
            <!-- 仅在解锁状态下显示解锁时间 -->
            <span class="unlock-date" v-if="achievement.unlocked">
              {{ $t('stats.unlockedAt', { date: formatDate(achievement.unlockedAt) }) }}
            </span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 4. 多维度分布饼图 (Tags & Status) -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="12" :lg="12" class="mb-20">
        <el-card class="glass-card">
          <template #header>
            <span>{{ $t('stats.focusDistributionByTag') }}</span>
          </template>
          <div ref="tagChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="glass-card">
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

// ==========================================
// 1. ECharts 实例与 DOM 引用
// ==========================================
const focusChartRef = ref(null)
const taskChartRef = ref(null)
const heatmapChartRef = ref(null)
const tagChartRef = ref(null)

let focusChart = null
let taskChart = null
let heatmapChart = null
let tagChart = null

// 趋势图交互状态
const focusTimeRange = ref('7days')
const focusTimeOffset = ref(0) // 时间偏移量：0 代表当前，1 代表上一个周期
const allDailyData = ref([]) // 缓存的全量每日数据，避免频繁请求后端

// 按钮响应时间粒度
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

// 成就与用户核心数据
const achievements = ref([])
const achievementsLoading = ref(false)
const userStats = ref({})

// ==========================================
// 2. Html2Canvas 分享卡片生成逻辑
// ==========================================
const shareCardRef = ref(null)
const shareDialogVisible = ref(false)
const shareImageUrl = ref('')
const generatingCard = ref(false)

const userLevel = computed(() => userStats.value.level || 1)
const totalFocusMinutes = computed(() => userStats.value.totalFocusMinutes || 0)
const streakDays = computed(() => userStats.value.streakDays || 0)
const completedTasks = ref(0)
const latestAchievement = computed(() => {
  return achievements.value.filter(a => a.unlocked).sort((a, b) => new Date(b.unlockedAt) - new Date(a.unlockedAt))[0]
})

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

// 聚合请求：并行拉取用户成就、等级与任务统计
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
    
    const doneTaskStat = taskStats.find(s => s.status === 'DONE')
    completedTasks.value = doneTaskStat ? doneTaskStat.count : 0
    
  } catch (e) {
    console.error(e)
  } finally {
    achievementsLoading.value = false
  }
}

// 调用 html2canvas 对隐藏的 DOM 节点进行高分辨率截图
const generateShareCard = async () => {
  if (!shareCardRef.value) return
  generatingCard.value = true
  
  try {
    const element = shareCardRef.value.querySelector('.share-card')
    const canvas = await html2canvas(element, {
      scale: 2, // 提升清晰度，防止在 Retina 屏幕下模糊
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

// ==========================================
// 3. ECharts 动态主题适配系统 (Dark Mode)
// ==========================================
const updateChartsTheme = () => {
  const isDark = themeStore.isDark
  const textColor = isDark ? '#ccc' : '#333'
  const borderColor = isDark ? '#1a1a1a' : '#fff'

  if (heatmapChart) {
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

  if (taskChart) {
    taskChart.setOption({
      legend: { textStyle: { color: textColor } },
      series: [{ itemStyle: { borderColor: borderColor } }]
    })
  }

  if (tagChart) {
    tagChart.setOption({
      legend: { textStyle: { color: textColor } }
    })
  }
  
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

// 监听 Pinia 中的全局暗黑模式状态，一旦改变，即刻重新应用 ECharts 的颜色配置
watch(() => themeStore.isDark, () => {
  updateChartsTheme()
})

// ==========================================
// 4. 趋势图数据处理与渲染逻辑
// 这部分代码是前端数据可视化的核心，它负责将后端返回的“全量按天统计的平铺数据”
// 动态计算、聚合、降维成 ECharts 柱状图所需的 X 轴 (dates) 和 Y 轴 (durations) 数据。
// ==========================================
const handlePrevFocusTime = () => {
  focusTimeOffset.value++ // 向历史追溯一个周期（比如前一个7天）
  updateFocusChart()
}

const handleNextFocusTime = () => {
  if (focusTimeOffset.value > 0) {
    focusTimeOffset.value-- // 向未来推进一个周期，直到 offset 为 0 (当前周期)
    updateFocusChart()
  }
}

const onFocusTimeRangeChange = () => {
  focusTimeOffset.value = 0 // 切换时间粒度（7天/30天/1年）时，必须将偏移量重置归零
  updateFocusChart()
}

// 核心计算函数：基于 offset (偏移) 和 range (粒度) 计算时间轴，并在缓存数据中进行匹配聚合
const updateFocusChart = () => {
  if (!focusChart) return
  
  const range = focusTimeRange.value
  const offset = focusTimeOffset.value
  let dates = [] // ECharts X 轴数据容器
  let durations = [] // ECharts Y 轴数据容器
  
  // 第一步：确定计算的“时间基准点 (endDate)”
  // 默认是今天。如果是按天计算，需要将时分秒归零，确保日期的纯净性。
  const endDate = new Date()
  endDate.setHours(0, 0, 0, 0)

  // 第二步：根据偏移量 (offset) 倒推结束日期
  // 比如：今天是 5月10日，range 是 7days，offset 是 1（前一个周期）。
  // 那么 endDate 就应该被推回到 5月3日。
  if (range === '7days') {
     endDate.setDate(endDate.getDate() - (offset * 7))
  } else if (range === '30days') {
     endDate.setDate(endDate.getDate() - (offset * 30))
  } else if (range === '1year') {
     // 对于年份聚合，直接操作 Date 的 Month 属性
     endDate.setMonth(endDate.getMonth() - (offset * 12))
  }
  
  // 第三步：建立高速缓存映射 (Map)
  // 将后端返回的数组 [{date: '2024-05-01', totalSeconds: 3600}]
  // 转化为 Map('2024-05-01' => 60分钟)
  // 这样做是为了在后续生成 X 轴时，将查找复杂度从 O(N^2) 降为 O(N)
  const dateMap = new Map()
  if (allDailyData.value && allDailyData.value.length > 0) {
    allDailyData.value.forEach(item => {
      dateMap.set(item.date, Math.round(item.totalSeconds / 60))
    })
  }

  // 第四步：构造 X 轴数据并填充 Y 轴（区分天级别和月级别）
  if (range === '7days' || range === '30days') {
    const days = range === '7days' ? 7 : 30
    
    // 逆序循环：从 (endDate - 6天) 一直递推到 endDate
    // 保证 X 轴的时间是从左到右递增的（过去 -> 现在）
    for (let i = days - 1; i >= 0; i--) {
      const d = new Date(endDate)
      d.setDate(endDate.getDate() - i)
      
      // 手动拼接 YYYY-MM-DD，用于去 Map 里做精确匹配
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const dateStr = `${year}-${month}-${day}`
      
      // X 轴的显示文案可以精简一点，省去不必要的年份
      dates.push(`${month}-${day}`)
      // 如果 Map 里没有这一天的数据，说明那天没专注，必须补 0（不能跳过，否则 X 轴会断层）
      durations.push(dateMap.get(dateStr) || 0)
    }
  } else if (range === '1year') {
    // 年级别的聚合相对复杂，因为后端返回的是“每天”的数据
    // 我们需要在前端做一次按“月”的二次聚合 (GROUP BY Month)
    const monthMap = new Map()
    if (allDailyData.value && allDailyData.value.length > 0) {
      allDailyData.value.forEach(item => {
        const monthStr = item.date.substring(0, 7) // 巧妙利用字符串截取 'YYYY-MM-DD' -> 'YYYY-MM'
        monthMap.set(monthStr, (monthMap.get(monthStr) || 0) + Math.round(item.totalSeconds / 60))
      })
    }
    
    // 生成过去 12 个月的 X 轴
    for (let i = 11; i >= 0; i--) {
      // 获取当月的第一天作为基准日期
      const d = new Date(endDate.getFullYear(), endDate.getMonth() - i, 1)
      
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const monthStr = `${year}-${month}` // 匹配我们上面生成的 'YYYY-MM' Key
      
      // 使用浏览器的原生多语言 API 生成带本地化特性的月份名（如 "May 2024" 或 "2024年5月"）
      const monthLabel = d.toLocaleDateString(undefined, { year: 'numeric', month: 'short' })
      dates.push(monthLabel)
      // 找不到就补 0
      durations.push(monthMap.get(monthStr) || 0)
    }
  }
  
  // 第五步：将计算好的最终数据注入 ECharts 实例
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
        itemStyle: { color: '#409eff' } // 品牌蓝
      }
    ]
  })
}

// 初始化所有的 ECharts 实例并拉取数据
const initCharts = async () => {
  if (!userStore.user || !userStore.user.id) return

  // 1. 初始化柱状图 (Focus Chart)
  if (focusChartRef.value) {
    focusChart = echarts.init(focusChartRef.value)
    focusChart.showLoading()
    
    try {
      const dailyData = await getDailyFocusStats(userStore.user.id)
      allDailyData.value = dailyData
      
      focusChart.hideLoading()
      focusChart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: { bottom: '0%', data: [t('stats.focusTimeLabel')] },
        grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
        xAxis: { type: 'category', axisTick: { alignWithLabel: true } },
        yAxis: { type: 'value', name: 'Minutes' }
      })
      
      updateFocusChart()

      // 2. 初始化日历热力图 (Heatmap)
      // 复用 dailyData，避免重复发请求
      if (heatmapChartRef.value) {
        heatmapChart = echarts.init(heatmapChartRef.value)
        const currentYear = new Date().getFullYear()
        
        const startDate = new Date(currentYear, 0, 1)
        const endDate = new Date(currentYear, 11, 31)
        const dateMap = new Map()
        
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
              if (params.value[1] === 0) return t('stats.noFocusRecords', { date: params.value[0] })
              return t('stats.minsFocus', { date: params.value[0], mins: params.value[1] })
            }
          },
          visualMap: {
            min: 0,
            max: 120, // 达到 2 小时即为最深色
            calculable: false,
            orient: 'horizontal',
            left: 'center',
            bottom: '0%'
          },
          calendar: {
            top: 30, left: 30, right: 30,
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
        updateChartsTheme() // 首次挂载即注入主题色
      }
    } catch (e) {
      console.error(e)
      focusChart.hideLoading()
    }
  }

  // 3. 初始化任务状态饼图 (Task Chart)
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
        tooltip: { trigger: 'item' },
        legend: {
          type: 'scroll', orient: 'vertical', left: '5%', top: 'middle',
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
            label: { show: false, position: 'center' },
            emphasis: {
              label: { show: true, fontSize: 20, fontWeight: 'bold' }
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

  // 4. 初始化标签专注分布玫瑰图 (Tag Chart)
  if (tagChartRef.value) {
    tagChart = echarts.init(tagChartRef.value)
    tagChart.showLoading()

    try {
      const tagData = await getTagFocusStats(userStore.user.id)
      const pieData = tagData.map(item => ({
        name: item.tagName,
        value: Math.round(item.totalSeconds / 60),
        itemStyle: { color: item.tagColor } // 直接使用后端返回的标签专属颜色
      }))

      tagChart.hideLoading()
      const isDark = themeStore.isDark
      
      tagChart.setOption({
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} mins ({d}%)'
        },
        legend: {
          type: 'scroll', orient: 'vertical', left: '5%', top: 'middle',
          textStyle: { color: isDark ? '#ccc' : '#333' }
        },
        series: [
          {
            name: t('stats.focusByTag'),
            type: 'pie',
            radius: [20, 90],
            center: ['65%', '50%'],
            roseType: 'radius', // 南丁格尔玫瑰图模式
            itemStyle: { borderRadius: 5 },
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

// 响应式核心：窗口缩放时强制重绘所有 ECharts 实例
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
  // 必须手动销毁 ECharts 实例以释放内存
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
