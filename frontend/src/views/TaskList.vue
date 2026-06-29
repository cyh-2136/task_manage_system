<template>
  <div class="task-list">
    <el-card shadow="hover">
      <template #header>
        <div class="task-header">
          <span>任务列表</span>
          <div>
            <el-button :icon="Download" @click="exportExcel">导出Excel</el-button>
            <el-button v-if="isMentor" type="primary" :icon="Plus" @click="openCreate">新建任务</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" size="small" class="task-filters">
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width:120px" @change="onFilterChange">
            <el-option label="待办" value="待办" />
            <el-option label="进行中" value="进行中" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="filters.deadlineStart" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" style="width:140px" @change="onFilterChange" />
          <span style="margin:0 6px">至</span>
          <el-date-picker v-model="filters.deadlineEnd" type="date" placeholder="结束日期" value-format="YYYY-MM-DD" style="width:140px" @change="onFilterChange" />
        </el-form-item>
        <el-form-item v-if="isMentor" label="负责人">
          <el-input v-model="filters.assigneeId" placeholder="用户ID" clearable style="width:140px" @change="onFilterChange" />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="搜索标题" clearable style="width:160px" @change="onFilterChange" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onFilterChange">搜索</el-button>
          <el-button :icon="Refresh" @click="loadTasks">刷新</el-button>
        </el-form-item>
      </el-form>

      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
        <span style="font-size:13px;color:#909399">共 {{ tasks.length }} 条</span>
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button value="table"><el-icon><Grid /></el-icon> 表格</el-radio-button>
          <el-radio-button value="card"><el-icon><Grid /></el-icon> 卡片</el-radio-button>
        </el-radio-group>
      </div>

      <el-table v-if="viewMode === 'table'" :data="tasks" stripe style="width:100%" v-loading="loading">
        <el-table-column prop="title" label="任务标题" min-width="160" />
        <el-table-column label="进度" width="200">
          <template #default="{ row }">
            <div class="step-bar" :class="{ 'is-mentor': isMentor }">
              <div class="step-node" :class="{ done: true, active: row.status === '待办' }" @click="isMentor && setStatus(row, '待办')" title="待办">
                <span v-if="row.status !== '待办'" class="step-check">&#10003;</span>
                <span v-else class="step-num">1</span>
              </div>
              <div class="step-line" :class="{ done: row.status !== '待办' }"></div>
              <div class="step-node" :class="{ done: row.status === '进行中' || row.status === '已完成', active: row.status === '进行中' }" @click="isMentor && setStatus(row, '进行中')" title="进行中">
                <span v-if="row.status === '已完成'" class="step-check">&#10003;</span>
                <span v-else class="step-num">2</span>
              </div>
              <div class="step-line" :class="{ done: row.status === '已完成' }"></div>
              <div class="step-node" :class="{ done: row.status === '已完成', active: row.status === '已完成' }" @click="isMentor && setStatus(row, '已完成')" title="已完成">
                <span v-if="row.status === '已完成'" class="step-check">&#10003;</span>
                <span v-else class="step-num">3</span>
              </div>
              <div class="step-labels">
                <span :class="{ on: true }">待办</span>
                <span :class="{ on: row.status !== '待办' }">进行中</span>
                <span :class="{ on: row.status === '已完成' }">已完成</span>
              </div>
            </div>
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
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button v-if="isMentor" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="isMentor" size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-row v-if="viewMode === 'card'" :gutter="12">
        <el-col v-for="t in tasks" :key="t.id" :span="8" style="margin-bottom:12px">
          <el-card shadow="hover" class="task-card">
            <div class="card-title" @click="openDetail(t)">{{ t.title }}</div>
            <div class="card-meta">
              <el-tag :type="priorityType(t.priority)" size="small">{{ t.priority }}</el-tag>
            </div>
            <p class="card-desc">{{ t.description || '' }}</p>
            <div class="step-bar card-step" :class="{ 'is-mentor': isMentor }">
              <div class="step-node" :class="{ done: true, active: t.status === '待办' }" @click="isMentor && setStatus(t, '待办')" title="待办">
                <span v-if="t.status !== '待办'" class="step-check">&#10003;</span>
                <span v-else class="step-num">1</span>
              </div>
              <div class="step-line" :class="{ done: t.status !== '待办' }"></div>
              <div class="step-node" :class="{ done: t.status === '进行中' || t.status === '已完成', active: t.status === '进行中' }" @click="isMentor && setStatus(t, '进行中')" title="进行中">
                <span v-if="t.status === '已完成'" class="step-check">&#10003;</span>
                <span v-else class="step-num">2</span>
              </div>
              <div class="step-line" :class="{ done: t.status === '已完成' }"></div>
              <div class="step-node" :class="{ done: t.status === '已完成', active: t.status === '已完成' }" @click="isMentor && setStatus(t, '已完成')" title="已完成">
                <span v-if="t.status === '已完成'" class="step-check">&#10003;</span>
                <span v-else class="step-num">3</span>
              </div>
              <div class="step-labels">
                <span :class="{ on: true }">待办</span>
                <span :class="{ on: t.status !== '待办' }">进行中</span>
                <span :class="{ on: t.status === '已完成' }">已完成</span>
              </div>
            </div>
            <div class="card-footer">
              <el-tag v-if="t.deadline" :type="deadlineInfo(t.deadline).type" size="small">{{ deadlineInfo(t.deadline).text }}</el-tag>
              <span v-else style="color:#909399;font-size:12px">无截止日</span>
              <div>
                <el-button v-if="isMentor" size="small" @click="openEdit(t)">编辑</el-button>
                <el-button v-if="isMentor" size="small" type="danger" @click="handleDelete(t.id)">删除</el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <div style="display:flex;justify-content:center;margin-top:16px">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadTasks"
          @size-change="loadTasks"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="任务详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ detail.description || '' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detail.status)" size="small">{{ detail.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="priorityType(detail.priority)" size="small">{{ detail.priority }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="负责人ID">{{ detail.assigneeId || '未分配' }}</el-descriptions-item>
        <el-descriptions-item label="创建人ID">{{ detail.creatorId }}</el-descriptions-item>
        <el-descriptions-item label="截止日期">{{ detail.deadline || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createdTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detail.updatedTime }}</el-descriptions-item>
      </el-descriptions>
      <el-divider />
      <div class="related-news">
        <h4 style="margin:0 0 8px">相关资讯</h4>
        <div v-if="relatedNews.length === 0" style="color:#909399;font-size:13px">暂无相关资讯</div>
        <el-timeline v-else>
          <el-timeline-item v-for="item in relatedNews" :key="item.url" :timestamp="item.publishTime" placement="top">
            <a :href="item.url" target="_blank" class="news-link">{{ item.title }}</a>
            <p style="font-size:12px;color:#909399;margin:4px 0 0">{{ item.source }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑任务' : '新建任务'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority">
            <el-option label="高" value="高" />
            <el-option label="中" value="中" />
            <el-option label="低" value="低" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人ID">
          <el-input v-model="form.assigneeId" placeholder="输入实习生用户ID" />
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="form.deadline" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Grid, Download, Search } from '@element-plus/icons-vue'
import request from '../utils/request'

const tasks = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const loading = ref(false)
const saveLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const viewMode = ref('table')
const formRef = ref(null)
const detail = ref({})
const relatedNews = ref([])
const isMentor = localStorage.getItem('role') === '导师'

const filters = reactive({
  status: '',
  deadlineStart: '',
  deadlineEnd: '',
  keyword: '',
  assigneeId: ''
})

const form = reactive({
  title: '', description: '', priority: '中', assigneeId: '', deadline: ''
})

const rules = { title: [{ required: true, message: '请输入标题', trigger: 'blur' }] }

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

function resetForm() {
  form.title = ''; form.description = ''; form.priority = '中'
  form.assigneeId = ''; form.deadline = ''
}

function onFilterChange() {
  page.value = 1
  loadTasks()
}

function buildParams() {
  const p = {}
  if (filters.status) p.status = filters.status
  if (filters.deadlineStart) p.deadlineStart = filters.deadlineStart
  if (filters.deadlineEnd) p.deadlineEnd = filters.deadlineEnd
  if (filters.keyword) p.keyword = filters.keyword
  if (filters.assigneeId) p.assigneeId = filters.assigneeId
  p.page = page.value
  p.pageSize = pageSize.value
  return p
}

async function loadTasks() {
  loading.value = true
  try {
    const res = await request.get('/tasks', { params: buildParams() })
    tasks.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error('加载任务失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false; editId.value = null; resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true; editId.value = row.id
  form.title = row.title; form.description = row.description || ''
  form.priority = row.priority; form.assigneeId = row.assigneeId
  form.deadline = row.deadline || ''
  dialogVisible.value = true
}

async function openDetail(row) {
  try {
    const res = await request.get('/tasks/' + row.id)
    detail.value = res.data || row
    const keyword = detail.value.title || ''
    const newsRes = await request.get('/news/search-by-task', { params: { keyword } })
    relatedNews.value = newsRes.data || []
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('获取任务详情失败')
  }
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => {})
  if (!valid) return
  saveLoading.value = true
  try {
    if (isEdit.value) {
      await request.put('/tasks/' + editId.value, form)
      ElMessage.success('更新成功')
    } else {
      await request.post('/tasks', form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadTasks()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    saveLoading.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确认删除该任务？')
    await request.delete('/tasks/' + id)
    ElMessage.success('删除成功')
    await loadTasks()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

async function handleStatus(id, status) {
  try {
    await request.put(`/tasks/${id}/status?status=${encodeURIComponent(status)}`)
    ElMessage.success('状态更新成功')
    await loadTasks()
  } catch (e) {
    ElMessage.error(e.message || '状态更新失败')
  }
}

function setStatus(row, target) {
  if (row.status !== target) handleStatus(row.id, target)
}

function exportExcel() {
  const params = buildParams()
  const query = Object.entries(params).map(([k, v]) => `${k}=${encodeURIComponent(v)}`).join('&')
  window.open(`/api/tasks/export?${query}`, '_blank')
}

onMounted(() => {
  loadTasks()
})
</script>

<style scoped>
.task-header { display: flex; justify-content: space-between; align-items: center; }
.task-filters { margin-bottom: 8px; }
.task-card .card-title { font-weight: 600; margin-bottom: 8px; cursor: pointer; color: #409eff; }
.task-card .card-title:hover { text-decoration: underline; }
.card-meta { display: flex; gap: 6px; margin-bottom: 6px; }
.card-desc { font-size: 13px; color: #606266; margin: 6px 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-footer { display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #909399; }
.news-link { color: #409eff; text-decoration: none; font-size: 14px; }
.news-link:hover { text-decoration: underline; }
.step-bar { display: flex; align-items: center; padding: 2px 0; user-select: none; }
.step-bar .step-node {
  width: 24px; height: 24px; border-radius: 50%; flex-shrink: 0; cursor: default;
  display: flex; align-items: center; justify-content: center;
  background: #e4e7ed; color: #909399; border: 2px solid #dcdfe6;
  transition: all .2s; line-height: 1;
}
.step-bar .step-node .step-check { font-size: 13px; font-weight: 700; }
.step-bar .step-node .step-num { font-size: 11px; font-weight: 700; }
.step-bar .step-node.done { background: #409eff; color: #fff; border-color: #409eff; }
.step-bar .step-node.active { background: #fff; color: #409eff; border-color: #409eff; box-shadow: 0 0 0 3px rgba(64,158,255,.2); }
.step-bar.is-mentor .step-node { cursor: pointer; }
.step-bar.is-mentor .step-node:not(.done):hover { transform: scale(1.2); border-color: #409eff; color: #409eff; }
.step-bar .step-line { flex: 1; height: 2px; background: #e4e7ed; }
.step-bar .step-line.done { background: #409eff; }
.step-bar .step-labels { display: flex; flex-direction: column; align-items: flex-start; margin-left: 6px; gap: 1px; line-height: 1.2; }
.step-bar .step-labels span { font-size: 11px; color: #c0c4cc; }
.step-bar .step-labels span.on { color: #409eff; font-weight: 600; }
.card-step { padding: 4px 0; }
</style>
