package com.example.ReadMark.controller;

import com.example.ReadMark.entity.Book;
import com.example.ReadMark.model.dto.BookDTO;
import com.example.ReadMark.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    // 책 생성
    @PostMapping
    public Book createBook(@RequestBody BookDTO dto) {
        return bookService.createBook(dto);
    }

    // 전체 책 조회
    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    // 책 업데이트
    @PutMapping("/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody BookDTO dto) {
        return bookService.updateBook(id, dto);
    }

    // 책 삭제
    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }
}
