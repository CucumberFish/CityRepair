<template>
  <section class="page">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="page-header-left">
        <el-button text class="back-btn" @click="goBack">
          <el-icon :size="20"><ArrowLeft /></el-icon>
          返回列表
        </el-button>
        <div class="title-divider"></div>
        <div>
          <h1 class="page-title">工单处理</h1>
          <p class="page-subtitle" v-if="orderDetail">{{ orderDetail.orderNo }} - {{ orderDetail.title }}</p>
        </div>
      </div>
      <div class="header-status" v-if="orderDetail">
        <el-tag :type="getStatusType(orderDetail.status)" size="large" effect="dark">
          {{ getStatusLabel(orderDetail.status) }}
        </el-tag>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-loading="loading" class="detail-container">
      <template v-if="orderDetail">
        <!-- 基础信息 -->
        <div class="panel info-panel">
          <div class="panel-header">
            <div class="panel-title">
              <el-icon><Document /></el-icon>
              <span>基础信息</span>
            </div>
          </div>
          <div class="panel-body">
            <el-descriptions :column="3" border>
              <el-descriptions-item label="工单编号">
                <span class="order-no">{{ orderDetail.orderNo }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="优先级">
                <el-tag :type="getPriorityType(orderDetail.priority)" size="small" effect="dark" round>
                  {{ getPriorityLabel(orderDetail.priority) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="报修类别">
                <el-tag size="small" effect="plain">{{ orderDetail.categoryName }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="故障地点" :span="2">
                <el-icon class="inline-icon"><Location /></el-icon>
                {{ orderDetail.location }}
              </el-descriptions-item>
              <el-descriptions-item label="联系电话">
                <el-icon class="inline-icon"><Phone /></el-icon>
                {{ orderDetail.contactPhone }}
              </el-descriptions-item>
              <el-descriptions-item label="创建时间" :span="3">
                <el-icon class="inline-icon"><Clock /></el-icon>
                {{ formatTime(orderDetail.createdAt) }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <!-- 居民信息 -->
        <div class="panel info-panel">
          <div class="panel-header">
            <div class="panel-title">
              <el-icon><User /></el-icon>
              <span>居民信息</span>
            </div>
          </div>
          <div class="panel-body">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="居民姓名">
                {{ orderDetail.residentName }}
              </el-descriptions-item>
              <el-descriptions-item label="联系电话">
                {{ orderDetail.residentPhone }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <!-- 问题描述 -->
        <div class="panel info-panel">
          <div class="panel-header">
            <div class="panel-title">
              <el-icon><EditPen /></el-icon>
              <span>问题描述</span>
            </div>
          </div>
          <div class="panel-body">
            <div class="description-content">{{ orderDetail.description }}</div>
          </div>
        </div>

        <!-- 处理时间线 -->
        <div class="panel info-panel">
          <div class="panel-header">
            <div class="panel-title">
              <el-icon><Timer /></el-icon>
              <span>处理时间线</span>
            </div>
          </div>
          <div class="panel-body">
            <div v-if="orderDetail.statusLogs && orderDetail.statusLogs.length > 0" class="timeline-wrapper">
              <el-timeline>
                <el-timeline-item
                  v-for="(log, index) in orderDetail.statusLogs"
                  :key="log.id"
                  :timestamp="formatTime(log.createdAt)"
                  placement="top"
                  :type="index === 0 ? 'primary' : ''"
                  :hollow="index !== 0"
                >
                  <div class="timeline-content">
                    <div class="timeline-header">
                      <span class="operator-name">{{ log.operatorName }}</span>
                      <el-tag size="small" :type="getActionType(log.action)" effect="plain">
                        {{ getActionLabel(log.action) }}
                      </el-tag>
                    </div>
                    <div class="timeline-status" v-if="log.fromStatus && log.toStatus && log.fromStatus !== log.toStatus">
                      {{ getStatusLabel(log.fromStatus) }} → {{ getStatusLabel(log.toStatus) }}
                    </div>
                    <div class="timeline-remark" v-if="log.remark">
                      <el-icon><ChatDotRound /></el-icon>
                      {{ log.remark }}
                    </div>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>
            <el-empty v-else description="暂无处理记录" :image-size="80" />
          </div>
        </div>

        <!-- 处理后图片 -->
        <div class="panel info-panel">
          <div class="panel-header">
            <div class="panel-title">
              <el-icon><Picture /></el-icon>
              <span>处理后图片</span>
            </div>
            <el-button
              v-if="canUpload"
              type="primary"
              size="small"
              @click="showUploadDialog = true"
            >
              <el-icon><Upload /></el-icon>
              上传图片
            </el-button>
          </div>
          <div class="panel-body">
            <div class="image-list" v-if="afterAttachments.length > 0">
              <div
                v-for="attachment in afterAttachments"
                :key="attachment.id"
                class="image-item"
              >
                <el-image
                  :src="`/api/uploads/${attachment.fileUrl}`"
                  :preview-src-list="[`/api/uploads/${attachment.fileUrl}`]"
                  fit="cover"
                  class="preview-image"
                />
                <div class="image-info">
                  <span class="image-name">{{ attachment.originalName }}</span>
                  <span class="image-size">{{ formatFileSize(attachment.fileSize) }}</span>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无图片" :image-size="80" />
          </div>
        </div>

        <!-- 完成结果 -->
        <div class="panel info-panel" v-if="orderDetail.completionResult">
          <div class="panel-header completed-header">
            <div class="panel-title">
              <el-icon><CircleCheck /></el-icon>
              <span>完成结果</span>
            </div>
            <el-tag type="success" effect="dark">已完成</el-tag>
          </div>
          <div class="panel-body">
            <div class="completion-result">
              <el-icon class="result-icon"><SuccessFilled /></el-icon>
              {{ orderDetail.completionResult }}
            </div>
          </div>
        </div>

        <!-- 操作区 -->
        <div class="panel action-panel" v-if="showActions">
          <div class="panel-header">
            <div class="panel-title">
              <el-icon><Operation /></el-icon>
              <span>操作</span>
            </div>
          </div>
          <div class="panel-body">
            <div class="action-buttons">
              <!-- 待接单状态：接单按钮 -->
              <template v-if="orderDetail.status === 'PENDING_ACCEPT'">
                <el-button
                  type="primary"
                  size="large"
                  class="action-btn-main"
                  @click="handleAccept"
                  :loading="actionLoading"
                >
                  <el-icon><Check /></el-icon>
                  接单处理
                </el-button>
              </template>

              <!-- 处理中状态：开始处理、提交进度、完成按钮 -->
              <template v-if="orderDetail.status === 'PROCESSING'">
                <el-button
                  type="success"
                  size="large"
                  class="action-btn-main"
                  @click="handleStart"
                  :loading="actionLoading"
                >
                  <el-icon><VideoPlay /></el-icon>
                  开始处理
                </el-button>
                <el-button
                  type="primary"
                  size="large"
                  class="action-btn"
                  @click="showProgressDialog = true"
                >
                  <el-icon><EditPen /></el-icon>
                  提交进度
                </el-button>
                <el-button
                  type="warning"
                  size="large"
                  class="action-btn"
                  @click="showCompleteDialog = true"
                >
                  <el-icon><CircleCheck /></el-icon>
                  完成工单
                </el-button>
              </template>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 提交进度对话框 -->
    <el-dialog
      v-model="showProgressDialog"
      title="提交进度"
      width="520px"
      :close-on-click-modal="false"
      class="custom-dialog"
    >
      <div class="dialog-content">
        <p class="dialog-hint">请描述当前处理进度</p>
        <el-input
          v-model="progressForm.content"
          type="textarea"
          :rows="4"
          placeholder="例如：已到达现场，正在检查损坏情况..."
          maxlength="500"
          show-word-limit
        />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showProgressDialog = false">取消</el-button>
          <el-button
            type="primary"
            @click="handleProgress"
            :loading="actionLoading"
          >
            <el-icon><Check /></el-icon>
            提交
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 完成工单对话框 -->
    <el-dialog
      v-model="showCompleteDialog"
      title="完成工单"
      width="520px"
      :close-on-click-modal="false"
      class="custom-dialog"
    >
      <div class="dialog-content">
        <el-alert
          title="完成后工单状态将变为已完成，无法再修改"
          type="warning"
          show-icon
          :closable="false"
          class="dialog-alert"
        />
        <p class="dialog-hint">请描述处理结果</p>
        <el-input
          v-model="completeForm.completionResult"
          type="textarea"
          :rows="4"
          placeholder="例如：已完成井盖更换，道路恢复正常通行..."
          maxlength="500"
          show-word-limit
        />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCompleteDialog = false">取消</el-button>
          <el-button
            type="primary"
            @click="handleComplete"
            :loading="actionLoading"
          >
            <el-icon><CircleCheck /></el-icon>
            确认完成
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 上传图片对话框 -->
    <el-dialog
      v-model="showUploadDialog"
      title="上传处理后图片"
      width="440px"
      :close-on-click-modal="false"
      class="custom-dialog"
    >
      <div class="dialog-content">
        <el-upload
          ref="uploadRef"
          drag
          :auto-upload="false"
          :limit="1"
          accept="image/*"
          :on-change="handleFileChange"
          class="upload-area"
        >
          <el-icon class="upload-icon"><UploadFilled /></el-icon>
          <div class="upload-text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="upload-tip">
              支持 JPG、PNG 格式，文件大小不超过 10MB
            </div>
          </template>
        </el-upload>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showUploadDialog = false">取消</el-button>
          <el-button
            type="primary"
            @click="handleUpload"
            :loading="actionLoading"
            :disabled="!uploadFile"
          >
            <el-icon><Upload /></el-icon>
            上传
          </el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  ArrowLeft, Document, User, EditPen, Timer, Picture, Upload,
  CircleCheck, SuccessFilled, Operation, Check, VideoPlay,
  Location, Phone, Clock, ChatDotRound, UploadFilled
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getWorkerOrderDetail,
  acceptOrder,
  startOrder,
  submitProgress,
  completeOrder,
  uploadAttachment,
  type WorkerOrderDetail
} from '../api/workerApi'

const router = useRouter()
const route = useRoute()

// 加载状态
const loading = ref(false)
const actionLoading = ref(false)

// 工单详情
const orderDetail = ref<WorkerOrderDetail | null>(null)

// 对话框状态
const showProgressDialog = ref(false)
const showCompleteDialog = ref(false)
const showUploadDialog = ref(false)

// 表单数据
const progressForm = reactive({
  content: ''
})

const completeForm = reactive({
  completionResult: ''
})

const uploadFile = ref<File | null>(null)

// 计算属性：是否显示操作按钮
const showActions = computed(() => {
  if (!orderDetail.value) return false
  const status = orderDetail.value.status
  return status === 'PENDING_ACCEPT' || status === 'PROCESSING'
})

// 计算属性：是否可以上传图片
const canUpload = computed(() => {
  if (!orderDetail.value) return false
  const status = orderDetail.value.status
  return status === 'PROCESSING' || status === 'COMPLETED'
})

// 计算属性：处理后图片
const afterAttachments = computed(() => {
  if (!orderDetail.value || !orderDetail.value.attachments) return []
  return orderDetail.value.attachments.filter(a => a.attachmentType === 'AFTER_REPAIR')
})

// 加载工单详情
const loadDetail = async () => {
  const id = Number(route.params.id)
  if (!id) {
    ElMessage.error('无效的工单ID')
    return
  }

  loading.value = true
  try {
    const response = await getWorkerOrderDetail(id)
    if (response.data.code === 0) {
      orderDetail.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '加载失败')
    }
  } catch (error) {
    console.error('加载工单详情失败:', error)
    ElMessage.error('加载工单详情失败')
  } finally {
    loading.value = false
  }
}

// 返回列表
const goBack = () => {
  router.push('/worker/orders')
}

// 接单
const handleAccept = async () => {
  try {
    await ElMessageBox.confirm('确认接单？接单后将开始处理该工单', '确认接单', {
      confirmButtonText: '确认接单',
      cancelButtonText: '取消',
      type: 'info',
      icon: 'QuestionFilled'
    })
  } catch {
    return
  }

  actionLoading.value = true
  try {
    const response = await acceptOrder(orderDetail.value!.id)
    if (response.data.code === 0) {
      ElMessage.success('接单成功')
      loadDetail()
    } else {
      ElMessage.error(response.data.message || '接单失败')
    }
  } catch (error) {
    console.error('接单失败:', error)
    ElMessage.error('接单失败')
  } finally {
    actionLoading.value = false
  }
}

// 开始处理
const handleStart = async () => {
  try {
    await ElMessageBox.confirm('确认开始处理该工单？', '确认开始', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
  } catch {
    return
  }

  actionLoading.value = true
  try {
    const response = await startOrder(orderDetail.value!.id)
    if (response.data.code === 0) {
      ElMessage.success('已开始处理')
      loadDetail()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    console.error('开始处理失败:', error)
    ElMessage.error('操作失败')
  } finally {
    actionLoading.value = false
  }
}

// 提交进度
const handleProgress = async () => {
  if (!progressForm.content.trim()) {
    ElMessage.warning('请输入进度说明')
    return
  }

  actionLoading.value = true
  try {
    const response = await submitProgress(orderDetail.value!.id, {
      content: progressForm.content
    })
    if (response.data.code === 0) {
      ElMessage.success('进度已提交')
      showProgressDialog.value = false
      progressForm.content = ''
      loadDetail()
    } else {
      ElMessage.error(response.data.message || '提交失败')
    }
  } catch (error) {
    console.error('提交进度失败:', error)
    ElMessage.error('提交失败')
  } finally {
    actionLoading.value = false
  }
}

// 完成工单
const handleComplete = async () => {
  if (!completeForm.completionResult.trim()) {
    ElMessage.warning('请输入完成结果')
    return
  }

  try {
    await ElMessageBox.confirm('确认完成工单？完成后无法修改', '确认完成', {
      confirmButtonText: '确认完成',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  actionLoading.value = true
  try {
    const response = await completeOrder(orderDetail.value!.id, {
      completionResult: completeForm.completionResult
    })
    if (response.data.code === 0) {
      ElMessage.success('工单已完成')
      showCompleteDialog.value = false
      completeForm.completionResult = ''
      loadDetail()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    console.error('完成工单失败:', error)
    ElMessage.error('操作失败')
  } finally {
    actionLoading.value = false
  }
}

// 文件选择
const handleFileChange = (file: any) => {
  uploadFile.value = file.raw
}

// 上传图片
const handleUpload = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  actionLoading.value = true
  try {
    const response = await uploadAttachment(orderDetail.value!.id, uploadFile.value)
    if (response.data.code === 0) {
      ElMessage.success('上传成功')
      showUploadDialog.value = false
      uploadFile.value = null
      loadDetail()
    } else {
      ElMessage.error(response.data.message || '上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败')
  } finally {
    actionLoading.value = false
  }
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化文件大小
const formatFileSize = (bytes: number) => {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let size = bytes
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return `${size.toFixed(1)} ${units[unitIndex]}`
}

// 获取状态标签
const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    'PENDING_REVIEW': '待审核',
    'PENDING_ASSIGN': '待分派',
    'PENDING_ACCEPT': '待接单',
    'PROCESSING': '处理中',
    'COMPLETED': '已完成',
    'EVALUATED': '已评价',
    'REJECTED': '已驳回',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

// 获取状态类型
const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    'PENDING_REVIEW': 'warning',
    'PENDING_ASSIGN': 'warning',
    'PENDING_ACCEPT': 'primary',
    'PROCESSING': 'primary',
    'COMPLETED': 'success',
    'EVALUATED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'info'
  }
  return map[status] || 'info'
}

// 获取优先级标签
const getPriorityLabel = (priority: string) => {
  const map: Record<string, string> = {
    'LOW': '低',
    'NORMAL': '普通',
    'HIGH': '高',
    'URGENT': '紧急'
  }
  return map[priority] || priority
}

// 获取优先级类型
const getPriorityType = (priority: string) => {
  const map: Record<string, string> = {
    'LOW': 'info',
    'NORMAL': 'primary',
    'HIGH': 'warning',
    'URGENT': 'danger'
  }
  return map[priority] || 'info'
}

// 获取操作标签
const getActionLabel = (action: string) => {
  const map: Record<string, string> = {
    'CREATE': '创建工单',
    'APPROVE': '审核通过',
    'REJECT': '驳回',
    'ASSIGN': '分派',
    'ACCEPT': '接单',
    'START': '开始处理',
    'PROGRESS': '提交进度',
    'COMPLETE': '完成',
    'EVALUATE': '评价',
    'CANCEL': '取消'
  }
  return map[action] || action
}

// 获取操作类型
const getActionType = (action: string) => {
  const map: Record<string, string> = {
    'CREATE': 'info',
    'APPROVE': 'success',
    'REJECT': 'danger',
    'ASSIGN': 'primary',
    'ACCEPT': 'primary',
    'START': 'primary',
    'PROGRESS': 'warning',
    'COMPLETE': 'success',
    'EVALUATE': 'success',
    'CANCEL': 'info'
  }
  return map[action] || 'info'
}

// 页面加载
onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
/* 页面标题区 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: linear-gradient(135deg, var(--cr-primary) 0%, var(--cr-deep-blue) 100%);
  border-radius: var(--cr-radius-card);
  box-shadow: var(--cr-shadow);
}

.page-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  color: #fff;
  font-size: 14px;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.title-divider {
  width: 1px;
  height: 36px;
  background: rgba(255, 255, 255, 0.3);
}

.page-title {
  margin: 0;
  color: #fff;
  font-size: 22px;
  font-weight: 700;
}

.page-subtitle {
  margin: 2px 0 0;
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
}

.header-status :deep(.el-tag) {
  font-size: 14px;
  padding: 8px 16px;
}

/* 详情容器 */
.detail-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 面板 */
.panel {
  background: var(--cr-surface);
  border-radius: var(--cr-radius-card);
  box-shadow: var(--cr-shadow);
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: #f8fafc;
  border-bottom: 1px solid var(--cr-border);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--cr-text);
}

.panel-body {
  padding: 20px;
}

.completed-header {
  background: rgba(46, 125, 91, 0.05);
}

/* 基础信息 */
.order-no {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: var(--cr-primary);
}

.inline-icon {
  margin-right: 6px;
  color: var(--cr-muted);
  vertical-align: middle;
}

/* 问题描述 */
.description-content {
  color: var(--cr-text);
  line-height: 1.8;
  white-space: pre-wrap;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 6px;
  border: 1px solid var(--cr-border);
}

/* 时间线 */
.timeline-wrapper {
  padding: 8px 0;
}

.timeline-content {
  padding: 4px 0;
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.operator-name {
  font-weight: 600;
  color: var(--cr-primary);
}

.timeline-status {
  color: var(--cr-muted);
  font-size: 13px;
  margin-bottom: 6px;
}

.timeline-remark {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  color: var(--cr-text);
  background: #f8fafc;
  padding: 10px 14px;
  border-radius: 6px;
  border: 1px solid var(--cr-border);
  font-size: 14px;
  line-height: 1.6;
}

.timeline-remark .el-icon {
  margin-top: 3px;
  color: var(--cr-muted);
  flex-shrink: 0;
}

/* 图片列表 */
.image-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}

.image-item {
  border: 1px solid var(--cr-border);
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.2s;
}

.image-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.preview-image {
  width: 100%;
  height: 140px;
}

.image-info {
  padding: 10px 12px;
  background: #f8fafc;
  border-top: 1px solid var(--cr-border);
}

.image-name {
  display: block;
  font-size: 12px;
  color: var(--cr-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.image-size {
  font-size: 11px;
  color: var(--cr-muted);
}

/* 完成结果 */
.completion-result {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 16px;
  background: rgba(46, 125, 91, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(46, 125, 91, 0.2);
  color: var(--cr-text);
  line-height: 1.8;
}

.result-icon {
  font-size: 20px;
  color: var(--cr-success);
  flex-shrink: 0;
  margin-top: 2px;
}

/* 操作区 */
.action-panel {
  background: var(--cr-surface);
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.action-btn-main {
  min-width: 140px;
  font-weight: 600;
}

.action-btn {
  min-width: 120px;
}

/* 对话框 */
.dialog-content {
  padding: 8px 0;
}

.dialog-hint {
  margin: 0 0 12px;
  color: var(--cr-muted);
  font-size: 14px;
}

.dialog-alert {
  margin-bottom: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 上传区域 */
.upload-area {
  width: 100%;
}

.upload-area :deep(.el-upload-dragger) {
  padding: 30px 20px;
}

.upload-icon {
  font-size: 48px;
  color: var(--cr-primary);
  margin-bottom: 12px;
}

.upload-text {
  color: var(--cr-text);
  font-size: 14px;
}

.upload-text em {
  color: var(--cr-primary);
  font-style: normal;
  font-weight: 500;
}

.upload-tip {
  margin-top: 10px;
  color: var(--cr-muted);
  font-size: 12px;
  text-align: center;
}
</style>
