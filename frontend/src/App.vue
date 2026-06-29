<template>
  <template v-if="isLogin">
    <router-view />
  </template>
  <template v-else>
    <el-container class="layout-container">
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon :size="24"><Menu /></el-icon>
          <span class="header-title">任务管理系统</span>
        </div>
        <div class="header-right">
          <span class="user-info">{{ getNickname() }}（{{ getRole() }}）</span>
          <el-button type="danger" size="small" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px" class="layout-aside">
          <el-menu :default-active="currentRoute" router>
            <el-menu-item index="/dashboard">
              <el-icon><DataAnalysis /></el-icon><span>仪表盘</span>
            </el-menu-item>
            <el-menu-item index="/tasks">
              <el-icon><List /></el-icon><span>任务列表</span>
            </el-menu-item>
            <el-menu-item index="/news">
              <el-icon><Reading /></el-icon><span>实时资讯</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="layout-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </template>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const isLogin = computed(() => route.path === '/login' || route.path === '/register')
const currentRoute = computed(() => route.path)

function getNickname() { return localStorage.getItem('nickname') || '' }
function getRole() { return localStorage.getItem('role') || '' }

function logout() {
  localStorage.clear()
  router.push('/login')
}
</script>

<style>
body { margin: 0; }
.layout-container { height: 100vh; }
.layout-header {
  display: flex; align-items: center; justify-content: space-between;
  background: #fff; border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(0,0,0,.08);
}
.header-left { display: flex; align-items: center; gap: 12px; font-size: 18px; font-weight: 600; }
.header-right { display: flex; align-items: center; gap: 16px; }
.user-info { font-size: 14px; color: #606266; }
.layout-aside { border-right: 1px solid #e4e7ed; background: #fff; }
.layout-main { background: #f5f7fb; min-height: calc(100vh - 60px); }
</style>
