package com.cityrepair.controller;

import com.cityrepair.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/demo-accounts")
    public ApiResponse<List<Map<String, String>>> demoAccounts() {
        return ApiResponse.success(List.of(
                Map.of("role", "ADMIN", "username", "admin", "password", "123456"),
                Map.of("role", "RESIDENT", "username", "resident1", "password", "123456"),
                Map.of("role", "WORKER", "username", "worker1", "password", "123456")
        ));
    }
}
