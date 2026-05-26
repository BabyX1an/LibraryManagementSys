<template>
    <div class="dashboard-container">
      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon">
              <el-icon :size="32" color="#409eff"><Reading /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.totalBooks }}</div>
              <div class="stat-label">图书总数</div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon">
              <el-icon :size="32" color="#67c23a"><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.borrowedBooks }}</div>
              <div class="stat-label">借阅总数</div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon">
              <el-icon :size="32" color="#e6a23c"><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.totalUsers }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon">
              <el-icon :size="32" color="#f56c6c"><Goods /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.lowStockBooks }}</div>
              <div class="stat-label">库存告急</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" class="charts-row">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <h3>借阅趋势（近7天）</h3>
              </div>
            </template>
            <div ref="trendChartRef" class="chart-container"></div>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <h3>图书分类占比</h3>
              </div>
            </template>
            <div ref="categoryChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 最新活动 -->
      <el-card class="recent-activity">
        <template #header>
          <div class="card-header">
            <h3>最新活动</h3>
          </div>
        </template>
        <el-timeline v-if="activities.length > 0">
          <el-timeline-item
            v-for="(item, index) in activities"
            :key="index"
            :timestamp="item.time"
            :color="item.type === 'borrow' ? '#409eff' : '#67c23a'"
          >
            {{ item.type === 'borrow' ? '借阅' : '归还' }}《{{ item.bookTitle }}》
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无活动记录" />
      </el-card>
    </div>
  </template>

  <script setup lang="ts">
  import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
  import { Reading, TrendCharts, User, Goods } from '@element-plus/icons-vue'
  import { bookApi } from '@/api/book'
  import { userApi } from '@/api/user'
  import { borrowApi } from '@/api/borrow'
  import * as echarts from 'echarts'

  interface Stats {
    totalBooks: number
    borrowedBooks: number
    totalUsers: number
    lowStockBooks: number
  }

  const stats = ref<Stats>({
    totalBooks: 0,
    borrowedBooks: 0,
    totalUsers: 0,
    lowStockBooks: 0
  })

  const activities = ref<Array<{ type: string; bookTitle: string; time: string }>>([])
  const trendChartRef = ref<HTMLElement>()
  const categoryChartRef = ref<HTMLElement>()
  let trendChart: echarts.ECharts | null = null
  let categoryChart: echarts.ECharts | null = null

  // 获取基础统计数据
  const fetchStats = async () => {
    try {
      const [booksData, usersData] = await Promise.all([
        bookApi.pageBooks(1, 1000),
        userApi.pageUsers(1, 1000)
      ])

      const books = booksData?.records || []
      const users = usersData?.records || []

      stats.value = {
        totalBooks: books.length,
        borrowedBooks: books.reduce((sum, book) => sum + (book.borrowedCount || 0), 0),
        totalUsers: users.length,
        lowStockBooks: books.filter(book => (book.stock || 0) <= 5).length
      }

      // 分类占比饼图数据
      const categoryMap: Record<string, number> = {}
      books.forEach(book => {
        const cat = book.category || '未分类'
        categoryMap[cat] = (categoryMap[cat] || 0) + 1
      })
      const categoryData = Object.entries(categoryMap).map(([name, value]) => ({ name, value }))
      await nextTick()
      renderCategoryChart(categoryData)
    } catch (error) {
      console.error('获取统计数据失败:', error)
    }
  }

  // 获取仪表盘数据（借阅趋势 + 最近活动）
  const fetchDashboardStats = async () => {
    try {
      const data = await borrowApi.getDashboardStats()
      if (data) {
        activities.value = data.recentActivities || []
        await nextTick()
        renderTrendChart(data.borrowTrend || [])
      }
    } catch (error) {
      console.error('获取仪表盘数据失败:', error)
    }
  }

  // 渲染借阅趋势折线图
  const renderTrendChart = (trendData: Array<{ date: string; count: number }>) => {
    if (!trendChartRef.value) return
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '10%', right: '5%', bottom: '15%', top: '10%' },
      xAxis: {
        type: 'category',
        data: trendData.map(item => item.date),
        axisLabel: { fontSize: 12 }
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        axisLabel: { fontSize: 12 }
      },
      series: [{
        name: '借阅量',
        type: 'line',
        data: trendData.map(item => item.count),
        smooth: true,
        areaStyle: { opacity: 0.15 },
        itemStyle: { color: '#409eff' }
      }]
    })
  }

  // 渲染分类占比饼图
  const renderCategoryChart = (data: Array<{ name: string; value: number }>) => {
    if (!categoryChartRef.value) return
    categoryChart = echarts.init(categoryChartRef.value)
    categoryChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c}本 ({d}%)' },
      legend: {
        orient: 'vertical',
        right: '5%',
        top: 'center',
        textStyle: { fontSize: 12 }
      },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        data
      }]
    })
  }

  // 窗口大小变化时自适应
  const handleResize = () => {
    trendChart?.resize()
    categoryChart?.resize()
  }

  onMounted(() => {
    fetchStats()
    fetchDashboardStats()
    window.addEventListener('resize', handleResize)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', handleResize)
    trendChart?.dispose()
    categoryChart?.dispose()
  })
  </script>

  <style scoped>
  .dashboard-container {
    .stats-row {
      margin-bottom: 20px;
    }

    .stat-card {
      display: flex;
      align-items: center;
      padding: 20px;

      .stat-icon {
        width: 60px;
        height: 60px;
        display: flex;
        justify-content: center;
        align-items: center;
        background-color: #ecf5ff;
        border-radius: 50%;
      }

      .stat-content {
        flex: 1;
        margin-left: 20px;

        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #303133;
          line-height: 1;
        }

        .stat-label {
          margin-top: 8px;
          font-size: 14px;
          color: #909399;
        }
      }
    }

    .charts-row {
      margin-bottom: 20px;

      .card-header {
        h3 {
          margin: 0;
          font-size: 16px;
          font-weight: 500;
          color: #303133;
        }
      }

      .chart-container {
        height: 300px;
        width: 100%;
      }
    }

    .recent-activity {
      .card-header {
        h3 {
          margin: 0;
          font-size: 16px;
          font-weight: 500;
          color: #303133;
        }
      }
    }
  }
  </style>
