<template>
  <div class="profile-container">
    <el-card v-loading="loading" class="profile-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">个人信息</span>
          <div v-if="!isEditing" class="card-actions">
            <el-button type="primary" @click="startEdit">编辑</el-button>
            <el-button type="info" plain>修改密码</el-button>
          </div>
          <div v-else class="card-actions">
            <el-button type="primary" @click="saveProfile" :loading="saving">保存</el-button>
            <el-button @click="cancelEdit">取消</el-button>
          </div>
        </div>
      </template>

      <!-- 查看模式 -->
      <div v-if="!isEditing" class="profile-view">
        <div class="avatar-section">
          <el-avatar :size="100" :src="user?.avatarUrl || ''">
            <el-icon><User /></el-icon>
          </el-avatar>
          <h3>{{ user?.realName || user?.username }}</h3>
          <p class="username">@{{ user?.username }}</p>
        </div>

        <el-descriptions :column="1" border class="info-descriptions">
          <el-descriptions-item label="用户名">{{ user?.username }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ user?.realName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ user?.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ user?.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="user?.role === 'ADMIN' ? 'danger' : 'success'">
              {{ user?.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="user?.status === 'ACTIVE' ? 'success' : 'warning'">
              {{ user?.status === 'ACTIVE' ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="最后登录时间">
            {{ formatDateTime(user?.lastLoginTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">
            {{ formatDateTime(user?.createdAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 编辑模式 -->
      <div v-else class="profile-edit">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="头像URL" prop="avatarUrl">
            <el-input v-model="form.avatarUrl" placeholder="请输入头像URL" />
          </el-form-item>
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import type { User as UserType } from '@/types'

const userStore = useUserStore()
const formRef = ref<FormInstance>()

const user = ref<UserType | null>(null)
const loading = ref(false)
const saving = ref(false)
const isEditing = ref(false)

const form = reactive({
  realName: '',
  email: '',
  phone: '',
  avatarUrl: ''
})

const rules: FormRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  avatarUrl: [
    { type: 'url', message: '请输入正确的URL', trigger: 'blur' }
  ]
}

// 格式化日期时间
const formatDateTime = (dateTime?: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 获取用户信息
const fetchUserInfo = async () => {
  loading.value = true
  try {
    const response = await userStore.getUserInfo()
    if (response) {
      user.value = response
      // 初始化表单数据
      form.realName = response.realName || ''
      form.email = response.email || ''
      form.phone = response.phone || ''
      form.avatarUrl = response.avatarUrl || ''
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取用户信息失败')
  } finally {
    loading.value = false
  }
}

// 开始编辑
const startEdit = () => {
  if (user.value) {
    form.realName = user.value.realName || ''
    form.email = user.value.email || ''
    form.phone = user.value.phone || ''
    form.avatarUrl = user.value.avatarUrl || ''
  }
  isEditing.value = true
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  formRef.value?.resetFields()
}

// 保存用户信息
const saveProfile = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  saving.value = true
  try {
    await userStore.updateUser(form)
    user.value = { ...user.value!, ...form }
    isEditing.value = false
    ElMessage.success('保存成功')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  // 如果store中有用户信息，直接使用
  if (userStore.user) {
    user.value = userStore.user
    form.realName = user.value.realName || ''
    form.email = user.value.email || ''
    form.phone = user.value.phone || ''
    form.avatarUrl = user.value.avatarUrl || ''
  } else {
    // 否则从服务器获取
    fetchUserInfo()
  }
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.profile-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
}

.card-actions {
  display: flex;
  gap: 10px;
}

.avatar-section {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.avatar-section h3 {
  margin: 15px 0 5px;
  font-size: 20px;
  font-weight: 600;
}

.avatar-section .username {
  color: #909399;
  margin: 0;
  font-size: 14px;
}

.info-descriptions {
  margin-top: 20px;
}

.profile-edit {
  max-width: 500px;
  margin: 0 auto;
}
</style>
