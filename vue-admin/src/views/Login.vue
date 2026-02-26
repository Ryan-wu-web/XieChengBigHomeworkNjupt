<template>
  <div class="login-container">
    <div class="login-content">
      <div class="login-header">
        <img src="https://element-plus.org/images/element-plus-logo.svg" alt="logo" class="logo-img">
        <h1 class="project-title">易宿酒店预订管理平台</h1>
      </div>
      <el-card class="login-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>用户登录</span>
          </div>
        </template>
        <el-form :model="form" size="large" @keyup.enter="login">
          <el-form-item>
            <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" round @click="login" style="width: 100%">立即登录</el-button>
          </el-form-item>
          <div class="form-footer">
            <router-link to="/register" class="link">注册新账号</router-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import request from '../utils/request'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const login = () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  request.post('/user/login', form).then((res: any) => {
    // res is Result<LoginResult> { code, msg, data: { token, user } }
    if (res.data && res.data.token && res.data.user) {
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('user', JSON.stringify(res.data.user))
        ElMessage.success('登录成功')
        router.push('/dashboard')
    } else {
        // Fallback or error if structure is unexpected
         ElMessage.error('登录响应异常')
    }
  }).finally(() => {
    loading.value = false
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-image: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  background-size: cover;
}

.login-content {
  text-align: center;
}

.login-header {
  margin-bottom: 20px;
}

.logo-img {
  height: 40px;
  vertical-align: middle;
  margin-right: 10px;
}

.project-title {
  display: inline-block;
  vertical-align: middle;
  font-size: 24px;
  color: #fff;
  margin: 0;
  font-weight: 600;
}

.login-card {
  width: 400px;
  border-radius: 8px;
  text-align: left;
}

.card-header {
  text-align: center;
  font-weight: bold;
  font-size: 18px;
}

.form-footer {
  text-align: right;
}

.link {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.link:hover {
  text-decoration: underline;
}
</style>
