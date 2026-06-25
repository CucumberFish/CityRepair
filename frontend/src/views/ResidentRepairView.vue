<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">居民报修</h1>
        <p class="muted">我要报修、我的报修和报修详情由本模块继续完善。</p>
      </div>
      <el-button type="primary">新建报修</el-button>
    </div>

    <div class="panel table-panel">
      <el-table :data="[]" empty-text="暂无报修工单">
        <el-table-column prop="orderNo" label="工单编号" min-width="140" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
      </el-table>
    </div>
  </section>
</template>

<style scoped>
.table-panel {
  padding: 12px;
}
</style>
