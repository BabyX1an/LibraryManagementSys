package com.library.service.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.service.book.entity.Book;
import com.library.service.book.entity.BorrowRecord;
import com.library.service.book.mapper.BookMapper;
import com.library.service.book.mapper.BorrowRecordMapper;
import com.library.service.book.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 借阅服务实现类
 */
@Service
public class BorrowServiceImpl extends ServiceImpl<BorrowRecordMapper, BorrowRecord> implements BorrowService {

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Autowired
    private BookMapper bookMapper;

    /**
     * 借阅期限（天）
     */
    private static final int BORROW_DAYS = 14;

    @Override
    @Transactional
    public BorrowRecord borrowBook(Long userId, Long bookId) {
        // 检查图书是否存在
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }

        // 检查库存
        if (book.getStock() == null || book.getStock() <= 0) {
            throw new RuntimeException("图书库存不足");
        }

        // 检查用户是否已借阅该书且未归还
        BorrowRecord existingBorrow = borrowRecordMapper.findUnreturnedBorrow(userId, bookId);
        if (existingBorrow != null) {
            throw new RuntimeException("您已借阅该书，请先归还后再借阅");
        }

        // 创建借阅记录
        LocalDateTime now = LocalDateTime.now();
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowTime(now);
        record.setDueTime(now.plusDays(BORROW_DAYS));
        record.setStatus(1); // 借阅中

        save(record);

        // 更新图书库存和借阅次数
        book.setStock(book.getStock() - 1);
        book.setBorrowedCount(book.getBorrowedCount() == null ? 1 : book.getBorrowedCount() + 1);
        bookMapper.updateById(book);

        // 返回带图书信息的借阅记录
        record.setBook(book);
        return record;
    }

    @Override
    @Transactional
    public boolean returnBook(Long recordId, Long userId) {
        // 查询借阅记录
        BorrowRecord record = getById(recordId);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }

        // 验证是否是用户本人的借阅记录
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此借阅记录");
        }

        // 检查是否已归还
        if (record.getStatus() == 2) {
            throw new RuntimeException("该书已归还");
        }

        // 更新借阅记录
        record.setReturnTime(LocalDateTime.now());
        record.setStatus(2); // 已归还
        updateById(record);

        // 更新图书库存
        Book book = bookMapper.selectById(record.getBookId());
        if (book != null) {
            book.setStock(book.getStock() == null ? 1 : book.getStock() + 1);
            bookMapper.updateById(book);
        }

        return true;
    }

    @Override
    public List<BorrowRecord> getCurrentBorrows(Long userId) {
        return borrowRecordMapper.listCurrentByUserId(userId);
    }

    @Override
    public Page<BorrowRecord> getBorrowHistory(Page<BorrowRecord> page, Long userId) {
        return borrowRecordMapper.pageHistoryByUserId(page, userId);
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> result = new HashMap<>();

        // 1. 借阅趋势：最近7天每日借阅量
        List<Map<String, Object>> trendData = borrowRecordMapper.countBorrowTrend(7);
        // 补全缺失的日期（没有借阅的天显示为0）
        Map<String, Object> trendMap = new LinkedHashMap<>();
        for (Map<String, Object> item : trendData) {
            trendMap.put(item.get("date").toString(), item.get("count"));
        }
        List<Map<String, Object>> borrowTrend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).format(formatter);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.substring(5)); // 只显示 MM-dd
            dayData.put("count", trendMap.getOrDefault(date, 0));
            borrowTrend.add(dayData);
        }
        result.put("borrowTrend", borrowTrend);

        // 2. 最近10条活动记录
        List<Map<String, Object>> activities = borrowRecordMapper.listRecentActivities(10);
        for (Map<String, Object> activity : activities) {
            int status = ((Number) activity.get("type")).intValue();
            activity.put("type", status == 2 ? "return" : "borrow");
        }
        result.put("recentActivities", activities);

        return result;
    }
}
