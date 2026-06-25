import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '@/views/DashboardView.vue'
import ResidentRepairView from '@/views/ResidentRepairView.vue'
import ResidentRepairDetail from '@/views/ResidentRepairDetail.vue'
import AdminOrdersView from '@/views/AdminOrdersView.vue'
import WorkerOrdersView from '@/views/WorkerOrdersView.vue'
import EvaluationView from '@/views/EvaluationView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/dashboard' },
    { path: '/dashboard', name: 'dashboard', component: DashboardView, meta: { title: '统计看板' } },
    { path: '/resident/repairs', name: 'resident-repairs', component: ResidentRepairView, meta: { title: '居民报修' } },
    { path: '/resident/repairs/:id', name: 'resident-repair-detail', component: ResidentRepairDetail, meta: { title: '报修详情' } },
    { path: '/admin/orders', name: 'admin-orders', component: AdminOrdersView, meta: { title: '审核与分派' } },
    { path: '/worker/orders', name: 'worker-orders', component: WorkerOrdersView, meta: { title: '维修处理' } },
    { path: '/evaluations', name: 'evaluations', component: EvaluationView, meta: { title: '评价与通知' } },
  ],
})

export default router
