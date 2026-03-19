import request from '@/utils/request'
import type { User, PageResponse, LoginRequest } from '@/types'

export const userApi = {
  // 分页查询用户
  pageUsers(current: number = 1, size: number = 10, username?: string, realName?: string) {
    return request.get<PageResponse<User>>('/v1/users', {
      params: { current, size, username, realName }
    })
  },

  // 根据ID查询用户
  getUserById(id: number) {
    return request.get<User>(`/v1/users/${id}`)
  },

  // 添加用户
  addUser(user: Partial<User>) {
    return request.post<User>('/v1/users', user)
  },

  // 更新用户
  updateUser(user: Partial<User>) {
    const id = user.id
    if (!id) {
      throw new Error('更新用户时必须提供用户ID')
    }
    return request.put<User>(`/v1/users/${id}`, user)
  },

  // 删除用户
  deleteUser(id: number) {
    return request.delete(`/v1/users/${id}`)
  },

  // 用户登录
  login(data: LoginRequest) {
    console.log('调用登录API，参数:', data)
    return request.post('/v1/users/login', data)
  },

  // 获取当前用户信息
  getCurrentUser() {
    return request.get<User>('/v1/users/me')
  },

  // 更新当前用户信息
  updateCurrentUser(user: Partial<User>) {
    return request.put<User>('/v1/users/me', user)
  }
}