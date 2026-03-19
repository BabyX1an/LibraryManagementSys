<template>
    <div class="users-container">
      <!-- 工具栏 -->
      <el-card class="toolbar-card">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="用户名">
            <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
          </el-form-item>
          <el-form-item label="真实姓名">
            <el-input v-model="searchForm.realName" placeholder="请输入真实姓名" clearable />
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
              新增用户
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 数据表格 -->
      <el-card class="table-card">
        <el-table :data="userList" v-loading="loading" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="realName" label="真实姓名" width="120" />
          <el-table-column prop="email" label="邮箱" width="180" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="row.role === 2 ? 'danger' : 'primary'" size="small">
                {{ row.role === 2 ? '管理员' : '普通用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastLoginTime" label="最后登录" width="180" />
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
            @current-change="fetchUsers"
            @size-change="fetchUsers"
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
        <el-form :model="userForm" :rules="rules" ref="userFormRef" label-width="100px">
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="userForm.username"
              placeholder="请输入用户名"
              :disabled="isEdit"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password" v-if="!isEdit">
            <el-input
              v-model="userForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="userForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="userForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="角色" prop="role">
            <el-radio-group v-model="userForm.role">
              <el-radio :value="1">普通用户</el-radio>
              <el-radio :value="2">管理员</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="userForm.status">
              <el-radio :value="1">正常</el-radio>
              <el-radio :value="2">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="头像URL">
            <el-input v-model="userForm.avatarUrl" placeholder="请输入头像图片URL" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </template>
      </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, RefreshRight, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api/user'
import type { User, FormInstance, FormRules } from '@/types'
import AdminLayout from './Layout.vue'

const loading = ref(false)
const dialogVisible = ref(false)
const userFormRef = ref<FormInstance>()
const isEdit = ref(false)
const userList = ref<User[]>([])
const total = ref(0)

const searchForm = reactive({
  username: '',
  realName: ''
})

const pagination = reactive({
  current: 1,
  size: 10
})

const userForm = reactive<Partial<User>>({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  role: 1,
  status: 1,
  avatarUrl: ''
})

const dialogTitle = computed(() => (isEdit.value ? '编辑用户' : '新增用户'))

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const { current, size } = pagination
    const pageData = await userApi.pageUsers(
      current,
      size,
      searchForm.username || undefined,
      searchForm.realName || undefined
    )
    userList.value = pageData?.records || []
    total.value = pageData?.total || 0
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchUsers()
}

// 重置
const handleReset = () => {
  searchForm.username = ''
  searchForm.realName = ''
  pagination.current = 1
  fetchUsers()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: User) => {
  isEdit.value = true
  Object.assign(userForm, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await userApi.deleteUser(id)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 提交
const handleSubmit = async () => {
  if (!userFormRef.value) return

  await userFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (isEdit.value) {
        await userApi.updateUser(userForm)
        ElMessage.success('更新成功')
      } else {
        await userApi.addUser(userForm)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      fetchUsers()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    }
  })
}

// 重置表单
const resetForm = () => {
  userForm.id = undefined
  userForm.username = ''
  userForm.password = ''
  userForm.realName = ''
  userForm.email = ''
  userForm.phone = ''
  userForm.role = 1
  userForm.status = 1
  userForm.avatarUrl = ''
  userFormRef.value?.resetFields()
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.users-container {
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