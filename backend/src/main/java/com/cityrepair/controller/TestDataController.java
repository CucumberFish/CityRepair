package com.cityrepair.controller;

import com.cityrepair.common.ApiResponse;
import com.cityrepair.entity.OrderAssignment;
import com.cityrepair.entity.OrderStatusLog;
import com.cityrepair.entity.RepairOrder;
import com.cityrepair.mapper.OrderAssignmentMapper;
import com.cityrepair.mapper.OrderStatusLogMapper;
import com.cityrepair.mapper.RepairOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试数据控制器（仅用于开发测试）
 */
@RestController
@RequestMapping("/test")
public class TestDataController {

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    @Autowired
    private OrderAssignmentMapper orderAssignmentMapper;

    @Autowired
    private OrderStatusLogMapper orderStatusLogMapper;

    /**
     * 重置工单为待接单状态
     * PUT /api/test/reset-order/{id}
     */
    @PutMapping("/reset-order/{id}")
    public ApiResponse<Map<String, Object>> resetOrder(@PathVariable Long id) {
        // 查询工单
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            return ApiResponse.error(400, "工单不存在");
        }

        // 重置状态
        order.setStatus("PENDING_ACCEPT");
        order.setCurrentWorkerId(3L); // worker1
        order.setAcceptedAt(null);
        order.setCompletedAt(null);
        order.setCompletionResult(null);
        order.setUpdatedAt(LocalDateTime.now());
        repairOrderMapper.updateById(order);

        // 删除处理相关的状态日志
        orderStatusLogMapper.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderStatusLog>()
                .eq(OrderStatusLog::getOrderId, id)
                .in(OrderStatusLog::getAction, "ACCEPT", "START", "PROGRESS", "COMPLETE")
        );

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("status", order.getStatus());

        return ApiResponse.success("工单已重置为待接单状态", result);
    }

    /**
     * 批量重置工单
     * PUT /api/test/reset-all
     */
    @PutMapping("/reset-all")
    public ApiResponse<String> resetAllOrders() {
        // 将worker1的所有非待接单工单重置
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RepairOrder> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(RepairOrder::getCurrentWorkerId, 3L)
               .ne(RepairOrder::getStatus, "PENDING_ACCEPT");

        java.util.List<RepairOrder> orders = repairOrderMapper.selectList(wrapper);

        for (RepairOrder order : orders) {
            order.setStatus("PENDING_ACCEPT");
            order.setAcceptedAt(null);
            order.setCompletedAt(null);
            order.setCompletionResult(null);
            order.setUpdatedAt(LocalDateTime.now());
            repairOrderMapper.updateById(order);

            // 删除处理相关的状态日志
            orderStatusLogMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderStatusLog>()
                    .eq(OrderStatusLog::getOrderId, order.getId())
                    .in(OrderStatusLog::getAction, "ACCEPT", "START", "PROGRESS", "COMPLETE")
            );
        }

        return ApiResponse.success("已重置 " + orders.size() + " 个工单为待接单状态", null);
    }
}
