package com.cityrepair.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cityrepair.entity.OrderStatusLog;

/**
 * 工单状态日志Service接口
 */
public interface OrderStatusLogService extends IService<OrderStatusLog> {

    /**
     * 记录状态变更日志
     * @param orderId 工单ID
     * @param operatorId 操作人ID
     * @param action 操作类型
     * @param fromStatus 原状态
     * @param toStatus 目标状态
     * @param remark 备注
     */
    void logStatusChange(Long orderId, Long operatorId, String action,
                         String fromStatus, String toStatus, String remark);
}
