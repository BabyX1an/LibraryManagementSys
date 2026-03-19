<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>图书管理系统</h2>
          <p>{{ isRegister ? '欢迎注册' : '欢迎登录' }}</p>
        </div>
      </template>

      <!-- 登录表单 -->
      <el-form v-if="!isRegister" :model="loginForm" :rules="loginRules" ref="loginFormRef">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>

        <div class="switch-link">
          还没有账号？<a @click="switchToRegister">立即注册</a>
        </div>
      </el-form>

      <!-- 注册表单 -->
      <el-form v-else :model="registerForm" :rules="registerRules" ref="registerFormRef">
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="账号"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item prop="realName">
          <el-input
            v-model="registerForm.realName"
            placeholder="真实姓名"
            :prefix-icon="UserFilled"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="手机号（选填）"
            :prefix-icon="Phone"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="邮箱（选填）"
            :prefix-icon="Message"
            size="large"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>

        <div class="switch-link">
          已有账号？<a @click="switchToLogin">返回登录</a>
        </div>
      </el-form>

      <div class="login-footer">
        <p>默认管理员账号: admin / admin123</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled, Phone, Message } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { userApi } from '@/api/user'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const isRegister = ref(false)

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
})

// 注册表单
const registerForm = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: ''
})

// 登录校验规则
const loginRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 注册校验规则
const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 切换到注册
const switchToRegister = () => {
  isRegister.value = true
  registerFormRef.value?.resetFields()
}

// 切换到登录
const switchToLogin = () => {
  isRegister.value = false
  loginFormRef.value?.resetFields()
}

// 登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await userStore.login(loginForm)
      ElMessage.success('登录成功')

      // 根据用户角色跳转
      const user = userStore.user
      if (user?.role === 2 || user?.role === 'ADMIN') {
        router.push('/admin/dashboard')
      } else {
        router.push('/home')
      }
    } catch (error: any) {
      ElMessage.error(error.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}

// 注册
const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await userApi.addUser({
        username: registerForm.username,
        password: registerForm.password,
        realName: registerForm.realName,
        role: 1,  // 普通用户
        phone: registerForm.phone || undefined,
        email: registerForm.email || undefined,
        status: 1
      })
      ElMessage.success('注册成功，请登录')
      switchToLogin()
      // 自动填充用户名
      loginForm.username = registerForm.username
    } catch (error: any) {
      ElMessage.error(error.message || '注册失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  border-radius: 10px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.card-header {
  text-align: center;

  h2 {
    margin: 0;
    font-size: 24px;
    color: #303133;
  }

  p {
    margin: 8px 0 0;
    font-size: 14px;
    color: #909399;
  }
}

.switch-link {
  text-align: center;
  margin-top: 10px;
  font-size: 14px;
  color: #909399;

  a {
    color: #409eff;
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }
}

.login-footer {
  margin-top: 20px;
  text-align: center;

  p {
    margin: 0;
    font-size: 12px;
    color: #909399;
  }
}

:deep(.el-card__header) {
  padding: 30px 20px;
}

:deep(.el-card__body) {
  padding: 30px;
}
</style>
