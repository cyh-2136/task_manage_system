<template>
  <div class="register-page">
    <el-card class="register-card" shadow="always">
      <h2 class="register-title">注册账号</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名（3~50个字符）" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称（选填）" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（6~50个字符）" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-select v-model="form.role" style="width:100%">
            <el-option label="实习生" value="实习生" />
            <el-option label="导师" value="导师" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width:100%" @click="handleRegister">注 册</el-button>
        </el-form-item>
        <el-form-item>
          <el-button style="width:100%" @click="goLogin">返回登录</el-button>
        </el-form-item>
      </el-form>
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
  nickname: '',
  password: '',
  role: '实习生'
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度3~50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度6~50个字符', trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => {})
  if (!valid) return
  loading.value = true
  try {
    await request.post('/auth/register', form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    ElMessage.error(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}

function goLogin() {
  router.push('/login')
}
</script>

<style scoped>
.register-page {
  display: flex; justify-content: center; align-items: center;
  height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.register-card { width: 400px; border-radius: 12px; }
.register-title { text-align: center; margin-bottom: 24px; color: #303133; }
</style>
