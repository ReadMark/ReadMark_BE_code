package com.example.ReadMark.controller;

import com.example.ReadMark.entity.UserBook;
import com.example.ReadMark.model.dto.UserBookDTO;
import com.example.ReadMark.service.UserBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/userbooks")
@RequiredArgsConstructor
public class UserBookController {
    private final UserBookService userBookService;

    @PostMapping
    public UserBook addUserBook(@RequestBody UserBookDTO dto) {
        return userBookService.addUserBook(dto);
    }

    @GetMapping
    public List<UserBook> getAllUserBooks() {
        return userBookService.getAllUserBooks();
    }

    @PutMapping("/{id}")
    public UserBook updateUserBook(@PathVariable Long id, @RequestBody UserBookDTO dto) {
        return userBookService.updateUserBook(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteUserBook(@PathVariable Long id) {
        userBookService.deleteUserBook(id);
    }
}
