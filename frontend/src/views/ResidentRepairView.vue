<template>
  <section class="page">
    <!-- 未登录 -->
    <template v-if="!auth.state.loaded || !auth.isLoggedIn()">
      <div v-if="!auth.state.loaded" class="loading-wrap">
        <el-skeleton :rows="3" animated />
      </div>
      <div v-else class="login-card panel">
        <h2 class="login-title">请先登录</h2>
        <p class="muted">使用演示账号登录后操作</p>
        <el-form :model="loginForm" class="login-form" @submit.prevent="handleLogin">
          <el-form-item>
            <el-input v-model="loginForm.username" placeholder="用户名 (resident1)" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="loginForm.password" type="password" placeholder="密码 (123456)" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" native-type="submit" :loading="logging" class="login-btn">登 录</el-button>
          </el-form-item>
        </el-form>
        <el-divider />
        <div class="demo-accounts">
          <p class="muted">演示账号：</p>
          <el-tag v-for="acc in demoAccounts" :key="acc.role" class="demo-tag"
            @click="fillLogin(acc.username, acc.password)">
            {{ acc.role }}: {{ acc.username }}/{{ acc.password }}
          </el-tag>
        </div>
      </div>
    </template>

    <!-- 已登录 -->
    <template v-else>
      <div class="page-header">
        <div>
          <h1 class="page-title">居民报修</h1>
          <p class="muted">{{ auth.state.user?.realName }}，欢迎使用</p>
        </div>
        <el-button type="primary" @click="showCreate = true">新建报修</el-button>
      </div>

      <!-- 筛选 -->
      <div class="panel filter-bar">
        <el-input v-model="listQuery.keyword" placeholder="搜索工单编号 / 标题" clearable style="width:240px"
          @clear="fetchList" @keyup.enter="fetchList" />
        <el-select v-model="listQuery.status" placeholder="全部状态" clearable style="width:140px" @change="fetchList">
          <el-option label="全部状态" value="" />
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-button @click="fetchList">查询</el-button>
      </div>

      <!-- 表格 -->
      <div class="panel table-panel">
        <el-table :data="list" v-loading="loading" empty-text="暂无报修工单" stripe>
          <el-table-column prop="orderNo" label="工单编号" width="170" />
          <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
          <el-table-column label="类别" width="100">
            <template #default="{ row }">{{ categoryMap[row.categoryId] || '-' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="优先级" width="80">
            <template #default="{ row }">
              <el-tag :type="priorityType(row.priority)" size="small">{{ row.priority }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="更新时间" width="170">
            <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="$router.push(`/resident/repairs/${row.id}`)">详情</el-button>
              <el-button v-if="row.status === 'PENDING_REVIEW'" link type="danger"
                @click="handleCancel(row)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination v-model:current-page="listQuery.page" :page-size="listQuery.pageSize"
            :total="total" layout="prev, pager, next, total" background small @current-change="fetchList" />
        </div>
      </div>
    </template>

    <!-- 新建报修对话框 -->
    <el-dialog v-model="showCreate" title="新建报修" width="580px" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="createForm" :rules="formRules" label-width="80px" @submit.prevent="handleCreate">
        <el-form-item label="报修类别" prop="categoryId">
          <el-select v-model="createForm.categoryId" placeholder="选择类别" style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="createForm.title" placeholder="如：小区门口道路坑洼" maxlength="100" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="createForm.location" placeholder="如：幸福小区南门" maxlength="200" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请详细描述问题情况" maxlength="500" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="createForm.contactPhone" placeholder="手机号码" maxlength="20" />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload :auto-upload="false" :file-list="uploadFiles" list-type="picture-card"
            :on-change="onFileChange" :on-remove="onFileRemove" accept="image/*" multiple>
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">提交报修</el-button>
      </template>
    </el-dialog>

    <!-- 取消确认 -->
    <el-dialog v-model="showCancel" title="取消报修" width="420px" destroy-on-close>
      <el-form :model="cancelForm">
        <el-form-item label="取消原因">
          <el-input v-model="cancelForm.reason" type="textarea" :rows="3" placeholder="请说明取消原因" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCancel = false">返回</el-button>
        <el-button type="danger" :loading="cancelling" @click="confirmCancel">确认取消</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { createOrder, getMyOrders, cancelOrder, getCategories, uploadAttachment, type CategoryVO } from '@/api/repair'
import { login } from '@/api/auth'
import type { OrderStatus } from '@/types/order'

const auth = useAuthStore()

/* ====== 登录 ====== */
const logging = ref(false)
const loginForm = reactive({ username: '', password: '' })
const demoAccounts = [
  { role: '居民', username: 'resident1', password: '123456' },
  { role: '管理员', username: 'admin', password: '123456' },
  { role: '维修工', username: 'worker1', password: '123456' },
]

function fillLogin(username: string, password: string) {
  loginForm.username = username
  loginForm.password = password
}

async function handleLogin() {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  logging.value = true
  try {
    await auth.doLogin(loginForm.username, loginForm.password)
    ElMessage.success('登录成功')
    fetchList()
    loadCategories()
  } catch { /* error handled by interceptor */ }
  finally { logging.value = false }
}

/* ====== 列表 ====== */
const loading = ref(false)
const list: any[] = reactive([])
const total = ref(0)
const listQuery = reactive({ page: 1, pageSize: 10, keyword: '', status: '' })

const statusOptions = [
  { value: 'PENDING_REVIEW', label: '待审核' },
  { value: 'PENDING_ASSIGN', label: '待分派' },
  { value: 'PENDING_ACCEPT', label: '待接单' },
  { value: 'PROCESSING', label: '处理中' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'EVALUATED', label: '已评价' },
  { value: 'REJECTED', label: '已驳回' },
  { value: 'CANCELLED', label: '已取消' },
]

function statusLabel(s: string) {
  const m: Record<string, string> = {
    PENDING_REVIEW: '待审核', PENDING_ASSIGN: '待分派', PENDING_ACCEPT: '待接单',
    PROCESSING: '处理中', COMPLETED: '已完成', EVALUATED: '已评价',
    REJECTED: '已驳回', CANCELLED: '已取消',
  }
  return m[s] || s
}

function statusType(s: string) {
  if (['PENDING_REVIEW', 'PENDING_ASSIGN', 'PENDING_ACCEPT'].includes(s)) return 'warning'
  if (s === 'PROCESSING') return 'primary'
  if (['COMPLETED', 'EVALUATED'].includes(s)) return 'success'
  return 'danger'
}

function priorityType(p: string) {
  if (p === 'LOW') return 'info'
  if (p === 'NORMAL') return ''
  if (p === 'HIGH') return 'warning'
  return 'danger'
}

function formatTime(t: string) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getMyOrders({ ...listQuery })
    const data = res.data.data
    list.length = 0
    list.push(...data.records)
    total.value = data.total
  } catch { list.length = 0; total.value = 0 }
  finally { loading.value = false }
}

