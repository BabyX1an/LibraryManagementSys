<template>

    <div class="book-detail-container">
      <el-card v-loading="loading">
        <template #header>
          <el-button @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
        </template>

        <div v-if="book" class="book-detail">
          <div class="book-left">
            <el-image
              :src="book.coverUrl || '/default-book.png'"
              fit="contain"
              style="width: 300px; height: 400px"
            >
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>

          <div class="book-right">
            <h1 class="book-title">{{ book.title }}</h1>
            <div class="book-meta-info">
              <p><span>作者:</span> {{ book.author }}</p>
              <p><span>出版社:</span> {{ book.publisher }}</p>
              <p><span>出版日期:</span> {{ book.publishDate }}</p>
              <p><span>分类:</span> {{ book.category }}</p>
              <p><span>ISBN:</span> {{ book.isbn }}</p>
              <p><span>库存:</span>
                <el-tag :type="book.stock && book.stock > 0 ? 'success' : 'danger'">
                  {{ book.stock }}
                </el-tag>
              </p>
              <p><span>借阅次数:</span> {{ book.borrowedCount }}</p>
            </div>
            <div class="book-description">
              <h3>图书简介</h3>
              <p>{{ book.description || '暂无简介' }}</p>
            </div>
            <div class="book-actions">
              <el-button
                type="primary"
                size="large"
                :disabled="book.stock === 0"
                @click="handleBorrow"
              >
                <el-icon><ShoppingCart /></el-icon>
                {{ !book.stock || book.stock === 0 ? '暂时缺货' : '借阅图书' }}
              </el-button>
            </div>
          </div>
        </div>

        <el-empty v-else-if="!loading" description="图书不存在或已被删除" />
      </el-card>
    </div>

</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Picture, ShoppingCart } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { bookApi } from '@/api/book'
import type { Book } from '@/types'
import Layout from '@/components/Layout.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const book = ref<Book | null>(null)

// 获取图书详情
const fetchBookDetail = async () => {
  loading.value = true
  try {
    const id = parseInt(route.params.id as string)
    const data = await bookApi.getBookById(id)
    book.value = data
  } catch (error) {
    console.error('获取图书详情失败:', error)
    ElMessage.error('获取图书详情失败')
  } finally {
    loading.value = false
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 借阅图书
const handleBorrow = () => {
  ElMessage.success('借阅请求已提交')
}

onMounted(() => {
  fetchBookDetail()
})
</script>

<style scoped>
.book-detail-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.book-detail {
  display: flex;
  gap: 40px;
}

.book-left {
  flex-shrink: 0;

  .el-image {
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  .image-slot {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    background-color: #f5f7fa;
    color: #909399;
    border-radius: 8px;

    .el-icon {
      font-size: 64px;
    }
  }
}

.book-right {
  flex: 1;

  .book-title {
    margin: 0 0 20px;
    font-size: 28px;
    font-weight: 600;
    color: #303133;
  }

  .book-meta-info {
    margin-bottom: 20px;
    padding: 20px;
    background-color: #f5f7fa;
    border-radius: 8px;

    p {
      margin: 8px 0;
      font-size: 14px;
      color: #606266;

      span {
        display: inline-block;
        width: 100px;
        color: #909399;
      }
    }
  }

  .book-description {
    margin-bottom: 30px;

    h3 {
      margin: 0 0 10px;
      font-size: 18px;
      font-weight: 500;
      color: #303133;
    }

    p {
      margin: 0;
      font-size: 14px;
      line-height: 1.8;
      color: #606266;
    }
  }

  .book-actions {
    display: flex;
    gap: 10px;
  }
}

@media (max-width: 768px) {
  .book-detail {
    flex-direction: column;
  }

  .book-left {
    display: flex;
    justify-content: center;
  }
}
</style>