package com.library.service.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.service.book.entity.Book;
import org.apache.ibatis.annotations.Param;

/**
 * 图书信息Mapper接口
 */
public interface BookMapper extends BaseMapper<Book> {

    /**
     * 分页查询图书
     * @param page 分页对象
     * @param title 书名
     * @param author 作者
     * @param category 分类
     * @return 分页结果
     */
    Page<Book> pageQuery(Page<Book> page,
                         @Param("title") String title,
                         @Param("author") String author,
                         @Param("category") String category);
}