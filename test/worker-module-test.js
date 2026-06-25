const { chromium } = require('playwright');

(async () => {
  console.log('========================================');
  console.log('维修人员模块自动化测试');
  console.log('========================================\n');

  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    viewport: { width: 1440, height: 900 }
  });
  const page = await context.newPage();

  // 测试结果统计
  let passed = 0;
  let failed = 0;

  try {
    // 测试1：打开前端页面
    console.log('测试1：打开前端页面');
    await page.goto('http://localhost:5174', { waitUntil: 'networkidle' });
    const title = await page.title();
    console.log('  页面标题:', title);
    if (title.includes('城市报修')) {
      console.log('  ✅ 通过\n');
      passed++;
    } else {
      console.log('  ❌ 失败\n');
      failed++;
    }

    // 测试2：点击维修工单菜单
    console.log('测试2：点击维修工单菜单');
    await page.click('text=维修工单');
    await page.waitForTimeout(1000);
    const pageHeader = await page.textContent('.page-title');
    console.log('  页面标题:', pageHeader);
    if (pageHeader && pageHeader.includes('维修工单')) {
      console.log('  ✅ 通过\n');
      passed++;
    } else {
      console.log('  ❌ 失败\n');
      failed++;
    }

    // 测试3：检查工单列表是否加载
    console.log('测试3：检查工单列表是否加载');
    await page.waitForSelector('.el-table', { timeout: 5000 });
    const rows = await page.$$('.el-table__row');
    console.log('  工单数量:', rows.length);
    if (rows.length > 0) {
      console.log('  ✅ 通过\n');
      passed++;
    } else {
      console.log('  ❌ 失败\n');
      failed++;
    }

    // 测试4：检查状态筛选功能
    console.log('测试4：检查状态筛选功能');
    const statusSelect = await page.$('.el-select');
    if (statusSelect) {
      console.log('  状态筛选组件存在');
      console.log('  ✅ 通过\n');
      passed++;
    } else {
      console.log('  ❌ 失败\n');
      failed++;
    }

    // 测试5：点击查看详情按钮
    console.log('测试5：点击查看详情按钮');
    const detailButton = await page.$('text=查看详情');
    if (detailButton) {
      await detailButton.click();
      await page.waitForTimeout(1000);
      const detailTitle = await page.textContent('.page-title');
      console.log('  详情页标题:', detailTitle);
      if (detailTitle && detailTitle.includes('工单处理')) {
        console.log('  ✅ 通过\n');
        passed++;
      } else {
        console.log('  ❌ 失败\n');
        failed++;
      }
    } else {
      console.log('  没有查看详情按钮\n');
      failed++;
    }

    // 测试6：检查详情页内容
    console.log('测试6：检查详情页内容');
    const infoPanel = await page.$('.info-panel');
    if (infoPanel) {
      console.log('  信息面板存在');
      console.log('  ✅ 通过\n');
      passed++;
    } else {
      console.log('  ❌ 失败\n');
      failed++;
    }

    // 测试7：检查返回按钮
    console.log('测试7：检查返回按钮');
    const backButton = await page.$('text=返回列表');
    if (backButton) {
      console.log('  返回按钮存在');
      console.log('  ✅ 通过\n');
      passed++;
    } else {
      console.log('  ❌ 失败\n');
      failed++;
    }

    // 测试8：返回列表页
    console.log('测试8：返回列表页');
    if (backButton) {
      await backButton.click();
      await page.waitForTimeout(1000);
      const listTitle = await page.textContent('.page-title');
      console.log('  列表页标题:', listTitle);
      if (listTitle && listTitle.includes('维修工单')) {
        console.log('  ✅ 通过\n');
        passed++;
      } else {
        console.log('  ❌ 失败\n');
        failed++;
      }
    }

    // 测试9：检查分页组件
    console.log('测试9：检查分页组件');
    const pagination = await page.$('.el-pagination');
    if (pagination) {
      console.log('  分页组件存在');
      console.log('  ✅ 通过\n');
      passed++;
    } else {
      console.log('  ❌ 失败\n');
      failed++;
    }

    // 测试10：检查刷新按钮
    console.log('测试10：检查刷新按钮');
    const refreshButton = await page.$('text=刷新');
    if (refreshButton) {
      console.log('  刷新按钮存在');
      console.log('  ✅ 通过\n');
      passed++;
    } else {
      console.log('  ❌ 失败\n');
      failed++;
    }

  } catch (error) {
    console.error('测试过程中发生错误:', error.message);
    failed++;
  } finally {
    await browser.close();
  }

  // 输出测试结果
  console.log('========================================');
  console.log('测试结果汇总');
  console.log('========================================');
  console.log(`通过: ${passed}`);
  console.log(`失败: ${failed}`);
  console.log(`总计: ${passed + failed}`);
  console.log(`通过率: ${((passed / (passed + failed)) * 100).toFixed(1)}%`);
  console.log('========================================');

  process.exit(failed > 0 ? 1 : 0);
})();
