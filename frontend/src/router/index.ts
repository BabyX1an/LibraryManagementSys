import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'
import Layout from '@/components/Layout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'Home',
    component: Layout,
    children: [
      {
        path: '',
        name: 'HomeContent',
        component: () => import('@/views/user/Home.vue'),
        meta: { title: '首页' }
      }
    ]
  },
  {
    path: '/books',
    name: 'Books',
    component: Layout,
    children: [
      {
        path: '',
        name: 'BooksContent',
        component: () => import('@/views/user/Books.vue'),
        meta: { title: '图书列表' }
      },
      {
        path: ':id',
        name: 'BookDetail',
        component: () => import('@/views/user/BookDetail.vue'),
        meta: { title: '图书详情' }
      }
    ]
  },
  {
    path: '/my-borrow',
    name: 'MyBorrow',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'MyBorrowContent',
        component: () => import('@/views/user/MyBorrow.vue'),
        meta: { title: '我的借阅', requiresAuth: true }
      }
    ]
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'ProfileContent',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人信息', requiresAuth: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: Layout,
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据统计', requiresAuth: true, roles: ['ADMIN'] }
      },
      {
        path: 'books',
        name: 'AdminBooks',
        component: () => import('@/views/admin/Books.vue'),
        meta: { title: '图书管理', requiresAuth: true, roles: ['ADMIN'] }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue'),
        meta: { title: '用户管理', requiresAuth: true, roles: ['ADMIN'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 图书管理系统` : '图书管理系统'

  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null
  const roles = (to.meta as any).roles as string[]

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (roles && user) {
    // 检查角色：支持数字 2 和字符串 'ADMIN'
    const userRole = user.role
    if (!roles.includes(userRole as string) && userRole !== 2) {
      ElMessage.error('无权访问此页面')
      next(from.path)
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
