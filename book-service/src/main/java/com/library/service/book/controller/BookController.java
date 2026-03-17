package com.library.service.book.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.service.book.entity.Book;
import com.library.service.book.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 图书管理控制器
 */
@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "图书管理", description = "图书管理相关接口")
public class BookController {

    @Autowired

    private BookService bookService;

    @GetMapping
    @Operation(summary = "分页查询图书")
    public ResponseEntity<Page<Book>> pageQuery(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "书名") @RequestParam(required = false) String title,
            @Parameter(description = "作者") @RequestParam(required = false) String author,
            @Parameter(description = "分类") @RequestParam(required = false) String category) {

        Page<Book> page = new Page<>(current, size);
        Page<Book> result = bookService.pageQueryBooks(page, title, author, category);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询图书")
    public ResponseEntity<Book> getById(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @PostMapping
    @Operation(summary = "添加图书")
    public ResponseEntity<Book> create(@RequestBody Book book) {
        bookService.addBook(book);
        Book createdBook = bookService.getBookById(book.getId());
        return ResponseEntity.status(201).body(createdBook);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新图书")
    public ResponseEntity<Book> update(
        @PathVariable Long id,
        @RequestBody Book book) {
        book.setId(id);
        bookService.updateBook(book);
        Book updatedBook = bookService.getBookById(id);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除图书")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.deleteBook(id));
    }

    @GetMapping("/popular")
    @Operation(summary = "获取热门图书")
    public ResponseEntity<List<Book>> getPopular(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") int limit) {
        List<Book> books = bookService.getPopularBooks(limit);
        return ResponseEntity.ok(books);
    }
}