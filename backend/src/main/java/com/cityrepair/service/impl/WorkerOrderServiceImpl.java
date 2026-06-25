package com.cityrepair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cityrepair.dto.CompleteRequest;
import com.cityrepair.dto.ProgressRequest;
import com.cityrepair.entity.OrderAssignment;
import com.cityrepair.entity.OrderStatusLog;
import com.cityrepair.entity.RepairCategory;
import com.cityrepair.entity.RepairOrder;
import com.cityrepair.entity.SysUser;
import com.cityrepair.enums.OrderStatus;
import com.cityrepair.exception.BusinessException;
import com.cityrepair.mapper.OrderAssignmentMapper;
import com.cityrepair.mapper.RepairCategoryMapper;
import com.cityrepair.mapper.RepairOrderMapper;
import com.cityrepair.mapper.SysUserMapper;
import com.cityrepair.service.OrderAttachmentService;
import com.cityrepair.service.OrderStatusLogService;
import com.cityrepair.service.WorkerOrderService;
import com.cityrepair.vo.WorkerOrderDetailVO;
import com.cityrepair.vo.WorkerOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 维修人员工单Service实现类
 */
@Service
public class WorkerOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder>
        implements WorkerOrderService {

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    @Autowired
    private OrderAssignmentMapper orderAssignmentMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private RepairCategoryMapper repairCategoryMapper;

    @Autowired
    private OrderStatusLogService orderStatusLogService;

    @Autowired
    private OrderAttachmentService orderAttachmentService;

    @Override
    public Page<WorkerOrderVO> listWorkerOrders(Long workerId, Integer page, Integer pageSize,
                                                 String keyword, String status) {
        // 构建查询条件
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairOrder::getCurrentWorkerId, workerId);

        // 状态筛选
        if (status != null && !status.isEmpty()) {
            wrapper.eq(RepairOrder::getStatus, status);
        }

        // 关键词搜索（搜索工单编号或标题）
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like(RepairOrder::getOrderNo, keyword)
                .or()
                .like(RepairOrder::getTitle, keyword)
            );
        }

        // 按创建时间倒序
        wrapper.orderByDesc(RepairOrder::getCreatedAt);

        // 执行分页查询
        Page<RepairOrder> orderPage = new Page<>(page, pageSize);
        Page<RepairOrder> result = page(orderPage, wrapper);

        // 转换为VO
        Page<WorkerOrderVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<WorkerOrderVO> voList = result.getRecords().stream()
            .map(this::convertToWorkerOrderVO)
            .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public WorkerOrderDetailVO getOrderDetail(Long orderId, Long workerId) {
        // 查询工单
        RepairOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }

        // 校验工单归属
        if (!order.getCurrentWorkerId().equals(workerId)) {
            throw new BusinessException(403, "无权查看该工单");
        }

        // 转换为详情VO
        WorkerOrderDetailVO detailVO = convertToWorkerOrderDetailVO(order);

        // 查询附件
        List<com.cityrepair.entity.OrderAttachment> attachments =
            orderAttachmentService.listByOrderId(orderId, null);
        List<WorkerOrderDetailVO.OrderAttachmentVO> attachmentVOs = attachments.stream()
            .map(this::convertToAttachmentVO)
            .collect(Collectors.toList());
        detailVO.setAttachments(attachmentVOs);

        // 查询状态日志
        LambdaQueryWrapper<OrderStatusLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(OrderStatusLog::getOrderId, orderId);
        logWrapper.orderByAsc(OrderStatusLog::getCreatedAt);
        List<OrderStatusLog> logs = orderStatusLogService.list(logWrapper);
        List<WorkerOrderDetailVO.OrderStatusLogVO> logVOs = logs.stream()
            .map(this::convertToStatusLogVO)
            .collect(Collectors.toList());
        detailVO.setStatusLogs(logVOs);

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptOrder(Long orderId, Long workerId) {
        // 查询工单
        RepairOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }

        // 校验工单归属
        if (!order.getCurrentWorkerId().equals(workerId)) {
            throw new BusinessException(403, "无权操作该工单");
        }

        // 校验工单状态
        if (!OrderStatus.PENDING_ACCEPT.name().equals(order.getStatus())) {
            throw new BusinessException("只有待接单的工单才能接单");
        }

        LocalDateTime now = LocalDateTime.now();

        // 更新工单状态
        order.setStatus(OrderStatus.PROCESSING.name());
        order.setAcceptedAt(now);
        order.setVersion(order.getVersion() + 1);
        updateById(order);

        // 更新派单记录的接单时间
        OrderAssignment assignment = orderAssignmentMapper.findCurrentByOrderId(orderId);
        if (assignment != null) {
            assignment.setAcceptedAt(now);
            orderAssignmentMapper.updateById(assignment);
        }

        // 记录状态日志
        orderStatusLogService.logStatusChange(
            orderId, workerId, "ACCEPT",
            OrderStatus.PENDING_ACCEPT.name(),
            OrderStatus.PROCESSING.name(),
            "维修人员接单"
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startOrder(Long orderId, Long workerId) {
        // 查询工单
        RepairOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }

        // 校验工单归属
        if (!order.getCurrentWorkerId().equals(workerId)) {
            throw new BusinessException(403, "无权操作该工单");
        }

        // 校验工单状态（允许待接单或处理中）
        String currentStatus = order.getStatus();
        if (!OrderStatus.PENDING_ACCEPT.name().equals(currentStatus) &&
            !OrderStatus.PROCESSING.name().equals(currentStatus)) {
            throw new BusinessException("当前状态不允许开始处理");
        }

        LocalDateTime now = LocalDateTime.now();

        // 更新工单状态
        order.setStatus(OrderStatus.PROCESSING.name());
        if (order.getAcceptedAt() == null) {
            order.setAcceptedAt(now);
        }
        order.setVersion(order.getVersion() + 1);
        updateById(order);

        // 更新派单记录
        OrderAssignment assignment = orderAssignmentMapper.findCurrentByOrderId(orderId);
        if (assignment != null && assignment.getAcceptedAt() == null) {
            assignment.setAcceptedAt(now);
            orderAssignmentMapper.updateById(assignment);
        }

        // 记录状态日志
        orderStatusLogService.logStatusChange(
            orderId, workerId, "START",
            currentStatus,
            OrderStatus.PROCESSING.name(),
            "开始处理"
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitProgress(Long orderId, Long workerId, ProgressRequest request) {
        // 校验请求参数
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BusinessException("进度说明不能为空");
        }

        // 查询工单
        RepairOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }

        // 校验工单归属
        if (!order.getCurrentWorkerId().equals(workerId)) {
            throw new BusinessException(403, "无权操作该工单");
        }

        // 校验工单状态
        if (!OrderStatus.PROCESSING.name().equals(order.getStatus())) {
            throw new BusinessException("只有处理中的工单才能提交进度");
        }

        // 记录状态日志（不改变工单状态）
        orderStatusLogService.logStatusChange(
            orderId, workerId, "PROGRESS",
            OrderStatus.PROCESSING.name(),
            OrderStatus.PROCESSING.name(),
            request.getContent()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId, Long workerId, CompleteRequest request) {
        // 校验请求参数
        if (request.getCompletionResult() == null || request.getCompletionResult().trim().isEmpty()) {
            throw new BusinessException("完成结果描述不能为空");
        }

        // 查询工单
        RepairOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }

        // 校验工单归属
        if (!order.getCurrentWorkerId().equals(workerId)) {
            throw new BusinessException(403, "无权操作该工单");
        }

        // 校验工单状态
        if (!OrderStatus.PROCESSING.name().equals(order.getStatus())) {
            throw new BusinessException("只有处理中的工单才能完成");
        }

        LocalDateTime now = LocalDateTime.now();

        // 更新工单状态
        order.setStatus(OrderStatus.COMPLETED.name());
        order.setCompletionResult(request.getCompletionResult());
        order.setCompletedAt(now);
        order.setVersion(order.getVersion() + 1);
        updateById(order);

        // 记录状态日志
        orderStatusLogService.logStatusChange(
            orderId, workerId, "COMPLETE",
            OrderStatus.PROCESSING.name(),
            OrderStatus.COMPLETED.name(),
            "工单处理完成"
        );
    }

    /**
     * 将工单实体转换为列表VO
     */
    private WorkerOrderVO convertToWorkerOrderVO(RepairOrder order) {
        WorkerOrderVO vo = new WorkerOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTitle(order.getTitle());
        vo.setCategoryId(order.getCategoryId());
        vo.setLocation(order.getLocation());
        vo.setPriority(order.getPriority());
        vo.setStatus(order.getStatus());
        vo.setContactPhone(order.getContactPhone());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());

        // 查询类别名称
        RepairCategory category = repairCategoryMapper.selectById(order.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getCategoryName());
        }

        // 查询居民信息
        SysUser resident = sysUserMapper.selectById(order.getResidentId());
        if (resident != null) {
            vo.setResidentName(resident.getRealName());
            vo.setResidentPhone(resident.getPhone());
        }

        return vo;
    }

    /**
     * 将工单实体转换为详情VO
     */
    private WorkerOrderDetailVO convertToWorkerOrderDetailVO(RepairOrder order) {
        WorkerOrderDetailVO vo = new WorkerOrderDetailVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTitle(order.getTitle());
        vo.setCategoryId(order.getCategoryId());
        vo.setLocation(order.getLocation());
        vo.setDescription(order.getDescription());
        vo.setContactPhone(order.getContactPhone());
        vo.setPriority(order.getPriority());
        vo.setStatus(order.getStatus());
        vo.setCompletionResult(order.getCompletionResult());
        vo.setAssignedAt(order.getAssignedAt());
        vo.setAcceptedAt(order.getAcceptedAt());
        vo.setCompletedAt(order.getCompletedAt());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());

        // 查询类别名称
        RepairCategory category = repairCategoryMapper.selectById(order.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getCategoryName());
        }

        // 查询居民信息
        SysUser resident = sysUserMapper.selectById(order.getResidentId());
        if (resident != null) {
            vo.setResidentName(resident.getRealName());
            vo.setResidentPhone(resident.getPhone());
        }

        return vo;
    }

    /**
     * 转换附件VO
     */
    private WorkerOrderDetailVO.OrderAttachmentVO convertToAttachmentVO(
            com.cityrepair.entity.OrderAttachment attachment) {
        WorkerOrderDetailVO.OrderAttachmentVO vo = new WorkerOrderDetailVO.OrderAttachmentVO();
        vo.setId(attachment.getId());
        vo.setAttachmentType(attachment.getAttachmentType());
        vo.setFileUrl(attachment.getFileUrl());
        vo.setOriginalName(attachment.getOriginalName());
        vo.setContentType(attachment.getContentType());
        vo.setFileSize(attachment.getFileSize());
        vo.setCreatedAt(attachment.getCreatedAt());
        return vo;
    }

    /**
     * 转换状态日志VO
     */
    private WorkerOrderDetailVO.OrderStatusLogVO convertToStatusLogVO(OrderStatusLog log) {
        WorkerOrderDetailVO.OrderStatusLogVO vo = new WorkerOrderDetailVO.OrderStatusLogVO();
        vo.setId(log.getId());
        vo.setOperatorId(log.getOperatorId());
        vo.setAction(log.getAction());
        vo.setFromStatus(log.getFromStatus());
        vo.setToStatus(log.getToStatus());
        vo.setRemark(log.getRemark());
        vo.setCreatedAt(log.getCreatedAt());

        // 查询操作人姓名
        SysUser operator = sysUserMapper.selectById(log.getOperatorId());
        if (operator != null) {
            vo.setOperatorName(operator.getRealName());
        }

        return vo;
    }
}
