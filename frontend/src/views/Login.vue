<template>
  <div class="login-page">
    <el-card class="login-card" shadow="always">
      <h2 class="login-title">任务管理系统</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width:100%" @click="handleLogin">登 录</el-button>
        </el-form-item>
        <el-form-item>
          <el-button style="width:100%" @click="handleRegister">注 册</el-button>
        </el-form-item>
      </el-form>
      <p class="login-hint">测试账号: mentor / 123456 或 lisi / 123456</p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '../utils/request'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => {})
  if (!valid) return
  loading.value = true
  try {
    const res = await request.post('/auth/login', form)
    if (res.data) {
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userId', res.data.user.id)
      localStorage.setItem('nickname', res.data.user.nickname)
      localStorage.setItem('role', res.data.user.role)
      localStorage.setItem('logged_in', 'true')
      ElMessage.success('登录成功')
      router.push('/dashboard')
    }
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}

function handleRegister() {
  router.push('/register')
}
</script>

<style scoped>
.login-page {
  display: flex; justify-content: center; align-items: center;
  height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card { width: 400px; border-radius: 12px; }
.login-title { text-align: center; margin-bottom: 24px; color: #303133; }
.login-hint { text-align: center; font-size: 12px; color: #909399; margin: 0; }
</style>
