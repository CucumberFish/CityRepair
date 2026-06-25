const { chromium } = require('playwright');
const path = require('path');

// 截图保存目录
const SCREENSHOT_DIR = path.join(__dirname, '../screenshots');

(async () => {
  console.log('========================================');
  console.log('维修人员模块异常场景自动化测试');
  console.log('========================================\n');

  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    viewport: { width: 1440, height: 900 }
  });
  const page = await context.newPage();

  // 测试结果统计
  let passed = 0;
  let failed = 0;
  const results = [];

  try {
    // 测试1：打开页面并获取工单列表
    console.log('测试1：打开维修工单页面');
    await page.goto('http://localhost:5173/worker/orders', { waitUntil: 'networkidle' });
    await page.waitForTimeout(2000);

    // 截图：工单列表
    await page.screenshot({
      path: path.join(SCREENSHOT_DIR, '袁翊博-工单列表-正常.png'),
      fullPage: true
    });
    console.log('  ✅ 页面打开成功，已截图\n');
    passed++;
    results.push({ name: '打开页面', status: 'pass' });

    // 测试2：查找一个已完成的工单，尝试接单（非法状态）
    console.log('测试2：非法状态测试 - 对已完成工单执行接单');
    const completedRow = await page.$('tr:has-text("已完成")');
    if (completedRow) {
      // 点击查看详情
      const detailBtn = await completedRow.$('text=查看详情');
      if (detailBtn) {
        await detailBtn.click();
        await page.waitForTimeout(1500);

        // 截图：已完成工单详情
        await page.screenshot({
          path: path.join(SCREENSHOT_DIR, '袁翊博-已完成工单详情.png'),
          fullPage: true
        });

        // 检查是否有接单按钮（不应该有）
        const acceptBtn = await page.$('text=接单处理');
        if (!acceptBtn) {
          console.log('  ✅ 已完成工单没有接单按钮（符合预期）');
          passed++;
          results.push({ name: '非法状态-无接单按钮', status: 'pass' });
        } else {
          console.log('  ❌ 已完成工单显示了接单按钮');
          failed++;
          results.push({ name: '非法状态-无接单按钮', status: 'fail' });
        }

        // 返回列表
        await page.click('text=返回列表');
        await page.waitForTimeout(1000);
      }
    } else {
      console.log('  ⚠️ 没有找到已完成工单，跳过测试');
    }
    console.log('');

    // 测试3：通过API测试非法状态接单
    console.log('测试3：API测试 - 非法状态接单（对已完成工单）');
    const response1 = await page.evaluate(async () => {
      const res = await fetch('/api/worker/orders/4/accept', { method: 'PUT' });
      return await res.json();
    });
    if (response1.code === 400) {
      console.log('  ✅ 返回错误码400：', response1.message);
      passed++;
      results.push({ name: 'API-非法状态接单', status: 'pass' });
    } else {
      console.log('  ❌ 期望返回400，实际返回：', response1.code);
      failed++;
      results.push({ name: 'API-非法状态接单', status: 'fail' });
    }
    console.log('');

    // 测试4：API测试 - 重复完成工单
    console.log('测试4：API测试 - 重复完成工单');
    const response2 = await page.evaluate(async () => {
      const res = await fetch('/api/worker/orders/4/complete', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ completionResult: 'test' })
      });
      return await res.json();
    });
    if (response2.code === 400) {
      console.log('  ✅ 返回错误码400：', response2.message);
      passed++;
      results.push({ name: 'API-重复完成工单', status: 'pass' });
    } else {
      console.log('  ❌ 期望返回400，实际返回：', response2.code);
      failed++;
      results.push({ name: 'API-重复完成工单', status: 'fail' });
    }
    console.log('');

    // 测试5：API测试 - 缺少处理说明完成
    console.log('测试5：API测试 - 缺少处理说明完成');
    const response3 = await page.evaluate(async () => {
      const res = await fetch('/api/worker/orders/2/complete', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ completionResult: '' })
      });
      return await res.json();
    });
    if (response3.code === 400) {
      console.log('  ✅ 返回错误码400：', response3.message);
      passed++;
      results.push({ name: 'API-缺少处理说明', status: 'pass' });
    } else {
      console.log('  ❌ 期望返回400，实际返回：', response3.code);
      failed++;
      results.push({ name: 'API-缺少处理说明', status: 'fail' });
    }
    console.log('');

    // 测试6：API测试 - 处理他人工单（模拟）
    console.log('测试6：API测试 - 工单不存在');
    const response4 = await page.evaluate(async () => {
      const res = await fetch('/api/worker/orders/999');
      return await res.json();
    });
    if (response4.code === 400) {
      console.log('  ✅ 返回错误码400：', response4.message);
      passed++;
      results.push({ name: 'API-工单不存在', status: 'pass' });
    } else {
      console.log('  ❌ 期望返回400，实际返回：', response4.code);
      failed++;
      results.push({ name: 'API-工单不存在', status: 'fail' });
    }
    console.log('');

    // 测试7：查看待接单工单详情
    console.log('测试7：查看待接单工单详情');
    await page.goto('http://localhost:5173/worker/orders', { waitUntil: 'networkidle' });
    await page.waitForTimeout(1500);

    const pendingRow = await page.$('tr:has-text("待接单")');
    if (pendingRow) {
      const detailBtn = await pendingRow.$('text=查看详情');
      if (detailBtn) {
        await detailBtn.click();
        await page.waitForTimeout(1500);

        // 截图：待接单工单详情（有接单按钮）
        await page.screenshot({
          path: path.join(SCREENSHOT_DIR, '袁翊博-待接单工单详情.png'),
          fullPage: true
        });

        // 检查是否有接单按钮
        const acceptBtn = await page.$('text=接单处理');
        if (acceptBtn) {
          console.log('  ✅ 待接单工单显示接单按钮');
          passed++;
          results.push({ name: '待接单工单-有接单按钮', status: 'pass' });

          // 截图：点击接单前的确认对话框
          await acceptBtn.click();
          await page.waitForTimeout(500);
          await page.screenshot({
            path: path.join(SCREENSHOT_DIR, '袁翊博-接单确认对话框.png'),
            fullPage: true
          });

          // 取消接单
          const cancelBtn = await page.$('.el-message-box__btns button:has-text("取消")');
          if (cancelBtn) {
            await cancelBtn.click();
            await page.waitForTimeout(500);
          }
        } else {
          console.log('  ❌ 待接单工单没有接单按钮');
          failed++;
          results.push({ name: '待接单工单-有接单按钮', status: 'fail' });
        }

        // 返回列表
        await page.click('text=返回列表');
        await page.waitForTimeout(1000);
      }
    }
    console.log('');

    // 测试8：测试完成工单流程
    console.log('测试8：测试完整接单→完成流程');
    // 先接单
    const acceptResponse = await page.evaluate(async () => {
      const res = await fetch('/api/worker/orders/2/accept', { method: 'PUT' });
      return await res.json();
    });
    if (acceptResponse.code === 0) {
      console.log('  ✅ 接单成功');

      // 完成工单
      const completeResponse = await page.evaluate(async () => {
        const res = await fetch('/api/worker/orders/2/complete', {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ completionResult: 'Test completed successfully' })
        });
        return await res.json();
      });
      if (completeResponse.code === 0) {
        console.log('  ✅ 完成工单成功');
        passed++;
        results.push({ name: '完整流程-接单到完成', status: 'pass' });

        // 刷新页面查看状态
        await page.goto('http://localhost:5173/worker/orders', { waitUntil: 'networkidle' });
        await page.waitForTimeout(1500);

        // 截图：完成后的工单列表
        await page.screenshot({
          path: path.join(SCREENSHOT_DIR, '袁翊博-工单完成后列表.png'),
          fullPage: true
        });
      } else {
        console.log('  ❌ 完成工单失败：', completeResponse.message);
        failed++;
        results.push({ name: '完整流程-接单到完成', status: 'fail' });
      }
    } else {
      console.log('  ❌ 接单失败：', acceptResponse.message);
      failed++;
      results.push({ name: '完整流程-接单到完成', status: 'fail' });
    }
    console.log('');

  } catch (error) {
    console.error('测试过程中发生错误:', error.message);
    failed++;
    results.push({ name: '异常', status: 'fail', error: error.message });
  } finally {
    await browser.close();
  }

  // 输出测试结果
  console.log('========================================');
  console.log('测试结果汇总');
  console.log('========================================');
  results.forEach(r => {
    const icon = r.status === 'pass' ? '✅' : '❌';
    console.log(`${icon} ${r.name}${r.error ? ': ' + r.error : ''}`);
  });
  console.log('----------------------------------------');
  console.log(`通过: ${passed}`);
  console.log(`失败: ${failed}`);
  console.log(`总计: ${passed + failed}`);
  console.log('========================================');
  console.log(`截图保存在: ${SCREENSHOT_DIR}`);
  console.log('========================================');

  process.exit(failed > 0 ? 1 : 0);
})();
