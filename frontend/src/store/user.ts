import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User, LoginRequest } from '@/types'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)

  // 登录
  const login = async (loginData: LoginRequest) => {
    const response = await request.post('/user/login', loginData)
    user.value = response.data.user
    token.value = response.data.token
    localStorage.setItem('token', response.data.token)
    localStorage.setItem('user', JSON.stringify(response.data.user))
  }

  // 登出
  const logout = () => {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  // 获取用户信息
  const getUserInfo = async () => {
    if (!token.value) return null

    try {
      const response = await request.get('/user/me')
      user.value = response.data
      localStorage.setItem('user', JSON.stringify(response.data))
      return response.data
    } catch (error) {
      logout()
      return null
    }
  }

  return {
    user,
    token,
    login,
    logout,
    getUserInfo
  }
})