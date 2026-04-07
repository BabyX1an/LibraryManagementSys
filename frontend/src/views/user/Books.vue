<template>

<div class="books-container">
  <!-- 搜索和筛选 -->
  <el-card class="search-card">
    <el-form :inline="true" :model="searchForm">
      <el-form-item label="书名">
        <el-input v-model="searchForm.title" placeholder="请输入书名" clearable />
      </el-form-item>
      <el-form-item label="作者">
        <el-input v-model="searchForm.author" placeholder="请输入作者" clearable />
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
          <el-option label="小说" value="小说" />
          <el-option label="科技" value="科技" />
          <el-option label="历史" value="历史" />
          <el-option label="文学" value="文学" />
          <el-option label="教育" value="教育" />
          <el-option label="艺术" value="艺术" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="handleReset">
          <el-icon><RefreshRight /></el-icon>
          重置
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>

  <!-- 图书列表 -->
  <el-card class="books-list">
    <div v-loading="loading" class="books-grid">
      <div
        v-for="book in bookList"
        :key="book.id"
        class="book-card"
        @click="viewBookDetail(book.id!)"
      >
        <div class="book-cover">
          <el-image
            :src="book.coverUrl || '/default-book.png'"
            fit="contain"
            :preview-src-list="[book.coverUrl || '/default-book.png']"
          >
            <template #error>
              <div class="image-slot">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <div v-if="book.stock === 0" class="out-of-stock">已售罄</div>
        </div>
        <div class="book-info">
          <h4 class="book-title">{{ book.title }}</h4>
          <p class="book-author">{{ book.author }}</p>
          <el-tag v-if="book.category" size="small" type="info">{{ book.category }}</el-tag>
          <div class="book-meta">
            <span class="stock">库存: {{ book.stock }}</span>
            <span class="borrowed">借阅: {{ book.borrowedCount }}</span>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && bookList.length === 0" description="暂无图书数据" />

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="total"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="fetchBooks"
        @size-change="fetchBooks"
      />
    </div>
  </el-card>
</div>

</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, RefreshRight, Picture } from '@element-plus/icons-vue'
import { bookApi } from '@/api/book'
import type { Book } from '@/types'
import Layout from '@/components/Layout.vue'

const router = useRouter()
const loading = ref(false)
const bookList = ref<Book[]>([])
const total = ref(0)

const searchForm = reactive({
  title: '',
  author: '',
  category: ''
})

const pagination = reactive({
  current: 1,
  size: 10
})

// 获取图书列表
const fetchBooks = async () => {
  loading.value = true
  try {
    const { current, size } = pagination
    const pageData = await bookApi.pageBooks(
      current,
      size,
      searchForm.title || undefined,
      searchForm.author || undefined,
      searchForm.category || undefined
    )
    bookList.value = pageData?.records || []
    total.value = pageData?.total || 0
  } catch (error) {
    console.error('获取图书列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchBooks()
}

// 重置
const handleReset = () => {
  searchForm.title = ''
  searchForm.author = ''
  searchForm.category = ''
  pagination.current = 1
  fetchBooks()
}

// 查看图书详情
const viewBookDetail = (id: number) => {
  router.push(`/books/${id}`)
}

onMounted(() => {
  fetchBooks()
})
</script>

<style scoped>
.books-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.search-card {
  margin-bottom: 20px;
}

.books-list {
  .books-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 20px;
    margin-bottom: 20px;
  }
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
  position: relative;
  width: 100%;
  height: 280px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #fff;  /* 白色背景 */

  .el-image {
    width: 100%;
    height: 100%;
  }

  /* 图片顶部对齐 */
  :deep(img) {
    object-position: top;
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

  .out-of-stock {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.6);
    display: flex;
    justify-content: center;
    align-items: center;
    color: #fff;
    font-size: 20px;
    font-weight: bold;
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

  .el-tag {
    margin-bottom: 8px;
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

.pagination-container {
  margin-top: 30px;
  text-align: center;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>