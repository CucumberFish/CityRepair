package com.cityrepair.controller;

import com.cityrepair.common.ApiResponse;
import com.cityrepair.dto.LoginRequest;
import com.cityrepair.entity.SysUser;
import com.cityrepair.mapper.SysUserMapper;
import com.cityrepair.service.UserContext;
import com.cityrepair.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final SysUserMapper userMapper;
    private final JwtUtil jwtUtil;

    public AuthController(SysUserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        SysUser user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, request.getUsername()));
        if (user == null || !user.getEnabled().equals(1)) {
            return ApiResponse.error(400, "用户名或密码错误");
        }

        String passwordHash = user.getPasswordHash();
        String inputPassword = "{noop}" + request.getPassword();
        if (!passwordHash.equals(inputPassword)) {
            return ApiResponse.error(400, "用户名或密码错误");
        }

        List<String> roles = userMapper.findRolesByUserId(user.getId());
        String token = jwtUtil.createToken(user.getId(), user.getUsername(), roles);

        return ApiResponse.success(Map.of(
                "token", token,
                "user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "realName", user.getRealName(),
                        "roles", roles
                )
        ));
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        UserContext.UserInfo user = UserContext.get();
        SysUser sysUser = userMapper.selectById(user.userId());
        return ApiResponse.success(Map.of(
                "id", sysUser.getId(),
                "username", sysUser.getUsername(),
                "realName", sysUser.getRealName(),
                "phone", sysUser.getPhone(),
                "roles", user.roles()
        ));
    }

    @GetMapping("/demo-accounts")
    public ApiResponse<List<Map<String, String>>> demoAccounts() {
        return ApiResponse.success(List.of(
                Map.of("role", "ADMIN", "username", "admin", "password", "123456"),
                Map.of("role", "RESIDENT", "username", "resident1", "password", "123456"),
                Map.of("role", "WORKER", "username", "worker1", "password", "123456")
        ));
    }
}
