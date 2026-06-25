<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">维修处理</h1>
        <p class="muted">维修人员待接单、处理中和完成结果提交。</p>
      </div>
    </div>

    <el-alert
      title="本页面只展示分派给当前维修人员的工单，后端必须校验归属。"
      type="warning"
      show-icon
      :closable="false"
    />
  </section>
</template>
