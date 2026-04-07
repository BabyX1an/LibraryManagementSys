package com.library.service.book.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.service.book.common.ApiResponse;
import com.library.service.book.entity.BorrowRecord;
import com.library.service.book.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 借阅管理控制器
 */
@RestController
@RequestMapping("/api/v1/borrow")
@Tag(name = "借阅管理", description = "借阅管理相关接口")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @PostMapping("/{bookId}")
    @Operation(summary = "借阅图书")
    public ResponseEntity<ApiResponse<BorrowRecord>> borrowBook(
            @PathVariable Long bookId,
            @RequestAttribute("userId") Long userId) {
        try {
            BorrowRecord record = borrowService.borrowBook(userId, bookId);
            return ResponseEntity.ok(ApiResponse.success("借阅成功", record));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PutMapping("/{recordId}/return")
    @Operation(summary = "归还图书")
    public ResponseEntity<ApiResponse<Boolean>> returnBook(
            @PathVariable Long recordId,
            @RequestAttribute("userId") Long userId) {
        try {
            boolean result = borrowService.returnBook(recordId, userId);
            return ResponseEntity.ok(ApiResponse.success("归还成功", result));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前借阅")
    public ResponseEntity<ApiResponse<List<BorrowRecord>>> getCurrentBorrows(
            @RequestAttribute("userId") Long userId) {
        List<BorrowRecord> records = borrowService.getCurrentBorrows(userId);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/history")
    @Operation(summary = "获取借阅历史")
    public ResponseEntity<ApiResponse<Page<BorrowRecord>>> getBorrowHistory(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @RequestAttribute("userId") Long userId) {
        Page<BorrowRecord> page = new Page<>(current, size);
        Page<BorrowRecord> result = borrowService.getBorrowHistory(page, userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
