package com.library.service.book.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.library.service.book.entity.BorrowRecord;

import java.util.List;

/**
 * 借阅服务接口
 */
public interface BorrowService extends IService<BorrowRecord> {

    /**
     * 借阅图书
     * @param userId 用户ID
     * @param bookId 图书ID
     * @return 借阅记录
     */
    BorrowRecord borrowBook(Long userId, Long bookId);

    /**
     * 归还图书
     * @param recordId 借阅记录ID
     * @param userId 用户ID（用于权限验证）
     * @return 是否成功
     */
    boolean returnBook(Long recordId, Long userId);

    /**
     * 获取用户当前借阅（借阅中）
     * @param userId 用户ID
     * @return 借阅记录列表
     */
    List<BorrowRecord> getCurrentBorrows(Long userId);

    /**
     * 获取用户借阅历史（已归还，分页）
     * @param page 分页对象
     * @param userId 用户ID
     * @return 分页结果
     */
    Page<BorrowRecord> getBorrowHistory(Page<BorrowRecord> page, Long userId);
}