/* ====== 类别 ====== */
const categories = ref<CategoryVO[]>([])
const categoryMap = computed(() => {
  const m: Record<number, string> = {}
  categories.value.forEach(c => { m[c.id] = c.categoryName })
  return m
})

async function loadCategories() {
  try {
    const res = await getCategories()
    categories.value = res.data.data
  } catch { /* ignore */ }
}

/* ====== 创建 ====== */
const showCreate = ref(false)
const creating = ref(false)
const formRef = ref<any>(null)
const uploadFiles = ref<any[]>([])

const createForm = reactive({
  categoryId: undefined as number | undefined,
  title: '',
  location: '',
  description: '',
  contactPhone: '',
})

const formRules = {
  categoryId: [{ required: true, message: '请选择报修类别', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
}

function onFileChange(uploadFile: any) {
  uploadFiles.value.push(uploadFile)
}

function onFileRemove() {
  // handled by el-upload
}

async function handleCreate() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  creating.value = true
  try {
    const res = await createOrder({
      categoryId: createForm.categoryId!,
      title: createForm.title,
      location: createForm.location,
      description: createForm.description,
      contactPhone: createForm.contactPhone,
    })
    const orderId = res.data.data.id

    // 上传图片
    if (uploadFiles.value.length > 0) {
      for (const f of uploadFiles.value) {
        try {
          await uploadAttachment(orderId, f.raw)
        } catch { /* partial upload ok */ }
      }
    }

    ElMessage.success('报修提交成功')
    showCreate.value = false
    resetForm()
    fetchList()
  } catch { /* error handled */ }
  finally { creating.value = false }
}

function resetForm() {
  createForm.categoryId = undefined
  createForm.title = ''
  createForm.location = ''
  createForm.description = ''
  createForm.contactPhone = ''
  uploadFiles.value = []
  formRef.value?.resetFields()
}

/* ====== 取消 ====== */
const showCancel = ref(false)
const cancelling = ref(false)
const cancelForm = reactive({ reason: '' })
const cancelTarget = ref<any>(null)

async function handleCancel(row: any) {
  cancelTarget.value = row
  cancelForm.reason = ''
  showCancel.value = true
}

async function confirmCancel() {
  if (!cancelForm.reason.trim()) {
    ElMessage.warning('请输入取消原因')
    return
  }
  cancelling.value = true
  try {
    await cancelOrder(cancelTarget.value.id, { reason: cancelForm.reason })
    ElMessage.success('已取消')
    showCancel.value = false
    fetchList()
  } catch { /* handled */ }
  finally { cancelling.value = false }
}

/* ====== 初始化 ====== */
onMounted(async () => {
  await auth.init()
  if (auth.isLoggedIn()) {
    fetchList()
    loadCategories()
  }
})
</script>

<style scoped>
.loading-wrap {
  padding: 40px;
}
.login-card {
  max-width: 420px;
  margin: 40px auto;
  padding: 32px;
}
.login-title {
  margin: 0 0 4px;
  color: var(--cr-deep-blue);
  font-size: 20px;
}
.login-form {
  margin-top: 20px;
}
.login-btn {
  width: 100%;
}
.demo-accounts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
.demo-tag {
  cursor: pointer;
}
.filter-bar {
  display: flex;
  gap: 12px;
  padding: 16px;
  align-items: center;
}
.table-panel {
  padding: 0;
}
.table-panel :deep(.el-table) {
  border-radius: var(--cr-radius-card);
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 16px;
}
</style>
