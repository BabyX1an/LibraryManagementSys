<template>
  <div class="home-container">
    <!-- 欢迎横幅 -->
    <el-card class="banner-card">
      <div class="banner-content">
        <h1>欢迎来到图书管理系统</h1>
        <p>发现好书，享受阅读的乐趣</p>
        <el-button type="primary" size="large" @click="goToBooks">
          <el-icon><Reading /></el-icon>
          浏览图书
        </el-button>
      </div>
    </el-card>

    <!-- 热门图书 -->
    <el-card class="popular-books">
      <template #header>
        <div class="card-header">
          <h3>热门图书</h3>
          <el-button text type="primary" @click="goToBooks">查看更多</el-button>
        </div>
      </template>

      <div v-loading="loading" class="books-grid">
        <div
          v-for="book in popularBooks"
          :key="book.id"
          class="book-card"
          @click="viewBookDetail(book.id!)"
        >
          <div class="book-cover">
            <el-image
              :src="book.coverUrl || '/default-book.svg'"
              fit="cover"
              :preview-src-list="[book.coverUrl || '/default-book.svg']"
            >
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
          <div class="book-info">
            <h4 class="book-title">{{ book.title }}</h4>
            <p class="book-author">{{ book.author }}</p>
            <div class="book-meta">
              <span class="stock">库存: {{ book.stock }}</span>
              <span class="borrowed">借阅: {{ book.borrowedCount }}</span>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && popularBooks.length === 0" description="暂无热门图书" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Reading, Picture } from '@element-plus/icons-vue'
import { bookApi } from '@/api/book'
import type { Book } from '@/types'

const router = useRouter()
const loading = ref(false)
const popularBooks = ref<Book[]>([])

// 获取热门图书
const fetchPopularBooks = async () => {
  loading.value = true
  try {
    const books = await bookApi.getPopularBooks(8)
    popularBooks.value = Array.isArray(books) ? books : []
  } catch (error) {
    console.error('获取热门图书失败:', error)
  } finally {
    loading.value = false
  }
}

// 跳转到图书列表
const goToBooks = () => {
  router.push('/books')
}

// 查看图书详情
const viewBookDetail = (id: number) => {
  router.push(`/books/${id}`)
}

onMounted(() => {
  fetchPopularBooks()
})
</script>

<style scoped>
.home-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.banner-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.banner-content {
  text-align: center;
  padding: 40px 20px;
  color: #fff;

  h1 {
    margin: 0 0 15px;
    font-size: 36px;
    font-weight: 600;
  }

  p {
    margin: 0 0 25px;
    font-size: 18px;
    opacity: 0.9;
  }

  .el-button {
    background-color: #fff;
    color: #667eea;
    border-color: #fff;

    &:hover {
      background-color: #f5f5f5;
      color: #667eea;
      border-color: #fff;
    }
  }
}

.popular-books {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 500;
      color: #303133;
    }
  }
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.book-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.book-cover {
  width: 100%;
  height: 280px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f5f5f5;

  .el-image {
    width: 100%;
    height: 100%;
  }

  .image-slot {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    background-color: #f5f7fa;
    color: #909399;

    .el-icon {
      font-size: 48px;
    }
  }
}

.book-info {
  padding: 12px 0 0;

  .book-title {
    margin: 0 0 6px;
    font-size: 14px;
    font-weight: 500;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .book-author {
    margin: 0 0 8px;
    font-size: 12px;
    color: #909399;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .book-meta {
    display: flex;
    justify-content: space-between;
    font-size: 12px;

    .stock {
      color: #67c23a;
    }

    .borrowed {
      color: #409eff;
    }
  }
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>