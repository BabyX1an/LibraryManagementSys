package com.library.service.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.service.user.entity.User;
import com.library.service.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<User> login(@RequestParam String username, @RequestParam String password) {
        User user = userService.login(username, password);
        return ResponseEntity.ok(user);
    }
}