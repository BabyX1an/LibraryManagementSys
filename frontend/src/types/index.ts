// 用户相关类型
export interface User {
  id?: number
  username: string
  password?: string
  realName?: string
  email?: string
  phone?: string
  role?: string
  status?: string
  avatarUrl?: string
  lastLoginTime?: string
  createdAt?: string
  updatedAt?: string
  deleted?: number
}

// 图书相关类型
export interface Book {
  id?: number
  isbn?: string
  title?: string
  author?: string
  publisher?: string
  publishDate?: string
  category?: string
  description?: string
  coverUrl?: string
  stock?: number
  borrowedCount?: number
  status?: string
  createdAt?: string
  updatedAt?: string
  deleted?: number
}

// 分页响应类型
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// API 响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 登录请求类型
export interface LoginRequest {
  username: string
  password: string
}

// Element Plus 表单类型
export interface FormInstance {
  validate: (callback: (valid: boolean) => void) => void
  resetFields: () => void
}

export interface FormRules {
  [key: string]: any[]
}
