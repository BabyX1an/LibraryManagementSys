package com.library.service.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.service.user.entity.User;
import com.library.service.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户管理相关接口")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "分页查询用户")
    public ResponseEntity<Page<User>> pageQuery(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "真实姓名") @RequestParam(required = false) String realName) {

        Page<User> page = new Page<>(current, size);
        Page<User> result = userService.pageQueryUsers(page, username, realName);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @Operation(summary = "添加用户")
    public ResponseEntity<User> create(@RequestBody User user) {
        userService.addUser(user);
        User createdUser = userService.getUserByUsername(user.getUsername());
        return ResponseEntity.status(201).body(createdUser);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public ResponseEntity<User> update(
        @PathVariable Long id,
        @RequestBody User user) {
        user.setId(id);
        userService.updateUser(user);
        User updatedUser = userService.getById(id);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        User user = userService.login(request.get("username"), request.get("password"));
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名或密码错误");
        }
        // 生成简单的token（Base64编码用户ID）
        String token = Base64.getEncoder().encodeToString(user.getId().toString().getBytes());
        return ResponseEntity.ok(new LoginResponse(user, token));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        try {
            Long userId = extractUserIdFromToken(authorization);
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户不存在");
            }
            // 清除密码字段
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效的token");
        }
    }

    @PutMapping("/me")
    @Operation(summary = "更新当前用户信息")
    public ResponseEntity<?> updateCurrentUser(
            @RequestHeader("Authorization") String authorization,
            @RequestBody User user) {
        try {
            Long userId = extractUserIdFromToken(authorization);
            user.setId(userId);
            boolean success = userService.updateCurrentUser(user);
            if (success) {
                User updatedUser = userService.getUserById(userId);
                updatedUser.setPassword(null);
                return ResponseEntity.ok(updatedUser);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("更新失败");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效的token");
        }
    }

    /**
     * 从Authorization头中提取用户ID
     */
    private Long extractUserIdFromToken(String authorization) {
        String token = authorization.replace("Bearer ", "");
        String userIdStr = new String(Base64.getDecoder().decode(token));
        return Long.parseLong(userIdStr);
    }

    /**
     * 登录响应包装类
     */
    private static class LoginResponse {
        private User user;
        private String token;

        public LoginResponse(User user, String token) {
            this.user = user;
            this.token = token;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}