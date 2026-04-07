<template>
  <div class="my-borrow-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>我的借阅</h2>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 借阅中 -->
        <el-tab-pane label="借阅中" name="current">
          <div v-loading="currentLoading" class="borrow-list">
            <el-empty v-if="currentBorrows.length === 0" description="暂无借阅中的图书" />
            <div v-else class="borrow-grid">
              <div v-for="record in currentBorrows" :key="record.id" class="borrow-card">
                <el-image
                  :src="record.book?.coverUrl || '/default-book.png'"
                  fit="cover"
                  class="book-cover"
                >
                  <template #error>
                    <div class="image-slot">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div class="book-info">
                  <h3 class="book-title">{{ record.book?.title }}</h3>
                  <p class="book-author">{{ record.book?.author }}</p>
                  <div class="borrow-info">
                    <p>借阅时间：{{ formatDate(record.borrowTime) }}</p>
                    <p>应还时间：{{ formatDate(record.dueTime) }}</p>
                    <p :class="getDueTimeInfo(record).className">
                      {{ getDueTimeInfo(record).text }}
                    </p>
                  </div>
                  <el-button type="primary" size="small" @click="handleReturn(record)">
                    还书
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 借阅历史 -->
        <el-tab-pane label="借阅历史" name="history">
          <div v-loading="historyLoading" class="borrow-list">
            <el-empty v-if="historyBorrows.records.length === 0" description="暂无借阅历史" />
            <div v-else>
              <div class="borrow-grid">
                <div v-for="record in historyBorrows.records" :key="record.id" class="borrow-card">
                  <el-image
                    :src="record.book?.coverUrl || '/default-book.png'"
                    fit="cover"
                    class="book-cover"
                  >
                    <template #error>
                      <div class="image-slot">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div class="book-info">
                    <h3 class="book-title">{{ record.book?.title }}</h3>
                    <p class="book-author">{{ record.book?.author }}</p>
                    <div class="borrow-info">
                      <p>借阅时间：{{ formatDate(record.borrowTime) }}</p>
                      <p>归还时间：{{ formatDate(record.returnTime) }}</p>
                    </div>
                    <el-button type="success" size="small" @click="handleBorrowAgain(record)">
                      再次借阅
                    </el-button>
                  </div>
                </div>
              </div>
              <div class="pagination-container">
                <el-pagination
                  v-model:current-page="historyCurrent"
                  v-model:page-size="historySize"
                  :total="historyBorrows.total"
                  :page-sizes="[10, 20, 50]"
                  layout="total, sizes, prev, pager, next"
                  @size-change="fetchHistoryBorrows"
                  @current-change="fetchHistoryBorrows"
                />
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { borrowApi } from '@/api/borrow'
import type { BorrowRecord, PageResponse } from '@/types'

const router = useRouter()

const activeTab = ref('current')
const currentLoading = ref(false)
const historyLoading = ref(false)
const currentBorrows = ref<BorrowRecord[]>([])
const historyBorrows = ref<PageResponse<BorrowRecord>>({
  records: [],
  total: 0,
  size: 10,
  current: 1,
  pages: 0
})
const historyCurrent = ref(1)
const historySize = ref(10)

// 获取当前借阅
const fetchCurrentBorrows = async () => {
  currentLoading.value = true
  try {
    const data = await borrowApi.getCurrentBorrows()
    currentBorrows.value = data
  } catch (error) {
    console.error('获取当前借阅失败:', error)
  } finally {
    currentLoading.value = false
  }
}

// 获取借阅历史
const fetchHistoryBorrows = async () => {
  historyLoading.value = true
  try {
    const data = await borrowApi.getBorrowHistory(historyCurrent.value, historySize.value)
    historyBorrows.value = data
  } catch (error) {
    console.error('获取借阅历史失败:', error)
  } finally {
    historyLoading.value = false
  }
}

// Tab切换
const handleTabChange = (name: string) => {
  if (name === 'current') {
    fetchCurrentBorrows()
  } else if (name === 'history') {
    fetchHistoryBorrows()
  }
}

// 还书
const handleReturn = async (record: BorrowRecord) => {
  try {
    await ElMessageBox.confirm('确定要归还这本书吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    await borrowApi.returnBook(record.id!)
    ElMessage.success('归还成功')
    fetchCurrentBorrows()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('归还失败:', error)
    }
  }
}

// 再次借阅
const handleBorrowAgain = async (record: BorrowRecord) => {
  if (record.bookId) {
    router.push(`/books/${record.bookId}`)
  }
}

// 格式化日期
const formatDate = (date?: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 计算剩余天数
const getRemainingDays = (record: BorrowRecord) => {
  if (!record.dueTime) return 0
  const dueDate = new Date(record.dueTime)
  const now = new Date()
  const diff = dueDate.getTime() - now.getTime()
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
}

// 获取剩余时间信息（文本和样式类）
const getDueTimeInfo = (record: BorrowRecord) => {
  const days = getRemainingDays(record)
  let text: string
  let className: string

  if (days < 0) {
    text = `已逾期 ${Math.abs(days)} 天`
    className = 'overdue'
  } else if (days === 0) {
    text = '今天到期'
    className = 'warning'
  } else {
    text = `剩余 ${days} 天`
    className = days <= 3 ? 'warning' : 'normal'
  }

  return { text, className }
}

onMounted(() => {
  fetchCurrentBorrows()
})
</script>

<style scoped>
.my-borrow-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
}

.borrow-list {
  min-height: 300px;
}

.borrow-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.borrow-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
}

.borrow-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.book-cover {
  width: 100%;
  height: 200px;
  background-color: #f5f7fa;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: #909399;
}

.image-slot .el-icon {
  font-size: 48px;
}

.book-info {
  padding: 16px;
}

.book-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  margin: 0 0 12px;
  font-size: 14px;
  color: #909399;
}

.borrow-info {
  margin-bottom: 12px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.borrow-info p {
  margin: 4px 0;
  font-size: 13px;
  color: #606266;
}

.borrow-info .overdue {
  color: #f56c6c;
  font-weight: 500;
}

.borrow-info .warning {
  color: #e6a23c;
  font-weight: 500;
}

.borrow-info .normal {
  color: #67c23a;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
