<template>
  <div class="news-page">
    <el-card shadow="hover">
      <template #header>
        <div class="news-header">
          <span>实时资讯</span>
          <div>
            <el-input v-model="keyword" placeholder="搜索资讯" size="small" style="width:200px;margin-right:4px" clearable @keyup.enter="onSearch" />
            <el-button type="primary" :icon="Search" size="small" @click="onSearch">搜索</el-button>
            <el-button :icon="Refresh" size="small" @click="loadNews">刷新</el-button>
          </div>
        </div>
      </template>
      <el-timeline v-loading="loading">
        <el-timeline-item v-for="item in newsList" :key="item.url" :timestamp="item.publishTime" placement="top">
          <el-card shadow="hover" class="news-item">
            <a :href="item.url" target="_blank" class="news-title">{{ item.title }}</a>
            <p class="news-desc">{{ item.description }}</p>
            <span class="news-source">{{ item.source }}</span>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <div style="display:flex;justify-content:center;margin-top:16px">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadNews"
          @size-change="loadNews"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import request from '../utils/request'

const keyword = ref('')
const newsList = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

function onSearch() {
  page.value = 1
  loadNews()
}

async function loadNews() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (keyword.value) params.keyword = keyword.value
    const res = await request.get('/news', { params })
    newsList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(loadNews)
</script>

<style scoped>
.news-header { display: flex; justify-content: space-between; align-items: center; }
.news-item { cursor: default; }
.news-title { font-weight: 600; color: #409eff; text-decoration: none; font-size: 15px; }
.news-title:hover { text-decoration: underline; }
.news-desc { font-size: 13px; color: #606266; margin: 6px 0; }
.news-source { font-size: 12px; color: #909399; }
</style>
