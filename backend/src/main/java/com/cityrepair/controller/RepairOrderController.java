package com.cityrepair.controller;

import com.cityrepair.common.ApiResponse;
import com.cityrepair.common.PageQuery;
import com.cityrepair.dto.CancelOrderRequest;
import com.cityrepair.dto.CreateOrderRequest;
import com.cityrepair.entity.OrderAttachment;
import com.cityrepair.entity.RepairCategory;
import com.cityrepair.entity.RepairOrder;
import com.cityrepair.enums.OrderStatus;
import com.cityrepair.service.FileUploadService;
import com.cityrepair.service.RepairOrderService;
import com.cityrepair.service.UserContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/repair-orders")
public class RepairOrderController {

    private final RepairOrderService repairOrderService;
    private final FileUploadService fileUploadService;

    public RepairOrderController(RepairOrderService repairOrderService,
                                 FileUploadService fileUploadService) {
        this.repairOrderService = repairOrderService;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping("/statuses")
    public ApiResponse<List<String>> statuses() {
        return ApiResponse.success(List.of(OrderStatus.PENDING_REVIEW.name(),
                OrderStatus.PENDING_ASSIGN.name(),
                OrderStatus.PENDING_ACCEPT.name(),
                OrderStatus.PROCESSING.name(),
                OrderStatus.COMPLETED.name(),
                OrderStatus.EVALUATED.name(),
                OrderStatus.REJECTED.name(),
                OrderStatus.CANCELLED.name()));
    }

    @GetMapping("/categories")
    public ApiResponse<List<RepairCategory>> categories() {
        return ApiResponse.success(repairOrderService.getCategories());
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody CreateOrderRequest request) {
        UserContext.UserInfo user = UserContext.get();
        if (!user.roles().contains("RESIDENT")) {
            return ApiResponse.error(403, "仅居民可创建报修");
        }

        RepairOrder order = repairOrderService.createOrder(
                user.userId(), request.getCategoryId(), request.getTitle(),
                request.getLocation(), request.getDescription(), request.getContactPhone());
        return ApiResponse.success(Map.of("id", order.getId(), "orderNo", order.getOrderNo()));
    }

    @GetMapping("/my")
    public ApiResponse<Map<String, Object>> myOrders(@ModelAttribute PageQuery query) {
        UserContext.UserInfo user = UserContext.get();
        Map<String, Object> data = repairOrderService.listMyOrders(
                user.userId(), query.getPage(), query.getPageSize(),
                query.getKeyword(), query.getStatus());
        return ApiResponse.success(data);
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        UserContext.UserInfo user = UserContext.get();
        RepairOrder order = repairOrderService.getOrderById(id);
        if (order == null) {
            return ApiResponse.error(404, "工单不存在");
        }
        if (!user.roles().contains("ADMIN") && !order.getResidentId().equals(user.userId())) {
            return ApiResponse.error(403, "无权访问该工单");
        }
        return ApiResponse.success(repairOrderService.getOrderDetail(id));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id,
                                    @Valid @RequestBody CancelOrderRequest request) {
        UserContext.UserInfo user = UserContext.get();
        try {
            repairOrderService.cancelOrder(id, user.userId(), request.getReason());
            return ApiResponse.success("取消成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/{id}/attachments")
    public ApiResponse<Map<String, Object>> uploadAttachment(@PathVariable Long id,
                                                             @RequestParam("file") MultipartFile file) throws IOException {
        UserContext.UserInfo user = UserContext.get();
        RepairOrder order = repairOrderService.getOrderById(id);
        if (order == null) {
            return ApiResponse.error(404, "工单不存在");
        }
        if (!order.getResidentId().equals(user.userId())) {
            return ApiResponse.error(403, "无权操作该工单");
        }

        String fileUrl = fileUploadService.uploadFile(file);

        OrderAttachment attachment = new OrderAttachment();
        attachment.setOrderId(id);
        attachment.setAttachmentType("BEFORE_REPAIR");
        attachment.setFileUrl(fileUrl);
        attachment.setOriginalName(file.getOriginalFilename());
        attachment.setContentType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setUploadedBy(user.userId());
        repairOrderService.saveAttachment(attachment);

        return ApiResponse.success(Map.of("url", fileUrl, "originalName", file.getOriginalFilename()));
    }
}
