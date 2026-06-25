package com.cityrepair.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 工单派单记录实体类
 * 对应表：order_assignment
 */
@TableName("order_assignment")
public class OrderAssignment {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 工单ID
     */
    private Long orderId;

    /**
     * 派单管理员ID
     */
    private Long adminId;

    /**
     * 维修人员ID
     */
    private Long workerId;

    /**
     * 派单时间
     */
    private LocalDateTime assignedAt;

    /**
     * 接单时间
     */
    private LocalDateTime acceptedAt;

    /**
     * 取消时间
     */
    private LocalDateTime canceledAt;

    /**
     * 是否为当前有效派单
     */
    private Boolean isCurrent;

    /**
     * 备注
     */
    private String remark;

    // 构造方法
    public OrderAssignment() {}

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }

    public void setCanceledAt(LocalDateTime canceledAt) {
        this.canceledAt = canceledAt;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
