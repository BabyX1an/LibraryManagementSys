<template>
  <el-container class="layout-container">
    <el-aside v-if="showSidebar" width="200px">
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        :collapse="isCollapse"
        router
        background-color="#545c64"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <el-menu-item index="/home">
          <el-icon><House /></el-icon>
          <template #title>首页</template>
        </el-menu-item>

        <el-menu-item index="/books">
          <el-icon><Reading /></el-icon>
          <template #title>图书列表</template>
        </el-menu-item>

        <el-menu-item index="/my-borrow">
          <el-icon><Notebook /></el-icon>
          <template #title>我的借阅</template>
        </el-menu-item>

        <el-sub-menu index="/admin" v-if="isAdmin">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>管理后台</span>
          </template>
          <el-menu-item index="/admin/dashboard">数据统计</el-menu-item>
          <el-menu-item index="/admin/books">图书管理</el-menu-item>
          <el-menu-item index="/admin/users">用户管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header>
        <div class="header-left">
          <el-button type="text" @click="toggleSidebar">
            <el-icon size="20"><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
          </el-button>
          <h2>{{ pageTitle }}</h2>
        </div>

        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              {{ user?.realName || user?.username }}
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const user = computed(() => userStore.user)

// 判断是否显示侧边栏
const showSidebar = computed(() => {
  return !['/login'].includes(route.path)
})

// 判断是否是管理员
const isAdmin = computed(() => {
  return user.value?.role === 'ADMIN' || user.value?.role === 2
})

// 当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 页面标题
const pageTitle = computed(() => {
  return route.meta?.title || '图书管理系统'
})

// 切换侧边栏
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 处理下拉菜单命令
const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      userStore.logout()
      router.push('/login')
      break
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.el-aside {
  background-color: #545c64;
  transition: width 0.3s;
}

.el-menu-vertical {
  border-right: none;
}

.el-header {
  background-color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;

  h2 {
    margin-left: 20px;
    font-size: 18px;
    font-weight: 500;
    color: #303133;
  }
}

.header-right {
  .user-dropdown {
    display: flex;
    align-items: center;
    cursor: pointer;
    color: #606266;

    &:hover {
      color: #409eff;
    }
  }
}

.el-main {
  background-color: #f5f5f5;
  padding: 0;
}
</style>