package com.library.service.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.service.book.entity.Book;
import com.library.service.book.mapper.BookMapper;
import com.library.service.book.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 图书信息服务实现类
 */
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Autowired
    private BookMapper bookMapper;

    @Override
    public Page<Book> pageQueryBooks(Page<Book> page, String title, String author, String category) {
        return bookMapper.pageQuery(page, title, author, category);
    }

    @Override
    public Book getBookById(Long id) {
        return getById(id);
    }

    @Override
    public boolean addBook(Book book) {
        return save(book);
    }

    @Override
    public boolean updateBook(Book book) {
        return updateById(book);
    }

    @Override
    public boolean deleteBook(Long id) {
        return removeById(id);
    }

    @Override
    public List<Book> getPopularBooks(int limit) {
        QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("borrowed_count")
                   .last("LIMIT " + limit);
        return list(queryWrapper);
    }
}