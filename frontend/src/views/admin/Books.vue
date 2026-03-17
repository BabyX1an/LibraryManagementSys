<template>
  <AdminLayout>
    <div class="books-container">
      <!-- 工具栏 -->
      <el-card class="toolbar-card">
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
            <el-button type="success" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增图书
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 数据表格 -->
      <el-card class="table-card">
        <el-table :data="bookList" v-loading="loading" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="书名" min-width="200" />
          <el-table-column prop="author" label="作者" width="120" />
          <el-table-column prop="publisher" label="出版社" width="150" />
          <el-table-column prop="category" label="分类" width="100">
            <template #default="{ row }">
              <el-tag size="small" type="info">{{ row.category }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.stock > 10 ? 'success' : row.stock > 0 ? 'warning' : 'danger'">
                {{ row.stock }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="borrowedCount" label="借阅次数" width="100" align="center" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'AVAILABLE' ? 'success' : 'danger'">
                {{ row.status === 'AVAILABLE' ? '可借' : '下架' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row.id!)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
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

      <!-- 新增/编辑对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="600px"
        :close-on-click-modal="false"
      >
        <el-form :model="bookForm" :rules="rules" ref="bookFormRef" label-width="100px">
          <el-form-item label="书名" prop="title">
            <el-input v-model="bookForm.title" placeholder="请输入书名" />
          </el-form-item>
          <el-form-item label="作者" prop="author">
            <el-input v-model="bookForm.author" placeholder="请输入作者" />
          </el-form-item>
          <el-form-item label="ISBN" prop="isbn">
            <el-input v-model="bookForm.isbn" placeholder="请输入ISBN" />
          </el-form-item>
          <el-form-item label="出版社">
            <el-input v-model="bookForm.publisher" placeholder="请输入出版社" />
          </el-form-item>
          <el-form-item label="出版日期">
            <el-date-picker
              v-model="bookForm.publishDate"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="bookForm.category" placeholder="请选择分类">
              <el-option label="小说" value="小说" />
              <el-option label="科技" value="科技" />
              <el-option label="历史" value="历史" />
              <el-option label="文学" value="文学" />
              <el-option label="教育" value="教育" />
              <el-option label="艺术" value="艺术" />
            </el-select>
          </el-form-item>
          <el-form-item label="库存">
            <el-input-number v-model="bookForm.stock" :min="0" :max="1000" />
          </el-form-item>
          <el-form-item label="封面URL">
            <el-input v-model="bookForm.coverUrl" placeholder="请输入封面图片URL" />
          </el-form-item>
          <el-form-item label="图书简介">
            <el-input
              v-model="bookForm.description"
              type="textarea"
              :rows="4"
              placeholder="请输入图书简介"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="bookForm.status">
              <el-radio label="AVAILABLE">可借</el-radio>
              <el-radio label="UNAVAILABLE">下架</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, RefreshRight, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bookApi } from '@/api/book'
import type { Book, FormInstance, FormRules } from '@/types'
import AdminLayout from './Layout.vue'

const loading = ref(false)
const dialogVisible = ref(false)
const bookFormRef = ref<FormInstance>()
const isEdit = ref(false)
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

const bookForm = reactive<Partial<Book>>({
  title: '',
  author: '',
  isbn: '',
  publisher: '',
  publishDate: '',
  category: '',
  stock: 10,
  coverUrl: '',
  description: '',
  status: 'AVAILABLE'
})

const dialogTitle = computed(() => (isEdit.value ? '编辑图书' : '新增图书'))

const rules: FormRules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }]
}

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

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: Book) => {
  isEdit.value = true
  Object.assign(bookForm, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该图书吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await bookApi.deleteBook(id)
    ElMessage.success('删除成功')
    fetchBooks()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 提交
const handleSubmit = async () => {
  if (!bookFormRef.value) return

  await bookFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (isEdit.value) {
        await bookApi.updateBook(bookForm)
        ElMessage.success('更新成功')
      } else {
        await bookApi.addBook(bookForm)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      fetchBooks()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    }
  })
}

// 重置表单
const resetForm = () => {
  bookForm.id = undefined
  bookForm.title = ''
  bookForm.author = ''
  bookForm.isbn = ''
  bookForm.publisher = ''
  bookForm.publishDate = ''
  bookForm.category = ''
  bookForm.stock = 10
  bookForm.coverUrl = ''
  bookForm.description = ''
  bookForm.status = 'AVAILABLE'
  bookFormRef.value?.resetFields()
}

onMounted(() => {
  fetchBooks()
})
</script>

<style scoped>
.books-container {
  .toolbar-card,
  .table-card {
    margin-bottom: 20px;
  }
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}
</style>