package com.cityrepair.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cityrepair.entity.OrderStatusLog;
import com.cityrepair.mapper.OrderStatusLogMapper;
import com.cityrepair.service.OrderStatusLogService;
import org.springframework.stereotype.Service;

/**
 * 工单状态日志Service实现类
 */
@Service
public class OrderStatusLogServiceImpl extends ServiceImpl<OrderStatusLogMapper, OrderStatusLog>
        implements OrderStatusLogService {

    @Override
    public void logStatusChange(Long orderId, Long operatorId, String action,
                                String fromStatus, String toStatus, String remark) {
        OrderStatusLog log = new OrderStatusLog(orderId, operatorId, action,
                fromStatus, toStatus, remark);
        save(log);
    }
}
