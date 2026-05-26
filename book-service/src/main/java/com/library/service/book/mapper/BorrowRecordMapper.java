package com.library.service.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.service.book.entity.BorrowRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 借阅记录Mapper接口
 */
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {

    /**
     * 分页查询用户借阅历史（已归还）
     * @param page 分页对象
     * @param userId 用户ID
     * @return 分页结果
     */
    Page<BorrowRecord> pageHistoryByUserId(Page<BorrowRecord> page, @Param("userId") Long userId);

    /**
     * 查询用户当前借阅（借阅中）
     * @param userId 用户ID
     * @return 借阅记录列表
     */
    List<BorrowRecord> listCurrentByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否已借阅该书且未归还
     * @param userId 用户ID
     * @param bookId 图书ID
     * @return 借阅记录
     */
    BorrowRecord findUnreturnedBorrow(@Param("userId") Long userId, @Param("bookId") Long bookId);

    /**
     * 统计最近N天每日借阅数量
     */
    List<Map<String, Object>> countBorrowTrend(@Param("days") int days);

    /**
     * 查询最近N条借阅/归还活动记录
     */
    List<Map<String, Object>> listRecentActivities(@Param("limit") int limit);
}
