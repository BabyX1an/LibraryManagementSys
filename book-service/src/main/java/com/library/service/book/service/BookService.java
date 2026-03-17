package com.library.service.book.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.library.service.book.entity.Book;

import java.util.List;

/**
 * 图书信息服务接口
 */
public interface BookService extends IService<Book> {

    /**
     * 分页查询图书
     * @param page 分页对象
     * @param title 书名
     * @param author 作者
     * @param category 分类
     * @return 分页结果
     */
    Page<Book> pageQueryBooks(Page<Book> page, String title, String author, String category);

    /**
     * 根据ID查询图书
     * @param id 图书ID
     * @return 图书信息
     */
    Book getBookById(Long id);

    /**
     * 添加图书
     * @param book 图书信息
     * @return 是否成功
     */
    boolean addBook(Book book);

    /**
     * 更新图书
     * @param book 图书信息
     * @return 是否成功
     */
    boolean updateBook(Book book);

    /**
     * 删除图书
     * @param id 图书ID
     * @return 是否成功
     */
    boolean deleteBook(Long id);

    /**
     * 查询热门图书（根据借阅次数）
     * @param limit 限制数量
     * @return 热门图书列表
     */
    List<Book> getPopularBooks(int limit);
}