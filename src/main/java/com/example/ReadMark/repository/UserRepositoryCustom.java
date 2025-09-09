package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.User;

import java.util.Optional;

public interface UserRepositoryCustom {
    Optional<User> findByEmailWithUserBooks(String email);
    Optional<User> findByEmailAndPassword(String email, String password);
    Optional<User> findUserWithAllRelations(Long userId);
}
