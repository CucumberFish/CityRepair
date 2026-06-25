package com.cityrepair.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cityrepair.dto.CompleteRequest;
import com.cityrepair.dto.ProgressRequest;
import com.cityrepair.vo.WorkerOrderDetailVO;
import com.cityrepair.vo.WorkerOrderVO;

/**
 * 维修人员工单Service接口
 */
public interface WorkerOrderService {

    /**
     * 查询分派给指定维修人员的工单列表
     * @param workerId 维修人员ID
     * @param page 页码
     * @param pageSize 每页条数
     * @param keyword 关键词
     * @param status 状态筛选
     * @return 分页结果
     */
    Page<WorkerOrderVO> listWorkerOrders(Long workerId, Integer page, Integer pageSize,
                                         String keyword, String status);

    /**
     * 查询工单详情
     * @param orderId 工单ID
     * @param workerId 维修人员ID（用于校验权限）
     * @return 工单详情
     */
    WorkerOrderDetailVO getOrderDetail(Long orderId, Long workerId);

    /**
     * 接单
     * @param orderId 工单ID
     * @param workerId 维修人员ID
     */
    void acceptOrder(Long orderId, Long workerId);

    /**
     * 开始处理
     * @param orderId 工单ID
     * @param workerId 维修人员ID
     */
    void startOrder(Long orderId, Long workerId);

    /**
     * 提交进度
     * @param orderId 工单ID
     * @param workerId 维修人员ID
     * @param request 进度请求
     */
    void submitProgress(Long orderId, Long workerId, ProgressRequest request);

    /**
     * 完成工单
     * @param orderId 工单ID
     * @param workerId 维修人员ID
     * @param request 完成请求
     */
    void completeOrder(Long orderId, Long workerId, CompleteRequest request);
}
