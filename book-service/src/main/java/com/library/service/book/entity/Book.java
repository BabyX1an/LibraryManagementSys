package com.library.service.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 图书信息表
 */
@Data
@TableName("book")
public class Book {

    /**
     * 图书ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * ISBN号
     */
    @TableField("isbn")
    private String isbn;

    /**
     * 书名
     */
    @TableField("title")
    private String title;

    /**
     * 作者
     */
    @TableField("author")
    private String author;

    /**
     * 出版社
     */
    @TableField("publisher")
    private String publisher;

    /**
     * 出版日期
     */
    @TableField("publish_date")
    private LocalDate publishDate;

    /**
     * 分类
     */
    @TableField("category")
    private String category;

    /**
     * 图书描述
     */
    @TableField("description")
    private String description;

    /**
     * 封面图片URL
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 库存数量
     */
    @TableField("stock")
    private Integer stock;

    /**
     * 借阅次数
     */
    @TableField("borrowed_count")
    private Integer borrowedCount;

    /**
     * 状态：1-在馆，2-借出，3-维修中
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
     * 删除标记：0-未删除，1-已删除
     */
    @TableField("deleted")
    private Integer deleted;
}