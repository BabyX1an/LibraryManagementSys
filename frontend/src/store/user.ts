import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User, LoginRequest } from '@/types'
import { userApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)

  // 初始化：从localStorage加载
  const initUser = () => {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    if (storedToken) {
      token.value = storedToken
    }
    if (storedUser) {
      try {
        user.value = JSON.parse(storedUser)
      } catch (e) {
        localStorage.removeItem('user')
      }
    }
  }

  // 登录
  const login = async (loginData: LoginRequest) => {
    try {
      console.log('发送登录请求:', loginData)
      const response = await userApi.login(loginData)
      console.log('API登录响应:', response)

      // 响应格式: { user: User, token: string }
      if (response && 'user' in response && 'token' in response) {
        console.log('响应包含user和token')
        user.value = response.user
        token.value = response.token
        localStorage.setItem('token', response.token)
        localStorage.setItem('user', JSON.stringify(response.user))
        return response
      } else {
        console.error('响应格式错误:', response)
        throw new Error('登录响应格式错误')
      }
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
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
      const response = await userApi.getCurrentUser()
      user.value = response
      localStorage.setItem('user', JSON.stringify(response))
      return response
    } catch (error) {
      logout()
      return null
    }
  }

  // 更新用户信息
  const updateUser = async (userData: Partial<User>) => {
    try {
      const response = await userApi.updateCurrentUser(userData)
      user.value = { ...user.value, ...response }
      localStorage.setItem('user', JSON.stringify(user.value))
      return response
    } catch (error) {
      throw error
    }
  }

  return {
    user,
    token,
    initUser,
    login,
    logout,
    getUserInfo,
    updateUser
  }
})
