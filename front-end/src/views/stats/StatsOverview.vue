<template>
  <div class="stats-container">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>Focus Time (Last 7 Days)</span>
          </template>
          <div ref="focusChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>Task Status Distribution</span>
          </template>
          <div ref="taskChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getDailyFocusStats, getTaskStatusStats } from '@/api/stats'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const focusChartRef = ref(null)
const taskChartRef = ref(null)
let focusChart = null
let taskChart = null

const initCharts = async () => {
  if (!userStore.user || !userStore.user.id) return

  // 1. Fetch & Render Focus Chart
  if (focusChartRef.value) {
    focusChart = echarts.init(focusChartRef.value)
    focusChart.showLoading()
    
    try {
      const dailyData = await getDailyFocusStats(userStore.user.id)
      const dates = dailyData.map(item => item.date)
      // Convert seconds to minutes for display
      const durations = dailyData.map(item => Math.round(item.totalSeconds / 60))
      
      focusChart.hideLoading()
      focusChart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisTick: { alignWithLabel: true }
        },
        yAxis: {
          type: 'value',
          name: 'Minutes'
        },
        series: [
          {
            name: 'Focus Time',
            type: 'bar',
            barWidth: '60%',
            data: durations,
            itemStyle: { color: '#409eff' }
          }
        ]
      })
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
      // Transform data for ECharts: { status: 'TODO', count: 5 } -> { name: 'TODO', value: 5 }
      const pieData = statusData.map(item => ({
        name: item.status,
        value: item.count
      }))

      taskChart.hideLoading()
      taskChart.setOption({
        tooltip: {
          trigger: 'item'
        },
        legend: {
          top: '5%',
          left: 'center'
        },
        series: [
          {
            name: 'Task Status',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
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
}

const resizeCharts = () => {
  focusChart?.resize()
  taskChart?.resize()
}

onMounted(() => {
  initCharts()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  focusChart?.dispose()
  taskChart?.dispose()
})
</script>

<style scoped>
.stats-container {
  padding: 20px;
}
</style>
