package com.cityrepair.controller;

import com.cityrepair.common.ApiResponse;
import com.cityrepair.common.PageQuery;
import com.cityrepair.enums.OrderStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/repair-orders")
public class RepairOrderController {

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

    @GetMapping("/my")
    public ApiResponse<Map<String, Object>> myOrders(@ModelAttribute PageQuery query) {
        return ApiResponse.success(Map.of(
                "page", query.getPage(),
                "pageSize", query.getPageSize(),
                "total", 0,
                "records", List.of()
        ));
    }
}
