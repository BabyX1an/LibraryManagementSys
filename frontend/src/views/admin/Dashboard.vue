<template>
  <AdminLayout>
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
                <h3>借阅趋势</h3>
              </div>
            </template>
            <div class="chart-placeholder">
              <el-empty description="暂无数据" />
            </div>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <h3>图书分类占比</h3>
              </div>
            </template>
            <div class="chart-placeholder">
              <el-empty description="暂无数据" />
            </div>
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
        <el-timeline>
          <el-timeline-item timestamp="2024-01-20" color="#0bbd87">
            新增图书 5 本
          </el-timeline-item>
          <el-timeline-item timestamp="2024-01-19" color="#409eff">
            用户 "张三" 借阅《Java编程思想》
          </el-timeline-item>
          <el-timeline-item timestamp="2024-01-18" color="#f56c6c">
            图书《算法导论》库存不足
          </el-timeline-item>
          <el-timeline-item timestamp="2024-01-17" color="#e6a23c">
            新增注册用户 3 人
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Reading, TrendCharts, User, Goods } from '@element-plus/icons-vue'
import { bookApi } from '@/api/book'
import { userApi } from '@/api/user'
import AdminLayout from './Layout.vue'

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

// 获取统计数据
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
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

onMounted(() => {
  fetchStats()
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

    .chart-placeholder {
      height: 300px;
      display: flex;
      justify-content: center;
      align-items: center;
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