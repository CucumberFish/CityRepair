package com.cityrepair.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cityrepair.common.ApiResponse;
import com.cityrepair.common.PageQuery;
import com.cityrepair.dto.CompleteRequest;
import com.cityrepair.dto.ProgressRequest;
import com.cityrepair.entity.OrderAttachment;
import com.cityrepair.service.OrderAttachmentService;
import com.cityrepair.service.WorkerOrderService;
import com.cityrepair.vo.WorkerOrderDetailVO;
import com.cityrepair.vo.WorkerOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 维修人员工单控制器
 * 处理维修人员相关的所有请求
 */
@RestController
@RequestMapping("/worker/orders")
public class WorkerOrderController {

    @Autowired
    private WorkerOrderService workerOrderService;

    @Autowired
    private OrderAttachmentService orderAttachmentService;

    /**
     * 查询分派给当前维修人员的工单列表
     * GET /api/worker/orders
     *
     * @param page 页码（默认1）
     * @param pageSize 每页条数（默认10）
     * @param keyword 关键词搜索
     * @param status 状态筛选
     * @return 工单列表
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> listOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {

        // TODO: 从登录上下文获取当前用户ID，暂时硬编码为worker1的ID=3
        Long workerId = 3L;

        // 查询工单列表
        Page<WorkerOrderVO> result = workerOrderService.listWorkerOrders(
            workerId, page, pageSize, keyword, status);

        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pageSize", result.getSize());
        data.put("pages", result.getPages());

        return ApiResponse.success(data);
    }

    /**
     * 查询工单详情
     * GET /api/worker/orders/{id}
     *
     * @param id 工单ID
     * @return 工单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<WorkerOrderDetailVO> getOrderDetail(@PathVariable Long id) {
        // TODO: 从登录上下文获取当前用户ID，暂时硬编码为worker1的ID=3
        Long workerId = 3L;

        WorkerOrderDetailVO detail = workerOrderService.getOrderDetail(id, workerId);
        return ApiResponse.success(detail);
    }

    /**
     * 接单
     * PUT /api/worker/orders/{id}/accept
     *
     * @param id 工单ID
     * @return 操作结果
     */
    @PutMapping("/{id}/accept")
    public ApiResponse<Void> acceptOrder(@PathVariable Long id) {
        // TODO: 从登录上下文获取当前用户ID，暂时硬编码为worker1的ID=3
        Long workerId = 3L;

        workerOrderService.acceptOrder(id, workerId);
        return ApiResponse.success("接单成功", null);
    }

    /**
     * 开始处理
     * PUT /api/worker/orders/{id}/start
     *
     * @param id 工单ID
     * @return 操作结果
     */
    @PutMapping("/{id}/start")
    public ApiResponse<Void> startOrder(@PathVariable Long id) {
        // TODO: 从登录上下文获取当前用户ID，暂时硬编码为worker1的ID=3
        Long workerId = 3L;

        workerOrderService.startOrder(id, workerId);
        return ApiResponse.success("开始处理", null);
    }

    /**
     * 提交进度
     * POST /api/worker/orders/{id}/progress
     *
     * @param id 工单ID
     * @param request 进度请求
     * @return 操作结果
     */
    @PostMapping("/{id}/progress")
    public ApiResponse<Void> submitProgress(@PathVariable Long id,
                                           @RequestBody ProgressRequest request) {
        // TODO: 从登录上下文获取当前用户ID，暂时硬编码为worker1的ID=3
        Long workerId = 3L;

        workerOrderService.submitProgress(id, workerId, request);
        return ApiResponse.success("进度已提交", null);
    }

    /**
     * 完成工单
     * PUT /api/worker/orders/{id}/complete
     *
     * @param id 工单ID
     * @param request 完成请求
     * @return 操作结果
     */
    @PutMapping("/{id}/complete")
    public ApiResponse<Void> completeOrder(@PathVariable Long id,
                                          @RequestBody CompleteRequest request) {
        // TODO: 从登录上下文获取当前用户ID，暂时硬编码为worker1的ID=3
        Long workerId = 3L;

        workerOrderService.completeOrder(id, workerId, request);
        return ApiResponse.success("工单已完成", null);
    }

    /**
     * 上传处理后图片
     * POST /api/worker/orders/{id}/attachments
     *
     * @param id 工单ID
     * @param file 上传的文件
     * @return 操作结果
     */
    @PostMapping("/{id}/attachments")
    public ApiResponse<OrderAttachment> uploadAttachment(@PathVariable Long id,
                                                        @RequestParam("file") MultipartFile file) {
        // TODO: 从登录上下文获取当前用户ID，暂时硬编码为worker1的ID=3
        Long workerId = 3L;

        OrderAttachment attachment = orderAttachmentService.uploadAttachment(
            id, file, "AFTER_REPAIR", workerId);
        return ApiResponse.success("上传成功", attachment);
    }
}
