import { http, type ApiResponse } from './http'

export interface HealthInfo {
  status: string
  service: string
}

export interface Overview {
  totalOrders: number
  pendingOrders: number
  completedOrders: number
  completionRate: number
}

export function getHealth() {
  return http.get<ApiResponse<HealthInfo>>('/health')
}

export function getOverview() {
  return http.get<ApiResponse<Overview>>('/statistics/overview')
}
