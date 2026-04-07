package com.library.service.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 借阅记录表
 */
@Data
@TableName("borrow_record")
public class BorrowRecord {

    /**
     * 借阅记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 图书ID
     */
    @TableField("book_id")
    private Long bookId;

    /**
     * 借阅时间
     */
    @TableField("borrow_time")
    private LocalDateTime borrowTime;

    /**
     * 应还时间（借阅时间+14天）
     */
    @TableField("due_time")
    private LocalDateTime dueTime;

    /**
     * 实际归还时间
     */
    @TableField("return_time")
    private LocalDateTime returnTime;

    /**
     * 状态：1-借阅中，2-已归还，3-已逾期
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 关联的图书信息（非数据库字段）
     */
    @TableField(exist = false)
    private Book book;
}
