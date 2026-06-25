package com.cityrepair.controller;

import com.cityrepair.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        return ApiResponse.success(Map.of(
                "totalOrders", 0,
                "pendingOrders", 0,
                "completedOrders", 0,
                "completionRate", 0
        ));
    }
}
