import { http } from './http'
import type { ApiResponse } from './http'

/**
 * 工单列表项
 */
export interface WorkerOrder {
  id: number
  orderNo: string
  title: string
  categoryId: number
  categoryName: string
  location: string
  priority: string
  status: string
  residentName: string
  residentPhone: string
  contactPhone: string
  createdAt: string
  updatedAt: string
}

/**
 * 工单详情
 */
export interface WorkerOrderDetail extends WorkerOrder {
  description: string
  completionResult: string
  assignedAt: string
  acceptedAt: string
  completedAt: string
  attachments: OrderAttachment[]
  statusLogs: StatusLog[]
}

/**
 * 附件
 */
export interface OrderAttachment {
  id: number
  attachmentType: string
  fileUrl: string
  originalName: string
  contentType: string
  fileSize: number
  createdAt: string
}

/**
 * 状态日志
 */
export interface StatusLog {
  id: number
  operatorId: number
  operatorName: string
  action: string
  fromStatus: string
  toStatus: string
  remark: string
  createdAt: string
}

/**
 * 分页响应
 */
export interface PageResponse<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
  pages: number
}

/**
 * 进度请求
 */
export interface ProgressRequest {
  content: string
}

/**
 * 完成请求
 */
export interface CompleteRequest {
  completionResult: string
}

/**
 * 查询分派给当前维修人员的工单列表
 */
export function getWorkerOrders(params?: {
  page?: number
  pageSize?: number
  keyword?: string
  status?: string
}) {
  return http.get<ApiResponse<PageResponse<WorkerOrder>>>('/worker/orders', { params })
}

/**
 * 查询工单详情
 */
export function getWorkerOrderDetail(id: number) {
  return http.get<ApiResponse<WorkerOrderDetail>>(`/worker/orders/${id}`)
}

/**
 * 接单
 */
export function acceptOrder(id: number) {
  return http.put<ApiResponse<void>>(`/worker/orders/${id}/accept`)
}

/**
 * 开始处理
 */
export function startOrder(id: number) {
  return http.put<ApiResponse<void>>(`/worker/orders/${id}/start`)
}

/**
 * 提交进度
 */
export function submitProgress(id: number, data: ProgressRequest) {
  return http.post<ApiResponse<void>>(`/worker/orders/${id}/progress`, data)
}

/**
 * 完成工单
 */
export function completeOrder(id: number, data: CompleteRequest) {
  return http.put<ApiResponse<void>>(`/worker/orders/${id}/complete`, data)
}

/**
 * 上传处理后图片
 */
export function uploadAttachment(id: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post<ApiResponse<OrderAttachment>>(`/worker/orders/${id}/attachments`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
