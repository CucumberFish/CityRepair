package com.cityrepair.controller;

import com.cityrepair.common.ApiResponse;
import com.cityrepair.common.PageQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @GetMapping
    public ApiResponse<Map<String, Object>> orders(@ModelAttribute PageQuery query) {
        return ApiResponse.success(Map.of(
                "page", query.getPage(),
                "pageSize", query.getPageSize(),
                "total", 0,
                "records", List.of()
        ));
    }
}
