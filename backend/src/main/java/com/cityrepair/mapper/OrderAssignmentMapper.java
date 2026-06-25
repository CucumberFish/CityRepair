package com.cityrepair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cityrepair.entity.OrderAssignment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 工单派单记录Mapper接口
 */
@Mapper
public interface OrderAssignmentMapper extends BaseMapper<OrderAssignment> {

    /**
     * 查询工单的当前有效派单
     */
    @Select("SELECT * FROM order_assignment WHERE order_id = #{orderId} AND is_current = 1")
    OrderAssignment findCurrentByOrderId(@Param("orderId") Long orderId);
}
