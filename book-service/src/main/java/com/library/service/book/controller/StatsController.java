package com.library.service.book.controller;

import com.library.service.book.common.ApiResponse;
import com.library.service.book.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 统计数据控制器
 */
@RestController
@RequestMapping("/api/v1/stats")
@Tag(name = "统计数据", description = "仪表盘统计相关接口")
public class StatsController {

    @Autowired
    private BorrowService borrowService;

    @GetMapping("/dashboard")
    @Operation(summary = "获取仪表盘统计数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = borrowService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
