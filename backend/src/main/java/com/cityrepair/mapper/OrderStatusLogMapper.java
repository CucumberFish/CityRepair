package com.cityrepair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cityrepair.entity.OrderStatusLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单状态流转日志Mapper接口
 */
@Mapper
public interface OrderStatusLogMapper extends BaseMapper<OrderStatusLog> {
}
