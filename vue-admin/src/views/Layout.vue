<template>
  <div class="layout">
    <el-container class="main-container">
      <el-aside width="220px" class="aside">
        <div class="brand">
          <el-icon class="brand-icon"><OfficeBuilding /></el-icon>
          <span class="brand-text">易宿管理后台</span>
        </div>
        <el-menu
          router
          :default-active="$route.path"
          background-color="#1F2937"
          text-color="#E5E7EB"
          active-text-color="#1E88E5"
          class="el-menu-vertical"
        >
          <el-menu-item index="/dashboard">
            <el-icon><House /></el-icon>
            <span>酒店列表</span>
          </el-menu-item>
          <el-menu-item index="/dashboard/hotel-add" v-if="user.role === 'MERCHANT' || user.role === 'ADMIN'">
            <el-icon><Plus /></el-icon>
            <span>发布酒店</span>
          </el-menu-item>
          <el-menu-item index="/dashboard/user-manage" v-if="user.role === 'ADMIN'">
            <el-icon><User /></el-icon>
            <span>酒店账号管理</span>
          </el-menu-item>
          <el-menu-item index="/dashboard/consumer-manage" v-if="user.role === 'ADMIN'">
            <el-icon><UserFilled /></el-icon>
            <span>消费者用户管理</span>
          </el-menu-item>
          <el-menu-item index="/dashboard/banner-manage" v-if="user.role === 'ADMIN'">
            <el-icon><Picture /></el-icon>
            <span>广告轮播图管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header class="header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ $route.meta.title || '当前页面' }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                <el-avatar 
                  :size="30" 
                  :src="user.avatar" 
                  icon="UserFilled" 
                  style="margin-right: 8px; background: #409EFF" 
                />
                {{ user.username }}
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <el-main class="main-content">
          <router-view v-slot="{ Component }">
             <transition name="fade-transform" mode="out-in">
               <component :is="Component" />
             </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { House, Plus, OfficeBuilding, ArrowDown, UserFilled, User, Picture } from '@element-plus/icons-vue'

const router = useRouter()

// 使用响应式的 ref 存储用户信息
const user = ref({
  username: 'Guest',
  role: 'MERCHANT',
  avatar: '',
  phone: '',
  email: ''
})

// 从 localStorage 加载用户信息
const loadUser = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      const userData = JSON.parse(userStr)
      user.value = { ...user.value, ...userData }
    } catch (e) {
      console.error('Failed to parse user data:', e)
    }
  }
}

// 监听自定义事件，当用户信息更新时刷新
const handleUserUpdate = () => {
  loadUser()
}

onMounted(() => {
  loadUser()
  // 监听用户信息更新事件
  window.addEventListener('user-updated', handleUserUpdate)
})

onUnmounted(() => {
  // 清理事件监听
  window.removeEventListener('user-updated', handleUserUpdate)
})

const handleCommand = (command: string) => {
  if (command === 'logout') {
    localStorage.clear()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/dashboard/profile')
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
  width: 100%;
}

.main-container {
  height: 100%;
}

.aside {
  background-color: #304156;
  color: white;
  display: flex;
  flex-direction: column;
}

.brand {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b2f3a;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}

.brand-icon {
  margin-right: 8px;
  font-size: 22px;
}

.el-menu-vertical {
  border-right: none;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
}

.main-content {
  background-color: #F3F4F6;
  padding: 24px;
}

/* 卡片动画 */
.el-card {
  transition: all 0.3s ease;
}

.el-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* Transitions */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
