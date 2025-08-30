package com.example.ReadMark.service;

import com.example.ReadMark.entity.Book;
import com.example.ReadMark.entity.User;
import com.example.ReadMark.entity.UserBook;
import com.example.ReadMark.model.dto.UserBookDTO;
import com.example.ReadMark.repository.UserBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserBookService {
    private final UserBookRepository userBookRepository;

    public UserBook addUserBook(UserBookDTO dto) {
        UserBook ub = new UserBook();
        ub.setUser(new User());
        ub.getUser().setUserId(dto.getUserId());
        ub.setBook(new Book());
        ub.getBook().setBookId(dto.getBookId());
        ub.setStatus(dto.getStatus());
        ub.setCurrentPage(dto.getCurrentPage());
        return userBookRepository.save(ub);
    }

    public List<UserBook> getAllUserBooks() {
        return userBookRepository.findAll();
    }

    public UserBook updateUserBook(Long id, UserBookDTO dto) {
        Optional<UserBook> optional = userBookRepository.findById(id);
        if (optional.isPresent()) {
            UserBook ub = optional.get();
            ub.setStatus(dto.getStatus());
            ub.setCurrentPage(dto.getCurrentPage());
            return userBookRepository.save(ub);
        }
        return null;
    }

    public void deleteUserBook(Long id) {
        userBookRepository.deleteById(id);
    }
}
