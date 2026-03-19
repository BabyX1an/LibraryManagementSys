import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    console.log('请求响应:', res)
    console.log('响应类型:', typeof res)
    console.log('响应包含code:', res && typeof res === 'object' && 'code' in res)

    // 检查是否是 ApiResponse 格式
    if (typeof res === 'object' && res !== null && 'code' in res) {
      const apiRes: ApiResponse = res
      console.log('ApiResponse格式:', apiRes)

      if (apiRes.code !== 200) {
        ElMessage.error(apiRes.message || '请求失败')

        // 401: Token过期或未登录
        if (apiRes.code === 401) {
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          window.location.href = '/login'
        }

        return Promise.reject(new Error(apiRes.message || '请求失败'))
      }

      return apiRes.data
    }

    // 如果不是 ApiResponse 格式，直接返回响应数据
    return response.data
  },
  (error) => {
    console.error('请求错误:', error)
    ElMessage.error(error.message || '网络请求失败')
    return Promise.reject(error)
  }
)

export default request
