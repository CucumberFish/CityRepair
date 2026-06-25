#!/bin/bash
# 维修人员模块API完整测试脚本

echo "========================================"
echo "维修人员模块API完整测试"
echo "========================================"
echo ""

BASE_URL="http://localhost:8080/api"
PASSED=0
FAILED=0

# 测试函数
test_api() {
    local name="$1"
    local method="$2"
    local url="$3"
    local data="$4"
    local expected_code="$5"

    echo "测试: $name"

    if [ "$method" = "GET" ]; then
        response=$(curl -s "$BASE_URL$url" 2>&1)
    elif [ "$method" = "PUT" ]; then
        if [ -n "$data" ]; then
            response=$(curl -s -X PUT "$BASE_URL$url" -H "Content-Type: application/json; charset=utf-8" -d "$data" 2>&1)
        else
            response=$(curl -s -X PUT "$BASE_URL$url" 2>&1)
        fi
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -X POST "$BASE_URL$url" -H "Content-Type: application/json; charset=utf-8" -d "$data" 2>&1)
    fi

    # 提取code值
    code=$(echo "$response" | python -c "import sys,json; print(json.load(sys.stdin)['code'])" 2>/dev/null)

    if [ "$code" = "$expected_code" ]; then
        echo "  ✅ 通过 (code=$code)"
        PASSED=$((PASSED + 1))
    else
        echo "  ❌ 失败 (期望code=$expected_code, 实际code=$code)"
        echo "  响应: $response"
        FAILED=$((FAILED + 1))
    fi
    echo ""
}

# 1. 健康检查
test_api "健康检查" "GET" "/health" "" "0"

# 2. 查询工单列表
test_api "查询工单列表" "GET" "/worker/orders?page=1&pageSize=10" "" "0"

# 3. 状态筛选 - 待接单
test_api "状态筛选-待接单" "GET" "/worker/orders?status=PENDING_ACCEPT" "" "0"

# 4. 状态筛选 - 处理中
test_api "状态筛选-处理中" "GET" "/worker/orders?status=PROCESSING" "" "0"

# 5. 状态筛选 - 已完成
test_api "状态筛选-已完成" "GET" "/worker/orders?status=COMPLETED" "" "0"

# 6. 关键词搜索（使用URL编码的中文）
test_api "关键词搜索" "GET" "/worker/orders?keyword=%E6%B0%B4%E7%AE%A1" "" "0"

# 7. 查询工单详情
test_api "查询工单详情" "GET" "/worker/orders/3" "" "0"

# 8. 测试异常 - 不存在的工单
test_api "异常-工单不存在" "GET" "/worker/orders/999" "" "400"

# 9. 测试异常 - 非法状态接单（对已完成工单）
test_api "异常-非法状态接单" "PUT" "/worker/orders/2/accept" "" "400"

# 10. 测试异常 - 缺少处理说明
test_api "异常-缺少处理说明" "PUT" "/worker/orders/3/complete" '{"completionResult": ""}' "400"

# 11. 查询一个待接单的工单
echo "查找待接单工单..."
PENDING_ORDER=$(curl -s "$BASE_URL/worker/orders?status=PENDING_ACCEPT" 2>&1 | python -c "
import sys,json
d=json.load(sys.stdin)
if d['data']['records']:
    print(d['data']['records'][0]['id'])
else:
    print('')
" 2>/dev/null)

if [ -n "$PENDING_ORDER" ]; then
    echo "  找到待接单工单: $PENDING_ORDER"
    echo ""

    # 12. 接单测试
    test_api "接单" "PUT" "/worker/orders/$PENDING_ORDER/accept" "" "0"

    # 13. 验证接单后状态
    echo "验证接单后状态:"
    STATUS=$(curl -s "$BASE_URL/worker/orders/$PENDING_ORDER" 2>&1 | python -c "import sys,json; print(json.load(sys.stdin)['data']['status'])" 2>/dev/null)
    if [ "$STATUS" = "PROCESSING" ]; then
        echo "  ✅ 状态已变为PROCESSING"
        PASSED=$((PASSED + 1))
    else
        echo "  ❌ 状态未正确更新: $STATUS"
        FAILED=$((FAILED + 1))
    fi
    echo ""

    # 14. 提交进度测试
    test_api "提交进度" "POST" "/worker/orders/$PENDING_ORDER/progress" '{"content": "Progress update test"}' "0"

    # 15. 完成工单测试
    test_api "完成工单" "PUT" "/worker/orders/$PENDING_ORDER/complete" '{"completionResult": "Completed successfully"}' "0"

    # 16. 验证完成后状态
    echo "验证完成后状态:"
    STATUS=$(curl -s "$BASE_URL/worker/orders/$PENDING_ORDER" 2>&1 | python -c "import sys,json; print(json.load(sys.stdin)['data']['status'])" 2>/dev/null)
    if [ "$STATUS" = "COMPLETED" ]; then
        echo "  ✅ 状态已变为COMPLETED"
        PASSED=$((PASSED + 1))
    else
        echo "  ❌ 状态未正确更新: $STATUS"
        FAILED=$((FAILED + 1))
    fi
    echo ""

    # 17. 验证状态日志
    echo "验证状态日志:"
    LOG_COUNT=$(curl -s "$BASE_URL/worker/orders/$PENDING_ORDER" 2>&1 | python -c "import sys,json; print(len(json.load(sys.stdin)['data']['statusLogs']))" 2>/dev/null)
    if [ "$LOG_COUNT" -gt 0 ]; then
        echo "  ✅ 状态日志数量: $LOG_COUNT"
        PASSED=$((PASSED + 1))
    else
        echo "  ❌ 状态日志为空"
        FAILED=$((FAILED + 1))
    fi
    echo ""
else
    echo "  ⚠️ 没有找到待接单工单，跳过接单流程测试"
    echo ""
fi

# 输出测试结果
echo "========================================"
echo "测试结果汇总"
echo "========================================"
echo "通过: $PASSED"
echo "失败: $FAILED"
TOTAL=$((PASSED + FAILED))
echo "总计: $TOTAL"
if [ $TOTAL -gt 0 ]; then
    RATE=$(echo "scale=1; $PASSED * 100 / $TOTAL" | bc)
    echo "通过率: ${RATE}%"
fi
echo "========================================"

if [ $FAILED -eq 0 ]; then
    echo "🎉 所有测试通过！"
    exit 0
else
    echo "⚠️ 有 $FAILED 个测试失败"
    exit 1
fi
