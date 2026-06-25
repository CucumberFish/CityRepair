<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">审核与分派</h1>
        <p class="muted">管理员审核、驳回、设置优先级和分派维修人员。</p>
      </div>
    </div>

    <div class="panel table-panel">
      <el-table :data="[]" empty-text="暂无待处理工单">
        <el-table-column prop="orderNo" label="工单编号" min-width="140" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="priority" label="优先级" width="120" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column label="操作" width="180" fixed="right" />
      </el-table>
    </div>
  </section>
</template>

<style scoped>
.table-panel {
  padding: 12px;
}
</style>
