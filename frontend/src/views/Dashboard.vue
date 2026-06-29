<template>
  <div class="dashboard">
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in statsCards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>任务状态分布</span></template>
          <v-chart :option="statusChart" style="height:300px" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>优先级分布</span></template>
          <v-chart :option="priorityChart" style="height:300px" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top:16px">
      <template #header><span>待办 & 进行中</span></template>
      <el-table :data="todoList" stripe style="width:100%">
        <el-table-column prop="title" label="任务标题" min-width="200" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)" size="small">{{ row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="截止日期" width="130">
          <template #default="{ row }">
            <el-tag v-if="row.deadline" :type="deadlineInfo(row.deadline).type" size="small">{{ deadlineInfo(row.deadline).text }}</el-tag>
            <span v-else style="color:#909399;font-size:12px">未设置</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import VChart from 'vue-echarts'
import 'echarts'
import request from '../utils/request'

const stats = ref({
  total: 0, todoCount: 0, inProgressCount: 0, doneCount: 0,
  completionRate: 0, highCount: 0, mediumCount: 0, lowCount: 0, overdueCount: 0
})
const todoList = ref([])

const statsCards = computed(() => [
  { label: '总任务', value: stats.value.total },
  { label: '待办', value: stats.value.todoCount },
  { label: '进行中', value: stats.value.inProgressCount },
  { label: '完成率', value: stats.value.completionRate + '%' }
])

const statusChart = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie', radius: ['40%', '70%'],
    data: [
      { value: stats.value.todoCount, name: '待办' },
      { value: stats.value.inProgressCount, name: '进行中' },
      { value: stats.value.doneCount, name: '已完成' }
    ],
    emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } }
  }]
}))

const priorityChart = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie', radius: ['40%', '70%'],
    data: [
      { value: stats.value.highCount, name: '高' },
      { value: stats.value.mediumCount, name: '中' },
      { value: stats.value.lowCount, name: '低' }
    ]
  }]
}))

function statusType(s) {
  return s === '已完成' ? 'success' : s === '进行中' ? 'warning' : 'info'
}
function priorityType(p) {
  return p === '高' ? 'danger' : p === '中' ? 'warning' : 'info'
}
function deadlineInfo(dateStr) {
  if (!dateStr) return { text: '', type: '' }
  const now = new Date(); now.setHours(0, 0, 0, 0)
  const d = new Date(dateStr); d.setHours(0, 0, 0, 0)
  const diff = Math.ceil((d - now) / (1000 * 60 * 60 * 24))
  if (diff < 0) return { text: `已逾期 ${-diff} 天`, type: 'danger' }
  if (diff === 0) return { text: '今日截止', type: 'danger' }
  if (diff <= 3) return { text: `剩余 ${diff} 天`, type: 'warning' }
  return { text: `剩余 ${diff} 天`, type: 'info' }
}

onMounted(async () => {
  try {
    const role = localStorage.getItem('role')
    const userId = localStorage.getItem('userId')
    const params = {}
    if (role === '导师' && userId) {
      params.creatorId = userId
    }
    const [statRes, taskRes] = await Promise.all([
      request.get('/tasks/statistics'),
      request.get('/tasks', { params })
    ])
    if (statRes.data) stats.value = statRes.data

    const priorityWeight = { '高': 1, '中': 2, '低': 3 }
    if (taskRes.data) {
      todoList.value = (taskRes.data.records || taskRes.data)
        .filter(t => t.status === '待办' || t.status === '进行中')
        .sort((a, b) => (priorityWeight[a.priority] || 9) - (priorityWeight[b.priority] || 9))
    }
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.stat-card { text-align: center; }
.stat-value { font-size: 32px; font-weight: 700; color: #409eff; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
</style>
